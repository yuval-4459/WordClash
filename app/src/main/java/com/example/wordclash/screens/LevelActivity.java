package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.Stats;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

/**
 * Level screen showing Words and Practice buttons
 */
public class LevelActivity extends AppCompatActivity {

    private TextView tvLevelTitle, tvProgress;
    private Button btnWords, btnPractice, btnBack;

    private User user;
    private Stats stats;
    private int currentRank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        currentRank = getIntent().getIntExtra("RANK", 1);
        user = SharedPreferencesUtils.getUser(this);

        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadStats();
    }

    private void initializeViews() {
        tvLevelTitle = findViewById(R.id.tvLevelTitle);
        tvProgress = findViewById(R.id.tvProgress);
        btnWords = findViewById(R.id.btnWords);
        btnPractice = findViewById(R.id.btnPractice);
        btnBack = findViewById(R.id.btnBack);

        tvLevelTitle.setText("Rank " + currentRank);

        btnWords.setOnClickListener(v -> openWordsList());
        btnPractice.setOnClickListener(v -> openPractice());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadStats() {
        DatabaseService.getInstance().getStats(user.getId(), new DatabaseService.DatabaseCallback<Stats>() {
            @Override
            public void onCompleted(Stats loadedStats) {
                if (loadedStats == null) {
                    // Create new stats for user
                    stats = new Stats(user.getId(), 1, 0, 0, false);
                    DatabaseService.getInstance().createStats(stats, null);
                } else {
                    stats = loadedStats;
                }
                updateUI();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(LevelActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                stats = new Stats(user.getId(), 1, 0, 0, false);
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (stats == null) return;

        // Update progress text
        int required = stats.getRequiredPracticeCount();
        if (currentRank == 5) {
            tvProgress.setText("Practice: " + stats.getPracticeCount() + " (Infinite)");
        } else {
            tvProgress.setText("Practice: " + stats.getPracticeCount() + " / " + required);
        }

        // Enable/disable practice button based on whether user reviewed words
        if (stats.isHasReviewedWords()) {
            btnPractice.setEnabled(true);
            btnPractice.setAlpha(1.0f);
        } else {
            btnPractice.setEnabled(false);
            btnPractice.setAlpha(0.5f);
        }
    }

    private void openWordsList() {
        Intent intent = new Intent(LevelActivity.this, WordsListActivity.class);
        intent.putExtra("RANK", currentRank);
        startActivity(intent);
    }

    private void openPractice() {
        if (!stats.isHasReviewedWords()) {
            Toast.makeText(this, "Please review the words first!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(LevelActivity.this, GameActivity.class);
        intent.putExtra("RANK", currentRank);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload stats when coming back from words list
        loadStats();
    }
}