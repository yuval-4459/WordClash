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
 * Word Builder Game - Build words from scrambled letters
 */
public class WordBuilderGameActivity extends AppCompatActivity {

    private final int TOTAL_WORDS = 10;
    private TextView tvHint, tvBuiltWord, tvProgress, tvScore;
    private LinearLayout lettersContainer;
    private Button btnSubmit, btnClear, btnBack, btnSkip;
    private User user;
    private List<Word> gameWords;
    private int currentWordIndex = 0;
    private int score = 0;
    private String targetWord;
    private String targetHint;
    private StringBuilder builtWord;
    private List<Button> letterButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_builder_game);

        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        tvHint = findViewById(R.id.tvHint);
        tvBuiltWord = findViewById(R.id.tvBuiltWord);
        tvProgress = findViewById(R.id.tvProgress);
        tvScore = findViewById(R.id.tvScore);
        lettersContainer = findViewById(R.id.lettersContainer);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnClear = findViewById(R.id.btnClear);
        btnBack = findViewById(R.id.btnBack);
        btnSkip = findViewById(R.id.btnSkip);

        btnSubmit.setOnClickListener(v -> checkAnswer());
        btnClear.setOnClickListener(v -> clearWord());
        btnBack.setOnClickListener(v -> finish());
        btnSkip.setOnClickListener(v -> skipWord());

        builtWord = new StringBuilder();
        letterButtons = new ArrayList<>();
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(WordBuilderGameActivity.this, "No words available", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Filter words (4-8 letters work best)
                gameWords = new ArrayList<>();
                for (Word word : words) {
                    String english = word.getEnglish();
                    if (english.length() >= 4 && english.length() <= 8) {
                        gameWords.add(word);
                    }
                }

                if (gameWords.isEmpty()) {
                    Toast.makeText(WordBuilderGameActivity.this, "No suitable words found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Collections.shuffle(gameWords);
                showNextWord();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(WordBuilderGameActivity.this, "Failed to load words", Toast.LENGTH_SHORT).show();
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

        // Determine which word to build based on learning language
        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";

        if (learningLanguage.equals("english")) {
            // Learning English: build English word from Hebrew hint
            targetWord = word.getEnglish().toUpperCase();
            targetHint = word.getHebrew();
            tvHint.setText("Hebrew: " + targetHint);
        } else {
            // Learning Hebrew: build Hebrew word from English hint
            targetWord = word.getHebrew();
            targetHint = word.getEnglish();
            tvHint.setText("English: " + targetHint);
        }

        clearWord();
        setupLetterButtons();
        updateProgress();
    }

    private void setupLetterButtons() {
        lettersContainer.removeAllViews();
        letterButtons.clear();

        // Create scrambled letters
        List<Character> letters = new ArrayList<>();
        for (char c : targetWord.toCharArray()) {
            letters.add(c);
        }
        Collections.shuffle(letters);

        // Add 2-3 extra random letters for difficulty
        String extraLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < 2 && letters.size() < 10; i++) {
            char randomChar = extraLetters.charAt((int) (Math.random() * extraLetters.length()));
            letters.add(randomChar);
        }
        Collections.shuffle(letters);

        // Create buttons
        for (char letter : letters) {
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

            btn.setOnClickListener(v -> addLetter(btn));

            letterButtons.add(btn);
            lettersContainer.addView(btn);
        }
    }

    private void addLetter(Button button) {
        if (button.getTag() != null && button.getTag().equals("used")) return;

        builtWord.append(button.getText());
        button.setTag("used");
        button.setBackgroundColor(Color.GRAY);  // Change color instead
        updateBuiltWord();
    }

    private void clearWord() {
        builtWord.setLength(0);
        updateBuiltWord();

        for (Button btn : letterButtons) {
            btn.setTag(null);
            btn.setBackgroundColor(Color.parseColor("#2196F3"));  // Reset to blue
        }
    }

    private void updateBuiltWord() {
        tvBuiltWord.setText(builtWord.length() > 0 ? builtWord.toString() : "___");
    }

    private void checkAnswer() {
        String answer = builtWord.toString();

        if (answer.equals(targetWord)) {
            // Correct!
            score += 10;
            updateScore();

            tvBuiltWord.setTextColor(Color.GREEN);
            Toast.makeText(this, "✓ Correct!", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                tvBuiltWord.setTextColor(Color.BLACK);
                currentWordIndex++;
                showNextWord();
            }, 1000);
        } else {
            // Wrong
            tvBuiltWord.setTextColor(Color.RED);
            Toast.makeText(this, "✗ Try again!", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                tvBuiltWord.setTextColor(Color.BLACK);
            }, 500);
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