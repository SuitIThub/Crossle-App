package com.example.crossle;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.crossle.classes.*;
import com.example.crossle.ShapeOverlayView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CamActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_IP = "ip_address";
    private static final String KEY_PORT = "port";

    private String ipAddress;
    private int port;

    // One Preview Image
    ImageView IVPreviewImage;
    Button BSendImage;
    ImageButton IBBackButton;
    ImageButton IBResetButton;
    private ProgressBar progressBar;
    private TextView progressText;
    TextView TVInstructions;
    EditText ETRows;
    EditText ETColumns;
    ImageButton IBSettingsButton;

    private ShapeOverlayView shapeOverlayView;

    private Uri mCurrentPhotoPath;

    private Bitmap bitmap;

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
        IBBackButton = findViewById(R.id.IBBack);
        IBResetButton = findViewById(R.id.IBReset);
        IBSettingsButton = findViewById(R.id.IBSettings);
        TVInstructions = findViewById(R.id.TVInstructions);

        ETRows = findViewById(R.id.ETRows);
        ETColumns = findViewById(R.id.ETColumns);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        IVPreviewImage = findViewById(R.id.IVPreviewImage);
        shapeOverlayView = findViewById(R.id.shapeOverlayView);
        shapeOverlayView.setImageView(IVPreviewImage);

        BSendImage.setEnabled(false); // Initially disable the button

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String rowsText = ETRows.getText().toString().trim();
                String columnsText = ETColumns.getText().toString().trim();
                BSendImage.setEnabled(!rowsText.isEmpty() && !columnsText.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        ETRows.addTextChangedListener(textWatcher);
        ETColumns.addTextChangedListener(textWatcher);


        BSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create a tcp connection and send the image and then the grid size as a json
                // then go to the work activity

                StringBuilder sb = new StringBuilder();
                for (int i : shapeOverlayView.getShapeCoordinatesRelativeToBitmap()) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(i);
                }
                String joinedString = sb.toString();

                JSONObject json = new JSONObject();
                try {
                    json.put("rows", Integer.parseInt(ETRows.getText().toString()));
                    json.put("columns", Integer.parseInt(ETColumns.getText().toString()));
                    json.put("points", joinedString);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {

                    Log.i("CamActivity", "Sending image and json to server with ip: " + ipAddress + " and port: " + port);

                    NetworkUtils.sendImageAndJsonToServerAsync(bitmap, json, ipAddress, port, new NetworkUtils.NetworkCallback() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("Response: " + response);
                        }
                    }, CamActivity.this);

                } catch (Exception e) {
                    progressText.setText("Failed to send image");
                    e.printStackTrace();
                }

                progressText.setText("Sending image to server...");
                progressBar.setProgress(0);

                ETRows.setVisibility(View.GONE);
                ETColumns.setVisibility(View.GONE);
                TVInstructions.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
                BSendImage.setVisibility(View.GONE);
                shapeOverlayView.clearShape();
            }
        });

        IBBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go back to the main activity
                System.out.println("Going back to main activity");
                Intent myIntent = new Intent(CamActivity.this, MainActivity.class);
                CamActivity.this.startActivity(myIntent);
            }
        });

        IBResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Resetting Image");
                shapeOverlayView.setShapeToImageCorners();
            }
        });

        loadIpAndPort();

        IBSettingsButton = findViewById(R.id.IBSettings);
        IBSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIpPortPopup();
            }
        });

        triggerImageChooser();
    }

    public void revertUIState(String msg) {
        runOnUiThread(() -> {
            ETRows.setVisibility(View.VISIBLE);
            ETColumns.setVisibility(View.VISIBLE);
            TVInstructions.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            BSendImage.setVisibility(View.VISIBLE);
            TVInstructions.setText(msg + "\nDouble-Tab for fine-mode.");
//            // sleep for 1 second
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            shapeOverlayView.activateShape();
//            shapeOverlayView.setShapeToImageCorners();
        });
    }

    private void showIpPortPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_ip_port, null);
        builder.setView(dialogView);

        EditText etIpAddress = dialogView.findViewById(R.id.etIpAddress);
        EditText etPort = dialogView.findViewById(R.id.etPort);

        etIpAddress.setText(ipAddress);
        etPort.setText(String.valueOf(port));

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            ipAddress = etIpAddress.getText().toString();
            port = Integer.parseInt(etPort.getText().toString());
            saveIpAndPort();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveIpAndPort() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_IP, ipAddress);
        editor.putInt(KEY_PORT, port);
        editor.apply();
    }

    private void loadIpAndPort() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        ipAddress = sharedPreferences.getString(KEY_IP, "default_ip");
        port = sharedPreferences.getInt(KEY_PORT, 12345);
    }

    public void openPuzzle(JSONObject data) {
        Intent myIntent = new Intent(CamActivity.this, WorkActivity.class);
        myIntent.putExtra("puzzle", data.toString());
        myIntent.putExtra("rows", Integer.parseInt(ETRows.getText().toString()));
        myIntent.putExtra("columns", Integer.parseInt(ETColumns.getText().toString()));
        CamActivity.this.startActivity(myIntent);
    }

    public void updateProgress(String message, int percentage) {
        runOnUiThread(() -> {
            if (message != null) {
                progressText.setText(message + " " + percentage + "%");
                progressBar.setProgress(percentage);
            }
        });
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
                        bitmap = rotatedBitmap;
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
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCurrentPhotoPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_LONG).show();

                        // go back to the main activity
                        Intent myIntent = new Intent(CamActivity.this, MainActivity.class);
                        CamActivity.this.startActivity(myIntent);
                    }
                }

//                BSendImage.setVisibility(View.VISIBLE);
                shapeOverlayView.setShapeToImageCorners();
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