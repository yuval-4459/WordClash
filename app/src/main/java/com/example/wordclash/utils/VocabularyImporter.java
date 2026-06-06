package com.example.wordclash.utils;

import android.content.Context;
import android.util.Log;

import com.example.wordclash.models.Word;
import com.example.wordclash.services.DatabaseService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Utility class to import vocabulary from JSON file to Firebase
 * Run this ONCE when app first launches to populate the database
 */
// מחלקת עזר שתפקידה לקרוא קובץ JSON מקומי מתיקיית ה-assets של האפליקציה
// ולהעלות את כל מילון המילים הראשוני ל-Firebase באופן אוטומטי כשהאפליקציה מופעלת לראשונה.
public class VocabularyImporter {

    private static final String TAG = "VocabularyImporter";

    /**
     * Import all vocabulary from JSON file to Firebase
     * This should be called once to populate the database
     */
    // הפונקציה קוראת את קובץ ה-JSON שורה אחר שורה באמצעות InputStream ו-BufferedReader,
    // ואז מפרסרת (הופכת) אותו לאובייקטי תצוגה ומחלקת אותם לפי דרגות ליבוא לענן.
    public static void importVocabularyFromAssets(Context context) {
        try {
            // read JSON file from assets
            InputStream is = context.getAssets().open("vocabulary_realtime_db.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();


            // Parse JSON
            // שימוש בספריית Gson החיצונית של גוגל כדי להפוך את מחרוזת הטקסט הגולמית של הקובץ לאובייקט קוד מסוג JsonObject,
            // המאפשר לשלוף רמות ומילים בקלות לפי מפתחות.
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(jsonString.toString(), JsonObject.class);
            JsonObject vocabulary = root.getAsJsonObject("vocabulary");

            // Import each level
            importLevel(vocabulary.getAsJsonObject("level1"), 1);
            importLevel(vocabulary.getAsJsonObject("level2"), 2);
            importLevel(vocabulary.getAsJsonObject("level3"), 3);
            importLevel(vocabulary.getAsJsonObject("level4"), 4);
            importLevel(vocabulary.getAsJsonObject("level5"), 5);

            Log.d(TAG, "Vocabulary import completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error importing vocabulary", e);
        }
    }

    // הפונקציה מקבלת את נתוני המילים של רמה ספציפית, רצה בלולאה על כל המילים, מחלצת את השדות באנגלית ובעברית, ומייצרת מכל שורה אובייקט מסוג Word.
    private static void importLevel(JsonObject levelData, int rank) {
        if (levelData == null) return;

        for (Map.Entry<String, com.google.gson.JsonElement> entry : levelData.entrySet()) {
            try {
                JsonObject wordData = entry.getValue().getAsJsonObject();
                String english = wordData.get("en").getAsString();
                String hebrew = wordData.get("he").getAsString();

                // שליחת אובייקט המילה שנוצר אל ה-DatabaseService לשמירה אסינכרונית ב-Firebase,
                // ושימוש ב-Log כדי להדפיס ב-Logcat של אנדרואיד סטודיו חיווי אם המילה עלתה בהצלחה או נכשלה.
                Word word = new Word(entry.getKey(), english, hebrew, rank);

                DatabaseService.getInstance().createWord(word, new DatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void unused) {
                        Log.d(TAG, "Imported word: " + english + " (Level " + rank + ")");
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "Failed to import word: " + english, e);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error importing word from level " + rank, e);
            }
        }
    }
}