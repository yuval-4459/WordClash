package com.example.wordclash.screens;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.Comparator;
import java.util.List;

public class AdminManageWordsActivity extends AppCompatActivity {

    private RecyclerView rvWords;
    private AdminWordAdapter wordAdapter;
    private Button btnBack;
    private EditText etSearch;
    private Spinner spinnerSort, spinnerRankFilter;

    private List<Word> allWords = new ArrayList<>();
    private List<Word> filteredWords = new ArrayList<>();
    private String currentSortOption = "Random";
    private int currentRankFilter = 0; // 0 = All ranks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_words);

        // Check if user is admin
        if (!SharedPreferencesUtils.getUser(this).isAdmin()) {
            Toast.makeText(this, "Access denied - Admin only", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupSpinners();
        setupSearchListener();
        loadWords();
    }

    private void initializeViews() {
        rvWords = findViewById(R.id.rvWords);
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        spinnerSort = findViewById(R.id.spinnerSort);
        spinnerRankFilter = findViewById(R.id.spinnerRankFilter);

        rvWords.setLayoutManager(new LinearLayoutManager(this));
        wordAdapter = new AdminWordAdapter(word -> confirmDelete(word));
        rvWords.setAdapter(wordAdapter);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        // Sort spinner
        String[] sortOptions = {"Random", "A-Z (English)", "א-ב (Hebrew)", "Rank"};
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
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Rank filter spinner
        String[] rankOptions = {"All Ranks", "Rank 1", "Rank 2", "Rank 3", "Rank 4", "Rank 5"};
        ArrayAdapter<String> rankAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, rankOptions);
        rankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRankFilter.setAdapter(rankAdapter);

        spinnerRankFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentRankFilter = position; // 0 = All, 1-5 = specific ranks
                applyFiltersAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFiltersAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadWords() {
        DatabaseService.getInstance().getAllWords(new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(AdminManageWordsActivity.this, "No words found", Toast.LENGTH_SHORT).show();
                    return;
                }
                allWords = new ArrayList<>(words);
                applyFiltersAndSort();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminManageWordsActivity.this, "Failed to load words: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyFiltersAndSort() {
        String searchQuery = etSearch.getText().toString().trim().toLowerCase();

        // Filter by search and rank
        filteredWords.clear();
        for (Word word : allWords) {
            // Filter by rank
            if (currentRankFilter != 0 && word.getRank() != currentRankFilter) {
                continue;
            }

            // Filter by search query
            if (!searchQuery.isEmpty()) {
                boolean matchesEnglish = word.getEnglish() != null &&
                        word.getEnglish().toLowerCase().contains(searchQuery);
                boolean matchesHebrew = word.getHebrew() != null &&
                        word.getHebrew().contains(searchQuery);

                if (!matchesEnglish && !matchesHebrew) {
                    continue;
                }
            }

            filteredWords.add(word);
        }

        // Sort based on selected option
        switch (currentSortOption) {
            case "Random":
                Collections.shuffle(filteredWords);
                break;

            case "A-Z (English)":
                Collections.sort(filteredWords, new Comparator<Word>() {
                    @Override
                    public int compare(Word w1, Word w2) {
                        String en1 = w1.getEnglish() != null ? w1.getEnglish().toLowerCase() : "";
                        String en2 = w2.getEnglish() != null ? w2.getEnglish().toLowerCase() : "";
                        return en1.compareTo(en2);
                    }
                });
                break;

            case "א-ב (Hebrew)":
                Collections.sort(filteredWords, new Comparator<Word>() {
                    @Override
                    public int compare(Word w1, Word w2) {
                        String he1 = w1.getHebrew() != null ? w1.getHebrew() : "";
                        String he2 = w2.getHebrew() != null ? w2.getHebrew() : "";
                        return he1.compareTo(he2);
                    }
                });
                break;

            case "Rank":
                Collections.sort(filteredWords, new Comparator<Word>() {
                    @Override
                    public int compare(Word w1, Word w2) {
                        return Integer.compare(w1.getRank(), w2.getRank());
                    }
                });
                break;
        }

        // Update adapter
        wordAdapter.setWordList(filteredWords);
    }

    private void confirmDelete(Word word) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Word")
                .setMessage("Are you sure you want to delete:\n\n" +
                        word.getEnglish() + " - " + word.getHebrew() +
                        "\n\n(Rank " + word.getRank() + ")")
                .setPositiveButton("Delete", (dialog, which) -> deleteWord(word))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteWord(Word word) {
        DatabaseService.getInstance().deleteWord(word, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void unused) {
                Toast.makeText(AdminManageWordsActivity.this,
                        "Word deleted successfully",
                        Toast.LENGTH_SHORT).show();

                // Remove from local lists
                allWords.remove(word);
                wordAdapter.removeWord(word);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminManageWordsActivity.this,
                        "Failed to delete word: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}