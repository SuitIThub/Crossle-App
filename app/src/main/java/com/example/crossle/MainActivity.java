package com.example.crossle;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.crossle.OnlineDBManager.ApiService;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.crossle.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private Button BTNTestButton;
    private TextView TVTestTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        BTNTestButton = findViewById(R.id.testButton);
        TVTestTextView = findViewById(R.id.testTextView);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, CamActivity.class);
//                myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(myIntent);
            }
        });

        BTNTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ApiService apiService = new ApiService();
                JSONObject json = new JSONObject();
                try {
                    json.put("question", "Gemeinde auf Sardinien");
                    json.put("length", 5);
                    apiService.getAnswers(json, new ApiService.ApiCallback() {
                        @Override
                        public void onResult(List<String> answers) {
                            String text = "";
                            for (String answer : answers) {
                                text += answer + "\n";
                            }
                            TVTestTextView.setText(text);
                        }
                    });

                } catch (JSONException e) {
                    TVTestTextView.setText(e.getMessage());
                    throw new RuntimeException(e);

                }
            }
        });
    }

}