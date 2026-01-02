package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.LanguageUtils;
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.util.ArrayList;

public class ChangeDetailsActivity extends AppCompatActivity {

    private EditText etUserName, etEmail, etPassword;
    private Spinner genderSpinner, languageSpinner;
    private Button btnUpdateDetails;
    private TextView tvCurrentLanguage;
    private String selectedGender = "";
    private String selectedLanguage = "";

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentUser = SharedPreferencesUtils.getUser(this);
        if (currentUser != null) {
            LanguageUtils.applyLanguageSettings(this, currentUser);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_details_page);

        if (currentUser != null) {
            LanguageUtils.setLayoutDirection(this, currentUser);
        }

        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etUserName = findViewById(R.id.UserName);
        etEmail = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        genderSpinner = findViewById(R.id.Gender);
        languageSpinner = findViewById(R.id.LanguageSpinner);
        btnUpdateDetails = findViewById(R.id.btnUpdateDetails);
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);

        setupGenderSpinner();
        setupLanguageSpinner();
        setupFieldsBasedOnRole();
        populateFields();

        btnUpdateDetails.setText(R.string.update_details);
        btnUpdateDetails.setOnClickListener(v -> updateDetails());
    }

    private void setupGenderSpinner() {
        ArrayList<String> genders = new ArrayList<>();
        genders.add(getString(R.string.male));
        genders.add(getString(R.string.female));
        genders.add(getString(R.string.other));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedGender = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedGender = "";
            }
        });
    }

    private void setupLanguageSpinner() {
        ArrayList<String> languages = new ArrayList<>();
        languages.add(getString(R.string.english));
        languages.add(getString(R.string.hebrew));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selected = adapterView.getItemAtPosition(position).toString();
                // Convert display name to language code
                if (selected.equals(getString(R.string.english))) {
                    selectedLanguage = "english";
                } else {
                    selectedLanguage = "hebrew";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedLanguage = "";
            }
        });
    }

    private void setupFieldsBasedOnRole() {
        if (currentUser.isAdmin()) {
            etEmail.setEnabled(true);
            etPassword.setEnabled(true);
            etUserName.setEnabled(true);
            genderSpinner.setEnabled(true);
            languageSpinner.setEnabled(true);
        } else {
            etEmail.setEnabled(false);
            etPassword.setEnabled(false);
            etUserName.setEnabled(true);
            genderSpinner.setEnabled(true);
            languageSpinner.setEnabled(true);
        }
    }

    private void populateFields() {
        etUserName.setText(currentUser.getUserName());
        etEmail.setText(currentUser.getEmail());
        etPassword.setText(currentUser.getPassword());

        // Set gender
        String[] genders = {getString(R.string.male), getString(R.string.female), getString(R.string.other)};
        String[] genderCodes = {"Male", "Female", "Other"};
        for (int i = 0; i < genderCodes.length; i++) {
            if (genderCodes[i].equalsIgnoreCase(currentUser.getGender())) {
                genderSpinner.setSelection(i);
                selectedGender = genders[i];
                break;
            }
        }

        // Set language
        String learningLanguage = currentUser.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";

        if (learningLanguage.equals("english")) {
            languageSpinner.setSelection(0);
            selectedLanguage = "english";
        } else {
            languageSpinner.setSelection(1);
            selectedLanguage = "hebrew";
        }

        // Show current language
        tvCurrentLanguage.setText(getString(R.string.learning_language,
                LanguageUtils.getLearningLanguageDisplayName(this, currentUser)));
    }

    private void updateDetails() {
        String userName = etUserName.getText().toString().trim();

        if (userName.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedGender.isEmpty()) {
            selectedGender = currentUser.getGender();
        }

        // Convert display gender back to English code
        String genderCode = selectedGender;
        if (selectedGender.equals(getString(R.string.male))) genderCode = "Male";
        else if (selectedGender.equals(getString(R.string.female))) genderCode = "Female";
        else if (selectedGender.equals(getString(R.string.other))) genderCode = "Other";

        currentUser.setUserName(userName);
        currentUser.setGender(genderCode);

        // Check if language changed
        String oldLanguage = currentUser.getLearningLanguage();
        if (oldLanguage == null) oldLanguage = "english";
        boolean languageChanged = !selectedLanguage.isEmpty() && !selectedLanguage.equals(oldLanguage);

        if (!selectedLanguage.isEmpty()) {
            currentUser.setLearningLanguage(selectedLanguage);
        }

        if (currentUser.isAdmin()) {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_fill_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            currentUser.setEmail(email);
            currentUser.setPassword(password);
        }

        if (languageChanged) {
            showLanguageChangeWarning();
        } else {
            saveChanges();
        }
    }

    private void showLanguageChangeWarning() {
        String message = selectedLanguage.equals("english")
                ? "האפליקציה תעבור לעברית. האם להמשיך?"
                : "The app will switch to English. Continue?";

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.change_learning_language))
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> saveChanges())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void saveChanges() {
        DatabaseService.getInstance().updateUser(currentUser, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void v) {
                SharedPreferencesUtils.saveUser(ChangeDetailsActivity.this, currentUser);

                Toast.makeText(ChangeDetailsActivity.this,
                        getString(R.string.details_updated),
                        Toast.LENGTH_SHORT).show();

                // Restart app to apply language changes
                Intent intent = new Intent(ChangeDetailsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ChangeDetailsActivity.this,
                        getString(R.string.error_saving) + ": " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}