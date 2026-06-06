package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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

public class AdminUserActivity extends AppCompatActivity {

    private Spinner genderSpinner, rankSpinner;
    private TextInputLayout emailLayout, passwordLayout, usernameLayout, scoreLayout;
    private EditText emailField, passwordField, usernameField, totalScoreField;
    private android.widget.Button updateBtn, deleteBtn;
    private CheckBox isAdminCheckBox;
    private ProgressBar loadingProgress;
    private CardView mainCard;
    private View rootView;

    private User selectedUser;
    private Stats userStats;
    private boolean isDataLoaded    = false;
    private boolean hasUnsavedChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_user_title);
        }

        selectedUser = getIntent().getSerializableExtra("user", User.class);

        if (selectedUser == null) {
            showError(getString(R.string.error_no_user_data));
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
        rootView    = findViewById(android.R.id.content);
        mainCard    = findViewById(R.id.mainCard);
        loadingProgress = findViewById(R.id.loadingProgress);

        emailLayout    = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        usernameLayout = findViewById(R.id.usernameLayout);
        scoreLayout    = findViewById(R.id.scoreLayout);

        emailField      = findViewById(R.id.Email);
        passwordField   = findViewById(R.id.Password);
        usernameField   = findViewById(R.id.UserName);
        totalScoreField = findViewById(R.id.TotalScore);

        genderSpinner = findViewById(R.id.Gender);
        rankSpinner   = findViewById(R.id.RankSpinner);

        isAdminCheckBox = findViewById(R.id.isAdminCheckBox);

        updateBtn = findViewById(R.id.updateUserBtn);
        deleteBtn = findViewById(R.id.deleteUserBtn);

        // שינוי תוכנתי לפתרון הבאג שמציג את המחרוזת הגולמית עם אסימון הפורמט %d
        if (scoreLayout != null) {
            scoreLayout.setHint(getString(R.string.score_label));
        }

        setButtonsEnabled(false);
    }

    private void setupSpinners() {
        // Gender — readable adapter
        String[] genders = {getString(R.string.male), getString(R.string.female), getString(R.string.other)};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, R.layout.item_spinner, genders);
        genderAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        genderSpinner.setAdapter(genderAdapter);

        // Rank — readable adapter
        String[] ranks = {
                getString(R.string.rank_1), getString(R.string.rank_2),
                getString(R.string.rank_3), getString(R.string.rank_4), getString(R.string.rank_5)};
        ArrayAdapter<String> rankAdapter = new ArrayAdapter<>(
                this, R.layout.item_spinner, ranks);
        rankAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        rankSpinner.setAdapter(rankAdapter);
    }

    private void setupListeners() {
        updateBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                showUpdateConfirmation();
            }
        });

        deleteBtn.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void setupChangeTracking() {
        TextWatcher changeWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                hasUnsavedChanges = true;
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        emailField.addTextChangedListener(changeWatcher);
        passwordField.addTextChangedListener(changeWatcher);
        usernameField.addTextChangedListener(changeWatcher);
        totalScoreField.addTextChangedListener(changeWatcher);

        genderSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent,
                                       View view, int position, long id) {
                if (isDataLoaded) hasUnsavedChanges = true;
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        rankSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent,
                                       View view, int position, long id) {
                if (isDataLoaded) hasUnsavedChanges = true;
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        isAdminCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> { if (isDataLoaded) hasUnsavedChanges = true; });
    }

    private void populateFields() {
        if (selectedUser == null) return;

        emailField.setText(selectedUser.getEmail());
        passwordField.setText(selectedUser.getPassword());
        usernameField.setText(selectedUser.getUserName());
        isAdminCheckBox.setChecked(selectedUser.isAdmin());

        String[] dbGenders = {"Male", "Female", "Other"};
        for (int i = 0; i < dbGenders.length; i++) {
            if (dbGenders[i].equalsIgnoreCase(selectedUser.getGender())) {
                genderSpinner.setSelection(i);
                break;
            }
        }
    }

    private void loadUserStats() {
        DatabaseService.getInstance().getStats(selectedUser.getId(),
                new DatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Stats stats) {
                        runOnUiThread(() -> {
                            if (stats != null) {
                                userStats = stats;
                                rankSpinner.setSelection(Math.max(0, Math.min(stats.getRank() - 1, 4)));
                                totalScoreField.setText(String.valueOf(stats.getTotalScore()));
                            } else {
                                userStats = new Stats(selectedUser.getId(), 1, 0);
                                rankSpinner.setSelection(0);
                                totalScoreField.setText("0");
                            }
                            showLoading(false);
                            setButtonsEnabled(true);
                            isDataLoaded     = true;
                            hasUnsavedChanges = false;
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() -> {
                            showError(getString(R.string.failed_load_user_stats, e.getMessage()));
                            userStats = new Stats(selectedUser.getId(), 1, 0);
                            rankSpinner.setSelection(0);
                            totalScoreField.setText("0");
                            showLoading(false);
                            setButtonsEnabled(true);
                            isDataLoaded     = true;
                            hasUnsavedChanges = false;
                        });
                    }
                });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String email    = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String score    = totalScoreField.getText().toString().trim();

        emailLayout.setError(null);
        passwordLayout.setError(null);
        usernameLayout.setError(null);
        scoreLayout.setError(null);

        if (email.isEmpty()) {
            emailLayout.setError(getString(R.string.validation_email_required));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.validation_invalid_email));
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.validation_password_required));
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.validation_password_short));
            isValid = false;
        }

        if (username.isEmpty()) {
            usernameLayout.setError(getString(R.string.validation_username_required));
            isValid = false;
        } else if (username.length() < 3) {
            usernameLayout.setError(getString(R.string.validation_username_short));
            isValid = false;
        }

        if (score.isEmpty()) {
            scoreLayout.setError(getString(R.string.validation_score_required));
            isValid = false;
        } else {
            try {
                int scoreValue = Integer.parseInt(score);
                if (scoreValue < 0) {
                    scoreLayout.setError(getString(R.string.validation_score_negative));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                scoreLayout.setError(getString(R.string.validation_score_invalid));
                isValid = false;
            }
        }

        return isValid;
    }

    private void showUpdateConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_update_title)
                .setMessage(R.string.confirm_update_msg)
                .setPositiveButton(R.string.update, (dialog, which) -> updateUser())
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_msg)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteUser())
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void updateUser() {
        if (selectedUser == null) return;

        showLoading(true);
        setButtonsEnabled(false);

        String email    = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String[] dbGenders = {"Male", "Female", "Other"};
        String gender   = dbGenders[genderSpinner.getSelectedItemPosition()];
        boolean isAdmin = isAdminCheckBox.isChecked();

        selectedUser.setEmail(email);
        selectedUser.setPassword(password);
        selectedUser.setUserName(username);
        selectedUser.setGender(gender);
        selectedUser.setAdmin(isAdmin);

        int newRank  = rankSpinner.getSelectedItemPosition() + 1;
        int newScore = 0;
        try {
            newScore = Integer.parseInt(totalScoreField.getText().toString().trim());
        } catch (NumberFormatException ignored) {}

        userStats.setRank(newRank);
        userStats.setTotalScore(newScore);

        DatabaseService.getInstance().updateUser(selectedUser,
                new DatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void v) {
                        DatabaseService.getInstance().updateStats(userStats,
                                new DatabaseService.DatabaseCallback<>() {
                                    @Override
                                    public void onCompleted(Void unused) {
                                        runOnUiThread(() -> {
                                            showLoading(false);
                                            hasUnsavedChanges = false;
                                            showSuccess(getString(R.string.user_updated_success));

                                            new android.os.Handler().postDelayed(() -> {
                                                Intent intent = new Intent(
                                                        AdminUserActivity.this, UserListActivity.class);
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
                                            showError(getString(R.string.stats_update_failed, e.getMessage()));
                                        });
                                    }
                                });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            setButtonsEnabled(true);
                            showError(getString(R.string.user_update_failed, e.getMessage()));
                        });
                    }
                });
    }

    private void deleteUser() {
        if (selectedUser == null) return;

        showLoading(true);
        setButtonsEnabled(false);

        DatabaseService.getInstance().deleteUser(selectedUser.getId(),
                new DatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void v) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            hasUnsavedChanges = false;
                            showSuccess(getString(R.string.user_deleted_success));

                            new android.os.Handler().postDelayed(() -> {
                                Intent intent = new Intent(
                                        AdminUserActivity.this, UserListActivity.class);
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
                            showError(getString(R.string.user_delete_failed, e.getMessage()));
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
                    .setTitle(R.string.unsaved_changes_title)
                    .setMessage(R.string.unsaved_changes_msg)
                    .setPositiveButton(R.string.leave, (dialog, which) -> finish())
                    .setNegativeButton(R.string.stay, null)
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