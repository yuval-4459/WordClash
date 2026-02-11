package com.example.wordclash.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordclash.R;
import com.example.wordclash.adapters.LeaderboardAdapter;
import com.example.wordclash.models.Stats;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Leaderboard screen showing top 10 players and current user's rank
 */
public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter leaderboardAdapter;
    private Button btnBack;
    private CardView cardYourRank;
    private TextView tvYourPosition, tvYourUsername, tvYourScore;

    private User currentUser;
    private List<LeaderboardEntry> allEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        currentUser = SharedPreferencesUtils.getUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadLeaderboard();
    }

    private void initializeViews() {
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        btnBack = findViewById(R.id.btnBack);
        cardYourRank = findViewById(R.id.cardYourRank);
        tvYourPosition = findViewById(R.id.tvYourPosition);
        tvYourUsername = findViewById(R.id.tvYourUsername);
        tvYourScore = findViewById(R.id.tvYourScore);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        leaderboardAdapter = new LeaderboardAdapter();
        rvLeaderboard.setAdapter(leaderboardAdapter);

        btnBack.setOnClickListener(v -> finish());

        // Hide user's rank card until data is loaded
        cardYourRank.setVisibility(View.GONE);
    }

    private void loadLeaderboard() {
        allEntries = new ArrayList<>();

        // Get all users
        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users == null || users.isEmpty()) {
                    Toast.makeText(LeaderboardActivity.this, "No users found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a map to store users by ID
                Map<String, User> userMap = new HashMap<>();
                for (User user : users) {
                    userMap.put(user.getId(), user);
                }

                // Load stats for all users
                loadAllStats(userMap);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(LeaderboardActivity.this,
                        "Failed to load users: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllStats(Map<String, User> userMap) {
        final int[] loadedCount = {0};
        final int totalUsers = userMap.size();

        for (User user : userMap.values()) {
            DatabaseService.getInstance().getStats(user.getId(), new DatabaseService.DatabaseCallback<Stats>() {
                @Override
                public void onCompleted(Stats stats) {
                    if (stats != null) {
                        LeaderboardEntry entry = new LeaderboardEntry();
                        entry.userId = user.getId();
                        entry.username = user.getUserName();
                        entry.totalScore = stats.getTotalScore();
                        entry.rank = stats.getRank();
                        allEntries.add(entry);
                    }

                    loadedCount[0]++;
                    if (loadedCount[0] == totalUsers) {
                        displayLeaderboard();
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    loadedCount[0]++;
                    if (loadedCount[0] == totalUsers) {
                        displayLeaderboard();
                    }
                }
            });
        }
    }

    private void displayLeaderboard() {
        // Sort by total score (descending)
        Collections.sort(allEntries, (e1, e2) ->
                Integer.compare(e2.totalScore, e1.totalScore));

        // Assign positions
        for (int i = 0; i < allEntries.size(); i++) {
            allEntries.get(i).position = i + 1;
        }

        // Get top 10 entries
        List<LeaderboardEntry> top10 = new ArrayList<>();
        for (int i = 0; i < Math.min(10, allEntries.size()); i++) {
            top10.add(allEntries.get(i));
        }

        // Display top 10 in RecyclerView
        leaderboardAdapter.setEntries(top10);

        // Find and display current user's position
        displayUserRank();
    }

    private void displayUserRank() {
        LeaderboardEntry userEntry = null;

        for (LeaderboardEntry entry : allEntries) {
            if (entry.userId.equals(currentUser.getId())) {
                userEntry = entry;
                break;
            }
        }

        if (userEntry != null) {
            cardYourRank.setVisibility(View.VISIBLE);
            tvYourPosition.setText("#" + userEntry.position);
            tvYourUsername.setText(userEntry.username);
            tvYourScore.setText(userEntry.totalScore + " pts");
        } else {
            cardYourRank.setVisibility(View.GONE);
        }
    }

    public static class LeaderboardEntry {
        public String userId;
        public String username;
        public int totalScore;
        public int rank;
        public int position;
    }
}