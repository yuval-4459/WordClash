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
import com.example.wordclash.services.EmailService;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_page);

        etEmail = findViewById(R.id.Email);
        btnSendCode = findViewById(R.id.btnSendCode);

        btnSendCode.setOnClickListener(v -> sendVerificationCode());
    }

    private void sendVerificationCode() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "נא להזין כתובת אימייל", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email exists in database
        DatabaseService.getInstance().checkIfEmailExists(email, new DatabaseService.DatabaseCallback<Boolean>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (!exists) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "אימייל לא נמצא במערכת",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Generate and send verification code
                String code = EmailService.generateVerificationCode();
                EmailService.sendVerificationEmail(email, code, new EmailService.EmailCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "קוד אימות נשלח לאימייל",
                                Toast.LENGTH_SHORT).show();

                        // Navigate to verification page
                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("code", code);
                        intent.putExtra("type", "forgot_password");
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "שגיאה בשליחת קוד: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ForgotPasswordActivity.this,
                        "שגיאה בבדיקת אימייל: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}