package com.example.wordclash.models;

import java.io.Serializable;

/// Model class for the user
/// This class represents a user in the application
/// It contains the user's information including learning language preference
/// @see Serializable
public class User implements Serializable {

    /// unique id of the user
    private String id;

    private String email, password;
    private String userName;
    private String gender;
    private boolean isAdmin;

    // Language the user wants to LEARN (not UI language)
    // "english" = learning English (UI in Hebrew)
    // "hebrew" = learning Hebrew (UI in English)
    private String learningLanguage;

    public User() {
        this.learningLanguage = "english"; // Default
    }

    public User(String id, String email, String password, String userName, String gender, boolean isAdmin) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.gender = gender;
        this.isAdmin = isAdmin;
        this.learningLanguage = "english"; // Default
    }

    public User(String id, String email, String password, String userName, String gender, boolean isAdmin, String learningLanguage) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.gender = gender;
        this.isAdmin = isAdmin;
        this.learningLanguage = learningLanguage;
    }

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

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", userName='" + userName + '\'' +
                ", gender='" + gender + '\'' +
                ", isAdmin=" + isAdmin +
                ", learningLanguage='" + learningLanguage + '\'' +
                '}';
    }
}