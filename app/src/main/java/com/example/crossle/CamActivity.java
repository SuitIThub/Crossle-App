package com.example.crossle;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CamActivity extends AppCompatActivity {

    // One Preview Image
    ImageView IVPreviewImage;
    Button BSendImage;

    private Uri mCurrentPhotoPath;

    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200;
    private final int CAMERA_PERMISSION_CODE = 100;

    private boolean cameraPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        // register the UI widgets with their appropriate IDs
        IVPreviewImage = findViewById(R.id.IVPreviewImage);
        BSendImage = findViewById(R.id.BSendImage);

        //TODO: set button on click handler to send image to analyze activity

        triggerImageChooser();
    }

    private void triggerImageChooser() {
        cameraPermission = checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        if (cameraPermission) {
            imageChooser();
        }
    }

    // this function is triggered when
    // the Select Image Button is clicked
    private void imageChooser() {
        // create an instance of the
        // intent of the type image
        Intent i = getPickImageChooserIntent();

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                if (data == null || data.getData() == null) {
                    // use the saved Uri when camera is used
                    System.out.println("Camera Way: " + mCurrentPhotoPath);
                    try {
                        Bitmap bitmapOrg = MediaStore.Images.Media.getBitmap(getContentResolver(), mCurrentPhotoPath);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapOrg, bitmapOrg.getWidth(), bitmapOrg.getHeight(), true);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                        IVPreviewImage.setImageBitmap(rotatedBitmap);
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_LONG).show();

                        // go back to the main activity
                        Intent myIntent = new Intent(CamActivity.this, MainActivity.class);
                        CamActivity.this.startActivity(myIntent);
                    }
                }
                else {
                    // Get the Uri from Intent
                    mCurrentPhotoPath = data.getData();
                    if (null != mCurrentPhotoPath) {
                        IVPreviewImage.setImageURI(mCurrentPhotoPath);
                    }
                    else {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_LONG).show();

                        // go back to the main activity
                        Intent myIntent = new Intent(CamActivity.this, MainActivity.class);
                        CamActivity.this.startActivity(myIntent);
                    }
                }

                BSendImage.setVisibility(View.VISIBLE);
            }
        }
        else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();

            // go back to the main activity
            Intent myIntent = new Intent(CamActivity.this, MainActivity.class);
            CamActivity.this.startActivity(myIntent);
        }
    }

    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera image to save.

        List<Intent> allIntents = new ArrayList();
        PackageManager packageManager = getPackageManager();

        if (cameraPermission) {
//         collect all camera intents
            Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
            for (ResolveInfo res : listCam) {
                Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(res.activityInfo.packageName);

                ContentValues values = new ContentValues();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp;
                values.put(MediaStore.Images.Media.TITLE, imageFileName);
                values.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " + System.currentTimeMillis());

                mCurrentPhotoPath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoPath);

                allIntents.add(intent);
            }
        }
        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    public boolean checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(CamActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(CamActivity.this, new String[] { permission }, requestCode);
            return false;
        }
        else {
            return true;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraPermission = true;
            }
            imageChooser();
        }
    }
}