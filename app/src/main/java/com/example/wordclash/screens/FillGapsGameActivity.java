package com.example.wordclash.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

public class FillGapsGameActivity extends AppCompatActivity {

    private final int TOTAL_WORDS = 10;
    private TextView tvHint, tvWord, tvProgress, tvScore;
    private LinearLayout lettersContainer;
    private User user;
    private int rank = 1;
    private List<Word> gameWords;
    private int currentWordIndex = 0;
    private int score            = 0;
    private String targetWord;
    private List<Integer> missingIndices;
    private StringBuilder currentGuess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) LanguageUtils.applyLanguageSettings(this, user);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_gaps_game);

        if (user != null) LanguageUtils.setLayoutDirection(this, user);

        rank = getIntent().getIntExtra("RANK", 1);
        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        tvHint           = findViewById(R.id.tvHint);
        tvWord           = findViewById(R.id.tvWord);
        tvProgress       = findViewById(R.id.tvProgress);
        tvScore          = findViewById(R.id.tvScore);
        lettersContainer = findViewById(R.id.lettersContainer);

        Button btnSkip = findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(v -> skipWord());
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(FillGapsGameActivity.this,
                            getString(R.string.no_words_available), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                gameWords = new ArrayList<>();
                for (Word word : words) {
                    String english = word.getEnglish();
                    if (english != null && english.length() >= 4 && english.length() <= 8) {
                        gameWords.add(word);
                    }
                }
                if (gameWords.isEmpty()) {
                    Toast.makeText(FillGapsGameActivity.this,
                            getString(R.string.no_suitable_words), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Collections.shuffle(gameWords);
                showNextWord();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(FillGapsGameActivity.this,
                        getString(R.string.failed_load_words, e.getMessage()),
                        Toast.LENGTH_SHORT).show();
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
            tvHint.setText(getString(R.string.word_builder_hint_hebrew, word.getHebrew()));
        } else {
            targetWord = word.getHebrew();
            tvHint.setText(getString(R.string.word_builder_hint_english, word.getEnglish()));
        }

        createWordWithGaps();
        setupLetterButtons();
        updateProgress();
    }

    private void createWordWithGaps() {
        missingIndices = new ArrayList<>();
        int numMissing = Math.max(2, targetWord.length() / 2);

        List<Integer> allIndices = new ArrayList<>();
        for (int i = 0; i < targetWord.length(); i++) allIndices.add(i);
        Collections.shuffle(allIndices);

        for (int i = 0; i < numMissing && i < allIndices.size(); i++) {
            missingIndices.add(allIndices.get(i));
        }

        currentGuess = new StringBuilder();
        for (int i = 0; i < targetWord.length(); i++) {
            currentGuess.append(missingIndices.contains(i) ? '_' : targetWord.charAt(i));
        }
        updateWordDisplay();
    }

    private void setupLetterButtons() {
        lettersContainer.removeAllViews();

        List<Character> missingLetters = new ArrayList<>();
        for (int index : missingIndices) missingLetters.add(targetWord.charAt(index));
        Collections.shuffle(missingLetters);

        int maxPerRow = 4;
        int total     = missingLetters.size();
        float density = getResources().getDisplayMetrics().density;
        int letterSizePx = (int)(56 * density);

        for (int rowStart = 0; rowStart < total; rowStart += maxPerRow) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, (int)(8 * density));
            row.setLayoutParams(rowParams);
            row.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

            int rowEnd = Math.min(rowStart + maxPerRow, total);
            for (int i = rowStart; i < rowEnd; i++) {
                final char letter = missingLetters.get(i);
                Button btn = new Button(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        letterSizePx, letterSizePx);
                params.setMargins((int)(6 * density), 0, (int)(6 * density), 0);
                btn.setLayoutParams(params);
                btn.setText(String.valueOf(letter));
                btn.setTextSize(22);
                btn.setBackgroundColor(Color.parseColor("#2196F3"));
                btn.setTextColor(Color.WHITE);
                btn.setPadding(0, 0, 0, 0);
                btn.setTag("available");
                btn.setOnClickListener(v -> fillLetter(letter, btn));
                row.addView(btn);
            }
            lettersContainer.addView(row);
        }
    }

    private void fillLetter(char letter, Button button) {
        if (!"available".equals(button.getTag())) return;

        int nextGapIndex = -1;
        for (int i = 0; i < currentGuess.length(); i++) {
            if (currentGuess.charAt(i) == '_') { nextGapIndex = i; break; }
        }
        if (nextGapIndex == -1) return;

        currentGuess.setCharAt(nextGapIndex, letter);
        button.setTag("used");
        button.setEnabled(false);
        button.setAlpha(0.3f);
        updateWordDisplay();

        if (currentGuess.indexOf("_") == -1) checkAnswer();
    }

    private void updateWordDisplay() {
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < currentGuess.length(); i++) {
            display.append(currentGuess.charAt(i)).append(" ");
        }
        tvWord.setText(display.toString().trim());
    }

    private void checkAnswer() {
        if (currentGuess.toString().equals(targetWord)) {
            score += 10 * rank;
            updateScore();
            tvWord.setTextColor(Color.parseColor("#43A047"));
            Toast.makeText(this, getString(R.string.game_correct), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                tvWord.setTextColor(Color.BLACK);
                currentWordIndex++;
                showNextWord();
            }, 1000);
        } else {
            tvWord.setTextColor(Color.RED);
            Toast.makeText(this, getString(R.string.game_wrong), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                tvWord.setTextColor(Color.BLACK);
                createWordWithGaps();
                setupLetterButtons();
            }, 1000);
        }
    }

    private void skipWord() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.skip_word_title))
                .setMessage(getString(R.string.skip_word_msg, targetWord))
                .setPositiveButton(getString(R.string.next), (d, w) -> {
                    currentWordIndex++;
                    showNextWord();
                })
                .show();
    }

    private void updateProgress() {
        tvProgress.setText(getString(R.string.word_progress, currentWordIndex + 1, TOTAL_WORDS));
    }

    private void updateScore() {
        tvScore.setText(getString(R.string.score, score));
    }

    private void showResults() {
        saveScoreToStats();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.game_complete_title))
                .setMessage(getString(R.string.game_complete_msg, score, TOTAL_WORDS * 10 * rank, rank))
                .setPositiveButton(getString(R.string.game_play_again), (d, w) -> {
                    currentWordIndex = 0;
                    score            = 0;
                    Collections.shuffle(gameWords);
                    showNextWord();
                })
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