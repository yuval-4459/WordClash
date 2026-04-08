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

        // Question is always in Hebrew (since this is first time)
        tvQuestion.setText("?איזו שפה תרצה ללמוד");

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

                // Show confirmation in Hebrew
                String message = learningLanguage.equals("english")
                        ? "בחרת ללמוד אנגלית! הממשק יהיה בעברית"
                        : "בחרת ללמוד עברית! הממשק יהיה באנגלית";
                Toast.makeText(LanguageSelectionActivity.this, message, Toast.LENGTH_SHORT).show();

                // Go to main activity
                Intent intent = new Intent(LanguageSelectionActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(LanguageSelectionActivity.this,
                        "שגיאה בשמירת העדפת שפה: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}