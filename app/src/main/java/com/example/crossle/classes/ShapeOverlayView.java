package com.example.crossle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class ShapeOverlayView extends View {

    private Paint paint;
    private Path path;
    private float[] points;
    private int selectedPointIndex = -1;
    private static final float TOUCH_TOLERANCE = 40;
    private Paint circlePaint;
    private ImageView imageView;

    public ShapeOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        circlePaint = new Paint();
        circlePaint.setColor(Color.WHITE);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setStrokeWidth(5);

        path = new Path();
        points = new float[]{100, 100, 300, 100, 300, 300, 100, 300}; // Initial rectangle
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.reset();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) {
            path.lineTo(points[i], points[i + 1]);
        }
        path.close();
        canvas.drawPath(path, paint);
        drawCircles(canvas);
    }

    private void drawCircles(Canvas canvas) {
        for (int i = 0; i < points.length; i += 2) {
            canvas.drawCircle(points[i], points[i + 1], 20, circlePaint);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setColor(Color.BLACK);
            canvas.drawCircle(points[i], points[i + 1], 20, circlePaint);
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setColor(Color.WHITE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float[] imageViewCorners = getImageViewCorners();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                selectedPointIndex = getSelectedPointIndex(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (selectedPointIndex != -1) {
                    // Apply constraints based on the selected point
                    switch (selectedPointIndex) {
                        case 0: // Top-left corner
                            x = Math.max(imageViewCorners[0], Math.min(x, Math.min(points[2], points[4])));
                            y = Math.max(imageViewCorners[1], Math.min(y, Math.min(points[5], points[7])));
                            break;
                        case 2: // Top-right corner
                            x = Math.max(Math.max(points[0], points[6]), Math.min(x, imageViewCorners[2]));
                            y = Math.max(imageViewCorners[1], Math.min(y, Math.min(points[5], points[7])));
                            break;
                        case 4: // Bottom-right corner
                            x = Math.max(Math.max(points[0], points[6]), Math.min(x, imageViewCorners[2]));
                            y = Math.max(Math.max(points[1], points[3]), Math.min(y, imageViewCorners[5]));
                            break;
                        case 6: // Bottom-left corner
                            x = Math.max(imageViewCorners[0], Math.min(x, Math.min(points[2], points[4])));
                            y = Math.max(Math.max(points[1], points[3]), Math.min(y, imageViewCorners[5]));
                            break;
                    }
                    points[selectedPointIndex] = x;
                    points[selectedPointIndex + 1] = y;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                // Print the shape coordinates relative to the bitmap
                float[] relativeCoordinates = getShapeCoordinatesRelativeToBitmap();
                for (int i = 0; i < relativeCoordinates.length; i += 2) {
                    System.out.println("Point " + (i / 2 + 1) + ": (" + relativeCoordinates[i] + ", " + relativeCoordinates[i + 1] + ")");
                }
                selectedPointIndex = -1;
                break;
        }
        return true;
    }

    private int getSelectedPointIndex(float x, float y) {
        for (int i = 0; i < points.length; i += 2) {
            if (Math.abs(x - points[i]) < TOUCH_TOLERANCE && Math.abs(y - points[i + 1]) < TOUCH_TOLERANCE) {
                return i;
            }
        }
        return -1;
    }

    public float[] getShapeCoordinatesRelativeToBitmap() {
        if (imageView == null || imageView.getDrawable() == null) {
            return new float[0];
        }

        // Get image dimensions
        float imageWidth = imageView.getDrawable().getIntrinsicWidth();
        float imageHeight = imageView.getDrawable().getIntrinsicHeight();

        // Get imageView dimensions
        float viewWidth = imageView.getWidth(); // Subtracting 16dp margin from both sides
        float viewHeight = imageView.getHeight();

        // Calculate the scale factor
        float scaleX = viewWidth / imageWidth;
        float scaleY = viewHeight / imageHeight;
        float scale = Math.min(scaleX, scaleY);

        // Calculate the actual image size in the view
        float actualImageWidth = imageWidth * scale;
        float actualImageHeight = imageHeight * scale;

        // Calculate the top-left corner of the image
        float left = (viewWidth - actualImageWidth) / 2; // Adding 16dp margin
        float top = (viewHeight - actualImageHeight) / 2;

        // Calculate the shape coordinates relative to the bitmap
        float[] relativeCoordinates = new float[points.length];
        for (int i = 0; i < points.length; i += 2) {
            relativeCoordinates[i] = (points[i] - left) / scale;
            relativeCoordinates[i + 1] = (points[i + 1] - top) / scale;
        }

        return relativeCoordinates;
    }

    public float get16DPInPixel() {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return 16 * scale + 0.5f;
    }

    public float[] getImageViewCorners() {
        if (imageView == null || imageView.getDrawable() == null) {
            return new float[0];
        }

        // Get image dimensions
        float[] imageCorners = new float[8];
        float imageWidth = imageView.getDrawable().getIntrinsicWidth();
        float imageHeight = imageView.getDrawable().getIntrinsicHeight();

        // Get imageView dimensions
        float viewWidth = imageView.getWidth(); // Subtracting 16dp margin from both sides
        float viewHeight = imageView.getHeight();

        // Calculate the scale factor
        float scaleX = viewWidth / imageWidth;
        float scaleY = viewHeight / imageHeight;
        float scale = Math.min(scaleX, scaleY);

        // Calculate the actual image size in the view
        float actualImageWidth = imageWidth * scale;
        float actualImageHeight = imageHeight * scale;

        // Calculate the top-left corner of the image
        float left = (viewWidth - actualImageWidth) / 2; // Adding 16dp margin
        float top = (viewHeight - actualImageHeight) / 2;

        // Set the coordinates of the image corners
        imageCorners[0] = left; // Top-left corner
        imageCorners[1] = top;
        imageCorners[2] = left + actualImageWidth; // Top-right corner
        imageCorners[3] = top;
        imageCorners[4] = left + actualImageWidth; // Bottom-right corner
        imageCorners[5] = top + actualImageHeight;
        imageCorners[6] = left; // Bottom-left corner
        imageCorners[7] = top + actualImageHeight;

        return imageCorners;
    }

    public void setShapeToImageCorners() {
        float[] imageViewCorners = getImageViewCorners();
        if (imageViewCorners.length == 8) {
            float offset = 40.0f;
            points[0] = imageViewCorners[0] + offset; // Top-left corner
            points[1] = imageViewCorners[1] + offset;
            points[2] = imageViewCorners[2] - offset; // Top-right corner
            points[3] = imageViewCorners[3] + offset;
            points[4] = imageViewCorners[4] - offset; // Bottom-right corner
            points[5] = imageViewCorners[5] - offset;
            points[6] = imageViewCorners[6] + offset; // Bottom-left corner
            points[7] = imageViewCorners[7] - offset;
            invalidate();

            Toast.makeText(getContext(), "Shape set to image corners", Toast.LENGTH_SHORT).show();
        }
    }
}