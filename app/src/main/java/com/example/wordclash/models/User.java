package com.example.wordclash.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

/// model class for the user
/// this class represents a user in the application
/// it contains the user's information (including learning language preference)
///
/// @see Serializable
// מחלקת מודל המייצגת את נתוני המשתמש באפליקציה.
// היא מממשת Serializable כדי שנוכל להעביר את אובייקט המשתמש השלם בין מסכים שונים (Activities) בעזרת Intent.
public class User implements Serializable {

    // משתנים פרטיים השומרים את פרטי המשתמש, כולל הרשאות מנהל, נתיב לתמונת פרופיל, שפת הלימוד הנבחרת ורשימת המילים האישית שלו מסוג Word.
    private String id;
    private String email, password;
    private String userName;
    private String gender;
    private boolean isAdmin;

    // profile picture URL (stored in Firebase Storage or base64)
    private String profilePictureUrl;

    // the language the user wants to learn (not UI language)
    // "english" = learning English (UI in Hebrew)
    // "hebrew" = learning Hebrew (UI in English)
    private String learningLanguage;

    private ArrayList<Word> words;

    // בנאי ריק שמאתחל את רשימת המילים וקובע ערכי ברירת מחדל.
    // הוא חובה עבור Firebase Realtime Database כדי לבצע המרה אוטומטית של נתונים מהענן לאובייקט באמצעות getValue.
    public User() {
        this.learningLanguage = "english"; // default
        this.profilePictureUrl = null; // default is no picture
        this.words = new ArrayList<>();
    }

    // בנאי עם פרמטרים המשמש ליצירת אובייקט משתמש חדש ואתחול כל משתניו בבת אחת בזמן הריצה, למשל בשלב ההרשמה באפליקציה (SignUpActivity).
    public User(String id, String email, String password, String userName,
                String gender, boolean isAdmin, String learningLanguage, ArrayList<Word> words) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.gender = gender;
        this.isAdmin = isAdmin;
        this.learningLanguage = learningLanguage;
        this.profilePictureUrl = null;
        this.words = words;
    }

    // פעולות תיווך ציבוריות המאפשרות לקרוא את ערכי המשתנים הפרטיים או לעדכן אותם בצורה בטוחה ומבוקרת מחוץ למחלקה.
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getLearningLanguage() {
        return learningLanguage;
    }

    public void setLearningLanguage(String learningLanguage) {
        this.learningLanguage = learningLanguage;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public ArrayList<Word> getWords() {
        return words;
    }

    public void setWords(ArrayList<Word> words) {
        this.words = words;
    }


    @NonNull
    @Override
    // דורסת את פעולת toString ומחזירה מחרוזת המציגה את כל ערכי המשתמש, המשמשת בעיקר לצורכי בדיקות והדפסה ב-Logcat לצורך Debugging.
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", userName='" + userName + '\'' +
                ", gender='" + gender + '\'' +
                ", isAdmin=" + isAdmin +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                ", learningLanguage='" + learningLanguage + '\'' +
                ", words=" + words +
                '}';
    }
}