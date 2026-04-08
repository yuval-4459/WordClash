package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.wordclash.R;
import com.example.wordclash.models.Stats;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RanksActivity extends AppCompatActivity {

    // Hardcoded manager ID extracted to a constant
    private static final String MANAGER_ID = "-OfhuEaP25z-o6NIsC5K";
    private TextView tvOnlinePlayers;
    private CardView cardLevel1, cardLevel2, cardLevel3, cardLevel4, cardLevel5;
    private Button btnLevel1;
    private Button btnLevel2;
    private Button btnLevel3;
    private Button btnLevel4;
    private Button btnLevel5;
    private TextView tvLevelStatus1, tvLevelStatus2, tvLevelStatus3, tvLevelStatus4, tvLevelStatus5;
    private User user;
    private int currentLevel = 1;
    private int onlinePlayersCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranks);

        initializeUser();
        initializeViews();
        loadUserStats();
        updateOnlinePlayersCount();

        Button btn_Ranks_back = findViewById(R.id.btnBack);
        btn_Ranks_back.setOnClickListener(v -> finish());
    }

    private void initializeUser() {
        user = SharedPreferencesUtils.getUser(RanksActivity.this);
        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        tvOnlinePlayers = findViewById(R.id.tvOnlinePlayers);

        cardLevel1 = findViewById(R.id.cardLevel1);
        cardLevel2 = findViewById(R.id.cardLevel2);
        cardLevel3 = findViewById(R.id.cardLevel3);
        cardLevel4 = findViewById(R.id.cardLevel4);
        cardLevel5 = findViewById(R.id.cardLevel5);

        btnLevel1 = findViewById(R.id.btnLevel1);
        btnLevel2 = findViewById(R.id.btnLevel2);
        btnLevel3 = findViewById(R.id.btnLevel3);
        btnLevel4 = findViewById(R.id.btnLevel4);
        btnLevel5 = findViewById(R.id.btnLevel5);

        tvLevelStatus1 = findViewById(R.id.tvLevelStatus1);
        tvLevelStatus2 = findViewById(R.id.tvLevelStatus2);
        tvLevelStatus3 = findViewById(R.id.tvLevelStatus3);
        tvLevelStatus4 = findViewById(R.id.tvLevelStatus4);
        tvLevelStatus5 = findViewById(R.id.tvLevelStatus5);
    }

    private void loadUserStats() {
        if (user == null) return;

        DatabaseService.getInstance().getStats(user.getId(), new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Stats stats) {
                if (stats != null) {
                    currentLevel = stats.getRank();
                } else {
                    Stats newStats = new Stats(user.getId(), 1, 0);
                    DatabaseService.getInstance().createStats(newStats, null);
                    currentLevel = 1;
                }
                setupLevelButtons();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(RanksActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                setupLevelButtons();
            }
        });
    }

    private void setupLevelButtons() {
        setupLevelButton(1, cardLevel1, btnLevel1, tvLevelStatus1);
        setupLevelButton(2, cardLevel2, btnLevel2, tvLevelStatus2);
        setupLevelButton(3, cardLevel3, btnLevel3, tvLevelStatus3);
        setupLevelButton(4, cardLevel4, btnLevel4, tvLevelStatus4);
        setupLevelButton(5, cardLevel5, btnLevel5, tvLevelStatus5);
    }

    private void setupLevelButton(int level, CardView card, Button button, TextView statusText) {
        boolean isUnlocked = level <= currentLevel;

        if (isUnlocked) {
            card.setAlpha(1.0f);
            button.setEnabled(true);
            // Use own color resource instead of deprecated AOSP holo_blue_light
            button.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
            button.setText("Play");
            statusText.setText(getString(R.string.unlocked));
            statusText.setTextColor(getResources().getColor(R.color.success));
            button.setOnClickListener(v -> startLevel(level));
        } else {
            card.setAlpha(0.5f);
            button.setEnabled(false);
            // Use own color resource instead of deprecated AOSP darker_gray
            button.setBackgroundTintList(getResources().getColorStateList(R.color.level_locked));
            button.setText("🔒");
            statusText.setText(getString(R.string.locked));
            statusText.setTextColor(getResources().getColor(R.color.level_locked));
            // Don't set a click listener on a disabled button — it won't fire reliably
        }
    }

    private void startLevel(int level) {
        Intent intent = new Intent(RanksActivity.this, LevelActivity.class);
        intent.putExtra("RANK", level);
        startActivity(intent);
    }

    private void updateOnlinePlayersCount() {
        DatabaseReference onlineRef = FirebaseDatabase.getInstance().getReference("online_users");

        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                onlinePlayersCount = (int) snapshot.getChildrenCount();
                tvOnlinePlayers.setText("🟢 " + onlinePlayersCount + " Players Online");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvOnlinePlayers.setText("🟢 -- Players Online");
            }
        });
    }

    private void setUserOnline() {
        if (user != null) {
            DatabaseReference onlineRef = FirebaseDatabase.getInstance()
                    .getReference("online_users")
                    .child(user.getId());
            onlineRef.setValue(true);
            onlineRef.onDisconnect().removeValue();
        }
    }

    private void setUserOffline() {
        if (user != null) {
            FirebaseDatabase.getInstance()
                    .getReference("online_users")
                    .child(user.getId())
                    .removeValue();
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

    @Override
    protected void onResume() {
        super.onResume();
        loadUserStats();
    }
}