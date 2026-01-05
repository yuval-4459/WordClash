package com.example.wordclash.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.models.Word;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.LanguageUtils;
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FillGapsGameActivity extends AppCompatActivity {

    private TextView tvHint, tvWord, tvProgress, tvScore;
    private LinearLayout lettersContainer;
    private Button btnBack, btnSkip;

    private User user;
    private List<Word> gameWords;
    private int currentWordIndex = 0;
    private int score = 0;
    private final int TOTAL_WORDS = 10;

    private String targetWord;
    private String displayWord;
    private List<Integer> missingIndices;
    private StringBuilder currentGuess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_gaps_game);

        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        tvHint = findViewById(R.id.tvHint);
        tvWord = findViewById(R.id.tvWord);
        tvProgress = findViewById(R.id.tvProgress);
        tvScore = findViewById(R.id.tvScore);
        lettersContainer = findViewById(R.id.lettersContainer);
        btnBack = findViewById(R.id.btnBack);
        btnSkip = findViewById(R.id.btnSkip);

        btnBack.setOnClickListener(v -> finish());
        btnSkip.setOnClickListener(v -> skipWord());
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(FillGapsGameActivity.this, "No words available", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                gameWords = new ArrayList<>();
                for (Word word : words) {
                    String english = word.getEnglish();
                    if (english.length() >= 4 && english.length() <= 8) {
                        gameWords.add(word);
                    }
                }

                if (gameWords.isEmpty()) {
                    Toast.makeText(FillGapsGameActivity.this, "No suitable words found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Collections.shuffle(gameWords);
                showNextWord();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(FillGapsGameActivity.this, "Failed to load words", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showNextWord() {
        if (currentWordIndex >= TOTAL_WORDS || currentWordIndex >= gameWords.size()) {
            showResults();
            return;
        }

        Word word = gameWords.get(currentWordIndex);

        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";

        if (learningLanguage.equals("english")) {
            targetWord = word.getEnglish().toUpperCase();
            tvHint.setText("Hebrew: " + word.getHebrew());
        } else {
            targetWord = word.getHebrew();
            tvHint.setText("English: " + word.getEnglish());
        }

        createWordWithGaps();
        setupLetterButtons();
        updateProgress();
    }

    private void createWordWithGaps() {
        missingIndices = new ArrayList<>();
        int numMissing = Math.max(2, targetWord.length() / 2);

        List<Integer> allIndices = new ArrayList<>();
        for (int i = 0; i < targetWord.length(); i++) {
            allIndices.add(i);
        }
        Collections.shuffle(allIndices);

        for (int i = 0; i < numMissing && i < allIndices.size(); i++) {
            missingIndices.add(allIndices.get(i));
        }

        currentGuess = new StringBuilder();
        for (int i = 0; i < targetWord.length(); i++) {
            if (missingIndices.contains(i)) {
                currentGuess.append('_');
            } else {
                currentGuess.append(targetWord.charAt(i));
            }
        }

        updateWordDisplay();
    }

    private void setupLetterButtons() {
        lettersContainer.removeAllViews();

        List<Character> missingLetters = new ArrayList<>();
        for (int index : missingIndices) {
            missingLetters.add(targetWord.charAt(index));
        }
        Collections.shuffle(missingLetters);

        for (int i = 0; i < missingLetters.size(); i++) {
            final char letter = missingLetters.get(i);
            final int position = i;

            Button btn = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            params.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(params);
            btn.setText(String.valueOf(letter));
            btn.setTextSize(18);
            btn.setBackgroundColor(Color.parseColor("#2196F3"));
            btn.setTextColor(Color.WHITE);
            btn.setPadding(8, 16, 8, 16);

            btn.setOnClickListener(v -> fillLetter(letter, btn));

            lettersContainer.addView(btn);
        }
    }

    private void fillLetter(char letter, Button button) {
        int nextGapIndex = -1;
        for (int i = 0; i < currentGuess.length(); i++) {
            if (currentGuess.charAt(i) == '_') {
                nextGapIndex = i;
                break;
            }
        }

        if (nextGapIndex == -1) return;

        currentGuess.setCharAt(nextGapIndex, letter);
        button.setEnabled(false);
        button.setAlpha(0.3f);

        updateWordDisplay();

        if (currentGuess.indexOf("_") == -1) {
            checkAnswer();
        }
    }

    private void updateWordDisplay() {
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < currentGuess.length(); i++) {
            display.append(currentGuess.charAt(i)).append(" ");
        }
        tvWord.setText(display.toString().trim());
    }

    private void checkAnswer() {
        String answer = currentGuess.toString();

        if (answer.equals(targetWord)) {
            score += 10;
            updateScore();

            tvWord.setTextColor(Color.GREEN);
            Toast.makeText(this, "✓ Correct!", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                tvWord.setTextColor(Color.BLACK);
                currentWordIndex++;
                showNextWord();
            }, 1000);
        } else {
            tvWord.setTextColor(Color.RED);
            Toast.makeText(this, "✗ Wrong! Try again", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                tvWord.setTextColor(Color.BLACK);
                createWordWithGaps();
                setupLetterButtons();
            }, 1000);
        }
    }

    private void skipWord() {
        new AlertDialog.Builder(this)
                .setTitle("Skip Word")
                .setMessage("The correct word was: " + targetWord)
                .setPositiveButton("Next", (dialog, which) -> {
                    currentWordIndex++;
                    showNextWord();
                })
                .show();
    }

    private void updateProgress() {
        tvProgress.setText("Word " + (currentWordIndex + 1) + " / " + TOTAL_WORDS);
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
    }

    private void showResults() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Game Complete!")
                .setMessage("Your Score: " + score + " / " + (TOTAL_WORDS * 10))
                .setPositiveButton("Play Again", (dialog, which) -> {
                    currentWordIndex = 0;
                    score = 0;
                    Collections.shuffle(gameWords);
                    showNextWord();
                })
                .setNegativeButton("Back", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}