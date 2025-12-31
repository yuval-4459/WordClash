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
 * FIXED: Now shows progress for THIS specific rank
 */
public class LevelActivity extends AppCompatActivity {

    private TextView tvLevelTitle, tvProgress;
    private Button btnWords, btnPractice, btn_level_Back;

    private User user;
    private Stats stats;
    private int currentRank;

    // Track progress for THIS rank
    private DatabaseService.RankProgressData rankProgress;

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
        btn_level_Back = findViewById(R.id.btnBack);

        tvLevelTitle.setText("Rank " + currentRank);

        btnWords.setOnClickListener(v -> openWordsList());
        btnPractice.setOnClickListener(v -> openPractice());
        btn_level_Back.setOnClickListener(v -> finish());
    }

    private void loadStats() {
        DatabaseService.getInstance().getStats(user.getId(), new DatabaseService.DatabaseCallback<Stats>() {
            @Override
            public void onCompleted(Stats loadedStats) {
                if (loadedStats == null) {
                    stats = new Stats(user.getId(), 1, 0);
                    DatabaseService.getInstance().createStats(stats, null);
                } else {
                    stats = loadedStats;
                }
                loadRankProgress();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(LevelActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                stats = new Stats(user.getId(), 1, 0);
                loadRankProgress();
            }
        });
    }

    private void loadRankProgress() {
        // Load progress for THIS specific rank
        DatabaseService.getInstance().getRankProgress(user.getId(), currentRank, new DatabaseService.DatabaseCallback<DatabaseService.RankProgressData>() {
            @Override
            public void onCompleted(DatabaseService.RankProgressData data) {
                rankProgress = data;
                if (rankProgress == null) {
                    rankProgress = new DatabaseService.RankProgressData();
                }
                updateUI();
            }

            @Override
            public void onFailed(Exception e) {
                rankProgress = new DatabaseService.RankProgressData();
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (stats == null || rankProgress == null) return;

        // Get required practice count for THIS rank
        int required = getRequiredPracticeForRank(currentRank);

        if (currentRank == 5) {
            tvProgress.setText("Practice: " + rankProgress.practiceCount + " (Infinite)");
        } else {
            tvProgress.setText("Practice: " + rankProgress.practiceCount + " / " + required);
        }

        // Enable/disable practice button based on reviewed status for THIS rank
        if (rankProgress.hasReviewedWords) {
            btnPractice.setEnabled(true);
            btnPractice.setAlpha(1.0f);
        } else {
            btnPractice.setEnabled(false);
            btnPractice.setAlpha(0.5f);
        }
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

    private void openWordsList() {
        Intent intent = new Intent(LevelActivity.this, WordsListActivity.class);
        intent.putExtra("RANK", currentRank);
        startActivity(intent);
    }

    private void openPractice() {
        if (rankProgress == null || !rankProgress.hasReviewedWords) {
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
        // Reload when coming back
        if (stats != null) {
            loadRankProgress();
        } else {
            loadStats();
        }
    }
}