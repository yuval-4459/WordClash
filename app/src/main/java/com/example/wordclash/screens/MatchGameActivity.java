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

/**
 * Match Game - Match English words to Hebrew translations
 */
public class MatchGameActivity extends AppCompatActivity {

    private TextView tvScore, tvMatches;
    private LinearLayout leftColumn, rightColumn;
    private Button btnBack, btnNewGame;

    private User user;
    private List<Word> gameWords;
    private List<Button> leftButtons;
    private List<Button> rightButtons;

    private Button selectedLeft = null;
    private Button selectedRight = null;
    private int matchesFound = 0;
    private int score = 0;
    private final int TOTAL_PAIRS = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_game);

        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        tvScore = findViewById(R.id.tvScore);
        tvMatches = findViewById(R.id.tvMatches);
        leftColumn = findViewById(R.id.leftColumn);
        rightColumn = findViewById(R.id.rightColumn);
        btnBack = findViewById(R.id.btnBack);
        btnNewGame = findViewById(R.id.btnNewGame);

        btnBack.setOnClickListener(v -> finish());
        btnNewGame.setOnClickListener(v -> loadWords());

        leftButtons = new ArrayList<>();
        rightButtons = new ArrayList<>();
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(MatchGameActivity.this, "No words available", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Select random words
                Collections.shuffle(words);
                gameWords = new ArrayList<>();
                for (int i = 0; i < Math.min(TOTAL_PAIRS, words.size()); i++) {
                    gameWords.add(words.get(i));
                }

                setupGame();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MatchGameActivity.this, "Failed to load words", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupGame() {
        matchesFound = 0;
        score = 0;
        selectedLeft = null;
        selectedRight = null;

        updateScore();

        // Clear columns
        leftColumn.removeAllViews();
        rightColumn.removeAllViews();
        leftButtons.clear();
        rightButtons.clear();

        // Determine language direction
        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";
        boolean isLearningEnglish = learningLanguage.equals("english");

        // Create left column (English or Hebrew)
        List<Word> leftWords = new ArrayList<>(gameWords);
        for (Word word : leftWords) {
            Button btn = createWordButton(isLearningEnglish ? word.getHebrew() : word.getEnglish(), word, true);
            leftButtons.add(btn);
            leftColumn.addView(btn);
        }

        // Create right column (Hebrew or English) - shuffled
        List<Word> rightWords = new ArrayList<>(gameWords);
        Collections.shuffle(rightWords);
        for (Word word : rightWords) {
            Button btn = createWordButton(isLearningEnglish ? word.getEnglish() : word.getHebrew(), word, false);
            rightButtons.add(btn);
            rightColumn.addView(btn);
        }
    }

    private Button createWordButton(String text, Word word, boolean isLeft) {
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
        button.setText(text);
        button.setTag(word);
        button.setTextSize(16);
        button.setBackgroundColor(Color.parseColor("#2196F3"));
        button.setTextColor(Color.WHITE);
        button.setPadding(16, 16, 16, 16);

        button.setOnClickListener(v -> handleButtonClick(button, isLeft));

        return button;
    }

    private void handleButtonClick(Button button, boolean isLeft) {
        // If already matched, ignore
        if (button.getAlpha() == 0.3f) return;

        if (isLeft) {
            // Deselect if clicking same button
            if (selectedLeft == button) {
                selectedLeft.setBackgroundColor(Color.parseColor("#2196F3"));
                selectedLeft = null;
                return;
            }

            // Deselect previous left button
            if (selectedLeft != null) {
                selectedLeft.setBackgroundColor(Color.parseColor("#2196F3"));
            }

            // Select new left button
            selectedLeft = button;
            selectedLeft.setBackgroundColor(Color.parseColor("#FF6F00"));
        } else {
            // Deselect if clicking same button
            if (selectedRight == button) {
                selectedRight.setBackgroundColor(Color.parseColor("#2196F3"));
                selectedRight = null;
                return;
            }

            // Deselect previous right button
            if (selectedRight != null) {
                selectedRight.setBackgroundColor(Color.parseColor("#2196F3"));
            }

            // Select new right button
            selectedRight = button;
            selectedRight.setBackgroundColor(Color.parseColor("#FF6F00"));
        }

        // Check if both sides selected
        if (selectedLeft != null && selectedRight != null) {
            checkMatch();
        }
    }

    private void checkMatch() {
        Word leftWord = (Word) selectedLeft.getTag();
        Word rightWord = (Word) selectedRight.getTag();

        if (leftWord.getId().equals(rightWord.getId())) {
            // Correct match!
            selectedLeft.setBackgroundColor(Color.GREEN);
            selectedRight.setBackgroundColor(Color.GREEN);
            selectedLeft.setAlpha(0.3f);
            selectedRight.setAlpha(0.3f);
            selectedLeft.setEnabled(false);
            selectedRight.setEnabled(false);

            matchesFound++;
            score += 10;
            updateScore();

            selectedLeft = null;
            selectedRight = null;

            // Check if game complete
            if (matchesFound == TOTAL_PAIRS) {
                new Handler().postDelayed(() -> showWinDialog(), 500);
            }
        } else {
            // Wrong match
            selectedLeft.setBackgroundColor(Color.RED);
            selectedRight.setBackgroundColor(Color.RED);

            new Handler().postDelayed(() -> {
                if (selectedLeft != null) {
                    selectedLeft.setBackgroundColor(Color.parseColor("#2196F3"));
                    selectedLeft = null;
                }
                if (selectedRight != null) {
                    selectedRight.setBackgroundColor(Color.parseColor("#2196F3"));
                    selectedRight = null;
                }
            }, 500);
        }
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
        tvMatches.setText("Matches: " + matchesFound + "/" + TOTAL_PAIRS);
    }

    private void showWinDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Congratulations!")
                .setMessage("You matched all pairs!\nScore: " + score + " points")
                .setPositiveButton("Play Again", (dialog, which) -> loadWords())
                .setNegativeButton("Back", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}