package com.example.crossle;

import static com.example.crossle.classes.Helper.dpToPx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.crossle.classes.Helper;

public class ShapeOverlayView extends View {

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            toggleFineMode();
            return true;
        }
    }

    private Paint paint;
    private Path path;
    private float[] points;
    private int selectedPointIndex = -1;
    private static final float TOUCH_TOLERANCE = 40;
    private Paint circlePaint;
    private ImageView imageView;

    private PopupWindow popupWindow;
    private ImageView zoomedImageView;

    private boolean fineMode = false;
    private GestureDetector gestureDetector;

    private boolean isDrawing = false;

    public ShapeOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initPopupWindow(context);
        gestureDetector = new GestureDetector(context, new GestureListener());
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
        isDrawing = true;
    }

    private void initPopupWindow(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_zoom_view, null);
        zoomedImageView = popupView.findViewById(R.id.zoomedImageView);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
    }

    private void showPopupWindow(float x, float y) {
        if (!popupWindow.isShowing()) {
            popupWindow.showAtLocation(this, Gravity.NO_GRAVITY, (int) x, (int) y - 150);
        }
    }

    private void updatePopupWindow(float x, float y) {
        if (imageView == null || imageView.getDrawable() == null) {
            return;
        }

        // Capture the bitmap from the ImageView
        Bitmap imageViewBitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas imageViewCanvas = new Canvas(imageViewBitmap);
        imageView.draw(imageViewCanvas);

        // Create a new bitmap with the same dimensions as the ImageView
        Bitmap combinedBitmap = Bitmap.createBitmap(imageViewBitmap.getWidth(), imageViewBitmap.getHeight(), imageViewBitmap.getConfig());

        // Draw the ImageView bitmap and the shape overlay onto the combined bitmap
        Canvas canvas = new Canvas(combinedBitmap);
        canvas.drawBitmap(imageViewBitmap, 0, 0, null);
        draw(canvas);

        // Calculate the zoomed area
        int zoomSize = 100; // Size of the zoomed area
        int left = (int) Math.max(0, x - zoomSize / 2);
        int top = (int) Math.max(0, y - zoomSize / 2);
        int right = (int) Math.min(combinedBitmap.getWidth(), x + zoomSize / 2);
        int bottom = (int) Math.min(combinedBitmap.getHeight(), y + zoomSize / 2);

        // Create the zoomed bitmap
        Bitmap zoomedBitmap = Bitmap.createBitmap(combinedBitmap, left, top, right - left, bottom - top);

        // Update the zoomedImageView with the zoomed bitmap
        zoomedImageView.setImageBitmap(zoomedBitmap);
        popupWindow.update((int) x - zoomSize / 2, (int) y - 120, dpToPx(200, getContext()), dpToPx(200, getContext()));
    }

    private void hidePopupWindow() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void toggleFineMode() {
        fineMode = !fineMode;
        Toast.makeText(getContext(), "Fine mode " + (fineMode ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (points.length < 2 || !isDrawing) {
            return;
        }
        super.onDraw(canvas);
        path.reset();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) {
            path.lineTo(points[i], points[i + 1]);
        }
        path.close();
        canvas.drawPath(path, paint);
        if (selectedPointIndex == -1) {
            drawCircles(canvas);
        }
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
        gestureDetector.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        float[] imageViewCorners = getImageViewCorners();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                selectedPointIndex = getSelectedPointIndex(x, y);
                if (selectedPointIndex != -1) {
                    showPopupWindow(x, y);
                    paint.setStrokeWidth(2);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (selectedPointIndex != -1) {
                    float dx = x - points[selectedPointIndex];
                    float dy = y - points[selectedPointIndex + 1];
                    if (fineMode) {
                        dx /= 10;
                        dy /= 10;
                    }
                    x = points[selectedPointIndex] + dx;
                    y = points[selectedPointIndex + 1] + dy;

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

                    // Create a zoomed-in bitmap of the current corner
                    updatePopupWindow(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                hidePopupWindow();
                selectedPointIndex = -1;
                paint.setStrokeWidth(5);
                invalidate();
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

    public void clearShape() {
//        path.reset();
//        points = new float[0]; // Clear the points array
        isDrawing = false;
        invalidate(); // Redraw the view
    }

    public int[] getShapeCoordinatesRelativeToBitmap() {
        if (imageView == null || imageView.getDrawable() == null) {
            return new int[0];
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
        int[] relativeCoordinates = new int[points.length];
        for (int i = 0; i < points.length; i += 2) {
            relativeCoordinates[i] = (int)((points[i] - left) / scale);
            relativeCoordinates[i + 1] = (int)((points[i + 1] - top) / scale);
        }

        return relativeCoordinates;
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

    public void activateShape() {
//        points = new float[]{100, 100, 300, 100, 300, 300, 100, 300};
        isDrawing = true;
//        setShapeToImageCorners();
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