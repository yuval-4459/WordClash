package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.Stats;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.LanguageUtils;
import com.example.wordclash.utils.SharedPreferencesUtils;

/**
 * Level screen showing Words and Practice buttons
 */
public class LevelActivity extends AppCompatActivity {

    private TextView tvProgress;
    private TextView tvPracticeHint;
    private Button btnPractice;

    private User user;
    private Stats stats;
    private int currentRank;

    private DatabaseService.RankProgressData rankProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // טעינת המשתמש והגדרת השפה *לפני* super.onCreate כדי שהטקסטים וה-Layout ייטענו נכון
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(getApplicationContext(), user);
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        // החלת כיוון התצוגה (RTL/LTR) בהתאם לשפה
        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        currentRank = getIntent().getIntExtra("RANK", 1);

        if (user == null) {
            Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadStats();
    }

    private void initializeViews() {
        TextView tvLevelTitle = findViewById(R.id.tvLevelTitle);
        tvProgress = findViewById(R.id.tvProgress);
        tvPracticeHint = findViewById(R.id.tvPracticeHint);
        Button btnWords = findViewById(R.id.btnWords);
        btnPractice = findViewById(R.id.btnPractice);

        // דריסה תוכנתית של הטקסטים מה-XML כדי להבטיח תרגום מלא לעברית/אנגלית דינמית
        if (tvLevelTitle != null) {
            tvLevelTitle.setText(getString(R.string.rank, currentRank));
        }
        if (btnWords != null) {
            btnWords.setText(getString(R.string.words));
        }
        if (btnPractice != null) {
            btnPractice.setText(getString(R.string.practice));
        }
        if (tvPracticeHint != null) {
            tvPracticeHint.setText(getString(R.string.review_words_first));
        }

        // עדכון כותרת ה-ActionBar העליונה (אם קיימת)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.rank, currentRank));
        }

        if (btnWords != null) {
            btnWords.setOnClickListener(v -> openWordsList());
        }
        if (btnPractice != null) {
            btnPractice.setOnClickListener(v -> openPractice());
        }
    }

    private void loadStats() {
        DatabaseService.getInstance().getStats(user.getId(), new DatabaseService.DatabaseCallback<>() {
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
                Toast.makeText(LevelActivity.this, getString(R.string.failed_load_stats), Toast.LENGTH_SHORT).show();
                stats = new Stats(user.getId(), 1, 0);
                loadRankProgress();
            }
        });
    }

    private void loadRankProgress() {
        DatabaseService.getInstance().getRankProgressSafe(user.getId(), currentRank,
                new DatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(DatabaseService.RankProgressData data) {
                        rankProgress = data;
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
        if (stats == null) return;
        if (rankProgress == null) return;

        int required = DatabaseService.getRequiredPracticeCount(currentRank);
        if (currentRank == 5) {
            tvProgress.setText(getString(R.string.practice_infinite, rankProgress.practiceCount));
        } else {
            tvProgress.setText(getString(R.string.practice_progress, rankProgress.practiceCount, required));
        }

        if (rankProgress.hasReviewedWords) {
            btnPractice.setEnabled(true);
            btnPractice.setAlpha(1.0f);
            if (tvPracticeHint != null) {
                tvPracticeHint.setVisibility(View.GONE);
            }
        } else {
            btnPractice.setEnabled(false);
            btnPractice.setAlpha(0.5f);
            if (tvPracticeHint != null) {
                tvPracticeHint.setVisibility(View.VISIBLE);
            }
        }
    }

    private void openWordsList() {
        Intent intent = new Intent(LevelActivity.this, WordsListActivity.class);
        intent.putExtra("RANK", currentRank);
        startActivity(intent);
    }

    private void openPractice() {
        if (rankProgress == null || !rankProgress.hasReviewedWords) {
            Toast.makeText(this, getString(R.string.review_words_first), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(LevelActivity.this, GamesActivity.class);
        intent.putExtra("RANK", currentRank);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }
}