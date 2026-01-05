package com.example.wordclash.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
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
import java.util.Collections;
import java.util.List;

public class MemoryCardsGameActivity extends AppCompatActivity {

    private TextView tvScore, tvMatches;
    private GridLayout gridCards;
    private Button btnBack, btnNewGame;

    private User user;
    private List<Word> gameWords;
    private List<Button> cardButtons;

    private Button firstCard = null;
    private Button secondCard = null;
    private int matchesFound = 0;
    private int score = 0;
    private final int TOTAL_PAIRS = 6;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_cards_game);

        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        initializeViews();
        loadWords();
    }

    private void initializeViews() {
        tvScore = findViewById(R.id.tvScore);
        tvMatches = findViewById(R.id.tvMatches);
        gridCards = findViewById(R.id.gridCards);
        btnBack = findViewById(R.id.btnBack);
        btnNewGame = findViewById(R.id.btnNewGame);

        btnBack.setOnClickListener(v -> finish());
        btnNewGame.setOnClickListener(v -> loadWords());

        cardButtons = new ArrayList<>();
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(MemoryCardsGameActivity.this, "No words available", Toast.LENGTH_SHORT).show();
                    return;
                }

                Collections.shuffle(words);
                gameWords = new ArrayList<>();
                for (int i = 0; i < Math.min(TOTAL_PAIRS, words.size()); i++) {
                    gameWords.add(words.get(i));
                }

                setupGame();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MemoryCardsGameActivity.this, "Failed to load words", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupGame() {
        matchesFound = 0;
        score = 0;
        firstCard = null;
        secondCard = null;
        isProcessing = false;

        updateScore();

        gridCards.removeAllViews();
        cardButtons.clear();

        List<CardData> cards = new ArrayList<>();
        for (Word word : gameWords) {
            cards.add(new CardData(word, word.getEnglish(), false));
            cards.add(new CardData(word, word.getHebrew(), true));
        }
        Collections.shuffle(cards);

        gridCards.setColumnCount(4);
        gridCards.setRowCount((cards.size() + 3) / 4);

        for (CardData card : cards) {
            Button btn = createCardButton(card);
            cardButtons.add(btn);
            gridCards.addView(btn);
        }
    }

    private Button createCardButton(CardData card) {
        Button button = new Button(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
        button.setText("?");
        button.setTag(card);
        button.setTextSize(20);
        button.setBackgroundColor(Color.parseColor("#2196F3"));
        button.setTextColor(Color.WHITE);
        button.setPadding(16, 32, 16, 32);

        button.setOnClickListener(v -> handleCardClick(button));

        return button;
    }

    private void handleCardClick(Button button) {
        if (isProcessing) return;
        if (button.getAlpha() == 0.3f) return;
        if (button == firstCard) return;

        CardData card = (CardData) button.getTag();
        button.setText(card.text);
        button.setBackgroundColor(Color.parseColor("#FF6F00"));

        if (firstCard == null) {
            firstCard = button;
        } else {
            secondCard = button;
            isProcessing = true;
            checkMatch();
        }
    }

    private void checkMatch() {
        CardData firstCardData = (CardData) firstCard.getTag();
        CardData secondCardData = (CardData) secondCard.getTag();

        if (firstCardData.word.getId().equals(secondCardData.word.getId())) {
            firstCard.setBackgroundColor(Color.GREEN);
            secondCard.setBackgroundColor(Color.GREEN);
            firstCard.setAlpha(0.3f);
            secondCard.setAlpha(0.3f);

            matchesFound++;
            score += 10;
            updateScore();

            firstCard = null;
            secondCard = null;
            isProcessing = false;

            if (matchesFound == TOTAL_PAIRS) {
                new Handler().postDelayed(() -> showWinDialog(), 500);
            }
        } else {
            new Handler().postDelayed(() -> {
                firstCard.setText("?");
                secondCard.setText("?");
                firstCard.setBackgroundColor(Color.parseColor("#2196F3"));
                secondCard.setBackgroundColor(Color.parseColor("#2196F3"));

                firstCard = null;
                secondCard = null;
                isProcessing = false;
            }, 1000);
        }
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
        tvMatches.setText("Matches: " + matchesFound + "/" + TOTAL_PAIRS);
    }

    private void showWinDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Congratulations!")
                .setMessage("You found all pairs!\nScore: " + score + " points")
                .setPositiveButton("Play Again", (dialog, which) -> loadWords())
                .setNegativeButton("Back", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private static class CardData {
        Word word;
        String text;
        boolean isHebrew;

        CardData(Word word, String text, boolean isHebrew) {
            this.word = word;
            this.text = text;
            this.isHebrew = isHebrew;
        }
    }
}