package com.example.wordclash.screens;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordclash.R;
import com.example.wordclash.adapters.AdminWordAdapter;
import com.example.wordclash.models.Word;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminDeleteWordActivity extends AppCompatActivity {

    private final List<Word> filteredWords = new ArrayList<>();
    private AdminWordAdapter wordAdapter;
    private EditText etSearch;
    private Spinner spinnerSort, spinnerRankFilter;
    private List<Word> allWords = new ArrayList<>();
    private String currentSortOption = "Random";
    private int currentRankFilter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_words);

        if (!SharedPreferencesUtils.getUser(this).isAdmin()) {
            Toast.makeText(this, getString(R.string.admin_access_denied), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupSpinners();
        setupSearchListener();
        loadWords();
    }

    private void initializeViews() {
        RecyclerView rvWords = findViewById(R.id.rvWords);
        // No back button — use system back gesture
        etSearch = findViewById(R.id.etSearch);
        spinnerSort = findViewById(R.id.spinnerSort);
        spinnerRankFilter = findViewById(R.id.spinnerRankFilter);

        rvWords.setLayoutManager(new LinearLayoutManager(this));
        wordAdapter = new AdminWordAdapter(this::confirmDelete);
        rvWords.setAdapter(wordAdapter);
    }

    private void setupSpinners() {
        String[] sortOptions = {
                getString(R.string.sort_random),
                getString(R.string.sort_english),
                getString(R.string.sort_hebrew),
                getString(R.string.sort_rank)
        };
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortOption = parent.getItemAtPosition(position).toString();
                applyFiltersAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        String[] rankOptions = {
                getString(R.string.all_ranks),
                getString(R.string.rank, 1),
                getString(R.string.rank, 2),
                getString(R.string.rank, 3),
                getString(R.string.rank, 4),
                getString(R.string.rank, 5)
        };
        ArrayAdapter<String> rankAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, rankOptions);
        rankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRankFilter.setAdapter(rankAdapter);

        spinnerRankFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentRankFilter = position;
                applyFiltersAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFiltersAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(AdminDeleteWordActivity.this, getString(R.string.no_words_found), Toast.LENGTH_SHORT).show();
                    return;
                }
                allWords = new ArrayList<>(words);
                applyFiltersAndSort();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminDeleteWordActivity.this, getString(R.string.failed_load_words, e.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyFiltersAndSort() {
        String searchQuery = etSearch.getText().toString().trim().toLowerCase();

        filteredWords.clear();
        for (Word word : allWords) {
            if (currentRankFilter != 0 && word.getRank() != currentRankFilter) continue;

            if (!searchQuery.isEmpty()) {
                boolean matchesEnglish = word.getEnglish() != null &&
                        word.getEnglish().toLowerCase().contains(searchQuery);
                boolean matchesHebrew = word.getHebrew() != null &&
                        word.getHebrew().contains(searchQuery);
                if (!matchesEnglish && !matchesHebrew) continue;
            }

            filteredWords.add(word);
        }

        if (currentSortOption.equals(getString(R.string.sort_random))) {
            Collections.shuffle(filteredWords);
        } else if (currentSortOption.equals(getString(R.string.sort_english))) {
            filteredWords.sort((w1, w2) -> {
                String en1 = w1.getEnglish() != null ? w1.getEnglish().toLowerCase() : "";
                String en2 = w2.getEnglish() != null ? w2.getEnglish().toLowerCase() : "";
                return en1.compareTo(en2);
            });
        } else if (currentSortOption.equals(getString(R.string.sort_hebrew))) {
            filteredWords.sort((w1, w2) -> {
                String he1 = w1.getHebrew() != null ? w1.getHebrew() : "";
                String he2 = w2.getHebrew() != null ? w2.getHebrew() : "";
                return he1.compareTo(he2);
            });
        } else if (currentSortOption.equals(getString(R.string.sort_rank))) {
            filteredWords.sort((w1, w2) -> Integer.compare(w1.getRank(), w2.getRank()));
        }

        wordAdapter.setWordList(filteredWords);
    }

    private void confirmDelete(Word word) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_word_title)
                .setMessage(getString(R.string.delete_word_msg, word.getEnglish(), word.getHebrew(), word.getRank()))
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteWord(word))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteWord(Word word) {
        DatabaseService.getInstance().deleteWord(word, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void unused) {
                Toast.makeText(AdminDeleteWordActivity.this,
                        getString(R.string.word_deleted_success),
                        Toast.LENGTH_SHORT).show();
                allWords.remove(word);
                wordAdapter.removeWord(word);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminDeleteWordActivity.this,
                        getString(R.string.word_delete_failed, e.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}