package com.example.wordclash.screens;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.Word;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

/**
 * Admin screen to add new words to the vocabulary
 */
public class AdminAddWordActivity extends AppCompatActivity {

    private EditText etEnglish, etHebrew;
    private Spinner spinnerRank;
    private Button btnAddWord, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_word);

        // Check if user is admin
        if (!SharedPreferencesUtils.getUser(this).isAdmin()) {
            Toast.makeText(this, "Access denied - Admin only", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupRankSpinner();
    }

    private void initializeViews() {
        etEnglish = findViewById(R.id.etEnglishWord);
        etHebrew = findViewById(R.id.etHebrewWord);
        spinnerRank = findViewById(R.id.spinnerWordRank);
        btnAddWord = findViewById(R.id.btnAddWord);
        btnBack = findViewById(R.id.btnBack);

        btnAddWord.setOnClickListener(v -> addWord());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRankSpinner() {
        String[] ranks = {"1", "2", "3", "4", "5"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ranks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRank.setAdapter(adapter);
    }

    private void addWord() {
        String english = etEnglish.getText().toString().trim();
        String hebrew = etHebrew.getText().toString().trim();

        if (english.isEmpty() || hebrew.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int rank = Integer.parseInt(spinnerRank.getSelectedItem().toString());

        // Generate unique ID for the word (public wrapper, not private generateNewId)
        String wordId = DatabaseService.getInstance().generateWordId(rank);

        Word word = new Word(wordId, english, hebrew, rank);

        DatabaseService.getInstance().createWord(word, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void unused) {
                Toast.makeText(AdminAddWordActivity.this,
                        "Word added successfully!",
                        Toast.LENGTH_SHORT).show();

                // Clear fields
                etEnglish.setText("");
                etHebrew.setText("");
                spinnerRank.setSelection(0);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminAddWordActivity.this,
                        "Failed to add word: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
