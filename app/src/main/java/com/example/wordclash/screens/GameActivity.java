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
import com.example.wordclash.models.Stats;
import com.example.wordclash.models.User;
import com.example.wordclash.models.Word;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Quiz game activity
 * FIXED: Now saves practice count to the correct rank
 */
public class GameActivity extends AppCompatActivity {

    private TextView tvQuestion, tvTimer, tvScore, tvProgress;
    private Button btnOption1, btnOption2, btnOption3, btnOption4;
    private ProgressBar progressBar;

    private User user;
    private Stats stats;
    private int currentRank;
    private List<Word> allWords;
    private List<Word> gameWords;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int totalQuestions;
    private CountDownTimer timer;
    private boolean answerSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        currentRank = getIntent().getIntExtra("RANK", 1);
        user = SharedPreferencesUtils.getUser(this);

        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadStatsAndWords();
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

        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3));
        btnOption4.setOnClickListener(v -> checkAnswer(btnOption4));
    }

    private void loadStatsAndWords() {
        DatabaseService.getInstance().getStats(user.getId(), new DatabaseService.DatabaseCallback<Stats>() {
            @Override
            public void onCompleted(Stats loadedStats) {
                stats = loadedStats;
                if (stats == null) {
                    Toast.makeText(GameActivity.this, "Stats not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                totalQuestions = stats.getQuestionsPerPractice();
                loadWords();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(GameActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadWords() {
        DatabaseService.getInstance().getWordsByRank(currentRank, new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(GameActivity.this, "No words available", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                allWords = words;
                selectRandomWords();
                showQuestion();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(GameActivity.this, "Failed to load words", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void selectRandomWords() {
        gameWords = new ArrayList<>();
        List<Word> shuffled = new ArrayList<>(allWords);
        Collections.shuffle(shuffled);

        for (int i = 0; i < Math.min(totalQuestions, shuffled.size()); i++) {
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

        // Randomly choose to show English or Hebrew
        Random random = new Random();
        boolean showEnglish = random.nextBoolean();

        if (showEnglish) {
            tvQuestion.setText(currentWord.getEnglish());
            setupOptions(currentWord, false); // Show Hebrew options
        } else {
            tvQuestion.setText(currentWord.getHebrew());
            setupOptions(currentWord, true); // Show English options
        }

        updateProgress();
        startTimer();
    }

    private void setupOptions(Word correctWord, boolean showEnglish) {
        List<Word> options = new ArrayList<>();
        options.add(correctWord);

        // Add 3 random wrong answers
        List<Word> otherWords = new ArrayList<>(allWords);
        otherWords.remove(correctWord);
        Collections.shuffle(otherWords);

        for (int i = 0; i < Math.min(3, otherWords.size()); i++) {
            options.add(otherWords.get(i));
        }

        Collections.shuffle(options);

        // Set button texts
        Button[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};
        for (int i = 0; i < buttons.length && i < options.size(); i++) {
            Word word = options.get(i);
            String text = showEnglish ? word.getEnglish() : word.getHebrew();
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

        progressBar.setMax(10);
        progressBar.setProgress(10);

        timer = new CountDownTimer(10000, 100) {
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
                    new Handler().postDelayed(() -> nextQuestion(), 2000);
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
            // Correct answer
            selectedButton.setBackgroundColor(Color.GREEN);
            score += 10;
            updateScore();
        } else {
            // Wrong answer
            selectedButton.setBackgroundColor(Color.RED);
            showCorrectAnswer();
        }

        disableAllButtons();
        new Handler().postDelayed(() -> nextQuestion(), 2000);
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

        // Update general stats
        stats.setTotalScore(stats.getTotalScore() + score);

        if (score >= 80) {
            // Increment practice count for THIS SPECIFIC RANK
            DatabaseService.getInstance().incrementPracticeForRank(user.getId(), currentRank, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void unused) {
                    // Also update global practice count
                    stats.setPracticeCount(stats.getPracticeCount() + 1);

                    // Check if can rank up
                    if (stats.canRankUp()) {
                        stats.setRank(stats.getRank() + 1);
                        stats.setPracticeCount(0);
                        stats.setHasReviewedWords(false);
                    }

                    DatabaseService.getInstance().updateStats(stats, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void unused) {
                            showResultDialog();
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(GameActivity.this, "Failed to save progress", Toast.LENGTH_SHORT).show();
                            showResultDialog();
                        }
                    });
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(GameActivity.this, "Failed to save rank progress", Toast.LENGTH_SHORT).show();
                    showResultDialog();
                }
            });
        } else {
            Toast.makeText(this, "You need at least 80 points to pass", Toast.LENGTH_SHORT).show();
            showResultDialog();
        }
    }

    private void showResultDialog() {
        String message;
        if (score >= 80) {
            message = "Score: " + score + " / " + (gameWords.size() * 10) + "\n\n🎉 Congratulations!";
        } else {
            message = "Score: " + score + " / " + (gameWords.size() * 10) + "\n\n❌ You failed.\nYou need at least 80 points to pass.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Practice Complete!")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
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