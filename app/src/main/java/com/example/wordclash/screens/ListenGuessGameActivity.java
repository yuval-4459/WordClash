package com.example.wordclash.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ListenGuessGameActivity extends AppCompatActivity {

    private final int TOTAL_QUESTIONS = 10;
    private TextView tvInstruction, tvProgress, tvScore;
    private Button btnListen, btnOption1, btnOption2, btnOption3, btnOption4, btnBack;
    private User user;
    private List<Word> allWords;
    private List<Word> gameWords;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answerSelected = false;

    private TextToSpeech tts;
    private boolean ttsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_guess_game);

        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        initializeViews();
        initializeTTS();
        loadWords();
    }

    private void initializeViews() {
        tvInstruction = findViewById(R.id.tvInstruction);
        tvProgress = findViewById(R.id.tvProgress);
        tvScore = findViewById(R.id.tvScore);
        btnListen = findViewById(R.id.btnListen);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnBack = findViewById(R.id.btnBack);

        btnListen.setOnClickListener(v -> speakWord());
        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3));
        btnOption4.setOnClickListener(v -> checkAnswer(btnOption4));
        btnBack.setOnClickListener(v -> finish());
    }

    private void initializeTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                String learningLanguage = user.getLearningLanguage();
                if (learningLanguage == null) learningLanguage = "english";

                if (learningLanguage.equals("english")) {
                    int result = tts.setLanguage(Locale.US);
                    ttsReady = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED);
                } else {
                    int result = tts.setLanguage(new Locale("he", "IL"));
                    ttsReady = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED);
                }

                if (!ttsReady) {
                    Toast.makeText(this, "Text-to-Speech not available for this language", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(ListenGuessGameActivity.this, "No words available", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                allWords = words;
                selectRandomWords();
                showQuestion();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ListenGuessGameActivity.this, "Failed to load words", Toast.LENGTH_SHORT).show();
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

        boolean isLearningEnglish = learningLanguage.equals("english");

        setupOptions(currentWord, !isLearningEnglish);
        updateProgress();

        btnListen.postDelayed(() -> speakWord(), 500);
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

    private void speakWord() {
        if (!ttsReady) {
            Toast.makeText(this, "Text-to-Speech not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        Word currentWord = gameWords.get(currentQuestionIndex);
        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";

        String textToSpeak = learningLanguage.equals("english") ? currentWord.getEnglish() : currentWord.getHebrew();
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void checkAnswer(Button selectedButton) {
        if (answerSelected) return;

        answerSelected = true;

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

        disableAllButtons();  // ← THIS IS THE PROBLEM!
        new Handler().postDelayed(() -> nextQuestion(), 1500);
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
        int percentage = (score * 100) / (gameWords.size() * 10);
        String message = "Your Score: " + score + " / " + (gameWords.size() * 10) + "\n" +
                "Accuracy: " + percentage + "%";

        new AlertDialog.Builder(this)
                .setTitle("🎉 Game Complete!")
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
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}