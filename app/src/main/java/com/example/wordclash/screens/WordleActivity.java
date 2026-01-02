package com.example.wordclash.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
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
import java.util.List;
import java.util.Random;

/**
 * Wordle game - Guess a 5-letter word in the learning language
 * If learning English -> Hebrew Wordle
 * If learning Hebrew -> English Wordle
 */
public class WordleActivity extends AppCompatActivity {

    private GridLayout gridGuesses;
    private Button btnSubmit, btnNewGame, btnBack;
    private TextView tvInstructions, tvTitle;
    private EditText hiddenInput;

    private User user;
    private String targetWord;
    private int currentAttempt = 0;
    private int currentLetterIndex = 0;
    private final int MAX_ATTEMPTS = 6;
    private final int WORD_LENGTH = 5;
    private List<Word> allFiveLetterWords;
    private String currentGuess = "";
    private boolean isHebrewWordle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordle);

        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        // Determine Wordle language based on learning preference
        String learningLanguage = user != null ? user.getLearningLanguage() : "english";
        if (learningLanguage == null) learningLanguage = "english";
        isHebrewWordle = learningLanguage.equals("hebrew"); // Learning Hebrew = Hebrew Wordle

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        gridGuesses = findViewById(R.id.gridGuesses);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnNewGame = findViewById(R.id.btnNewGame);
        btnBack = findViewById(R.id.btnBack);
        tvInstructions = findViewById(R.id.tvInstructions);
        tvTitle = findViewById(R.id.tvTitle);
        hiddenInput = findViewById(R.id.hiddenInput);

        tvTitle.setText(getString(R.string.wordle_title));
        tvInstructions.setText(getString(R.string.wordle_instructions));
        btnSubmit.setText(getString(R.string.submit_guess));
        btnNewGame.setText(getString(R.string.new_game));
        btnBack.setText(getString(R.string.back));

        btnSubmit.setOnClickListener(v -> submitGuess());
        btnNewGame.setOnClickListener(v -> startNewGame());
        btnBack.setOnClickListener(v -> finish());

        setupGrid();
        setupHiddenInput();
    }

    private void setupHiddenInput() {
        hiddenInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        hiddenInput.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        hiddenInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);

        hiddenInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();

                if (text.length() > 0) {
                    for (int i = 0; i < text.length(); i++) {
                        char letter = text.charAt(i);
                        if (isValidLetter(letter)) {
                            handleLetterInput(Character.toUpperCase(letter));
                        }
                    }

                    hiddenInput.removeTextChangedListener(this);
                    hiddenInput.setText("");
                    hiddenInput.addTextChangedListener(this);
                }
            }
        });

        hiddenInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    handleBackspace();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    submitGuess();
                    return true;
                }
            }
            return false;
        });

        hiddenInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                submitGuess();
                return true;
            }
            return false;
        });

        gridGuesses.setOnClickListener(v -> {
            hiddenInput.requestFocus();
            showKeyboard();
        });
    }

    private boolean isValidLetter(char c) {
        if (isHebrewWordle) {
            // Hebrew letters
            return (c >= 'א' && c <= 'ת') || (c >= 'ا' && c <= 'ي');
        } else {
            // English letters
            return Character.isLetter(c) && ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'));
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(hiddenInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(hiddenInput.getWindowToken(), 0);
    }

    private void handleLetterInput(char letter) {
        if (currentLetterIndex < WORD_LENGTH) {
            int cellIndex = currentAttempt * WORD_LENGTH + currentLetterIndex;
            TextView tv = (TextView) gridGuesses.getChildAt(cellIndex);
            tv.setText(String.valueOf(letter));
            currentGuess += letter;
            currentLetterIndex++;
        }
    }

    private void handleBackspace() {
        if (currentLetterIndex > 0) {
            currentLetterIndex--;
            int cellIndex = currentAttempt * WORD_LENGTH + currentLetterIndex;
            TextView tv = (TextView) gridGuesses.getChildAt(cellIndex);
            tv.setText("");
            currentGuess = currentGuess.substring(0, currentGuess.length() - 1);
        }
    }

    private void setupGrid() {
        gridGuesses.removeAllViews();
        gridGuesses.setColumnCount(WORD_LENGTH);
        gridGuesses.setRowCount(MAX_ATTEMPTS);

        // Set direction based on language
        int direction = isHebrewWordle ? android.view.View.LAYOUT_DIRECTION_RTL : android.view.View.LAYOUT_DIRECTION_LTR;
        gridGuesses.setLayoutDirection(direction);

        int size = (int) (getResources().getDisplayMetrics().widthPixels * 0.15);

        for (int i = 0; i < MAX_ATTEMPTS * WORD_LENGTH; i++) {
            TextView tv = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(6, 6, 6, 6);
            tv.setLayoutParams(params);

            tv.setGravity(android.view.Gravity.CENTER);
            tv.setTextSize(32);
            tv.setTextColor(Color.BLACK);
            tv.setBackgroundColor(Color.WHITE);
            tv.setTextDirection(android.view.View.TEXT_DIRECTION_LOCALE);
            tv.setLayoutDirection(direction);

            tv.setBackground(getDrawable(android.R.drawable.edit_text));

            gridGuesses.addView(tv);
        }
    }

    private void loadWords() {
        allFiveLetterWords = new ArrayList<>();

        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words != null && !words.isEmpty()) {
                    // Filter 5-letter words in the target language
                    for (Word word : words) {
                        String targetText = isHebrewWordle ? word.getHebrew() : word.getEnglish();
                        if (targetText != null && targetText.trim().length() == 5) {
                            allFiveLetterWords.add(word);
                        }
                    }

                    if (allFiveLetterWords.isEmpty()) {
                        Toast.makeText(WordleActivity.this,
                                "No 5-letter words available",
                                Toast.LENGTH_LONG).show();
                    } else {
                        startNewGame();
                    }
                } else {
                    Toast.makeText(WordleActivity.this,
                            "No words available",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(WordleActivity.this,
                        getString(R.string.error_loading),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startNewGame() {
        if (allFiveLetterWords == null || allFiveLetterWords.isEmpty()) {
            Toast.makeText(this, "No words available", Toast.LENGTH_SHORT).show();
            return;
        }

        Random random = new Random();
        Word selectedWord = allFiveLetterWords.get(random.nextInt(allFiveLetterWords.size()));
        targetWord = (isHebrewWordle ? selectedWord.getHebrew() : selectedWord.getEnglish()).toUpperCase().trim();

        currentAttempt = 0;
        currentLetterIndex = 0;
        currentGuess = "";
        btnSubmit.setEnabled(true);
        setupGrid();

        hiddenInput.requestFocus();
        hiddenInput.postDelayed(() -> showKeyboard(), 200);
    }

    private void submitGuess() {
        if (currentGuess.length() != WORD_LENGTH) {
            Toast.makeText(this, "Please enter a " + WORD_LENGTH + "-letter word",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        displayGuess(currentGuess);

        if (currentGuess.equals(targetWord)) {
            gameWon();
        } else {
            currentAttempt++;
            currentLetterIndex = 0;
            currentGuess = "";
            if (currentAttempt >= MAX_ATTEMPTS) {
                gameLost();
            } else {
                hiddenInput.requestFocus();
                hiddenInput.postDelayed(() -> showKeyboard(), 100);
            }
        }
    }

    private void displayGuess(String guess) {
        int startIndex = currentAttempt * WORD_LENGTH;

        char[] targetChars = targetWord.toCharArray();
        boolean[] used = new boolean[WORD_LENGTH];

        // First pass: Mark exact matches (GREEN)
        for (int i = 0; i < WORD_LENGTH; i++) {
            TextView tv = (TextView) gridGuesses.getChildAt(startIndex + i);
            char letter = guess.charAt(i);

            if (letter == targetChars[i]) {
                tv.setBackgroundColor(Color.GREEN);
                tv.setTextColor(Color.WHITE);
                used[i] = true;
            }
        }

        // Second pass: Mark wrong position matches (YELLOW) and misses (GRAY)
        for (int i = 0; i < WORD_LENGTH; i++) {
            TextView tv = (TextView) gridGuesses.getChildAt(startIndex + i);
            char letter = guess.charAt(i);

            if (letter == targetChars[i]) {
                continue;
            }

            boolean foundInTarget = false;
            for (int j = 0; j < WORD_LENGTH; j++) {
                if (!used[j] && letter == targetChars[j]) {
                    tv.setBackgroundColor(Color.YELLOW);
                    tv.setTextColor(Color.BLACK);
                    used[j] = true;
                    foundInTarget = true;
                    break;
                }
            }

            if (!foundInTarget) {
                tv.setBackgroundColor(Color.DKGRAY);
                tv.setTextColor(Color.WHITE);
            }
        }
    }

    private void gameWon() {
        btnSubmit.setEnabled(false);
        hideKeyboard();

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.congratulations))
                .setMessage(getString(R.string.word_was, targetWord) +
                        "\n" + getString(R.string.attempts, currentAttempt + 1, MAX_ATTEMPTS))
                .setPositiveButton(getString(R.string.new_game), (dialog, which) -> startNewGame())
                .setNegativeButton(getString(R.string.back), (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void gameLost() {
        btnSubmit.setEnabled(false);
        hideKeyboard();

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.game_over))
                .setMessage(getString(R.string.word_was, targetWord))
                .setPositiveButton(getString(R.string.new_game), (dialog, which) -> startNewGame())
                .setNegativeButton(getString(R.string.back), (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}