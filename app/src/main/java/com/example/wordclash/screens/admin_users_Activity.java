package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.wordclash.R;
import com.example.wordclash.models.Stats;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;


public class admin_users_Activity extends AppCompatActivity {

    // UI Components
    private Spinner genderSpinner, rankSpinner;
    private TextInputLayout emailLayout, passwordLayout, usernameLayout, scoreLayout;
    private EditText emailField, passwordField, usernameField, totalScoreField;
    private Button updateBtn, deleteBtn, cancelBtn;
    private CheckBox isAdminCheckBox;
    private ProgressBar loadingProgress;
    private CardView mainCard;
    private View rootView;

    // Data
    private User selectedUser;
    private Stats userStats;
    private boolean isDataLoaded = false;
    private boolean hasUnsavedChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit User");
        }

        selectedUser = getIntent().getSerializableExtra("user", User.class);

        if (selectedUser == null) {
            showError("Error: No user data received");
            finish();
            return;
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });


        initViews();
        setupSpinners();
        setupListeners();
        setupChangeTracking();
        showLoading(true);
        populateFields();
        loadUserStats();
    }

    private void initViews() {
        rootView = findViewById(android.R.id.content);
        mainCard = findViewById(R.id.mainCard);
        loadingProgress = findViewById(R.id.loadingProgress);

        // TextInputLayouts for better material design
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        usernameLayout = findViewById(R.id.usernameLayout);
        scoreLayout = findViewById(R.id.scoreLayout);

        // EditTexts
        emailField = findViewById(R.id.Email);
        passwordField = findViewById(R.id.Password);
        usernameField = findViewById(R.id.UserName);
        totalScoreField = findViewById(R.id.TotalScore);

        // Spinners
        genderSpinner = findViewById(R.id.Gender);
        rankSpinner = findViewById(R.id.RankSpinner);

        // Checkbox
        isAdminCheckBox = findViewById(R.id.isAdminCheckBox);

        // Buttons
        updateBtn = findViewById(R.id.updateUserBtn);
        deleteBtn = findViewById(R.id.deleteUserBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        // Initially disable buttons until data loads
        setButtonsEnabled(false);
    }

    private void setupSpinners() {
        // Gender spinner with better styling
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Rank spinner (1-5 ranks)
        String[] ranks = {"Rank 1 - Beginner", "Rank 2 - Intermediate",
                "Rank 3 - Advanced", "Rank 4 - Expert", "Rank 5 - Master"};
        ArrayAdapter<String> rankAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ranks);
        rankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rankSpinner.setAdapter(rankAdapter);
    }

    private void setupListeners() {
        updateBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                showUpdateConfirmation();
            }
        });

        deleteBtn.setOnClickListener(v -> showDeleteConfirmation());

        cancelBtn.setOnClickListener(v -> handleBackPress());
    }

    private void setupChangeTracking() {
        TextWatcher changeWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hasUnsavedChanges = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        emailField.addTextChangedListener(changeWatcher);
        passwordField.addTextChangedListener(changeWatcher);
        usernameField.addTextChangedListener(changeWatcher);
        totalScoreField.addTextChangedListener(changeWatcher);

        genderSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (isDataLoaded) hasUnsavedChanges = true;
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        rankSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (isDataLoaded) hasUnsavedChanges = true;
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        isAdminCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isDataLoaded) hasUnsavedChanges = true;
        });
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
                runOnUiThread(() -> {
                    if (stats != null) {
                        userStats = stats;
                        rankSpinner.setSelection(Math.max(0, Math.min(stats.getRank() - 1, 4)));
                        totalScoreField.setText(String.valueOf(stats.getTotalScore()));
                    } else {
                        // Create default stats if none exist
                        userStats = new Stats(selectedUser.getId(), 1, 0);
                        rankSpinner.setSelection(0);
                        totalScoreField.setText("0");
                    }
                    showLoading(false);
                    setButtonsEnabled(true);
                    isDataLoaded = true;
                    hasUnsavedChanges = false;
                });
            }

            @Override
            public void onFailed(Exception e) {
                runOnUiThread(() -> {
                    showError("Failed to load user stats: " + e.getMessage());
                    // Create default stats
                    userStats = new Stats(selectedUser.getId(), 1, 0);
                    rankSpinner.setSelection(0);
                    totalScoreField.setText("0");
                    showLoading(false);
                    setButtonsEnabled(true);
                    isDataLoaded = true;
                    hasUnsavedChanges = false;
                });
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String score = totalScoreField.getText().toString().trim();

        // Clear previous errors
        emailLayout.setError(null);
        passwordLayout.setError(null);
        usernameLayout.setError(null);
        scoreLayout.setError(null);

        // Validate email
        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email format");
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        }

        // Validate username
        if (username.isEmpty()) {
            usernameLayout.setError("Username is required");
            isValid = false;
        } else if (username.length() < 3) {
            usernameLayout.setError("Username must be at least 3 characters");
            isValid = false;
        }

        // Validate score
        if (score.isEmpty()) {
            scoreLayout.setError("Score is required");
            isValid = false;
        } else {
            try {
                int scoreValue = Integer.parseInt(score);
                if (scoreValue < 0) {
                    scoreLayout.setError("Score cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                scoreLayout.setError("Invalid score format");
                isValid = false;
            }
        }

        return isValid;
    }

    private void showUpdateConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Update")
                .setMessage("Are you sure you want to update this user's information?")
                .setPositiveButton("Update", (dialog, which) -> updateUser())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this user? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void updateUser() {
        if (selectedUser == null) return;

        showLoading(true);
        setButtonsEnabled(false);

        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        boolean isAdmin = isAdminCheckBox.isChecked();

        // Update user details
        selectedUser.setEmail(email);
        selectedUser.setPassword(password);
        selectedUser.setUserName(username);
        selectedUser.setGender(gender);
        selectedUser.setAdmin(isAdmin);

        // Update stats (extract rank number from "Rank X - Description" format)
        int newRank = rankSpinner.getSelectedItemPosition() + 1;

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
                        runOnUiThread(() -> {
                            showLoading(false);
                            hasUnsavedChanges = false;
                            showSuccess("User updated successfully");

                            // Delay before returning to list
                            new android.os.Handler().postDelayed(() -> {
                                Intent intent = new Intent(admin_users_Activity.this, UserListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }, 1500);
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            setButtonsEnabled(true);
                            showError("Failed to update stats: " + e.getMessage());
                        });
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    setButtonsEnabled(true);
                    showError("Update failed: " + e.getMessage());
                });
            }
        });
    }

    private void deleteUser() {
        if (selectedUser == null) return;

        showLoading(true);
        setButtonsEnabled(false);

        DatabaseService.getInstance().deleteUser(selectedUser.getId(), new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void v) {
                runOnUiThread(() -> {
                    showLoading(false);
                    hasUnsavedChanges = false;
                    showSuccess("User deleted successfully");

                    // Delay before returning to list
                    new android.os.Handler().postDelayed(() -> {
                        Intent intent = new Intent(admin_users_Activity.this, UserListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }, 1500);
                });
            }

            @Override
            public void onFailed(Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    setButtonsEnabled(true);
                    showError("Delete failed: " + e.getMessage());
                });
            }
        });
    }

    private void showLoading(boolean show) {
        loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        mainCard.setAlpha(show ? 0.5f : 1.0f);
    }

    private void setButtonsEnabled(boolean enabled) {
        updateBtn.setEnabled(enabled);
        deleteBtn.setEnabled(enabled);
        cancelBtn.setEnabled(enabled);
        emailField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        totalScoreField.setEnabled(enabled);
        genderSpinner.setEnabled(enabled);
        rankSpinner.setEnabled(enabled);
        isAdminCheckBox.setEnabled(enabled);
    }

    private void showError(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark))
                .show();
    }

    private void showSuccess(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark))
                .show();
    }

    private void handleBackPress() {
        if (hasUnsavedChanges) {
            new AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Are you sure you want to leave?")
                    .setPositiveButton("Leave", (dialog, which) -> finish())
                    .setNegativeButton("Stay", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackPress();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}