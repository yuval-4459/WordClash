package com.example.wordclash.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.utils.SharedPreferencesUtils;
import com.example.wordclash.utils.VocabularyImporter;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Button btnLogout;
    Button btnRanks;
    Button btnWordle;
    Button btnLeaderboard;

    private User user;
    private TextView tvHelloUser;

    // Drawer components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private Toolbar toolbar;

    private static final String PREFS_NAME = "WordClashPrefs";
    private static final String KEY_VOCABULARY_IMPORTED = "vocabulary_imported";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        user = SharedPreferencesUtils.getUser(MainActivity.this);
        assert user != null;

        tvHelloUser = findViewById(R.id.tvHelloUser);
        tvHelloUser.setText("Hello " + user.getUserName());

        // Import vocabulary on first launch
        importVocabularyIfNeeded();

        // Setup drawer
        setupDrawer();

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        btnRanks = findViewById(R.id.btnRanks);
        btnRanks.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, RanksActivity.class)));

        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnLeaderboard.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, LeaderboardActivity.class)));

        btnWordle = findViewById(R.id.WordleButton);
        btnWordle.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, WordleActivity.class)));

        // Setup menu based on user role
        setupMenuForUser();

        // Handle back button press - modern approach
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
    }

    private void setupDrawer() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuIcon = findViewById(R.id.menu_icon);

        navigationView.setNavigationItemSelectedListener(this);

        // Always show hamburger menu for all users
        menuIcon.setVisibility(View.VISIBLE);

        // Setup hamburger menu click
        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void setupMenuForUser() {
        Menu menu = navigationView.getMenu();

        if (!user.isAdmin()) {
            // Hide admin-only menu items for regular users
            menu.findItem(R.id.nav_users_table).setVisible(false);
            menu.findItem(R.id.nav_add_word).setVisible(false);
            menu.findItem(R.id.nav_manage_words).setVisible(false);
            // Keep only "Change Details" visible
            menu.findItem(R.id.nav_change_details).setVisible(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_users_table) {
            startActivity(new Intent(MainActivity.this, UserListActivity.class));
        } else if (id == R.id.nav_add_word) {
            startActivity(new Intent(MainActivity.this, AdminAddWordActivity.class));
        } else if (id == R.id.nav_manage_words) {
            startActivity(new Intent(MainActivity.this, AdminManageWordsActivity.class));
        } else if (id == R.id.nav_change_details) {
            startActivity(new Intent(MainActivity.this, ChangeDetailsActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void importVocabularyIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean imported = prefs.getBoolean(KEY_VOCABULARY_IMPORTED, false);

        if (!imported) {
            Toast.makeText(this, "Importing vocabulary... This may take a moment.",
                    Toast.LENGTH_LONG).show();

            VocabularyImporter.importVocabularyFromAssets(this);

            // Mark as imported
            prefs.edit().putBoolean(KEY_VOCABULARY_IMPORTED, true).apply();

            Toast.makeText(this, "Vocabulary imported successfully!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        SharedPreferencesUtils.signOutUser(MainActivity.this);
        Intent intent = new Intent(MainActivity.this, StartPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}