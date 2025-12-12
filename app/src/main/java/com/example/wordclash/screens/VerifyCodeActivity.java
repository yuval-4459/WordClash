package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText etCode;
    private Button btnVerify;

    private String email;
    private String correctCode;
    private String verificationType; // "forgot_password" or "change_details"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_code_page);

        etCode = findViewById(R.id.etVerificationCode);
        btnVerify = findViewById(R.id.btnVerify);

        // Get data from intent
        email = getIntent().getStringExtra("email");
        correctCode = getIntent().getStringExtra("code");
        verificationType = getIntent().getStringExtra("type");

        btnVerify.setOnClickListener(v -> verifyCode());
    }

    private void verifyCode() {
        String enteredCode = etCode.getText().toString().trim();

        if (enteredCode.isEmpty()) {
            Toast.makeText(this, "נא להזין את הקוד", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredCode.equals(correctCode)) {
            Toast.makeText(this, "אימות הצליח!", Toast.LENGTH_SHORT).show();

            if ("forgot_password".equals(verificationType)) {
                // Navigate to reset password page
                Intent intent = new Intent(VerifyCodeActivity.this, ResetPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else if ("change_details".equals(verificationType)) {
                // Navigate to change details page
                Intent intent = new Intent(VerifyCodeActivity.this, ChangeDetailsActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(this, "קוד שגוי, נסה שוב", Toast.LENGTH_SHORT).show();
            etCode.setText("");
        }
    }
}