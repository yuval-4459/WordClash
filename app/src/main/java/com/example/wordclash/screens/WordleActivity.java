package com.example.wordclash.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.Word;
import com.example.wordclash.services.DatabaseService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Wordle game - Guess a 5-letter English word
 */
public class WordleActivity extends AppCompatActivity {

    private GridLayout gridGuesses;
    private EditText etGuess;
    private Button btnSubmit, btnNewGame, btnBack;
    private TextView tvInstructions;

    private String targetWord;
    private int currentAttempt = 0;
    private final int MAX_ATTEMPTS = 6;
    private final int WORD_LENGTH = 5;
    private List<Word> allFiveLetterWords;
    private TextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordle);

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        gridGuesses = findViewById(R.id.gridGuesses);
        etGuess = findViewById(R.id.etGuess);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnNewGame = findViewById(R.id.btnNewGame);
        btnBack = findViewById(R.id.btnBack);
        tvInstructions = findViewById(R.id.tvInstructions);

        btnSubmit.setOnClickListener(v -> submitGuess());
        btnNewGame.setOnClickListener(v -> startNewGame());
        btnBack.setOnClickListener(v -> finish());

        // Limit EditText to 5 uppercase letters
        etGuess.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(WORD_LENGTH),
                new InputFilter.AllCaps()
        });

        // Show letters in grid as user types
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCurrentRowWithInput(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        etGuess.addTextChangedListener(textWatcher);

        setupGrid();
    }

    private void setupGrid() {
        gridGuesses.removeAllViews();
        gridGuesses.setColumnCount(WORD_LENGTH);
        gridGuesses.setRowCount(MAX_ATTEMPTS);
        gridGuesses.setLayoutDirection(android.view.View.LAYOUT_DIRECTION_LTR);

        int size = (int) (getResources().getDisplayMetrics().widthPixels * 0.15);

        for (int i = 0; i < MAX_ATTEMPTS * WORD_LENGTH; i++) {
            TextView tv = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(6, 6, 6, 6);
            tv.setLayoutParams(params);

            tv.setGravity(android.view.Gravity.CENTER);
            tv.setTextSize(32);
            tv.setTextColor(Color.BLACK);
            tv.setBackgroundColor(Color.WHITE);
            tv.setTextDirection(android.view.View.TEXT_DIRECTION_LTR);
            tv.setLayoutDirection(android.view.View.LAYOUT_DIRECTION_LTR);

            // Add border
            tv.setBackground(getDrawable(android.R.drawable.edit_text));

            gridGuesses.addView(tv);
        }
    }

    private void updateCurrentRowWithInput(String input) {
        int startIndex = currentAttempt * WORD_LENGTH;

        // Clear current row first
        for (int i = 0; i < WORD_LENGTH; i++) {
            TextView tv = (TextView) gridGuesses.getChildAt(startIndex + i);
            tv.setText("");
        }

        // Fill in the letters
        for (int i = 0; i < input.length() && i < WORD_LENGTH; i++) {
            TextView tv = (TextView) gridGuesses.getChildAt(startIndex + i);
            tv.setText(String.valueOf(input.charAt(i)));
        }
    }

    private void loadWords() {
        allFiveLetterWords = new ArrayList<>();

        DatabaseService.getInstance().getAllFiveLetterWords(new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words != null && !words.isEmpty()) {
                    allFiveLetterWords = words;
                    startNewGame();
                } else {
                    Toast.makeText(WordleActivity.this,
                            "No 5-letter words available. Please contact admin.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(WordleActivity.this,
                        "Failed to load words: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startNewGame() {
        if (allFiveLetterWords == null || allFiveLetterWords.isEmpty()) {
            Toast.makeText(this, "No words available", Toast.LENGTH_SHORT).show();
            return;
        }

        Random random = new Random();
        targetWord = allFiveLetterWords.get(random.nextInt(allFiveLetterWords.size()))
                .getEnglish()
                .toUpperCase();

        currentAttempt = 0;
        etGuess.setText("");
        etGuess.setEnabled(true);
        btnSubmit.setEnabled(true);
        setupGrid();

        tvInstructions.setText("🎮 Guess the 5-letter word!\n" +
                "🟩 = Correct position\n" +
                "🟨 = Wrong position\n" +
                "⬜ = Not in word");
    }

    private void submitGuess() {
        String guess = etGuess.getText().toString().toUpperCase().trim();

        if (guess.length() != WORD_LENGTH) {
            Toast.makeText(this, "Please enter a " + WORD_LENGTH + "-letter word",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!guess.matches("[A-Z]+")) {
            Toast.makeText(this, "Please use only letters", Toast.LENGTH_SHORT).show();
            return;
        }

        displayGuess(guess);

        // Remove TextWatcher temporarily to avoid clearing the grid
        etGuess.removeTextChangedListener(textWatcher);
        etGuess.setText("");
        etGuess.addTextChangedListener(textWatcher);

        if (guess.equals(targetWord)) {
            gameWon();
        } else {
            currentAttempt++;
            if (currentAttempt >= MAX_ATTEMPTS) {
                gameLost();
            }
        }
    }

    private void displayGuess(String guess) {
        int startIndex = currentAttempt * WORD_LENGTH;

        char[] targetChars = targetWord.toCharArray();
        boolean[] used = new boolean[WORD_LENGTH];

        // First pass: Mark exact matches (GREEN)
        for (int i = 0; i < WORD_LENGTH; i++) {
            TextView tv = (TextView) gridGuesses.getChildAt(startIndex + i);
            char letter = guess.charAt(i);
            // DON'T set text again - it's already there from typing
            // tv.setText(String.valueOf(letter));

            if (letter == targetChars[i]) {
                tv.setBackgroundColor(Color.GREEN);
                tv.setTextColor(Color.WHITE);
                used[i] = true;
            }
        }

        // Second pass: Mark wrong position matches (YELLOW) and misses (GRAY)
        for (int i = 0; i < WORD_LENGTH; i++) {
            TextView tv = (TextView) gridGuesses.getChildAt(startIndex + i);
            char letter = guess.charAt(i);

            if (letter == targetChars[i]) {
                continue;
            }

            boolean foundInTarget = false;
            for (int j = 0; j < WORD_LENGTH; j++) {
                if (!used[j] && letter == targetChars[j]) {
                    tv.setBackgroundColor(Color.YELLOW);
                    tv.setTextColor(Color.BLACK);
                    used[j] = true;
                    foundInTarget = true;
                    break;
                }
            }

            if (!foundInTarget) {
                tv.setBackgroundColor(Color.DKGRAY);
                tv.setTextColor(Color.WHITE);
            }
        }
    }

    private void gameWon() {
        etGuess.setEnabled(false);
        btnSubmit.setEnabled(false);

        new AlertDialog.Builder(this)
                .setTitle("🎉 Congratulations!")
                .setMessage("You guessed the word: " + targetWord +
                        "\nAttempts: " + (currentAttempt + 1) + "/" + MAX_ATTEMPTS)
                .setPositiveButton("New Game", (dialog, which) -> startNewGame())
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void gameLost() {
        etGuess.setEnabled(false);
        btnSubmit.setEnabled(false);

        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("The word was: " + targetWord)
                .setPositiveButton("New Game", (dialog, which) -> startNewGame())
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}