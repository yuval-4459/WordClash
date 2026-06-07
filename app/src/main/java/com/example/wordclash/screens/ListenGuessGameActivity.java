package com.example.wordclash.screens;

import android.graphics.Color;
import android.graphics.Typeface;
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

    private static final int COLOR_DEFAULT = Color.parseColor("#2196F3");
    private static final int COLOR_CORRECT = Color.parseColor("#43A047");
    private static final int COLOR_WRONG = Color.parseColor("#E53935");
    private TextView tvProgress, tvScore;
    private Button btnListen;
    private Button btnOption1, btnOption2, btnOption3, btnOption4;
    private User user;
    private int rank = 1;
    private List<Word> allWords;
    private List<Word> gameWords;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answerSelected = false;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean wordsLoaded = false;
    private boolean ttsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) LanguageUtils.applyLanguageSettings(this, user);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_guess_game);

        if (user != null) LanguageUtils.setLayoutDirection(this, user);

        rank = getIntent().getIntExtra("RANK", 1);
        initializeViews();
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

        // רץ על המערך מההתחלה ועד הסוף (לולאת for each)
        for (Button b : new Button[]{btnOption1, btnOption2, btnOption3, btnOption4}) {
            b.setTextSize(20f);
            b.setTypeface(null, Typeface.BOLD);
            b.setPadding(16, 24, 16, 24);
        }

        btnListen.setOnClickListener(v -> speakWord());
        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3));
        btnOption4.setOnClickListener(v -> checkAnswer(btnOption4));

        btnListen.setEnabled(false);
        btnListen.setAlpha(0.5f);
    }

    // יוצרת רכיב שממיר טקסט לדיבור (מנוע הדיבור של גוגל בטלפון).
    // הסטטוס זה מאזין.
    // אני צריך לבדוק האם זה עלה בהצלחה בכלל. אם כן, המשך בהתאם לשפת הלימוד.
    private void initializeTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                String learningLanguage = user.getLearningLanguage();
                if (learningLanguage == null) learningLanguage = "english";
                // האם הצלחנו להגדיר את השפה או לא
                boolean languageSet = false;

                if (learningLanguage.equals("english")) {
                    int result = tts.setLanguage(Locale.US);
                    // השפה נתמכת, אך קבצי הקול חסרים במכשיר ויש להורידם LANG_MISSING_DATA
                    // LANG_NOT_SUPPORTED - השפה אינה נתמכת כלל על ידי מנוע ה(tts) במכשיר
                    if (result != TextToSpeech.LANG_MISSING_DATA
                            && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        languageSet = true;
                    }
                } else {
                    Locale[] hebrewLocales = {
                            new Locale("iw", "IL"), new Locale("iw"),
                            new Locale("he", "IL"), new Locale("he")
                    };
                    // רץ על המערך מההתחלה ועד הסוף (לולאת for each)
                    for (Locale locale : hebrewLocales) {
                        int result = tts.setLanguage(locale);
                        if (result != TextToSpeech.LANG_MISSING_DATA
                                && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                            languageSet = true;
                            break;
                        }
                    }
                    if (!languageSet) {
                        try {
                            for (Locale available : tts.getAvailableLanguages()) {
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
                        if (wordsLoaded && currentQuestionIndex < gameWords.size()) {
                            new Handler().postDelayed(this::speakWord, 300);
                        }
                    } else {
                        Toast.makeText(this,
                                getString(R.string.tts_hebrew_missing),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                runOnUiThread(() -> {
                    ttsInitialized = true;
                    Toast.makeText(this, getString(R.string.tts_failed), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(ListenGuessGameActivity.this,
                            getString(R.string.no_words_available), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                allWords = words;
                selectRandomWords();
                wordsLoaded = true;
                showQuestion();
                if (ttsReady) {
                    new Handler().postDelayed(ListenGuessGameActivity.this::speakWord, 500);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ListenGuessGameActivity.this,
                        getString(R.string.failed_load_words, e.getMessage()),
                        Toast.LENGTH_SHORT).show();
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
        for (int i = 0; i < Math.min(3, otherWords.size()); i++) options.add(otherWords.get(i));
        Collections.shuffle(options);

        Button[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};
        for (int i = 0; i < buttons.length && i < options.size(); i++) {
            Word word = options.get(i);
            String text = showHebrew ? word.getHebrew() : word.getEnglish();
            buttons[i].setText(text);
            buttons[i].setTag(word);
            buttons[i].setBackgroundColor(COLOR_DEFAULT);
            buttons[i].setTextColor(Color.WHITE);
            buttons[i].setEnabled(true);
            buttons[i].setAlpha(1.0f);
        }
    }

    private void speakWord() {
        if (!ttsReady) {
            Toast.makeText(this,
                    ttsInitialized
                            // אם true
                            ? getString(R.string.tts_not_available)
                            //אחרת false
                            : getString(R.string.tts_loading),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (gameWords == null || currentQuestionIndex >= gameWords.size()) return;

        Word currentWord = gameWords.get(currentQuestionIndex);
        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";
        String textToSpeak = learningLanguage.equals("english")
                ? currentWord.getEnglish() : currentWord.getHebrew();
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void checkAnswer(Button selectedButton) {
        if (answerSelected) return;
        answerSelected = true;

        Word selectedWord = (Word) selectedButton.getTag();
        Word correctWord = gameWords.get(currentQuestionIndex);

        disableAllButtons();

        if (selectedWord.getId().equals(correctWord.getId())) {
            selectedButton.setBackgroundColor(COLOR_CORRECT);
            score += 10 * rank;
            updateScore();
        } else {
            selectedButton.setBackgroundColor(COLOR_WRONG);
            highlightCorrect(correctWord);
        }

        new Handler().postDelayed(this::nextQuestion, 1800);
    }

    private void highlightCorrect(Word correctWord) {
        Button[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};
        for (Button button : buttons) {
            Word word = (Word) button.getTag();
            if (word != null && word.getId().equals(correctWord.getId())) {
                button.setBackgroundColor(COLOR_CORRECT);
                break;
            }
        }
    }

    private void disableAllButtons() {
        for (Button b : new Button[]{btnOption1, btnOption2, btnOption3, btnOption4}) {
            b.setEnabled(false);
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        showQuestion();
        if (ttsReady && currentQuestionIndex < gameWords.size()) {
            new Handler().postDelayed(this::speakWord, 500);
        }
    }

    private void updateProgress() {
        tvProgress.setText(getString(R.string.question_progress,
                currentQuestionIndex + 1, gameWords.size()));
    }

    private void updateScore() {
        tvScore.setText(getString(R.string.score, score));
    }

    private void endGame() {
        saveScoreToStats();
        int percentage = (score * 100) / (gameWords.size() * 10 * rank);
        String message = getString(R.string.your_score, score, gameWords.size() * 10 * rank)
                + "\n" + getString(R.string.game_complete_msg, score, gameWords.size() * 10 * rank, rank);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.game_complete_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.game_play_again), (d, w) -> {
                    currentQuestionIndex = 0;
                    score = 0;
                    selectRandomWords();
                    showQuestion();
                    if (ttsReady) new Handler().postDelayed(this::speakWord, 500);
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

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}