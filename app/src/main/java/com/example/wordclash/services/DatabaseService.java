package com.example.wordclash.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wordclash.models.Stats;
import com.example.wordclash.models.User;
import com.example.wordclash.models.Word;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class DatabaseService {

    private static final String TAG = "DatabaseService";
    private static final String USERS_PATH = "users";
    private static final String STATS_PATH = "stats";
    private static final String WORDS_PATH = "vocabulary";
    private static final String RANK_PROGRESS_PATH = "rank_progress"; // NEW

    public interface DatabaseCallback<T> {
        public void onCompleted(T object);
        public void onFailed(Exception e);
    }

    private static DatabaseService instance;
    private final DatabaseReference databaseReference;

    private DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    // region private generic methods

    private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        readData(path).setValue(data, (error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    private void deleteData(@NotNull final String path, @Nullable final DatabaseCallback<Void> callback) {
        readData(path).removeValue((error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    private DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }

    private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            T data = task.getResult().getValue(clazz);
            callback.onCompleted(data);
        });
    }

    private <T> void getDataList(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<List<T>> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<T> tList = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                T t = dataSnapshot.getValue(clazz);
                tList.add(t);
            });
            callback.onCompleted(tList);
        });
    }

    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    private <T> void runTransaction(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull UnaryOperator<T> function, @NotNull final DatabaseCallback<T> callback) {
        readData(path).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                T currentValue = currentData.getValue(clazz);
                if (currentValue == null) {
                    currentValue = function.apply(null);
                } else {
                    currentValue = function.apply(currentValue);
                }
                currentData.setValue(currentValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed", error.toException());
                    callback.onFailed(error.toException());
                    return;
                }
                T result = currentData != null ? currentData.getValue(clazz) : null;
                callback.onCompleted(result);
            }
        });
    }

    // endregion

    // region User Section

    public String generateUserId() {
        return generateNewId(USERS_PATH);
    }

    public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData(USERS_PATH + "/" + user.getId(), user, callback);
    }

    public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
        getData(USERS_PATH + "/" + uid, User.class, callback);
    }

    public void getUserList(@NotNull final DatabaseCallback<List<User>> callback) {
        getDataList(USERS_PATH, User.class, callback);
    }

    public void deleteUser(@NotNull final String uid, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(USERS_PATH + "/" + uid, callback);
    }

    public void login(@NotNull final String email, @NotNull final String password, @NotNull final DatabaseCallback<User> callback) {
        getUserList(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (Objects.equals(user.getEmail(), email) && Objects.equals(user.getPassword(), password)) {
                        callback.onCompleted(user);
                        return;
                    }
                }
                callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    public void checkIfEmailExists(@NotNull final String email, @NotNull final DatabaseCallback<Boolean> callback) {
        getUserList(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (Objects.equals(user.getEmail(), email)) {
                        callback.onCompleted(true);
                        return;
                    }
                }
                callback.onCompleted(false);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    public void updateUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        runTransaction(USERS_PATH + "/" + user.getId(), User.class, currentUser -> user, new DatabaseCallback<User>() {
            @Override
            public void onCompleted(User object) {
                if (callback != null) {
                    callback.onCompleted(null);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) {
                    callback.onFailed(e);
                }
            }
        });
    }

    // endregion

    // region Stats Section

    public void getStats(@NotNull final String userId, @NotNull final DatabaseCallback<Stats> callback) {
        getData(STATS_PATH + "/" + userId, Stats.class, callback);
    }

    public void createStats(@NotNull final Stats stats, @Nullable final DatabaseCallback<Void> callback) {
        writeData(STATS_PATH + "/" + stats.getUserId(), stats, callback);
    }

    public void updateStats(@NotNull final Stats stats, @Nullable final DatabaseCallback<Void> callback) {
        writeData(STATS_PATH + "/" + stats.getUserId(), stats, callback);
    }

    /**
     * SAFE: If stats node was deleted, recreate defaults and return it.
     */
    public void getStatsSafe(@NotNull final String userId, @NotNull final DatabaseCallback<Stats> callback) {
        getStats(userId, new DatabaseCallback<Stats>() {
            @Override
            public void onCompleted(Stats loaded) {
                if (loaded == null) {
                    Stats s = new Stats(userId, 1, 0);
                    createStats(s, null);
                    callback.onCompleted(s);
                } else {
                    callback.onCompleted(loaded);
                }
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    /**
     * Update rank in: stats/{userId}/rank
     */
    public void updateCurrentRank(@NotNull final String userId, int newRank, @Nullable final DatabaseCallback<Void> callback) {
        writeData(STATS_PATH + "/" + userId + "/rank", newRank, callback);
    }

    /**
     * Update totalScore in: stats/{userId}/totalScore
     */
    public void updateTotalScore(@NotNull final String userId, int newTotalScore, @Nullable final DatabaseCallback<Void> callback) {
        writeData(STATS_PATH + "/" + userId + "/totalScore", newTotalScore, callback);
    }

    // endregion

    // region Words Section

    public void getWordsByRank(int rank, @NotNull final DatabaseCallback<List<Word>> callback) {
        String levelPath = WORDS_PATH + "/level" + rank;

        readData(levelPath).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onFailed(task.getException());
                return;
            }

            List<Word> list = new ArrayList<>();

            for (DataSnapshot s : task.getResult().getChildren()) {

                String id = s.getKey(); // ✅ חשוב! מזהה ייחודי
                String en = s.child("en").getValue(String.class);
                String he = s.child("he").getValue(String.class);

                if (id != null && en != null && he != null) {
                    Word w = new Word();
                    w.setId(id);          //  זה מה שימנע לי את הקריסה
                    w.setEnglish(en);
                    w.setHebrew(he);
                    w.setRank(rank);
                    list.add(w);
                }
            }

            callback.onCompleted(list);
        });
    }

    public void createWord(@NotNull final Word word, @Nullable final DatabaseCallback<Void> callback) {
        writeData(WORDS_PATH + "/level" + word.getRank() + "/" + word.getId(), word, callback);
    }

    /**
     * Public helper for admin add-word: generates a key under vocabulary/level{rank}
     */
    public String generateWordId(int rank) {
        return generateNewId(WORDS_PATH + "/level" + rank);
    }

    /**
     * Get all 5-letter words from ALL ranks for Wordle game
     */
    public void getAllFiveLetterWords(@NotNull final DatabaseCallback<List<Word>> callback) {
        DatabaseReference wordsRef = databaseReference.child(WORDS_PATH);

        wordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Word> fiveLetterWords = new ArrayList<>();

                // Loop through all rank nodes (level1, level2, level3, level4, level5)
                for (DataSnapshot levelSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot wordSnapshot : levelSnapshot.getChildren()) {

                        String en = wordSnapshot.child("en").getValue(String.class);
                        if (en != null) {
                            en = en.trim();
                            if (en.length() == 5) {
                                Word w = new Word();
                                w.setEnglish(en);
                                fiveLetterWords.add(w);
                            }
                        }
                    }
                }

                callback.onCompleted(fiveLetterWords);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailed(error.toException());
            }
        });
    }

    // endregion

    // region Rank Progress Section - NEW ADDITION

    /**
     * Simple data holder for rank-specific progress
     */
    public static class RankProgressData {
        public int practiceCount;
        public boolean hasReviewedWords;

        public RankProgressData() {
            this.practiceCount = 0;
            this.hasReviewedWords = false;
        }
    }

    /**
     * Get progress for specific rank
     */
    public void getRankProgress(@NotNull final String userId, int rank, @NotNull final DatabaseCallback<RankProgressData> callback) {
        String path = RANK_PROGRESS_PATH + "/" + userId + "/rank_" + rank;
        getData(path, RankProgressData.class, callback);
    }

    /**
     * SAFE: If rank node was deleted, recreate defaults and return it.
     */
    public void getRankProgressSafe(@NotNull final String userId, int rank, @NotNull final DatabaseCallback<RankProgressData> callback) {
        getRankProgress(userId, rank, new DatabaseCallback<RankProgressData>() {
            @Override
            public void onCompleted(RankProgressData data) {
                if (data == null) {
                    RankProgressData d = new RankProgressData();
                    updateRankProgress(userId, rank, d, null);
                    callback.onCompleted(d);
                } else {
                    callback.onCompleted(data);
                }
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    /**
     * Update progress for specific rank
     */
    public void updateRankProgress(@NotNull final String userId, int rank, @NotNull final RankProgressData progress, @Nullable final DatabaseCallback<Void> callback) {
        String path = RANK_PROGRESS_PATH + "/" + userId + "/rank_" + rank;
        writeData(path, progress, callback);
    }

    /**
     * Mark words as reviewed for specific rank
     */
    public void markWordsReviewedForRank(@NotNull final String userId, int rank, @Nullable final DatabaseCallback<Void> callback) {
        getRankProgress(userId, rank, new DatabaseCallback<RankProgressData>() {
            @Override
            public void onCompleted(RankProgressData data) {
                if (data == null) {
                    data = new RankProgressData();
                }
                data.hasReviewedWords = true;
                updateRankProgress(userId, rank, data, callback);
            }

            @Override
            public void onFailed(Exception e) {
                RankProgressData data = new RankProgressData();
                data.hasReviewedWords = true;
                updateRankProgress(userId, rank, data, callback);
            }
        });
    }

    /**
     * Increment practice count for specific rank
     */
    public void incrementPracticeForRank(@NotNull final String userId, int rank, @Nullable final DatabaseCallback<Void> callback) {
        getRankProgress(userId, rank, new DatabaseCallback<RankProgressData>() {
            @Override
            public void onCompleted(RankProgressData data) {
                if (data == null) {
                    data = new RankProgressData();
                }
                data.practiceCount++;
                updateRankProgress(userId, rank, data, callback);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) {
                    callback.onFailed(e);
                }
            }
        });
    }

    // endregion

    // region Helpers (used by UI)

    public static int getRequiredPracticeCount(int rank) {
        switch (rank) {
            case 1: return 15;
            case 2: return 25;
            case 3: return 40;
            case 4: return 60;
            case 5: return Integer.MAX_VALUE;
            default: return 15;
        }
    }

    public static int getQuestionsPerPractice(int rank) {
        switch (rank) {
            case 1: return 10;
            case 2: return 13;
            case 3: return 16;
            case 4: return 20;
            case 5: return 25;
            default: return 10;
        }
    }

    // endregion
}
