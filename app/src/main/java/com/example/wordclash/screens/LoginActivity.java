package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;



public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnConfirm;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);



        // same IDs as signup
        etEmail = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        btnConfirm = findViewById(R.id.ConfirmsignUpButton); // re-used as Confirm/Login button

        btnConfirm.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }


    }
}
