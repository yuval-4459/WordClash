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
    private Button btnWords, btnPractice, btn_level_Back;

    private User user;
    private Stats stats;
    private int currentRank;

    // NEW: rank progress is not inside Stats anymore
    private DatabaseService.RankProgressData rankProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        currentRank = getIntent().getIntExtra("RANK", 1);

        // this = המסך הונכחי (הactivity הזה).
        // מעבירים אותו כ־Context כדי ש־SharedPreferences ידע מאיזו מסך
        // לקרוא את המשתמש השמור בזיכרון של הטלפון

        // context - מידע שאומר לאנדרואיד באיזו אפליקציה ואיפה הקוד רץ.
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
                    // Create new stats for user
                    stats = new Stats(user.getId(), 1, 0);
                    DatabaseService.getInstance().createStats(stats, null);
                } else {
                    stats = loadedStats;
                }

                // NEW: load rank progress after stats
                loadRankProgress();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(LevelActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                stats = new Stats(user.getId(), 1, 0);

                // NEW: still try to load rank progress
                loadRankProgress();
            }
        });
    }

    // NEW: load practiceCount + hasReviewedWords from rank_progress
    private void loadRankProgress() {
        DatabaseService.getInstance().getRankProgressSafe(user.getId(), currentRank,
                new DatabaseService.DatabaseCallback<DatabaseService.RankProgressData>() {
                    @Override
                    public void onCompleted(DatabaseService.RankProgressData data) {
                        rankProgress = data;
                        updateUI();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        // Fallback to defaults to avoid crashes
                        rankProgress = new DatabaseService.RankProgressData();
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (stats == null) return;
        if (rankProgress == null) return;

        // Update progress text
        int required = DatabaseService.getRequiredPracticeCount(currentRank);
        if (currentRank == 5) {
            tvProgress.setText("Practice: " + rankProgress.practiceCount + " (Infinite)");
        } else {
            tvProgress.setText("Practice: " + rankProgress.practiceCount + " / " + required);
        }

        // Enable/disable practice button based on whether user reviewed words
        if (rankProgress.hasReviewedWords) {
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
        // Reload stats when coming back from words list
        loadStats();
    }
}
