package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

/**
 * Language selection screen shown after first sign up
 * User chooses which language they want to LEARN
 */
public class LanguageSelectionActivity extends AppCompatActivity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        user = SharedPreferencesUtils.getUser(this);
        if (user == null) {
            finish();
            return;
        }

        initializeViews();
    }

    private void initializeViews() {
        TextView tvQuestion = findViewById(R.id.tvLanguageQuestion);
        CardView cardEnglish = findViewById(R.id.cardEnglish);
        CardView cardHebrew = findViewById(R.id.cardHebrew);
        Button btnEnglish = findViewById(R.id.btnEnglish);
        Button btnHebrew = findViewById(R.id.btnHebrew);

        tvQuestion.setText(R.string.language_question);

        btnEnglish.setOnClickListener(v -> selectLanguage("english"));
        btnHebrew.setOnClickListener(v -> selectLanguage("hebrew"));

        cardEnglish.setOnClickListener(v -> selectLanguage("english"));
        cardHebrew.setOnClickListener(v -> selectLanguage("hebrew"));
    }

    private void selectLanguage(String learningLanguage) {
        user.setLearningLanguage(learningLanguage);

        DatabaseService.getInstance().updateUser(user, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void unused) {
                SharedPreferencesUtils.saveUser(LanguageSelectionActivity.this, user);

                // show confirmation in Hebrew/English
                String message = learningLanguage.equals("english")
                        ? getString(R.string.choose_english) + "! " + getString(R.string.interface_hebrew)
                        : getString(R.string.choose_hebrew) + "! " + getString(R.string.interface_english);
                Toast.makeText(LanguageSelectionActivity.this, message, Toast.LENGTH_SHORT).show();

                // go to main activity
                Intent intent = new Intent(LanguageSelectionActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(LanguageSelectionActivity.this,
                        getString(R.string.error_saving) + ": " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}