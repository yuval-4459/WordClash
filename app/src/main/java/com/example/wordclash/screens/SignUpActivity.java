package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.Stats;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;


public class SignUpActivity extends AppCompatActivity {

    private DatabaseService db;

    private EditText etEmail, etPassword, etPassword2, etUserName;
    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword, tilUserName;
    private String selectedGender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);

        etEmail    = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        etPassword2 = findViewById(R.id.PassswordAuthentication);
        etUserName  = findViewById(R.id.UserName);

        tilEmail           = findViewById(R.id.tilEmail);
        tilPassword        = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilUserName        = findViewById(R.id.tilUserName);

        Spinner genderSpinner = findViewById(R.id.Gender);
        Button btnConfirm = findViewById(R.id.ConfirmsignUpButton);

        Button btnBack = findViewById(R.id.BacktoLandingButton);
        btnBack.setOnClickListener(view -> finish());

        db = DatabaseService.getInstance();

        // ------------------------------------------------------------------
        // Gender Spinner — use readable layouts so the chosen option is visible
        // ------------------------------------------------------------------
        ArrayList<String> genders = new ArrayList<>();
        genders.add("Choose");
        genders.add("Male");
        genders.add("Female");
        genders.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.item_spinner, genders);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        genderSpinner.setAdapter(adapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView,
                                       View view, int position, long l) {
                selectedGender = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedGender = "";
            }
        });

        btnConfirm.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String password2 = etPassword2.getText().toString().trim();
        String userName  = etUserName.getText().toString().trim();

        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilUserName.setError(null);

        boolean hasError = false;

        // --- Validation: same rules as AdminUserActivity ---
        if (email.isEmpty()) {
            tilEmail.setError("נא למלא את השדה");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("כתובת אימייל לא תקינה");
            hasError = true;
        }

        if (password.isEmpty()) {
            tilPassword.setError("נא למלא את השדה");
            hasError = true;
        } else if (password.length() < 6) {
            tilPassword.setError("סיסמה חייבת להכיל לפחות 6 תווים");
            hasError = true;
        }

        if (password2.isEmpty()) {
            tilConfirmPassword.setError("נא למלא את השדה");
            hasError = true;
        }

        if (userName.isEmpty()) {
            tilUserName.setError("נא למלא את השדה");
            hasError = true;
        } else if (userName.length() < 3) {
            tilUserName.setError("שם משתמש חייב להכיל לפחות 3 תווים");
            hasError = true;
        }

        if (hasError) return;

        if (!password.equals(password2)) {
            tilConfirmPassword.setError("הסיסמאות אינן תואמות");
            return;
        }

        if (selectedGender.equals("Choose") || selectedGender.isEmpty()) {
            Toast.makeText(this, "You need to choose a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        String id   = db.generateUserId();
        User   user = new User(id, email, password, userName,
                selectedGender, false, null, new ArrayList<>());

        db.checkIfEmailExists(user.getEmail(), new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Boolean exist) {
                if (exist) {
                    tilEmail.setError("מייל כבר קיים");
                } else {
                    db.createNewUser(user, new DatabaseService.DatabaseCallback<>() {
                        @Override
                        public void onCompleted(Void v) {
                            Stats initialStats = new Stats(user.getId(), 1, 0);
                            db.createStats(initialStats, null);
                            Toast.makeText(SignUpActivity.this,
                                    "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                            SharedPreferencesUtils.saveUser(SignUpActivity.this, user);
                            Intent intent = new Intent(
                                    SignUpActivity.this, LanguageSelectionActivity.class);
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(SignUpActivity.this,
                                    "נכשל בשמירת המשתמש: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(SignUpActivity.this,
                        "נכשל בשמירת המשתמש: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}