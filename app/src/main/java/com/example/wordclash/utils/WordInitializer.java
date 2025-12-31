package com.example.wordclash.utils;

import com.example.wordclash.models.Word;
import com.example.wordclash.services.DatabaseService;

/**
 * Helper class to initialize sample words in Firebase
 * THIS IS TEMPORARY - Replace with your real words from PDF
 */
public class WordInitializer {

    public static void initializeSampleWords() {
        // Rank 1 - Basic words
        String[][] rank1 = {
                {"hello", "שלום"},
                {"world", "עולם"},
                {"water", "מים"},
                {"house", "בית"},
                {"phone", "טלפון"},
                {"table", "שולחן"},
                {"chair", "כיסא"},
                {"book", "ספר"},
                {"door", "דלת"},
                {"window", "חלון"},
                {"happy", "שמח"},
                {"sad", "עצוב"},
                {"big", "גדול"},
                {"small", "קטן"},
                {"good", "טוב"}
        };

        // Rank 2 - Intermediate words
        String[][] rank2 = {
                {"computer", "מחשב"},
                {"school", "בית ספר"},
                {"teacher", "מורה"},
                {"student", "תלמיד"},
                {"friend", "חבר"},
                {"family", "משפחה"},
                {"mother", "אמא"},
                {"father", "אבא"},
                {"brother", "אח"},
                {"sister", "אחות"},
                {"weather", "מזג אוויר"},
                {"summer", "קיץ"},
                {"winter", "חורף"},
                {"spring", "אביב"},
                {"autumn", "סתיו"}
        };

        // Rank 3 - Advanced words
        String[][] rank3 = {
                {"university", "אוניברסיטה"},
                {"education", "חינוך"},
                {"knowledge", "ידע"},
                {"information", "מידע"},
                {"technology", "טכנולוגיה"},
                {"science", "מדע"},
                {"mathematics", "מתמטיקה"},
                {"history", "היסטוריה"},
                {"geography", "גיאוגרפיה"},
                {"biology", "ביולוגיה"},
                {"chemistry", "כימיה"},
                {"physics", "פיזיקה"},
                {"language", "שפה"},
                {"literature", "ספרות"},
                {"philosophy", "פילוסופיה"}
        };

        // Rank 4 - Professional words
        String[][] rank4 = {
                {"management", "ניהול"},
                {"economy", "כלכלה"},
                {"business", "עסקים"},
                {"marketing", "שיווק"},
                {"strategy", "אסטרטגיה"},
                {"leadership", "מנהיגות"},
                {"innovation", "חדשנות"},
                {"development", "פיתוח"},
                {"research", "מחקר"},
                {"analysis", "ניתוח"},
                {"solution", "פתרון"},
                {"problem", "בעיה"},
                {"opportunity", "הזדמנות"},
                {"challenge", "אתגר"},
                {"success", "הצלחה"}
        };

        // Rank 5 - Expert words
        String[][] rank5 = {
                {"sophisticated", "מתוחכם"},
                {"comprehensive", "מקיף"},
                {"extraordinary", "יוצא דופן"},
                {"theoretical", "תיאורטי"},
                {"practical", "מעשי"},
                {"fundamental", "יסודי"},
                {"essential", "חיוני"},
                {"significant", "משמעותי"},
                {"considerable", "ניכר"},
                {"substantial", "משמעותי"},
                {"remarkable", "יוצא דופן"},
                {"exceptional", "חריג"},
                {"outstanding", "מצוין"},
                {"magnificent", "נפלא"},
                {"brilliant", "מבריק"}
        };

        // Upload to Firebase
        uploadWords(rank1, 1);
        uploadWords(rank2, 2);
        uploadWords(rank3, 3);
        uploadWords(rank4, 4);
        uploadWords(rank5, 5);
    }

    private static void uploadWords(String[][] words, int rank) {
        DatabaseService db = DatabaseService.getInstance();

        for (String[] pair : words) {
            String id = "word_" + rank + "_" + pair[0];
            Word word = new Word(id, pair[0], pair[1], rank);

            db.createWord(word, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void unused) {
                    // Word uploaded successfully
                }

                @Override
                public void onFailed(Exception e) {
                    // Failed to upload word
                }
            });
        }
    }
}