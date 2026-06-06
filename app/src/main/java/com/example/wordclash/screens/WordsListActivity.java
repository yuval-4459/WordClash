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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordclash.R;
import com.example.wordclash.adapters.WordAdapter;
import com.example.wordclash.models.User;
import com.example.wordclash.models.Word;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordsListActivity extends AppCompatActivity {

    private final List<Word> filteredWords = new ArrayList<>();
    private WordAdapter wordAdapter;
    private Button btnReady;
    private EditText etSearch;
    private Spinner spinnerSort;
    private User user;
    private int currentRank;
    private List<Word> allWords = new ArrayList<>();
    private String currentSortOption = "Random";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words_list);

        currentRank = getIntent().getIntExtra("RANK", 1);
        user = SharedPreferencesUtils.getUser(this);

        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupSortSpinner();
        setupSearchListener();
        loadWords();
    }

    private void initializeViews() {
        TextView tvTitle = findViewById(R.id.tvWordsTitle);
        RecyclerView rvWords = findViewById(R.id.rvWords);
        btnReady = findViewById(R.id.btnReady);
        etSearch = findViewById(R.id.etSearch);
        spinnerSort = findViewById(R.id.spinnerSort);

        tvTitle.setText("Rank " + currentRank + " Vocabulary");

        rvWords.setLayoutManager(new LinearLayoutManager(this));
        wordAdapter = new WordAdapter();
        rvWords.setAdapter(wordAdapter);

        // button is now anchored to the BOTTOM of the screen.
        // hide when scrolling down (user goes deeper into list, button obscures nothing important)
        // show when scrolling up (user comes back toward top — ready button should reappear)
        // this is the OPPOSITE of the previous logic where the button was at the top.
        rvWords.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int buttonState = 1; // 1 = visible, 0 = hidden

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && buttonState == 1) {
                    // scrolling down — hide the bottom button to see more list
                    buttonState = 0;
                    btnReady.animate().cancel();
                    btnReady.animate()
                            .translationY(btnReady.getHeight() + 64f)
                            .alpha(0.0f)
                            .setDuration(200)
                            .withEndAction(() -> btnReady.setVisibility(View.GONE))
                            .start();
                } else if (dy < 0 && buttonState == 0) {
                    // scrolling up — show the bottom button again
                    buttonState = 1;
                    btnReady.animate().cancel();
                    btnReady.setVisibility(View.VISIBLE);
                    btnReady.animate()
                            .translationY(0)
                            .alpha(1.0f)
                            .setDuration(200)
                            .start();
                }
            }
        });

        btnReady.setOnClickListener(v -> markAsReviewed());
    }

    private void setupSortSpinner() {
        String[] sortOptions = {"Random", "A-Z (English)", "א-ב (Hebrew)"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(android.graphics.Color.BLACK);
                    ((TextView) v).setTextSize(18);
                }
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(android.graphics.Color.WHITE);
                    ((TextView) v).setTextSize(18);
                }
                return v;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);

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
        DatabaseService.getInstance().getWordsByRank(currentRank, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(WordsListActivity.this, "No words found for this rank", Toast.LENGTH_SHORT).show();
                    return;
                }
                allWords = new ArrayList<>(words);
                applyFiltersAndSort();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(WordsListActivity.this, "Failed to load words: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyFiltersAndSort() {
        String searchQuery = etSearch.getText().toString().trim().toLowerCase();

        filteredWords.clear();
        if (searchQuery.isEmpty()) {
            filteredWords.addAll(allWords);
        } else {
            for (Word word : allWords) {
                boolean matchesEnglish = word.getEnglish() != null &&
                        word.getEnglish().toLowerCase().contains(searchQuery);
                boolean matchesHebrew = word.getHebrew() != null &&
                        word.getHebrew().contains(searchQuery);

                if (matchesEnglish || matchesHebrew) {
                    filteredWords.add(word);
                }
            }
        }

        switch (currentSortOption) {
            case "Random":
                Collections.shuffle(filteredWords);
                break;
            case "A-Z (English)":
                filteredWords.sort((w1, w2) -> {
                    String en1 = w1.getEnglish() != null ? w1.getEnglish().toLowerCase() : "";
                    String en2 = w2.getEnglish() != null ? w2.getEnglish().toLowerCase() : "";
                    return en1.compareTo(en2);
                });
                break;
            case "א-ב (Hebrew)":
                filteredWords.sort((w1, w2) -> {
                    String he1 = w1.getHebrew() != null ? w1.getHebrew() : "";
                    String he2 = w2.getHebrew() != null ? w2.getHebrew() : "";
                    return he1.compareTo(he2);
                });
                break;
        }

        wordAdapter.setWordList(filteredWords);
    }

    private void markAsReviewed() {
        DatabaseService.getInstance().markWordsReviewedForRank(user.getId(), currentRank, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void unused) {
                Toast.makeText(WordsListActivity.this, "You're ready to practice Rank " + currentRank + "!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(WordsListActivity.this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}