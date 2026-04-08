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
public class GamesActivity extends AppCompatActivity {

    private int rank = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        User user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);

        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rank = getIntent().getIntExtra("RANK", 1);

        initializeViews();
    }

    private void initializeViews() {
        Button btnBack = findViewById(R.id.btnBack);
        CardView cardMatchGame = findViewById(R.id.cardMatchGame);
        CardView cardFillGaps = findViewById(R.id.cardFillGaps);
        CardView cardListenGuess = findViewById(R.id.cardListenGuess);
        CardView cardWordBuilder = findViewById(R.id.cardWordBuilder);
        CardView cardTrueFalse = findViewById(R.id.cardTrueFalse);
        CardView cardMemoryCards = findViewById(R.id.cardMemoryCards);
        CardView cardWordle = findViewById(R.id.cardWordle);
        CardView cardSpeedQuiz = findViewById(R.id.cardSpeedQuiz);

        btnBack.setOnClickListener(v -> finish());

        // Match Game
        cardMatchGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, MatchGameActivity.class);
            intent.putExtra("RANK", rank);
            startActivity(intent);
        });

        // Fill the Gaps
        cardFillGaps.setOnClickListener(v -> {
            Intent intent = new Intent(this, FillGapsGameActivity.class);
            intent.putExtra("RANK", rank);
            startActivity(intent);
        });

        // Listen & Guess
        cardListenGuess.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListenGuessGameActivity.class);
            intent.putExtra("RANK", rank);
            startActivity(intent);
        });

        // Word Builder
        cardWordBuilder.setOnClickListener(v -> {
            Intent intent = new Intent(this, WordBuilderGameActivity.class);
            intent.putExtra("RANK", rank);
            startActivity(intent);
        });

        // True/False
        cardTrueFalse.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrueFalseGameActivity.class);
            intent.putExtra("RANK", rank);
            startActivity(intent);
        });

        // Memory Cards
        cardMemoryCards.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemoryCardsGameActivity.class);
            intent.putExtra("RANK", rank);
            startActivity(intent);
        });

        // Wordle
        cardWordle.setOnClickListener(v -> {
            Intent intent = new Intent(this, WordleGameActivity.class);
            intent.putExtra("RANK", rank);
            startActivity(intent);
        });

        // Speed Quiz
        cardSpeedQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(this, SpeedQuizGameActivity.class);
            intent.putExtra("RANK", rank);
            startActivity(intent);
        });
    }
}