package com.example.wordclash.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model class for a word
 * Contains English word, Hebrew translation, and rank/level
 */
// מחלקת מודל המייצגת מילה בודדת באפליקציה (אנגלית, עברית ודרגה).
// היא מממשת Serializable כדי שנוכל להעביר אובייקטים של מילים בין מסכי המשחקים השונים דרך Intent.
public class Word implements Serializable {

    // משתנים פרטיים לשמירת נתוני המילה. המשתנה id מייצג את המפתח הייחודי של המילה כפי שהוא נשמר בתוך בסיס הנתונים Firebase.
    private String id;
    private String english;
    private String hebrew;
    private int rank; // 1-5

    // בנאי ריק שהוא חובה עבור Firebase Realtime Database כדי לאפשר המרה אוטומטית של הנתונים מהענן לאובייקט ג'אווה באמצעות הפעולה getValue.
    public Word() {
    }

    // בנאי עם פרמטרים המשמש ליצירת אובייקט מילה חדש ואתחול כל משתניו בבת אחת בזמן ריצה, למשל במסך הוספת מילים על ידי המנהל (AdminAddWordActivity).
    public Word(String id, String english, String hebrew, int rank) {
        this.id = id;
        this.english = english;
        this.hebrew = hebrew;
        this.rank = rank;
    }

    // פעולות תיווך ציבוריות המאפשרות לקרוא את ערכי המשתנים הפרטיים או לעדכן אותם בצורה בטוחה ומבוקרת מחוץ למחלקה הנוכחית.
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getHebrew() {
        return hebrew;
    }

    public void setHebrew(String hebrew) {
        this.hebrew = hebrew;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @NonNull
    @Override
    // דורסת את פעולת toString ומחזירה מחרוזת המציגה את ערכי המילה, המשמשת בעיקר לצורכי בדיקות והדפסה ב-Logcat לצורך Debugging.
    public String toString() {
        return "Word{" +
                "id='" + id + '\'' +
                ", english='" + english + '\'' +
                ", hebrew='" + hebrew + '\'' +
                ", rank=" + rank +
                '}';
    }


    // דורסות את פעולות ההשוואה המובנות של ג'אווה כדי להשוות בין מילים לפי ה-id שלהן.
    // זה קריטי עבור פונקציות כמו indexOf ו-remove באדפטרים (למשל ב-AdminWordAdapter),
    // שמסתמכות על equals כדי למצוא ולמחוק את המילה הנכונה מהרשימה.
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Objects.equals(id, word.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}