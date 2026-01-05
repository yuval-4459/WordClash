package com.example.wordclash.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;
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

public class SpeedQuizGameActivity extends AppCompatActivity {

    private TextView tvQuestion, tvTimer, tvScore, tvProgress;
    private Button btnOption1, btnOption2, btnOption3, btnOption4;
    private ProgressBar progressBar;
    private Button btnBack;

    private User user;
    private List<Word> allWords;
    private List<Word> gameWords;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private final int TOTAL_QUESTIONS = 20;
    private CountDownTimer timer;
    private boolean answerSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_quiz_game);

        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnBack = findViewById(R.id.btnBack);

        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3));
        btnOption4.setOnClickListener(v -> checkAnswer(btnOption4));
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(SpeedQuizGameActivity.this, "No words available", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                allWords = words;
                selectRandomWords();
                showQuestion();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(SpeedQuizGameActivity.this, "Failed to load words", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void selectRandomWords() {
        gameWords = new ArrayList<>();
        List<Word> shuffled = new ArrayList<>(allWords);
        Collections.shuffle(shuffled);

        for (int i = 0; i < Math.min(TOTAL_QUESTIONS, shuffled.size()); i++) {
            gameWords.add(shuffled.get(i));
        }
    }

    private void showQuestion() {
        if (currentQuestionIndex >= gameWords.size()) {
            endGame();
            return;
        }

        answerSelected = false;
        Word currentWord = gameWords.get(currentQuestionIndex);

        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";

        boolean showHebrewQuestion = learningLanguage.equals("english");

        if (showHebrewQuestion) {
            tvQuestion.setText(currentWord.getHebrew());
            setupOptions(currentWord, false);
        } else {
            tvQuestion.setText(currentWord.getEnglish());
            setupOptions(currentWord, true);
        }

        updateProgress();
        startTimer();
    }

    private void setupOptions(Word correctWord, boolean showHebrew) {
        List<Word> options = new ArrayList<>();
        options.add(correctWord);

        List<Word> otherWords = new ArrayList<>(allWords);
        otherWords.remove(correctWord);
        Collections.shuffle(otherWords);

        for (int i = 0; i < Math.min(3, otherWords.size()); i++) {
            options.add(otherWords.get(i));
        }

        Collections.shuffle(options);

        Button[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};
        for (int i = 0; i < buttons.length && i < options.size(); i++) {
            Word word = options.get(i);
            String text = showHebrew ? word.getHebrew() : word.getEnglish();
            buttons[i].setText(text);
            buttons[i].setTag(word);
            buttons[i].setBackgroundColor(Color.parseColor("#2196F3"));
            buttons[i].setEnabled(true);
        }
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }

        progressBar.setMax(7);
        progressBar.setProgress(7);

        timer = new CountDownTimer(7000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.valueOf(secondsLeft + 1));
                progressBar.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("0");
                if (!answerSelected) {
                    showCorrectAnswer();
                    new Handler().postDelayed(() -> nextQuestion(), 1000);
                }
            }
        }.start();
    }

    private void checkAnswer(Button selectedButton) {
        if (answerSelected) return;

        answerSelected = true;
        timer.cancel();

        Word selectedWord = (Word) selectedButton.getTag();
        Word correctWord = gameWords.get(currentQuestionIndex);

        if (selectedWord.getId().equals(correctWord.getId())) {
            selectedButton.setBackgroundColor(Color.GREEN);
            score += 10;
            updateScore();
        } else {
            selectedButton.setBackgroundColor(Color.RED);
            showCorrectAnswer();
        }

        disableAllButtons();
        new Handler().postDelayed(() -> nextQuestion(), 1000);
    }

    private void showCorrectAnswer() {
        Word correctWord = gameWords.get(currentQuestionIndex);
        Button[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};

        for (Button button : buttons) {
            Word word = (Word) button.getTag();
            if (word != null && word.getId().equals(correctWord.getId())) {
                button.setBackgroundColor(Color.GREEN);
                break;
            }
        }
    }

    private void disableAllButtons() {
        btnOption1.setEnabled(false);
        btnOption2.setEnabled(false);
        btnOption3.setEnabled(false);
        btnOption4.setEnabled(false);
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        showQuestion();
    }

    private void updateProgress() {
        tvProgress.setText("Question " + (currentQuestionIndex + 1) + " / " + gameWords.size());
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
    }

    private void endGame() {
        if (timer != null) {
            timer.cancel();
        }

        int percentage = (score * 100) / (gameWords.size() * 10);
        String message = "Your Score: " + score + " / " + (gameWords.size() * 10) + "\n" +
                "Accuracy: " + percentage + "%";

        new AlertDialog.Builder(this)
                .setTitle("🎉 Speed Quiz Complete!")
                .setMessage(message)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    currentQuestionIndex = 0;
                    score = 0;
                    selectRandomWords();
                    showQuestion();
                })
                .setNegativeButton("Back", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}