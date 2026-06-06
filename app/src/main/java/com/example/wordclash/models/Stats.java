package com.example.wordclash.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Model class for user statistics
 * Tracks user's current rank and total score
 */
//מחלקת מודל (Model) המייצגת סטטיסטיקות משתמש, המממשת Serializable כדי לאפשר העברת האובייקט השלם בין מסכים (Activities) שונים דרך Intent.
public class Stats implements Serializable {

    private String userId;
    private int rank; // Current rank (1-5)
    private int totalScore; // Total score earned across ALL ranks

    // בנאי ריק שהוא חובה עבור Firebase Realtime Database כדי שהמערכת תוכל לשלוף נתונים ולהמיר אותם אוטומטית לאובייקט באמצעות getValue.
    public Stats() {
    }

    // בנאי עם פרמטרים המשמש ליצירת אובייקט חדש ואתחול כל משתניו בבת אחת בזמן הריצה.
    public Stats(String userId, int rank, int totalScore) {
        this.userId = userId;
        this.rank = rank;
        this.totalScore = totalScore;
    }


    // פעולות תיווך ציבוריות המאפשרות לקרוא את ערכי המשתנים הפרטיים או לעדכן אותם בצורה בטוחה ומבוקרת מחוץ למחלקה.
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }


    @NonNull
    @Override
    // דורסת את פעולת toString ומחזירה מחרוזת המציגה את ערכי האובייקט, המשמשת בעיקר לצורכי בדיקות והדפסה ב-Logcat (Debugging).
    public String toString() {
        return "Stats{" +
                "userId='" + userId + '\'' +
                ", rank=" + rank +
                ", totalScore=" + totalScore +
                '}';
    }
}