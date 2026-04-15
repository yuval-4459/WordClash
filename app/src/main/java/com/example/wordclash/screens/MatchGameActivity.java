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
import com.example.wordclash.models.Stats;
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

    private final int TOTAL_PAIRS = 6;
    private TextView tvScore, tvMatches;
    private LinearLayout leftColumn, rightColumn;
    private User user;
    private int rank = 1;
    private List<Word> gameWords;
    private List<Button> leftButtons;
    private List<Button> rightButtons;
    private Button selectedLeft = null;
    private Button selectedRight = null;
    private int matchesFound = 0;
    private int score = 0;

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

        rank = getIntent().getIntExtra("RANK", 1);

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        tvScore = findViewById(R.id.tvScore);
        tvMatches = findViewById(R.id.tvMatches);
        leftColumn = findViewById(R.id.leftColumn);
        rightColumn = findViewById(R.id.rightColumn);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnNewGame = findViewById(R.id.btnNewGame);

        btnBack.setOnClickListener(v -> finish());
        btnNewGame.setOnClickListener(v -> loadWords());

        leftButtons = new ArrayList<>();
        rightButtons = new ArrayList<>();
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(MatchGameActivity.this, "No words available", Toast.LENGTH_SHORT).show();
                    return;
                }

                // select random words
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

        // clear columns
        leftColumn.removeAllViews();
        rightColumn.removeAllViews();
        leftButtons.clear();
        rightButtons.clear();

        // determine language direction
        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";
        boolean isLearningEnglish = learningLanguage.equals("english");

        // create left column (English or Hebrew)
        List<Word> leftWords = new ArrayList<>(gameWords);
        for (Word word : leftWords) {
            Button btn = createWordButton(isLearningEnglish ? word.getHebrew() : word.getEnglish(), word, true);
            leftButtons.add(btn);
            leftColumn.addView(btn);
        }

        // create right column (Hebrew or English) - shuffled
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
        // if already matched, ignore
        if (button.getAlpha() == 0.3f) return;

        if (isLeft) {
            // deselect if clicking same button
            if (selectedLeft == button) {
                selectedLeft.setBackgroundColor(Color.parseColor("#2196F3"));
                selectedLeft = null;
                return;
            }

            // deselect previous left button
            if (selectedLeft != null) {
                selectedLeft.setBackgroundColor(Color.parseColor("#2196F3"));
            }

            // select new left button
            selectedLeft = button;
            selectedLeft.setBackgroundColor(Color.parseColor("#FF6F00"));
        } else {
            // deselect if clicking same button
            if (selectedRight == button) {
                selectedRight.setBackgroundColor(Color.parseColor("#2196F3"));
                selectedRight = null;
                return;
            }

            // deselect previous right button
            if (selectedRight != null) {
                selectedRight.setBackgroundColor(Color.parseColor("#2196F3"));
            }

            // select new right button
            selectedRight = button;
            selectedRight.setBackgroundColor(Color.parseColor("#FF6F00"));
        }

        // check if both sides selected
        if (selectedLeft != null && selectedRight != null) {
            checkMatch();
        }
    }

    private void checkMatch() {
        Word leftWord = (Word) selectedLeft.getTag();
        Word rightWord = (Word) selectedRight.getTag();

        if (leftWord.getId().equals(rightWord.getId())) {
            // correct match
            selectedLeft.setBackgroundColor(Color.GREEN);
            selectedRight.setBackgroundColor(Color.GREEN);
            selectedLeft.setAlpha(0.3f);
            selectedRight.setAlpha(0.3f);
            selectedLeft.setEnabled(false);
            selectedRight.setEnabled(false);

            matchesFound++;
            score += 10 * rank;
            updateScore();

            selectedLeft = null;
            selectedRight = null;

            // check if game complete
            if (matchesFound == TOTAL_PAIRS) {
                new Handler().postDelayed(this::showWinDialog, 500);
            }
        } else {
            // wrong match
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
        saveScoreToStats();

        new AlertDialog.Builder(this)
                .setTitle("🎉 Congratulations!")
                .setMessage("You matched all pairs!\nScore: " + score + " points\n(Rank " + rank + " bonus applied!)")
                .setPositiveButton("Play Again", (dialog, which) -> loadWords())
                .setNegativeButton("Back", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void saveScoreToStats() {
        DatabaseService.getInstance().getStats(user.getId(), new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Stats stats) {
                if (stats == null) stats = new Stats(user.getId(), 1, 0);
                stats.setTotalScore(stats.getTotalScore() + score);
                DatabaseService.getInstance().updateStats(stats, null);
            }

            @Override
            public void onFailed(Exception e) {
            }
        });
    }
}