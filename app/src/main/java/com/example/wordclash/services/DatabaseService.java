package com.example.wordclash.services;

import com.example.wordclash.models.Stats;
import com.example.wordclash.models.User;
import com.example.wordclash.models.Word;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * שירות לניהול Firebase Database
 * כל הפעולות על המשתמשים, מילים וסטטיסטיקות
 */
public class DatabaseService {

    // ========== חלק 1: הגדרות בסיסיות ==========

    private static final String USERS_PATH = "users";           // נתיב למשתמשים
    private static final String STATS_PATH = "stats";           // נתיב לסטטיסטיקות
    private static final String WORDS_PATH = "vocabulary";      // נתיב למילים
    private static final String RANK_PROGRESS = "rank_progress"; // נתיב להתקדמות
    /**
     * כמה תרגולים נדרשים לעבור דרגה
     */
    private static final Map<Integer, Integer> RANK_REQUIREMENTS = new HashMap<>();
    private static DatabaseService instance;                    // סינגלטון

    static {
        RANK_REQUIREMENTS.put(1, 15);
        RANK_REQUIREMENTS.put(2, 25);
        RANK_REQUIREMENTS.put(3, 40);
        RANK_REQUIREMENTS.put(4, 60);
        RANK_REQUIREMENTS.put(5, Integer.MAX_VALUE);
    }

    private final DatabaseReference db;                         // חיבור ל-Firebase

    // ========== חלק 2: ממשק Callback ==========

    // קונסטרוקטור פרטי (סינגלטון)
    private DatabaseService() {
        db = FirebaseDatabase.getInstance().getReference();
    }

    // ========== חלק 3: משתמשים (Users) ==========

    // קבלת המופע היחיד של השירות
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public static int getRequiredPracticeCount(int rank) {
        return RANK_REQUIREMENTS.getOrDefault(rank, 15);
    }

    /**
     * כמה שאלות בכל תרגול
     */
    public static int getQuestionsCount(int rank) {
        switch (rank) {
            case 1:
                return 10;
            case 2:
                return 13;
            case 3:
                return 16;
            case 4:
                return 20;
            case 5:
                return 25;
            default:
                return 10;
        }
    }

    /**
     * יצירת מזהה חדש למשתמש
     */
    public String generateUserId() {
        return db.child(USERS_PATH).push().getKey();
    }

