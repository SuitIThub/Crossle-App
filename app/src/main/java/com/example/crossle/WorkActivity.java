package com.example.crossle;

import static androidx.core.widget.TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM;
import static androidx.core.widget.TextViewCompat.setAutoSizeTextTypeUniformWithPresetSizes;
import static androidx.core.widget.TextViewCompat.setAutoSizeTextTypeWithDefaults;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;
import androidx.gridlayout.widget.GridLayout;

import org.json.*;

import java.util.ArrayList;
import java.util.Vector;

public class WorkActivity extends AppCompatActivity {

    private Uri myUri;

    private GridLayout GPuzzleGrid;

    private static int AUTO_SIZE_TEXT_TYPE_UNIFORM = 1;

    private EditText[][] puzzleGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        Intent intent = getIntent();

        myUri = Uri.parse(intent.getStringExtra("imageUri"));

        createPuzzleGrid(13, 24, new JSONObject());
    }

    private void configurePuzzleGrid(int rows, int cols) {
        GPuzzleGrid = findViewById(R.id.gridLayoutPuzzle);
        GPuzzleGrid.setRowCount(rows);
        GPuzzleGrid.setColumnCount(cols);
    }

    private void buildPuzzleGrid(int width, int height, ArrayList<String> questions) {
        configurePuzzleGrid(height, width);

        puzzleGrid = new EditText[height][width];

        int count = 0;

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                EditText piece = new EditText(this);
                puzzleGrid[x][y] = piece;

                LayoutParams params = new LayoutParams(dpToPx(48), dpToPx(48));
                piece.setLayoutParams(params);

                TextViewCompat.setAutoSizeTextTypeWithDefaults(piece, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

                piece.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                if (!questions.contains(y + ":" + x)) {
                    piece.setFilters(new InputFilter[] { new InputFilter.LengthFilter(1) });
                }
                GradientDrawable border = new GradientDrawable();
                border.setColor(Color.TRANSPARENT);
                border.setStroke(1, Color.WHITE);
                piece.setBackground(border);
                piece.setTextAlignment(EditText.TEXT_ALIGNMENT_CENTER);
                piece.setText("" + count);
                GPuzzleGrid.addView(piece);
                count++;
                if (count >= 10)
                    count = 1;
            }
        }
    }

    private void createPuzzleGrid(int width, int height, JSONObject json) {
        buildPuzzleGrid(width, height, new ArrayList<String>());
    }

    public int dpToPx(int dp) {
        float density = WorkActivity.this.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

}