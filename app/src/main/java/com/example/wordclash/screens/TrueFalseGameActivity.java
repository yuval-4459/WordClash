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
import com.example.wordclash.models.Stats;
import com.example.wordclash.models.User;
import com.example.wordclash.models.Word;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.LanguageUtils;
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TrueFalseGameActivity extends AppCompatActivity {

    private final int TOTAL_QUESTIONS = 15;
    private TextView tvQuestion, tvProgress, tvScore;
    private Button btnTrue, btnFalse;
    private User user;
    private int rank = 1;
    private List<Word> allWords;
    private int currentQuestion = 0;
    private int score            = 0;
    private boolean answerSelected = false;
    private boolean isCorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) LanguageUtils.applyLanguageSettings(this, user);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_true_false_game);

        if (user != null) LanguageUtils.setLayoutDirection(this, user);

        rank = getIntent().getIntExtra("RANK", 1);

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvProgress = findViewById(R.id.tvProgress);
        tvScore    = findViewById(R.id.tvScore);
        btnTrue    = findViewById(R.id.btnTrue);
        btnFalse   = findViewById(R.id.btnFalse);

        btnTrue.setOnClickListener(v -> checkAnswer(true));
        btnFalse.setOnClickListener(v -> checkAnswer(false));
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.size() < 10) {
                    Toast.makeText(TrueFalseGameActivity.this,
                            getString(R.string.no_suitable_words), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                allWords = words;
                Collections.shuffle(allWords);
                showNextQuestion();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(TrueFalseGameActivity.this,
                        getString(R.string.failed_load_words, e.getMessage()),
                        Toast.LENGTH_SHORT).show();
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

        Word correctWord = allWords.get(currentQuestion % allWords.size());

        Random random = new Random();
        isCorrect = random.nextBoolean();

        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";

        String displayEnglish, displayHebrew;
        if (learningLanguage.equals("english")) {
            displayHebrew = correctWord.getHebrew();
            if (isCorrect) {
                displayEnglish = correctWord.getEnglish();
            } else {
                Word wrongWord = allWords.get((currentQuestion + 1) % allWords.size());
                displayEnglish = wrongWord.getEnglish();
            }
            tvQuestion.setText(displayHebrew + " = " + displayEnglish);
        } else {
            displayEnglish = correctWord.getEnglish();
            if (isCorrect) {
                displayHebrew = correctWord.getHebrew();
            } else {
                Word wrongWord = allWords.get((currentQuestion + 1) % allWords.size());
                displayHebrew = wrongWord.getHebrew();
            }
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
            score += 10 * rank;
            if (userAnswer) btnTrue.setBackgroundColor(Color.GREEN);
            else            btnFalse.setBackgroundColor(Color.GREEN);
        } else {
            if (userAnswer) {
                btnTrue.setBackgroundColor(Color.RED);
                btnFalse.setBackgroundColor(Color.GREEN);
            } else {
                btnFalse.setBackgroundColor(Color.RED);
                btnTrue.setBackgroundColor(Color.GREEN);
            }
        }

        updateScore();

        new Handler().postDelayed(() -> {
            currentQuestion++;
            showNextQuestion();
        }, 1000);
    }

    private void updateProgress() {
        tvProgress.setText(getString(R.string.question_progress, currentQuestion + 1, TOTAL_QUESTIONS));
    }

    private void updateScore() {
        tvScore.setText(getString(R.string.score, score));
    }

    private void showResults() {
        saveScoreToStats();

        int percentage = (score * 100) / (TOTAL_QUESTIONS * 10 * rank);
        String message = getString(R.string.your_score, score, TOTAL_QUESTIONS * 10 * rank)
                + "\n" + getString(R.string.game_complete_msg, score, TOTAL_QUESTIONS * 10 * rank, rank);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.game_complete_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.game_play_again), (d, w) -> {
                    currentQuestion = 0;
                    score           = 0;
                    Collections.shuffle(allWords);
                    showNextQuestion();
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