package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;

    private Button btnConfirm, btnForgotPassword;

    private DatabaseService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        etEmail = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        btnConfirm = findViewById(R.id.ConfirmsignUpButton); // same button id
        btnForgotPassword = findViewById(R.id.ForgotPasswordButton);

        db = DatabaseService.getInstance();

        btnConfirm.setOnClickListener(v -> loginUser());
        btnForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        db.login(email, password, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user == null) {
                    Toast.makeText(LoginActivity.this,
                            "אימייל או סיסמה שגויים",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(LoginActivity.this,
                        "התחברת בהצלחה " + user.getUserName(),
                        Toast.LENGTH_SHORT).show();

                SharedPreferencesUtils.saveUser(LoginActivity.this, user);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(LoginActivity.this,
                        "נכשל בהתחברות: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
