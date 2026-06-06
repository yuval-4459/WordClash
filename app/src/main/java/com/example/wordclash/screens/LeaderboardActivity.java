package com.example.wordclash.screens;

import android.os.Bundle;
import android.view.View;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    private LeaderboardAdapter leaderboardAdapter;
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
        RecyclerView rvLeaderboard = findViewById(R.id.rvLeaderboard);
        // btnBack הוסר — system back מספיק
        cardYourRank = findViewById(R.id.cardYourRank);
        tvYourPosition = findViewById(R.id.tvYourPosition);
        tvYourUsername = findViewById(R.id.tvYourUsername);
        tvYourScore = findViewById(R.id.tvYourScore);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        leaderboardAdapter = new LeaderboardAdapter();
        rvLeaderboard.setAdapter(leaderboardAdapter);

        cardYourRank.setVisibility(View.GONE);
    }

    private void loadLeaderboard() {
        allEntries = new ArrayList<>();

        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users == null || users.isEmpty()) {
                    Toast.makeText(LeaderboardActivity.this, "No users found", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, User> userMap = new HashMap<>();
                for (User user : users) {
                    userMap.put(user.getId(), user);
                }

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
            DatabaseService.getInstance().getStats(user.getId(), new DatabaseService.DatabaseCallback<>() {
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
        allEntries.sort((e1, e2) ->
                Integer.compare(e2.totalScore, e1.totalScore));

        for (int i = 0; i < allEntries.size(); i++) {
            allEntries.get(i).position = i + 1;
        }

        List<LeaderboardEntry> top10 = new ArrayList<>();
        for (int i = 0; i < Math.min(10, allEntries.size()); i++) {
            top10.add(allEntries.get(i));
        }

        leaderboardAdapter.setEntries(top10);
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