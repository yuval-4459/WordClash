package com.example.wordclash.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.utils.SharedPreferencesUtils;
import com.example.wordclash.utils.VocabularyImporter;

public class MainActivity extends AppCompatActivity {

    Button btnUsersTable;
    Button btnAddWord; // NEW
    Button btnLogout;
    Button btnChangeDetails;
    Button btnRanks;
    Button btnWordle;

    private User user;
    private TextView tvHelloUser;

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

        btnUsersTable = findViewById(R.id.UsersTableButton);
        btnUsersTable.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, UserListActivity.class)));

        btnAddWord = findViewById(R.id.AddWordButton); // NEW
        btnAddWord.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, AdminAddWordActivity.class)));

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        btnChangeDetails = findViewById(R.id.btnChangeDetails);
        btnChangeDetails.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, ChangeDetailsActivity.class)));

        btnRanks = findViewById(R.id.btnRanks);
        btnRanks.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, RanksActivity.class)));

        btnWordle = findViewById(R.id.WordleButton);
        btnWordle.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, WordleActivity.class)));

        HideAdminButtons();
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

    private void HideAdminButtons() {
        if (!user.isAdmin()) {
            btnUsersTable.setVisibility(View.GONE);
            btnAddWord.setVisibility(View.GONE); // NEW
        } else {
            btnUsersTable.setVisibility(View.VISIBLE);
            btnAddWord.setVisibility(View.VISIBLE); // NEW
        }
    }

    private void logout() {
        SharedPreferencesUtils.signOutUser(MainActivity.this);
        Intent intent = new Intent(MainActivity.this, StartPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}