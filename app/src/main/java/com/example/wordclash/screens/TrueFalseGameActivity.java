package com.example.wordclash.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
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

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * True/False Game - Verify if translations are correct
 */
public class TrueFalseGameActivity extends AppCompatActivity {

    private TextView tvQuestion, tvProgress, tvScore;
    private Button btnTrue, btnFalse, btnBack;

    private User user;
    private List<Word> allWords;
    private int currentQuestion = 0;
    private int score = 0;
    private final int TOTAL_QUESTIONS = 15;
    private boolean answerSelected = false;

    // Current question data
    private String displayEnglish;
    private String displayHebrew;
    private boolean isCorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_true_false_game);

        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvProgress = findViewById(R.id.tvProgress);
        tvScore = findViewById(R.id.tvScore);
        btnTrue = findViewById(R.id.btnTrue);
        btnFalse = findViewById(R.id.btnFalse);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnTrue.setOnClickListener(v -> checkAnswer(true));
        btnFalse.setOnClickListener(v -> checkAnswer(false));
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.size() < 10) {
                    Toast.makeText(TrueFalseGameActivity.this, "Not enough words", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                allWords = words;
                Collections.shuffle(allWords);
                showNextQuestion();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(TrueFalseGameActivity.this, "Failed to load words", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showNextQuestion() {
        if (currentQuestion >= TOTAL_QUESTIONS) {
            showResults();
            return;
        }

        answerSelected = false;
        btnTrue.setEnabled(true);
        btnFalse.setEnabled(true);
        btnTrue.setBackgroundColor(Color.parseColor("#43A047"));
        btnFalse.setBackgroundColor(Color.parseColor("#E53935"));

        // Get random word
        Word correctWord = allWords.get(currentQuestion % allWords.size());

        // 50% chance of showing correct translation
        Random random = new Random();
        isCorrect = random.nextBoolean();

        if (isCorrect) {
            // Show correct pair
            displayEnglish = correctWord.getEnglish();
            displayHebrew = correctWord.getHebrew();
        } else {
            // Show incorrect pair (mix with another word)
            Word wrongWord = allWords.get((currentQuestion + 1) % allWords.size());
            displayEnglish = correctWord.getEnglish();
            displayHebrew = wrongWord.getHebrew();
        }

        // Display based on learning language
        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";

        if (learningLanguage.equals("english")) {
            // Learning English: show Hebrew = English?
            tvQuestion.setText(displayHebrew + " = " + displayEnglish);
        } else {
            // Learning Hebrew: show English = Hebrew?
            tvQuestion.setText(displayEnglish + " = " + displayHebrew);
        }

        updateProgress();
    }

    private void checkAnswer(boolean userAnswer) {
        if (answerSelected) return;
        answerSelected = true;

        btnTrue.setEnabled(false);
        btnFalse.setEnabled(false);

        if (userAnswer == isCorrect) {
            // Correct!
            score += 10;
            if (userAnswer) {
                btnTrue.setBackgroundColor(Color.GREEN);
            } else {
                btnFalse.setBackgroundColor(Color.GREEN);
            }
        } else {
            // Wrong
            if (userAnswer) {
                btnTrue.setBackgroundColor(Color.RED);
                btnFalse.setBackgroundColor(Color.GREEN);
            } else {
                btnFalse.setBackgroundColor(Color.RED);
                btnTrue.setBackgroundColor(Color.GREEN);
            }
        }

        updateScore();

        // Next question after delay
        new Handler().postDelayed(() -> {
            currentQuestion++;
            showNextQuestion();
        }, 1000);
    }

    private void updateProgress() {
        tvProgress.setText("Question " + (currentQuestion + 1) + " / " + TOTAL_QUESTIONS);
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
    }

    private void showResults() {
        int percentage = (score * 100) / (TOTAL_QUESTIONS * 10);
        String message = "Your Score: " + score + " / " + (TOTAL_QUESTIONS * 10) + "\n" +
                "Accuracy: " + percentage + "%";

        new AlertDialog.Builder(this)
                .setTitle("🎉 Game Complete!")
                .setMessage(message)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    currentQuestion = 0;
                    score = 0;
                    Collections.shuffle(allWords);
                    showNextQuestion();
                })
                .setNegativeButton("Back", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}