package com.example.crossle;

import static androidx.core.widget.TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM;
import static androidx.core.widget.TextViewCompat.setAutoSizeTextTypeUniformWithPresetSizes;
import static androidx.core.widget.TextViewCompat.setAutoSizeTextTypeWithDefaults;

import static com.example.crossle.classes.Helper.*;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;
import androidx.gridlayout.widget.GridLayout;

import com.example.crossle.classes.Vector2Int;

import org.json.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class WorkActivity extends AppCompatActivity {

    private static final Integer BOX_WIDTH = 64;
    private static final Integer BOX_HEIGHT = 64;

    private Uri myUri;

    private HashMap<Integer, JSONObject> questions = new HashMap<>();

    private GridLayout GPuzzleGrid;
    private TextView GPuzzleText;

    private Button activeButton = null;

    private int focusedId = -1;
    private ArrayList<Vector2Int> focusedSlots = new ArrayList<>();

    private static int AUTO_SIZE_TEXT_TYPE_UNIFORM = 1;

    private View[][] puzzleGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        GPuzzleGrid = findViewById(R.id.gridLayoutPuzzle);
        GPuzzleText = findViewById(R.id.textViewPuzzle);

        Intent intent = getIntent();

        myUri = Uri.parse(intent.getStringExtra("imageUri"));

        createPuzzleGrid(5, 5, getDummyJson());
    }

    private void configurePuzzleGrid(int rows, int cols) {

        GPuzzleGrid.setRowCount(rows);
        GPuzzleGrid.setColumnCount(cols);
    }

    private void buildPuzzleGrid(int width, int height, ArrayList<JSONObject> jsons) {
        configurePuzzleGrid(height, width);

        puzzleGrid = new View[height][width];

        HashMap<Integer, Vector2Int> questionPositions = getQuestionPositions(jsons);
        HashMap<Integer, Vector2Int> questionDirections = getQuestionDirection(jsons);

        for (int id : questionPositions.keySet()) {
            Vector2Int pos = questionPositions.get(id);
            Button btn = (Button) puzzleGrid[pos.getX()][pos.getY()];
            if (btn == null) {
                btn = buildQButton();
                puzzleGrid[pos.getX()][pos.getY()] = btn;
            }
            Object obj = btn.getTag();
            ArrayList<Integer> tagList = (ArrayList<Integer>) obj;
            tagList.add(id);
            btn.setTag(tagList);
            System.out.println("ID: " + id + " tag_length: " + tagList.size() + " x: " + pos.getX() + " y: " + pos.getY());
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (puzzleGrid[x][y] != null) {
                    GPuzzleGrid.addView(puzzleGrid[x][y]);
                    continue;
                }

                EditText piece = buildEditText("", new Vector2Int(x, y));
                puzzleGrid[x][y] = piece;

                GPuzzleGrid.addView(piece);
            }
        }
    }

    private HashMap<Integer, Vector2Int> getQuestionPositions(ArrayList<JSONObject> jsons) {
        HashMap<Integer, Vector2Int> positions = new HashMap<>();

        for (JSONObject json : jsons) {
            try {
                String position = json.getString("position");
                positions.put(json.getInt("id"), new Vector2Int(position));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return positions;
    }

    private ArrayList<Vector2Int> getQuestionSlots(int id) {
        JSONObject question = questions.get(id);
        ArrayList<Vector2Int> slots = new ArrayList<>();
        try {
            JSONArray jsonSlots = question.getJSONArray("slots");
            for (int i = 0; i < jsonSlots.length(); i++) {
                slots.add(new Vector2Int(jsonSlots.getString(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return slots;
    }

    private HashMap<Integer, Vector2Int> getQuestionDirection(ArrayList<JSONObject> jsons) {
        HashMap<Integer, Vector2Int> directions = new HashMap<>();

        for (JSONObject json : jsons) {
            try {
                JSONArray slots = json.getJSONArray("slots");
                String position = json.getString("position");
                String slot = slots.getString(0);

                Vector2Int slotPosition = new Vector2Int(slot);
                Vector2Int questionPosition = new Vector2Int(position);
                slotPosition.subtract(questionPosition);

                directions.put(json.getInt("id"), slotPosition);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return directions;
    }

    private EditText buildEditText(String text, Vector2Int pos) {
        EditText piece = new EditText(this);

        LayoutParams params = new LayoutParams(dpToPx(BOX_WIDTH), dpToPx(BOX_HEIGHT));
        piece.setLayoutParams(params);

        TextViewCompat.setAutoSizeTextTypeWithDefaults(
                piece,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
        );

        piece.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        piece.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.TRANSPARENT);
        border.setStroke(1, Color.WHITE);
        piece.setBackground(border);
        piece.setTextAlignment(EditText.TEXT_ALIGNMENT_CENTER);
        piece.setText(text);
        piece.setTag(pos);
        piece.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Vector2Int pos = (Vector2Int) piece.getTag();

                if (s.length() == 1 && focusedSlots.contains(pos)) {
                    int index = focusedSlots.indexOf(pos);
                    focusNextEdit(index);
                }
            }
        });
        return piece;
    }

    private void focusNextEdit(int index) {
        int count = 0;
        Vector2Int currentPos = focusedSlots.get(index);
        EditText current = (EditText) puzzleGrid[currentPos.getX()][currentPos.getY()];
        EditText next = current;
        // get the next editText from the positions of the focused Slots that has no text in it. Start searching at position index and go through it to the end, then jump to the beginning of the list and go until you reach index again. if there is no free field anymore return
        while (count++ <= focusedSlots.size()) {
            if (next.getText().length() == 0) {
                focusEditText(next);
                return;
            }

            index = (index + 1) % focusedSlots.size();

            Vector2Int nextPos = focusedSlots.get(index);
            next = (EditText) puzzleGrid[nextPos.getX()][nextPos.getY()];
        }

        unfocusEditText(current);
        return;
    }

    private void focusEditText(EditText text) {
        text.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(text, InputMethodManager.SHOW_IMPLICIT);
    }

    private void unfocusEditText(EditText text) {
        text.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
    }

    private Button buildQButton() {
        Button piece = new Button(this);

        ConstraintLayout.LayoutParams params =
                new ConstraintLayout.LayoutParams(dpToPx(BOX_WIDTH), dpToPx(BOX_HEIGHT));
        params.setMargins(0, -3, 0, 0);
        piece.setLayoutParams(params);

        piece.setBackgroundColor(Color.GRAY);
        piece.setTextAlignment(Button.TEXT_ALIGNMENT_CENTER);
        piece.setText("Q");
        piece.setTag(new ArrayList<Integer>());
        piece.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Integer> ids = (ArrayList<Integer>) v.getTag();

                int index = ids.indexOf(focusedId);
                if (index == -1)
                    index = 0;
                else
                    index = (index + 1) % ids.size();

                focusedId = ids.get(index);
                focusedSlots = getQuestionSlots(focusedId);
                focusNextEdit(0);

                try {
                    String text = "Question " + focusedId + ": " + questions.get(focusedId).getString("question");
                    if (ids.size() > 1)
                        text += "   [+" + (ids.size() - 1) +"]";
                    GPuzzleText.setText(text);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return piece;
    }

    private void createPuzzleGrid(int width, int height, ArrayList<JSONObject> json) {
        for (JSONObject obj : json) {
            try {
                questions.put(obj.getInt("id"), obj);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        buildPuzzleGrid(width, height, json);
    }

    public int dpToPx(int dp) {
        float density = WorkActivity.this.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
}