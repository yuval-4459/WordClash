package com.example.wordclash.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.utils.LanguageUtils;
import com.example.wordclash.utils.SharedPreferencesUtils;
import com.example.wordclash.utils.VocabularyImporter;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Button btnLogout;
    private Button btnRanks;
    private Button btnMiniGames;
    private Button btnLeaderboard;

    private TextView tvHelloUser;
    private ImageView ivUserAvatar;
    private TextView tvUserInitial;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;

    private User user;

    private static final String PREFS_NAME = "WordClashPrefs";
    private static final String KEY_VOCABULARY_IMPORTED = "vocabulary_imported";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = SharedPreferencesUtils.getUser(this);

        if (user != null) {
            LanguageUtils.applyLanguageSettings(this, user);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        if (user == null) {
            startActivity(new Intent(this, StartPageActivity.class));
            finish();
            return;
        }

        LanguageUtils.setLayoutDirection(this, user);

        setupUI();
        importVocabularyIfNeeded();
        setupMenu();
        loadProfilePicture();
    }

    private String getGreetingByTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return getString(R.string.good_morning);
        } else if (hour >= 12 && hour < 17) {
            return getString(R.string.good_afternoon);
        } else if (hour >= 17 && hour < 21) {
            return getString(R.string.good_evening);
        } else {
            return getString(R.string.good_night);
        }
    }

    private void setupUI() {
        tvHelloUser = findViewById(R.id.tvHelloUser);
        tvHelloUser.setText(getGreetingByTime() + ", " + user.getUserName());

        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        tvUserInitial = findViewById(R.id.tvUserInitial);

        View avatarContainer = findViewById(R.id.avatarContainer);
        if (avatarContainer != null) {
            avatarContainer.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfilePictureActivity.class);
                startActivity(intent);
            });
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuIcon = findViewById(R.id.menu_icon);

        navigationView.setNavigationItemSelectedListener(this);

        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setText(R.string.logout);
        btnLogout.setOnClickListener(v -> logout());

        btnRanks = findViewById(R.id.btnRanks);
        btnRanks.setText(R.string.start_playing);
        btnRanks.setOnClickListener(v ->
                startActivity(new Intent(this, RanksActivity.class))
        );

        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnLeaderboard.setText(R.string.leaderboard);
        btnLeaderboard.setOnClickListener(v ->
                startActivity(new Intent(this, LeaderboardActivity.class))
        );

        btnMiniGames = findViewById(R.id.MiniGamesButton);
        btnMiniGames.setText(R.string.mini_games);
        btnMiniGames.setOnClickListener(v ->
                startActivity(new Intent(this, MiniGamesActivity.class))
        );
    }

    private void setupMenu() {
        if (!user.isAdmin()) {
            navigationView.getMenu().findItem(R.id.nav_users_table).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_add_word).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_manage_words).setVisible(false);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_users_table) {
            startActivity(new Intent(this, UserListActivity.class));
        } else if (id == R.id.nav_add_word) {
            startActivity(new Intent(this, AdminAddWordActivity.class));
        } else if (id == R.id.nav_manage_words) {
            startActivity(new Intent(this, AdminManageWordsActivity.class));
        } else if (id == R.id.nav_change_details) {
            startActivity(new Intent(this, ChangeDetailsActivity.class));
        } else if (id == R.id.nav_profile_picture) {
            startActivity(new Intent(this, ProfilePictureActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void importVocabularyIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean imported = prefs.getBoolean(KEY_VOCABULARY_IMPORTED, false);

        if (!imported) {
            VocabularyImporter.importVocabularyFromAssets(this);
            prefs.edit().putBoolean(KEY_VOCABULARY_IMPORTED, true).apply();
        }
    }

    private void loadProfilePicture() {
        String profilePictureUrl = user.getProfilePictureUrl();

        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            ivUserAvatar.setVisibility(View.VISIBLE);
            tvUserInitial.setVisibility(View.GONE);

            try {
                byte[] decodedString = Base64.decode(profilePictureUrl, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivUserAvatar.setImageBitmap(decodedByte);
            } catch (Exception e) {
                showDefaultAvatar();
            }
        } else {
            showDefaultAvatar();
        }
    }

    private void showDefaultAvatar() {
        ivUserAvatar.setVisibility(View.GONE);
        tvUserInitial.setVisibility(View.VISIBLE);

        String initial = "";
        if (user.getUserName() != null && !user.getUserName().isEmpty()) {
            initial = String.valueOf(user.getUserName().charAt(0)).toUpperCase();
        }
        tvUserInitial.setText(initial);
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = SharedPreferencesUtils.getUser(this);
        if (user != null) {
            loadProfilePicture();
            tvHelloUser.setText(getGreetingByTime() + ", " + user.getUserName());
        }
    }

    private void logout() {
        SharedPreferencesUtils.signOutUser(this);
        Intent intent = new Intent(this, StartPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}