package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.Stats;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;

/**
 * Admin panel to edit user details, rank, and score
 */
public class admin_users_Activity extends AppCompatActivity {

    private Spinner genderSpinner, rankSpinner;
    private EditText emailField, passwordField, usernameField, totalScoreField;
    private Button updateBtn, deleteBtn;
    private CheckBox isAdminCheckBox;

    private User selectedUser;
    private Stats userStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        selectedUser = getIntent().getSerializableExtra("user", User.class);

        initViews();
        setupSpinners();
        setupListeners();
        populateFields();
        loadUserStats();
    }

    private void initViews() {
        genderSpinner = findViewById(R.id.Gender);
        rankSpinner = findViewById(R.id.RankSpinner);
        emailField = findViewById(R.id.Email);
        passwordField = findViewById(R.id.Password);
        usernameField = findViewById(R.id.UserName);
        totalScoreField = findViewById(R.id.TotalScore);
        isAdminCheckBox = findViewById(R.id.isAdminCheckBox);
        updateBtn = findViewById(R.id.updateUserBtn);
        deleteBtn = findViewById(R.id.deleteUserBtn);
    }

    private void setupSpinners() {
        // Gender spinner
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Rank spinner
        String[] ranks = {"1", "2", "3", "4", "5"};
        ArrayAdapter<String> rankAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ranks);
        rankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rankSpinner.setAdapter(rankAdapter);
    }

    private void setupListeners() {
        updateBtn.setOnClickListener(v -> updateUser());
        deleteBtn.setOnClickListener(v -> deleteUser());
    }

    private void populateFields() {
        if (selectedUser == null) return;

        emailField.setText(selectedUser.getEmail());
        passwordField.setText(selectedUser.getPassword());
        usernameField.setText(selectedUser.getUserName());
        isAdminCheckBox.setChecked(selectedUser.isAdmin());

        // Set gender
        String[] genders = {"Male", "Female", "Other"};
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equalsIgnoreCase(selectedUser.getGender())) {
                genderSpinner.setSelection(i);
                break;
            }
        }
    }

    private void loadUserStats() {
        DatabaseService.getInstance().getStats(selectedUser.getId(), new DatabaseService.DatabaseCallback<Stats>() {
            @Override
            public void onCompleted(Stats stats) {
                if (stats != null) {
                    userStats = stats;
                    // Set rank spinner
                    rankSpinner.setSelection(stats.getRank() - 1);
                    // Set total score
                    totalScoreField.setText(String.valueOf(stats.getTotalScore()));
                } else {
                    // Create default stats if none exist
                    userStats = new Stats(selectedUser.getId(), 1, 0);
                    rankSpinner.setSelection(0);
                    totalScoreField.setText("0");
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(admin_users_Activity.this,
                        "Failed to load user stats",
                        Toast.LENGTH_SHORT).show();
                // Create default stats
                userStats = new Stats(selectedUser.getId(), 1, 0);
                rankSpinner.setSelection(0);
                totalScoreField.setText("0");
            }
        });
    }

    private void updateUser() {
        if (selectedUser == null) return;

        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        boolean isAdmin = isAdminCheckBox.isChecked();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update user details
        selectedUser.setEmail(email);
        selectedUser.setPassword(password);
        selectedUser.setUserName(username);
        selectedUser.setGender(gender);
        selectedUser.setAdmin(isAdmin);

        // Update stats
        int newRank = Integer.parseInt(rankSpinner.getSelectedItem().toString());
        int newScore;
        try {
            newScore = Integer.parseInt(totalScoreField.getText().toString().trim());
        } catch (NumberFormatException e) {
            newScore = 0;
        }

        userStats.setRank(newRank);
        userStats.setTotalScore(newScore);

        // Save user
        DatabaseService.getInstance().updateUser(selectedUser, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void v) {
                // Save stats
                DatabaseService.getInstance().updateStats(userStats, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void unused) {
                        Toast.makeText(admin_users_Activity.this,
                                "User updated successfully",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(admin_users_Activity.this, UserListActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(admin_users_Activity.this,
                                "Failed to update stats: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(admin_users_Activity.this,
                        "Update failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser() {
        if (selectedUser == null) return;

        DatabaseService.getInstance().deleteUser(selectedUser.getId(), new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void v) {
                Toast.makeText(admin_users_Activity.this, "User deleted", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(admin_users_Activity.this, UserListActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(admin_users_Activity.this,
                        "Delete failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}