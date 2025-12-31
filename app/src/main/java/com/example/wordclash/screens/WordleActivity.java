package com.example.wordclash.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
import java.util.List;
import java.util.Random;

/**
 * Wordle game activity
 * User has 6 attempts to guess a 5-letter English word
 */
public class WordleActivity extends AppCompatActivity {

    private GridLayout gridGuesses;
    private EditText etGuess;
    private Button btnSubmit, btnNewGame;
    private TextView tvInstructions;

    private String targetWord;
    private int currentAttempt = 0;
    private final int MAX_ATTEMPTS = 6;
    private final int WORD_LENGTH = 5;
    private List<Word> allWords;

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
        tvInstructions = findViewById(R.id.tvInstructions);

        btnSubmit.setOnClickListener(v -> submitGuess());
        btnNewGame.setOnClickListener(v -> startNewGame());

        setupGrid();
    }

    private void setupGrid() {
        gridGuesses.removeAllViews();
        gridGuesses.setColumnCount(WORD_LENGTH);
        gridGuesses.setRowCount(MAX_ATTEMPTS);

        for (int i = 0; i < MAX_ATTEMPTS * WORD_LENGTH; i++) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new GridLayout.LayoutParams());
            tv.setWidth(100);
            tv.setHeight(100);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setTextSize(24);
            tv.setTextColor(Color.BLACK);
            tv.setBackgroundColor(Color.LTGRAY);
            tv.setPadding(4, 4, 4, 4);

            GridLayout.LayoutParams params = (GridLayout.LayoutParams) tv.getLayoutParams();
            params.setMargins(4, 4, 4, 4);
            tv.setLayoutParams(params);

            gridGuesses.addView(tv);
        }
    }

    private void loadWords() {
        allWords = new ArrayList<>();

        // Load words from all ranks
        for (int rank = 1; rank <= 5; rank++) {
            final int currentRank = rank;
            DatabaseService.getInstance().getWordsByRank(rank, new DatabaseService.DatabaseCallback<List<Word>>() {
                @Override
                public void onCompleted(List<Word> words) {
                    if (words != null) {
                        // Filter only 5-letter words
                        for (Word word : words) {
                            if (word.getEnglish() != null &&
                                    word.getEnglish().length() == WORD_LENGTH) {
                                allWords.add(word);
                            }
                        }
                    }

                    // Start game once we have words
                    if (currentRank == 5 && !allWords.isEmpty()) {
                        startNewGame();
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    if (currentRank == 5) {
                        // Use fallback words if loading fails
                        useFallbackWords();
                        startNewGame();
                    }
                }
            });
        }
    }

    private void useFallbackWords() {
        // Fallback 5-letter words
        String[] fallback = {"HELLO", "WORLD", "PHONE", "HOUSE", "WATER",
                "HAPPY", "SMILE", "MUSIC", "BRAIN", "PEACE"};
        allWords.clear();
        for (int i = 0; i < fallback.length; i++) {
            allWords.add(new Word(String.valueOf(i), fallback[i], "", 1));
        }
    }

    private void startNewGame() {
        if (allWords.isEmpty()) {
            Toast.makeText(this, "Loading words...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Select random word
        Random random = new Random();
        targetWord = allWords.get(random.nextInt(allWords.size()))
                .getEnglish()
                .toUpperCase();

        currentAttempt = 0;
        etGuess.setText("");
        etGuess.setEnabled(true);
        btnSubmit.setEnabled(true);
        setupGrid();

        tvInstructions.setText("Guess the 5-letter word!\n🟩 = Correct position\n🟨 = Wrong position\n⬜ = Not in word");
    }

    private void submitGuess() {
        String guess = etGuess.getText().toString().toUpperCase().trim();

        if (guess.length() != WORD_LENGTH) {
            Toast.makeText(this, "Please enter a " + WORD_LENGTH + "-letter word", Toast.LENGTH_SHORT).show();
            return;
        }

        displayGuess(guess);
        etGuess.setText("");

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

        for (int i = 0; i < WORD_LENGTH; i++) {
            TextView tv = (TextView) gridGuesses.getChildAt(startIndex + i);
            char letter = guess.charAt(i);
            tv.setText(String.valueOf(letter));

            if (targetWord.charAt(i) == letter) {
                // Correct position - Green
                tv.setBackgroundColor(Color.GREEN);
            } else if (targetWord.contains(String.valueOf(letter))) {
                // Wrong position - Yellow
                tv.setBackgroundColor(Color.YELLOW);
            } else {
                // Not in word - Dark Gray
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
                .setMessage("You guessed the word: " + targetWord + "\nAttempts: " + (currentAttempt + 1))
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