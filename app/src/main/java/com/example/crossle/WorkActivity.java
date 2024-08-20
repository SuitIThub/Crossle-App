package com.example.crossle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkActivity extends AppCompatActivity {

    private Uri myUri;

    static {
        OpenCVLoader.initDebug();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_work);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        OpenCVLoader.initDebug();
        Intent intent = getIntent();

        myUri = Uri.parse(intent.getStringExtra("imageUri"));

        convertImageToJson(myUri);
    }

    //TODO: Add image analyzer
    public void convertImageToJson(Uri uri){
        String jsonstring = new String();
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);  // Load OpenCV library
        Mat img = Imgcodecs.imread(uri.getPath());
        int h = img.rows();
        int w = img.cols();

        //CreateMask
        Mat mask = new Mat();
        Core.inRange(img, new Scalar(240, 240, 240), new Scalar(255, 255, 255), mask);

        Mat kernelOpen = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernelOpen);

        Mat kernelClose = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(21, 21));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernelClose);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        //findContours
        MatOfPoint selectedContour = null;
        for (MatOfPoint contour : contours) {
            if (Imgproc.pointPolygonTest(new MatOfPoint2f(contour.toArray()), new Point(w / 2, h / 2), false) > 0) {
                selectedContour = contour;
                break;
            }
        }

        mask.setTo(new Scalar(0));
        Imgproc.drawContours(mask, contours, contours.indexOf(selectedContour), new Scalar(255), Core.FILLED);

        //find the extreme outerpoints
        Rect boundingRect = Imgproc.boundingRect(selectedContour);
        int x = boundingRect.x;
        int y = boundingRect.y;
        int rectWidth = boundingRect.width;
        int rectHeight = boundingRect.height;

        int[] l = {x, (int) Core.minMaxLoc(mask.col(x)).maxLoc.y};
        int[] r = {x + rectWidth - 1, (int) Core.minMaxLoc(mask.col(x + rectWidth - 1)).maxLoc.y};
        int[] t = {(int) Core.minMaxLoc(mask.row(y)).maxLoc.x, y};
        int[] b = {(int) Core.minMaxLoc(mask.row(y + rectHeight - 1)).maxLoc.x, y + rectHeight - 1};

        //Perform perspective transform
        MatOfPoint2f pts1 = new MatOfPoint2f(
                new Point(t[0], t[1]),
                new Point(l[0], l[1]),
                new Point(b[0], b[1]),
                new Point(r[0], r[1])
        );
        MatOfPoint2f pts2 = new MatOfPoint2f(
                new Point(0, 0),
                new Point(0, 899),
                new Point(1599, 899),
                new Point(1599, 0)
        );

        Mat M = Imgproc.getPerspectiveTransform(pts1, pts2);
        Mat warped = new Mat();
        Imgproc.warpPerspective(img, warped, M, new Size(1600, 900));

        File outputDir = WorkActivity.this.getCacheDir(); // context being the Activity pointer
        try {
            File outputFile = File.createTempFile("warped_output", ".jpg", outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Imgcodecs.imwrite(outputDir + "/warped_output.jpg", warped);

        //function(jsonstring)
    }
}