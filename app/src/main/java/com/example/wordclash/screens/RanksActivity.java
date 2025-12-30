package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.utils.SharedPreferencesUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Ranks activity displaying available game levels and online player count
 */
public class RanksActivity extends AppCompatActivity {

    // UI Components
    private TextView tvOnlinePlayers;
    private CardView cardLevel1, cardLevel2, cardLevel3, cardLevel4, cardLevel5;
    private Button btnLevel1, btnLevel2, btnLevel3, btnLevel4, btnLevel5;
    private TextView tvLevelStatus1, tvLevelStatus2, tvLevelStatus3, tvLevelStatus4, tvLevelStatus5;

    // Data
    private User user;
    private int currentLevel = 3; // Example: user is on level 3
    private int onlinePlayersCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranks);

        initializeUser();
        initializeViews();
        setupLevelButtons();
        updateOnlinePlayersCount();
    }

    /**
     * Retrieves the current user from shared preferences
     */
    private void initializeUser() {
        user = SharedPreferencesUtils.getUser(RanksActivity.this);
        if (user != null) {
            // Get user's current level (you should have this in your User model)
            // currentLevel = user.getCurrentLevel();
            currentLevel = 3; // Placeholder - replace with actual user level
        }
    }

    /**
     * Initializes all view components
     */
    private void initializeViews() {
        tvOnlinePlayers = findViewById(R.id.tvOnlinePlayers);

        // Level cards
        cardLevel1 = findViewById(R.id.cardLevel1);
        cardLevel2 = findViewById(R.id.cardLevel2);
        cardLevel3 = findViewById(R.id.cardLevel3);
        cardLevel4 = findViewById(R.id.cardLevel4);
        cardLevel5 = findViewById(R.id.cardLevel5);

        // Level buttons
        btnLevel1 = findViewById(R.id.btnLevel1);
        btnLevel2 = findViewById(R.id.btnLevel2);
        btnLevel3 = findViewById(R.id.btnLevel3);
        btnLevel4 = findViewById(R.id.btnLevel4);
        btnLevel5 = findViewById(R.id.btnLevel5);

        // Level status texts
        tvLevelStatus1 = findViewById(R.id.tvLevelStatus1);
        tvLevelStatus2 = findViewById(R.id.tvLevelStatus2);
        tvLevelStatus3 = findViewById(R.id.tvLevelStatus3);
        tvLevelStatus4 = findViewById(R.id.tvLevelStatus4);
        tvLevelStatus5 = findViewById(R.id.tvLevelStatus5);
    }

    /**
     * Sets up level buttons with locked/unlocked states
     */
    private void setupLevelButtons() {
        setupLevelButton(1, cardLevel1, btnLevel1, tvLevelStatus1);
        setupLevelButton(2, cardLevel2, btnLevel2, tvLevelStatus2);
        setupLevelButton(3, cardLevel3, btnLevel3, tvLevelStatus3);
        setupLevelButton(4, cardLevel4, btnLevel4, tvLevelStatus4);
        setupLevelButton(5, cardLevel5, btnLevel5, tvLevelStatus5);
    }

    /**
     * Configures individual level button based on user's progress
     */
    private void setupLevelButton(int level, CardView card, Button button, TextView statusText) {
        boolean isUnlocked = level <= currentLevel;

        if (isUnlocked) {
            // Unlocked level
            card.setAlpha(1.0f);
            button.setEnabled(true);
            statusText.setText("Unlocked");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            button.setOnClickListener(v -> startLevel(level));
        } else {
            // Locked level
            card.setAlpha(0.5f);
            button.setEnabled(false);
            statusText.setText("🔒 Locked");
            statusText.setTextColor(getResources().getColor(android.R.color.darker_gray));

            button.setOnClickListener(v ->
                    Toast.makeText(RanksActivity.this,
                            "Complete Level " + (level - 1) + " to unlock!",
                            Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Starts the selected level
     */
    private void startLevel(int level) {
        Toast.makeText(this, "Starting Level " + level + "...", Toast.LENGTH_SHORT).show();
        // TODO: Navigate to game activity with level parameter
        // Intent intent = new Intent(RanksActivity.this, GameActivity.class);
        // intent.putExtra("LEVEL", level);
        // startActivity(intent);
    }

    /**
     * Updates and displays the count of online players
     */
    private void updateOnlinePlayersCount() {
        DatabaseReference onlineRef = FirebaseDatabase.getInstance().getReference("online_users");

        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                onlinePlayersCount = (int) snapshot.getChildrenCount();
                tvOnlinePlayers.setText("🟢 " + onlinePlayersCount + " Players Online");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tvOnlinePlayers.setText("🟢 -- Players Online");
            }
        });
    }

    /**
     * Sets user as online in Firebase
     */
    private void setUserOnline() {
        if (user != null) {
            DatabaseReference onlineRef = FirebaseDatabase.getInstance()
                    .getReference("online_users")
                    .child(user.getId());

            onlineRef.setValue(true);

            // Automatically remove user from online list when they disconnect
            onlineRef.onDisconnect().removeValue();
        }
    }

    /**
     * Sets user as offline in Firebase
     */
    private void setUserOffline() {
        if (user != null) {
            DatabaseReference onlineRef = FirebaseDatabase.getInstance()
                    .getReference("online_users")
                    .child(user.getId());

            onlineRef.removeValue();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUserOnline();
        updateOnlinePlayersCount();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setUserOffline();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setUserOffline();
    }
}