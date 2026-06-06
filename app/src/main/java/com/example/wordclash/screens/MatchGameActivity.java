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

public class MatchGameActivity extends AppCompatActivity {

    private final int TOTAL_PAIRS = 6;
    private TextView tvScore, tvMatches;
    private LinearLayout leftColumn, rightColumn;
    private User user;
    private int rank = 1;
    private List<Word> gameWords;
    private List<Button> leftButtons;
    private List<Button> rightButtons;
    private Button selectedLeft  = null;
    private Button selectedRight = null;
    private int matchesFound = 0;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) LanguageUtils.applyLanguageSettings(this, user);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_game);

        if (user != null) LanguageUtils.setLayoutDirection(this, user);

        rank = getIntent().getIntExtra("RANK", 1);

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        tvScore    = findViewById(R.id.tvScore);
        tvMatches  = findViewById(R.id.tvMatches);
        leftColumn  = findViewById(R.id.leftColumn);
        rightColumn = findViewById(R.id.rightColumn);
        Button btnBack    = findViewById(R.id.btnBack);
        Button btnNewGame = findViewById(R.id.btnNewGame);

        btnBack.setOnClickListener(v -> finish());
        btnNewGame.setOnClickListener(v -> loadWords());

        leftButtons  = new ArrayList<>();
        rightButtons = new ArrayList<>();
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(MatchGameActivity.this,
                            getString(R.string.no_words_available), Toast.LENGTH_SHORT).show();
                    return;
                }

                Collections.shuffle(words);
                gameWords = new ArrayList<>();
                for (int i = 0; i < Math.min(TOTAL_PAIRS, words.size()); i++) {
                    gameWords.add(words.get(i));
                }
                setupGame();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MatchGameActivity.this,
                        getString(R.string.failed_load_words, e.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupGame() {
        matchesFound = 0;
        score        = 0;
        selectedLeft  = null;
        selectedRight = null;

        updateScore();

        leftColumn.removeAllViews();
        rightColumn.removeAllViews();
        leftButtons.clear();
        rightButtons.clear();

        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";
        boolean isLearningEnglish = learningLanguage.equals("english");

        List<Word> leftWords = new ArrayList<>(gameWords);
        for (Word word : leftWords) {
            Button btn = createWordButton(
                    isLearningEnglish ? word.getHebrew() : word.getEnglish(), word, true);
            leftButtons.add(btn);
            leftColumn.addView(btn);
        }

        List<Word> rightWords = new ArrayList<>(gameWords);
        Collections.shuffle(rightWords);
        for (Word word : rightWords) {
            Button btn = createWordButton(
                    isLearningEnglish ? word.getEnglish() : word.getHebrew(), word, false);
            rightButtons.add(btn);
            rightColumn.addView(btn);
        }
    }

    private Button createWordButton(String text, Word word, boolean isLeft) {
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
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
        if (button.getAlpha() == 0.3f) return;

        if (isLeft) {
            if (selectedLeft == button) {
                selectedLeft.setBackgroundColor(Color.parseColor("#2196F3"));
                selectedLeft = null;
                return;
            }
            if (selectedLeft != null) selectedLeft.setBackgroundColor(Color.parseColor("#2196F3"));
            selectedLeft = button;
            selectedLeft.setBackgroundColor(Color.parseColor("#FF6F00"));
        } else {
            if (selectedRight == button) {
                selectedRight.setBackgroundColor(Color.parseColor("#2196F3"));
                selectedRight = null;
                return;
            }
            if (selectedRight != null) selectedRight.setBackgroundColor(Color.parseColor("#2196F3"));
            selectedRight = button;
            selectedRight.setBackgroundColor(Color.parseColor("#FF6F00"));
        }

        if (selectedLeft != null && selectedRight != null) checkMatch();
    }

    private void checkMatch() {
        Word leftWord  = (Word) selectedLeft.getTag();
        Word rightWord = (Word) selectedRight.getTag();

        if (leftWord.getId().equals(rightWord.getId())) {
            selectedLeft.setBackgroundColor(Color.GREEN);
            selectedRight.setBackgroundColor(Color.GREEN);
            selectedLeft.setAlpha(0.3f);
            selectedRight.setAlpha(0.3f);
            selectedLeft.setEnabled(false);
            selectedRight.setEnabled(false);

            matchesFound++;
            score += 10 * rank;
            updateScore();

            selectedLeft  = null;
            selectedRight = null;

            if (matchesFound == TOTAL_PAIRS) {
                new Handler().postDelayed(this::showWinDialog, 500);
            }
        } else {
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
        tvScore.setText(getString(R.string.score, score));
        tvMatches.setText(getString(R.string.matches_progress, matchesFound, TOTAL_PAIRS));
    }

    private void showWinDialog() {
        saveScoreToStats();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.congratulations))
                .setMessage(getString(R.string.match_congrats_msg, score, rank))
                .setPositiveButton(getString(R.string.game_play_again), (d, w) -> loadWords())
                .setNegativeButton(getString(R.string.game_back), (d, w) -> finish())
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
            public void onFailed(Exception e) { }
        });
    }
}