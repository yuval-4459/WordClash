package com.example.wordclash.screens;

import static com.example.wordclash.R.*;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wordclash.R;
import com.example.wordclash.utils.SharedPreferencesUtils;

public class StartPageActivity extends AppCompatActivity {
    Button btnSignUp;
    Button btnLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.start_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (SharedPreferencesUtils.isUserLoggedIn(StartPageActivity.this)) {
            Intent intent = new Intent(StartPageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        btnSignUp = findViewById(R.id.signUpButton);
        btnSignUp.setOnClickListener(view -> startActivity(new Intent(StartPageActivity.this, SignUpActivity.class)));

        btnLogin = findViewById(R.id.LoginButton);
        btnLogin.setOnClickListener(view -> startActivity(new Intent(StartPageActivity.this, LoginActivity.class)));

    }
}