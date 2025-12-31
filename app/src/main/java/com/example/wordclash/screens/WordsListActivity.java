package com.example.wordclash.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordclash.R;
import com.example.wordclash.adapters.WordAdapter;
import com.example.wordclash.models.User;
import com.example.wordclash.models.Word;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.util.List;

/**
 * Words list screen with floating "I'm Ready" button
 * FIXED: Now marks words as reviewed for THIS specific rank
 */
public class WordsListActivity extends AppCompatActivity {

    private RecyclerView rvWords;
    private WordAdapter wordAdapter;
    private Button btnReady;
    private TextView tvTitle;

    private User user;
    private int currentRank;

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
        loadWords();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvWordsTitle);
        rvWords = findViewById(R.id.rvWords);
        btnReady = findViewById(R.id.btnReady);

        tvTitle.setText("Rank " + currentRank + " Vocabulary");

        rvWords.setLayoutManager(new LinearLayoutManager(this));
        wordAdapter = new WordAdapter();
        rvWords.setAdapter(wordAdapter);

        // Scroll listener for floating button
        rvWords.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    // Scrolling down - hide button
                    if (btnReady.getVisibility() == View.VISIBLE) {
                        btnReady.animate()
                                .translationY(-btnReady.getHeight())
                                .alpha(0.0f)
                                .setDuration(300)
                                .withEndAction(() -> btnReady.setVisibility(View.GONE));
                    }
                } else if (dy < 0) {
                    // Scrolling up - show button
                    if (btnReady.getVisibility() == View.GONE) {
                        btnReady.setVisibility(View.VISIBLE);
                        btnReady.animate()
                                .translationY(0)
                                .alpha(1.0f)
                                .setDuration(300);
                    }
                }
            }
        });

        btnReady.setOnClickListener(v -> markAsReviewed());
    }

    private void loadWords() {
        DatabaseService.getInstance().getWordsByRank(currentRank, new DatabaseService.DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> words) {
                if (words == null || words.isEmpty()) {
                    Toast.makeText(WordsListActivity.this, "No words found for this rank", Toast.LENGTH_SHORT).show();
                    return;
                }
                wordAdapter.setWordList(words);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(WordsListActivity.this, "Failed to load words: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void markAsReviewed() {
        // Mark words as reviewed for THIS SPECIFIC RANK
        DatabaseService.getInstance().markWordsReviewedForRank(user.getId(), currentRank, new DatabaseService.DatabaseCallback<Void>() {
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