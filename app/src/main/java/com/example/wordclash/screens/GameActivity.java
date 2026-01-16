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
import androidx.core.content.ContextCompat;

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
    private int pointsPerQuestion;
    private int passingScore;
    private CountDownTimer timer;
    private boolean answerSelected = false;

    // Define better colors
    private int colorCorrect;
    private int colorWrong;
    private int colorNeutral;
    private int colorDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        currentRank = getIntent().getIntExtra("RANK", 1);

        if (user == null) {
            Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize colors from resources
        colorCorrect = ContextCompat.getColor(this, R.color.game_correct);
        colorWrong = ContextCompat.getColor(this, R.color.game_wrong);
        colorNeutral = ContextCompat.getColor(this, R.color.info);
        colorDefault = ContextCompat.getColor(this, R.color.info);

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
                    Toast.makeText(GameActivity.this, getString(R.string.error_loading), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                totalQuestions = getQuestionsForRank(currentRank);
                pointsPerQuestion = 100 / totalQuestions;
                passingScore = 80;

                loadWords();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(GameActivity.this, getString(R.string.error_loading), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private int getQuestionsForRank(int rank) {
        switch (rank) {
            case 1: return 10;
            case 2: return 13;
            case 3: return 16;
            case 4: return 20;
            case 5: return 25;
            default: return 10;
        }
    }

    private void loadWords() {
        DatabaseService.getInstance().getWordsByRank(currentRank, new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(GameActivity.this, getString(R.string.no_words_found), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                allWords = words;
                selectRandomWords();
                showQuestion();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(GameActivity.this, getString(R.string.error_loading), Toast.LENGTH_SHORT).show();
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
            buttons[i].setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.info));
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
            selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.game_correct));
            score += pointsPerQuestion;
            updateScore();
        } else {
            selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.game_wrong));
            showCorrectAnswer();
        }

        new Handler().postDelayed(() -> nextQuestion(), 2000);
    }

    private void showCorrectAnswer() {
        Word correctWord = gameWords.get(currentQuestionIndex);
        Button[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};

        for (Button button : buttons) {
            Word word = (Word) button.getTag();
            if (word != null && word.getId().equals(correctWord.getId())) {
                button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.game_correct));
                break;
            }
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        showQuestion();
    }

    private void updateProgress() {
        tvProgress.setText(getString(R.string.question_progress,
                currentQuestionIndex + 1, gameWords.size()));
    }

    private void updateScore() {
        tvScore.setText(getString(R.string.score, score));
    }

    private void endGame() {
        if (timer != null) {
            timer.cancel();
        }

        stats.setTotalScore(stats.getTotalScore() + score);

        if (score >= passingScore) {
            DatabaseService.getInstance().incrementPracticeForRank(user.getId(), currentRank, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void unused) {
                    checkAndUpdateRank();
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(GameActivity.this, getString(R.string.error_saving), Toast.LENGTH_SHORT).show();
                    showResultDialog();
                }
            });
        } else {
            DatabaseService.getInstance().updateStats(stats, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void unused) {
                    showResultDialog();
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(GameActivity.this, getString(R.string.error_saving), Toast.LENGTH_SHORT).show();
                    showResultDialog();
                }
            });
        }
    }

    private void checkAndUpdateRank() {
        DatabaseService.getInstance().getRankProgress(user.getId(), currentRank, new DatabaseService.DatabaseCallback<DatabaseService.RankProgressData>() {
            @Override
            public void onCompleted(DatabaseService.RankProgressData progress) {
                if (progress != null) {
                    int required = getRequiredPracticeForRank(currentRank);
                    if (progress.practiceCount >= required && currentRank < 5) {
                        stats.setRank(currentRank + 1);
                    }
                }

                DatabaseService.getInstance().updateStats(stats, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void unused) {
                        showResultDialog();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(GameActivity.this, getString(R.string.error_saving), Toast.LENGTH_SHORT).show();
                        showResultDialog();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                DatabaseService.getInstance().updateStats(stats, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void unused) {
                        showResultDialog();
                    }

                    @Override
                    public void onFailed(Exception ex) {
                        showResultDialog();
                    }
                });
            }
        });
    }

    private int getRequiredPracticeForRank(int rank) {
        switch (rank) {
            case 1: return 15;
            case 2: return 25;
            case 3: return 40;
            case 4: return 60;
            case 5: return Integer.MAX_VALUE;
            default: return 15;
        }
    }

    private void showResultDialog() {
        String message;
        if (score >= passingScore) {
            message = getString(R.string.your_score, score, 100)
                    + "\n\n" + getString(R.string.congratulations);
        } else {
            message = getString(R.string.your_score, score, 100)
                    + "\n\n" + getString(R.string.you_failed);
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.practice_complete))
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> finish())
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