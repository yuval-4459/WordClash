package com.example.wordclash.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model class for a word
 * Contains English word, Hebrew translation, and rank/level
 */
public class Word implements Serializable {

    private String id;
    private String english;
    private String hebrew;
    private int rank; // 1-5

    public Word() {
    }

    public Word(String id, String english, String hebrew, int rank) {
        this.id = id;
        this.english = english;
        this.hebrew = hebrew;
        this.rank = rank;
    }

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
    public String toString() {
        return "Word{" +
                "id='" + id + '\'' +
                ", english='" + english + '\'' +
                ", hebrew='" + hebrew + '\'' +
                ", rank=" + rank +
                '}';
    }

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