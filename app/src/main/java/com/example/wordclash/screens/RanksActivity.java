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
import com.example.wordclash.utils.LanguageUtils;
import com.example.wordclash.utils.SharedPreferencesUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RanksActivity extends AppCompatActivity {

    // hardcoded manager ID extracted to a constant
    private static final String MANAGER_ID = "-OfhuEaP25z-o6NIsC5K";
    private TextView tvOnlinePlayers;
    private CardView cardLevel1, cardLevel2, cardLevel3, cardLevel4, cardLevel5;
    private Button btnLevel1;
    private Button btnLevel2;
    private Button btnLevel3;
    private Button btnLevel4;
    private Button btnLevel5;
    private TextView tvLevelStatus1, tvLevelStatus2, tvLevelStatus3, tvLevelStatus4, tvLevelStatus5;
    private TextView tvLevelTitle1, tvLevelTitle2, tvLevelTitle3, tvLevelTitle4, tvLevelTitle5;
    private User user;
    private int currentLevel = 1;
    private int onlinePlayersCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // טעינת המשתמש והגדרת השפה *לפני* super.onCreate כדי שהטקסטים וה-Layout ייטענו נכון
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            LanguageUtils.applyLanguageSettings(getApplicationContext(), user);
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranks);

        // החלת כיוון התצוגה (RTL/LTR) בהתאם לשפה
        if (user != null) {
            LanguageUtils.setLayoutDirection(this, user);
        }

        initializeUser();
        initializeViews();
        loadUserStats();
        updateOnlinePlayersCount();
    }

    private void initializeUser() {
        if (user == null) {
            user = SharedPreferencesUtils.getUser(RanksActivity.this);
        }
        if (user == null) {
            Toast.makeText(this, R.string.user_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        tvOnlinePlayers = findViewById(R.id.tvOnlinePlayers);
        TextView tvTitle = findViewById(R.id.tvTitle);

        // עדכון כותרת המסך הראשית לפי השפה הנבחרת
        if (tvTitle != null) {
            tvTitle.setText(getString(R.string.choose_level));
        }

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

        // קישור כותרות הרמות שבתוך ה-Cards
        tvLevelTitle1 = findViewById(R.id.tvLevelTitle1);
        tvLevelTitle2 = findViewById(R.id.tvLevelTitle2);
        tvLevelTitle3 = findViewById(R.id.tvLevelTitle3);
        tvLevelTitle4 = findViewById(R.id.tvLevelTitle4);
        tvLevelTitle5 = findViewById(R.id.tvLevelTitle5);

        // עדכון כותרות הרמות דינמית (יציג "דרגה 1" בעברית או "Level 1" באנגלית)
        if (tvLevelTitle1 != null) tvLevelTitle1.setText(getString(R.string.level, 1));
        if (tvLevelTitle2 != null) tvLevelTitle2.setText(getString(R.string.level, 2));
        if (tvLevelTitle3 != null) tvLevelTitle3.setText(getString(R.string.level, 3));
        if (tvLevelTitle4 != null) tvLevelTitle4.setText(getString(R.string.level, 4));
        if (tvLevelTitle5 != null) tvLevelTitle5.setText(getString(R.string.level, 5));
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
                Toast.makeText(RanksActivity.this, R.string.failed_load_stats, Toast.LENGTH_SHORT).show();
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
            button.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
            button.setText(getString(R.string.play_level, level));
            statusText.setText(getString(R.string.unlocked));
            statusText.setTextColor(getResources().getColor(R.color.success));
            button.setOnClickListener(v -> startLevel(level));
        } else {
            card.setAlpha(0.5f);
            button.setEnabled(false);
            button.setBackgroundTintList(getResources().getColorStateList(R.color.level_locked));
            button.setText("🔒");
            statusText.setText(getString(R.string.locked));
            statusText.setTextColor(getResources().getColor(R.color.level_locked));
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
                tvOnlinePlayers.setText(getString(R.string.players_online, onlinePlayersCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvOnlinePlayers.setText("🟢 --");
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