    /**
     * יצירת משתמש חדש
     */
    public void createNewUser(User user, DatabaseCallback<Void> callback) {
        db.child(USERS_PATH).child(user.getId()).setValue(user)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onCompleted(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailed(e);
                });
    }

    /**
     * קבלת משתמש לפי ID
     */
    public void getUser(String userId, DatabaseCallback<User> callback) {
        db.child(USERS_PATH).child(userId).get()
                .addOnSuccessListener(snapshot -> {
                    User user = snapshot.getValue(User.class);
                    callback.onCompleted(user);
                })
                .addOnFailureListener(callback::onFailed);
    }

    /**
     * קבלת כל המשתמשים
     */
    public void getUserList(DatabaseCallback<List<User>> callback) {
        db.child(USERS_PATH).get()
                .addOnSuccessListener(snapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        User user = child.getValue(User.class);
                        if (user != null) users.add(user);
                    }
                    callback.onCompleted(users);
                })
                .addOnFailureListener(callback::onFailed);
    }

    /**
     * עדכון משתמש
     */
    public void updateUser(User user, DatabaseCallback<Void> callback) {
        db.child(USERS_PATH).child(user.getId()).setValue(user)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onCompleted(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailed(e);
                });
    }

    // ========== חלק 4: סטטיסטיקות (Stats) ==========

    /**
     * מחיקת משתמש
     */
    public void deleteUser(String userId, DatabaseCallback<Void> callback) {
        db.child(USERS_PATH).child(userId).removeValue()
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onCompleted(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailed(e);
                });
    }

    /**
     * התחברות למערכת
     */
    public void login(String email, String password, DatabaseCallback<User> callback) {
        getUserList(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                // חיפוש משתמש עם אימייל וסיסמה תואמים
                for (User user : users) {
                    if (user.getEmail().equals(email) &&
                            user.getPassword().equals(password)) {
                        callback.onCompleted(user);
                        return;
                    }
                }
                callback.onCompleted(null); // לא נמצא
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    /**
     * בדיקה אם אימייל קיים
     */
    public void checkIfEmailExists(String email, DatabaseCallback<Boolean> callback) {
        getUserList(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (user.getEmail().equals(email)) {
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

    // ========== חלק 5: מילים (Words) ==========

    /**
     * יצירת סטטיסטיקה חדשה למשתמש
     */
    public void createStats(Stats stats, DatabaseCallback<Void> callback) {
        db.child(STATS_PATH).child(stats.getUserId()).setValue(stats)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onCompleted(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailed(e);
                });
    }

    /**
     * קבלת סטטיסטיקות של משתמש
     */
    public void getStats(String userId, DatabaseCallback<Stats> callback) {
        db.child(STATS_PATH).child(userId).get()
                .addOnSuccessListener(snapshot -> {
                    Stats stats = snapshot.getValue(Stats.class);
                    callback.onCompleted(stats);
                })
                .addOnFailureListener(callback::onFailed);
    }

    /**
     * עדכון סטטיסטיקות
     */
    public void updateStats(Stats stats, DatabaseCallback<Void> callback) {
        db.child(STATS_PATH).child(stats.getUserId()).setValue(stats)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onCompleted(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailed(e);
                });
    }

    /**
     * יצירת מזהה חדש למילה
     */
    public String generateWordId(int rank) {
        return db.child(WORDS_PATH).child("level" + rank).push().getKey();
    }

    /**
     * הוספת מילה חדשה
     */
    public void createWord(Word word, DatabaseCallback<Void> callback) {
        HashMap<String, String> wordData = new HashMap<>();
        wordData.put("en", word.getEnglish());
        wordData.put("he", word.getHebrew());

        String path = WORDS_PATH + "/level" + word.getRank() + "/" + word.getId();
        db.child(path).setValue(wordData)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onCompleted(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailed(e);
                });
    }

    /**
     * קבלת מילים לפי דרגה
     */
    public void getWordsByRank(int rank, DatabaseCallback<List<Word>> callback) {
        db.child(WORDS_PATH).child("level" + rank).get()
                .addOnSuccessListener(snapshot -> {
                    List<Word> words = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String id = child.getKey();
                        String en = child.child("en").getValue(String.class);
                        String he = child.child("he").getValue(String.class);

                        if (id != null && en != null && he != null) {
                            Word word = new Word(id, en, he, rank);
                            words.add(word);
                        }
                    }
                    callback.onCompleted(words);
                })
                .addOnFailureListener(callback::onFailed);
    }

    // ========== חלק 6: התקדמות בדרגות (Rank Progress) ==========

    /**
     * קבלת כל המילים
     */
    public void getAllWords(DatabaseCallback<List<Word>> callback) {
        db.child(WORDS_PATH).get()
                .addOnSuccessListener(snapshot -> {
                    List<Word> allWords = new ArrayList<>();

                    // עבור על כל הרמות (level1, level2, ...)
                    for (DataSnapshot levelSnapshot : snapshot.getChildren()) {
                        String levelKey = levelSnapshot.getKey();
                        if (levelKey == null) continue;

                        // חילוץ מספר הדרגה
                        int rank = Integer.parseInt(levelKey.replace("level", ""));

                        // עבור על כל המילים ברמה
                        for (DataSnapshot wordSnapshot : levelSnapshot.getChildren()) {
                            String id = wordSnapshot.getKey();
                            String en = wordSnapshot.child("en").getValue(String.class);
                            String he = wordSnapshot.child("he").getValue(String.class);

                            if (id != null && en != null && he != null) {
                                Word word = new Word(id, en, he, rank);
                                allWords.add(word);
                            }
                        }
                    }
                    callback.onCompleted(allWords);
                })
                .addOnFailureListener(callback::onFailed);
    }

    /**
     * מחיקת מילה
     */
    public void deleteWord(Word word, DatabaseCallback<Void> callback) {
        String path = WORDS_PATH + "/level" + word.getRank() + "/" + word.getId();
        db.child(path).removeValue()
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onCompleted(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailed(e);
                });
    }

    /**
     * קבלת מילים בעלות 5 אותיות (למשחק Wordle)
     */
    public void getAllFiveLetterWords(DatabaseCallback<List<Word>> callback) {
        getAllWords(new DatabaseCallback<List<Word>>() {
            @Override
            public void onCompleted(List<Word> allWords) {
                List<Word> fiveLetterWords = new ArrayList<>();
                for (Word word : allWords) {
                    if (word.getEnglish().trim().length() == 5) {
                        fiveLetterWords.add(word);
                    }
                }
                callback.onCompleted(fiveLetterWords);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    /**
     * קבלת התקדמות לדרגה מסוימת
     */
    public void getRankProgress(String userId, int rank, DatabaseCallback<RankProgressData> callback) {
        String path = RANK_PROGRESS + "/" + userId + "/rank_" + rank;
        db.child(path).get()
                .addOnSuccessListener(snapshot -> {
                    RankProgressData data = snapshot.getValue(RankProgressData.class);
                    callback.onCompleted(data);
                })
                .addOnFailureListener(callback::onFailed);
    }

    /**
     * קבלת התקדמות בצורה בטוחה (יוצר אם לא קיים)
     */
    public void getRankProgressSafe(String userId, int rank, DatabaseCallback<RankProgressData> callback) {
        getRankProgress(userId, rank, new DatabaseCallback<RankProgressData>() {
            @Override
            public void onCompleted(RankProgressData data) {
                if (data == null) {
                    data = new RankProgressData();
                    updateRankProgress(userId, rank, data, null);
                }
                callback.onCompleted(data);
            }

            @Override
            public void onFailed(Exception e) {
                // במקרה של שגיאה, החזר ערכים ברירת מחדל
                callback.onCompleted(new RankProgressData());
            }
        });
    }

    /**
     * עדכון התקדמות
     */
    public void updateRankProgress(String userId, int rank, RankProgressData data, DatabaseCallback<Void> callback) {
        String path = RANK_PROGRESS + "/" + userId + "/rank_" + rank;
        db.child(path).setValue(data)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onCompleted(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailed(e);
                });
    }

    // ========== חלק 7: פונקציות עזר ==========

    /**
     * סימון שהמשתמש סקר את המילים
     */
    public void markWordsReviewedForRank(String userId, int rank, DatabaseCallback<Void> callback) {
        getRankProgressSafe(userId, rank, new DatabaseCallback<RankProgressData>() {
            @Override
            public void onCompleted(RankProgressData data) {
                data.hasReviewedWords = true;
                updateRankProgress(userId, rank, data, callback);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }

    /**
     * הוספה למספר התרגולים
     */
    public void incrementPracticeForRank(String userId, int rank, DatabaseCallback<Void> callback) {
        getRankProgressSafe(userId, rank, new DatabaseCallback<RankProgressData>() {
            @Override
            public void onCompleted(RankProgressData data) {
                data.practiceCount++;
                updateRankProgress(userId, rank, data, callback);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }

    /**
     * ממשק לטיפול בתגובות מהדאטהבייס
     */
    public interface DatabaseCallback<T> {
        void onCompleted(T result);  // הצלחה

        void onFailed(Exception e);   // כשל
    }

    /**
     * מחלקה לנתוני התקדמות
     */
    public static class RankProgressData {
        public int practiceCount;         // כמה פעמים תרגל
        public boolean hasReviewedWords;  // האם סקר את המילים

        public RankProgressData() {
            this.practiceCount = 0;
            this.hasReviewedWords = false;
        }
    }
}