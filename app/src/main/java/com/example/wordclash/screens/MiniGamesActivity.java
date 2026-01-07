package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.utils.LanguageUtils;
import com.example.wordclash.utils.SharedPreferencesUtils;

/**
 * Mini Games selection screen
 * Shows all available vocabulary-based mini games INCLUDING Wordle
 */
public class MiniGamesActivity extends AppCompatActivity {

    private Button btnBack;
    private CardView cardMatchGame, cardFillGaps, cardListenGuess, cardWordBuilder;
    private CardView cardTrueFalse, cardMemoryCards, cardWordle;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_games);

        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        cardMatchGame = findViewById(R.id.cardMatchGame);
        cardFillGaps = findViewById(R.id.cardFillGaps);
        cardListenGuess = findViewById(R.id.cardListenGuess);
        cardWordBuilder = findViewById(R.id.cardWordBuilder);
        cardTrueFalse = findViewById(R.id.cardTrueFalse);
        cardMemoryCards = findViewById(R.id.cardMemoryCards);
        cardWordle = findViewById(R.id.cardWordle);

        btnBack.setOnClickListener(v -> finish());

        // Match Game
        cardMatchGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, MatchGameActivity.class);
            startActivity(intent);
        });

        // Fill the Gaps
        cardFillGaps.setOnClickListener(v -> {
            Intent intent = new Intent(this, FillGapsGameActivity.class);
            startActivity(intent);
        });

        // Listen & Guess
        cardListenGuess.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListenGuessGameActivity.class);
            startActivity(intent);
        });

        // Word Builder
        cardWordBuilder.setOnClickListener(v -> {
            Intent intent = new Intent(this, WordBuilderGameActivity.class);
            startActivity(intent);
        });

        // True/False
        cardTrueFalse.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrueFalseGameActivity.class);
            startActivity(intent);
        });

        // Memory Cards
        cardMemoryCards.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemoryCardsGameActivity.class);
            startActivity(intent);
        });

        // Wordle
        cardWordle.setOnClickListener(v -> {
            Intent intent = new Intent(this, WordleActivity.class);
            startActivity(intent);
        });
    }
}