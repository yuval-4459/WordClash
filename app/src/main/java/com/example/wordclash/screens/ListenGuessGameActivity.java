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
import com.example.wordclash.models.Stats;
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

    private TextView tvProgress;
    private TextView tvScore;
    private Button btnListen;
    private Button btnOption1;
    private Button btnOption2;
    private Button btnOption3;
    private Button btnOption4;
    private User user;
    private int rank = 1;
    private List<Word> allWords;
    private List<Word> gameWords;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answerSelected = false;

    private TextToSpeech tts;
    private boolean ttsReady = false;
    // Tracks whether words are loaded and waiting for TTS to be ready
    private boolean wordsLoaded = false;
    private boolean ttsInitialized = false;

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

        rank = getIntent().getIntExtra("RANK", 1);

        initializeViews();
        // Start TTS and word loading in parallel
        initializeTTS();
        loadWords();
    }

    private void initializeViews() {
        tvProgress = findViewById(R.id.tvProgress);
        tvScore = findViewById(R.id.tvScore);
        btnListen = findViewById(R.id.btnListen);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        Button btnBack = findViewById(R.id.btnBack);

        btnListen.setOnClickListener(v -> speakWord());
        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3));
        btnOption4.setOnClickListener(v -> checkAnswer(btnOption4));
        btnBack.setOnClickListener(v -> finish());

        // Disable listen button until TTS is ready
        btnListen.setEnabled(false);
        btnListen.setAlpha(0.5f);
    }

    private void initializeTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                String learningLanguage = user.getLearningLanguage();
                if (learningLanguage == null) learningLanguage = "english";

                boolean languageSet = false;

                if (learningLanguage.equals("english")) {
                    int result = tts.setLanguage(Locale.US);
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        languageSet = true;
                    }
                } else {
                    // Hebrew: try multiple locale variations
                    Locale[] hebrewLocales = {
                            new Locale("iw", "IL"),
                            new Locale("iw"),
                            new Locale("he", "IL"),
                            new Locale("he"),
                    };

                    for (Locale locale : hebrewLocales) {
                        int result = tts.setLanguage(locale);
                        if (result != TextToSpeech.LANG_MISSING_DATA
                                && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                            languageSet = true;
                            break;
                        }
                    }

                    // If none of the standard locales worked, try to find any available Hebrew voice
                    if (!languageSet) {
                        try {
                            for (java.util.Locale available : tts.getAvailableLanguages()) {
                                String lang = available.getLanguage();
                                if (lang.equals("iw") || lang.equals("he")) {
                                    int result = tts.setLanguage(available);
                                    if (result != TextToSpeech.LANG_MISSING_DATA
                                            && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                                        languageSet = true;
                                        break;
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                            // getAvailableLanguages() can throw on some devices
                        }
                    }
                }

                final boolean finalLanguageSet = languageSet;
                runOnUiThread(() -> {
                    ttsInitialized = true;
                    if (finalLanguageSet) {
                        ttsReady = true;
                        btnListen.setEnabled(true);
                        btnListen.setAlpha(1.0f);
                        // If words are already loaded, speak the first word now
                        if (wordsLoaded && currentQuestionIndex < gameWords.size()) {
                            new Handler().postDelayed(this::speakWord, 300);
                        }
                    } else {
                        Toast.makeText(this,
                                "Hebrew Text-to-Speech is not installed on this device.\n" +
                                        "Please install it in Settings → Language & Input → Text-to-Speech.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                runOnUiThread(() -> {
                    ttsInitialized = true;
                    Toast.makeText(this, "Text-to-Speech failed to initialize.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(ListenGuessGameActivity.this, "No words available", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                allWords = words;
                selectRandomWords();

                wordsLoaded = true;
                showQuestion();

                // If TTS is already ready by the time words load, speak immediately
                if (ttsReady) {
                    new Handler().postDelayed(ListenGuessGameActivity.this::speakWord, 500);
                }
                // Otherwise speakWord will be called from the TTS init callback above
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

        int TOTAL_QUESTIONS = 10;
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
            if (!ttsInitialized) {
                Toast.makeText(this, "Text-to-Speech is still loading, please wait...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Text-to-Speech is not available for Hebrew on this device.", Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (gameWords == null || currentQuestionIndex >= gameWords.size()) return;

        Word currentWord = gameWords.get(currentQuestionIndex);
        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";

        String textToSpeak = learningLanguage.equals("english")
                ? currentWord.getEnglish()
                : currentWord.getHebrew();

        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void checkAnswer(Button selectedButton) {
        if (answerSelected) return;

        answerSelected = true;

        Word selectedWord = (Word) selectedButton.getTag();
        Word correctWord = gameWords.get(currentQuestionIndex);

        if (selectedWord.getId().equals(correctWord.getId())) {
            selectedButton.setBackgroundColor(Color.GREEN);
            score += 10 * rank;
            updateScore();
        } else {
            selectedButton.setBackgroundColor(Color.RED);
            showCorrectAnswer();
        }

        disableAllButtons();
        new Handler().postDelayed(this::nextQuestion, 1500);
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
        // Auto-speak next word if TTS is ready
        if (ttsReady && currentQuestionIndex < gameWords.size()) {
            new Handler().postDelayed(this::speakWord, 500);
        }
    }

    private void updateProgress() {
        tvProgress.setText("Question " + (currentQuestionIndex + 1) + " / " + gameWords.size());
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
    }

    private void endGame() {
        saveScoreToStats();

        int percentage = (score * 100) / (gameWords.size() * 10 * rank);
        String message = "Your Score: " + score + " / " + (gameWords.size() * 10 * rank) + "\n" +
                "Accuracy: " + percentage + "%\n(Rank " + rank + " bonus applied!)";

        new AlertDialog.Builder(this)
                .setTitle("🎉 Game Complete!")
                .setMessage(message)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    currentQuestionIndex = 0;
                    score = 0;
                    selectRandomWords();
                    showQuestion();
                    if (ttsReady) {
                        new Handler().postDelayed(this::speakWord, 500);
                    }
                })
                .setNegativeButton("Back", (dialog, which) -> finish())
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

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}