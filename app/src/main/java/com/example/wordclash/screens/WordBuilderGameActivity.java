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

public class WordBuilderGameActivity extends AppCompatActivity {

    private final int TOTAL_WORDS = 10;
    private TextView tvHint, tvBuiltWord, tvProgress, tvScore;
    private LinearLayout lettersContainer;
    private User user;
    private int rank = 1;
    private List<Word> gameWords;
    private int currentWordIndex = 0;
    private int score = 0;
    private String targetWord;
    private StringBuilder builtWord;
    private List<Button> letterButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) LanguageUtils.applyLanguageSettings(this, user);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_builder_game);

        if (user != null) LanguageUtils.setLayoutDirection(this, user);

        rank = getIntent().getIntExtra("RANK", 1);

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        tvHint = findViewById(R.id.tvHint);
        tvBuiltWord = findViewById(R.id.tvBuiltWord);
        tvProgress = findViewById(R.id.tvProgress);
        tvScore = findViewById(R.id.tvScore);
        lettersContainer = findViewById(R.id.lettersContainer);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        Button btnClear = findViewById(R.id.btnClear);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnSkip = findViewById(R.id.btnSkip);

        btnSubmit.setOnClickListener(v -> checkAnswer());
        btnClear.setOnClickListener(v -> clearWord());
        btnBack.setOnClickListener(v -> finish());
        btnSkip.setOnClickListener(v -> skipWord());

        builtWord = new StringBuilder();
        letterButtons = new ArrayList<>();
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(WordBuilderGameActivity.this,
                            getString(R.string.no_words_available), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(WordBuilderGameActivity.this,
                            getString(R.string.no_suitable_words), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Collections.shuffle(gameWords);
                showNextWord();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(WordBuilderGameActivity.this,
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

        clearWord();
        setupLetterButtons();
        updateProgress();
    }

    private void setupLetterButtons() {
        lettersContainer.removeAllViews();
        letterButtons.clear();

        List<Character> letters = new ArrayList<>();
        for (char c : targetWord.toCharArray()) letters.add(c);
        Collections.shuffle(letters);

        String extraLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < 2 && letters.size() < 10; i++) {
            letters.add(extraLetters.charAt((int) (Math.random() * extraLetters.length())));
        }
        Collections.shuffle(letters);

        for (char letter : letters) {
            Button btn = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            params.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(params);
            btn.setText(String.valueOf(letter));
            btn.setTextSize(18);
            btn.setBackgroundColor(Color.parseColor("#2196F3"));
            btn.setTextColor(Color.WHITE);
            btn.setPadding(8, 16, 8, 16);
            btn.setOnClickListener(v -> addLetter(btn));
            letterButtons.add(btn);
            lettersContainer.addView(btn);
        }
    }

    private void addLetter(Button button) {
        if (button.getTag() != null && button.getTag().equals("used")) return;
        builtWord.append(button.getText());
        button.setTag("used");
        button.setBackgroundColor(Color.GRAY);
        updateBuiltWord();
    }

    private void clearWord() {
        builtWord.setLength(0);
        updateBuiltWord();
        for (Button btn : letterButtons) {
            btn.setTag(null);
            btn.setBackgroundColor(Color.parseColor("#2196F3"));
        }
    }

    private void updateBuiltWord() {
        tvBuiltWord.setText(builtWord.length() > 0 ? builtWord.toString() : "___");
    }

    private void checkAnswer() {
        String answer = builtWord.toString();

        if (answer.equals(targetWord)) {
            score += 10 * rank;
            updateScore();
            tvBuiltWord.setTextColor(Color.GREEN);
            Toast.makeText(this, getString(R.string.game_correct), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                tvBuiltWord.setTextColor(Color.BLACK);
                currentWordIndex++;
                showNextWord();
            }, 1000);
        } else {
            tvBuiltWord.setTextColor(Color.RED);
            Toast.makeText(this, getString(R.string.game_wrong), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> tvBuiltWord.setTextColor(Color.BLACK), 500);
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
                    score = 0;
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
            public void onFailed(Exception e) {
            }
        });
    }
}