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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Leaderboard screen showing top 10 players and current user's rank
 */
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

        // שליפת אובייקט המשתמש הנוכחי מתוך ה-SharedPreferences המקומי כדי שנוכל בהמשך להשוות אותו לשאר המשתמשים ולדעת מה המיקום שלו בטבלה.
        currentUser = SharedPreferencesUtils.getUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadLeaderboard();
    }

    // קישור רכיבי הגרפיקה מה-XML ל-Java, אתחול ה-RecyclerView עם מנהל פריסה LinearLayoutManager, וחיבורו לאדפטר LeaderboardAdapter.
    private void initializeViews() {
        RecyclerView rvLeaderboard = findViewById(R.id.rvLeaderboard);
        Button btnBack = findViewById(R.id.btnBack);
        cardYourRank = findViewById(R.id.cardYourRank);
        tvYourPosition = findViewById(R.id.tvYourPosition);
        tvYourUsername = findViewById(R.id.tvYourUsername);
        tvYourScore = findViewById(R.id.tvYourScore);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        leaderboardAdapter = new LeaderboardAdapter();
        rvLeaderboard.setAdapter(leaderboardAdapter);

        btnBack.setOnClickListener(v -> finish());

        // hide the user's rank card until data is loaded
        cardYourRank.setVisibility(View.GONE);
    }

    // שליפת רשימת כל המשתמשים מה-Firebase באופן אסינכרוני.
    // לאחר השליפה, המערכת מייצרת Map שממפה משתמשים לפי המזהה הייחודי שלהם (ID) כדי לייעל את חיבור הנתונים בשלב הבא.
    private void loadLeaderboard() {
        allEntries = new ArrayList<>();

        // get all users
        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users == null || users.isEmpty()) {
                    Toast.makeText(LeaderboardActivity.this, "No users found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // create a map to store users by ID
                Map<String, User> userMap = new HashMap<>();
                for (User user : users) {
                    userMap.put(user.getId(), user);
                }

                // load the stats for all the users
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

    // ריצה בלולאה על כל המשתמשים ושליפת הסטטיסטיקות שלהם מה-Firebase.
    // הפונקציה משתמשת במערך מונים סופי (loadedCount) כדי לעקוב אחרי הקריאות האסינכרוניות ולדעת מתי כל הנתונים הגיעו כדי לעבור להצגת הטבלה.
    private void loadAllStats(Map<String, User> userMap) {
        final int[] loadedCount = {0};
        final int totalUsers = userMap.size();

        // load stats for each user
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

    // מיון כל רשימת השחקנים בסדר יורד לפי הניקוד שלהם באמצעות פונקציית למדא והשורה Integer.compare.
    // לאחר מכן, הפונקציה מחלצת את 10 המקומות הראשונים בלבד ומעבירה אותם לאדפטר לרענון המסך.
    private void displayLeaderboard() {
        // sort by total score (descending)
        allEntries.sort((e1, e2) ->
                Integer.compare(e2.totalScore, e1.totalScore));

        // assign positions
        for (int i = 0; i < allEntries.size(); i++) {
            allEntries.get(i).position = i + 1;
        }

        // get top 10 entries
        List<LeaderboardEntry> top10 = new ArrayList<>();
        for (int i = 0; i < Math.min(10, allEntries.size()); i++) {
            top10.add(allEntries.get(i));
        }

        // display top 10 in RecyclerView
        leaderboardAdapter.setEntries(top10);

        // find and display the user's current position
        displayUserRank();
    }

    // ריצה על כל השחקנים בטבלה כדי למצוא את המיקום של המשתמש הנוכחי לפי ה-ID שלו,
    // והצגת כרטיסיית הציון האישית שלו בתחתית המסך באמצעות שינוי הראות ל-VISIBLE.
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

    // מחלקה פנימית סטטית (Static Inner Class) המשמשת כמודל עזר זמני המאחד את פרטי המשתמש מהרשימה יחד עם הניקוד והמיקום שלו,
    // לצורך הצגה נוחה באדפטר של הטבלה.
    public static class LeaderboardEntry {
        public String userId;
        public String username;
        public int totalScore;
        public int rank;
        public int position;
    }
}