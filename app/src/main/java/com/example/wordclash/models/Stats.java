package com.example.wordclash.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Model class for user statistics
 * Tracks user's current rank and total score
 */
public class Stats implements Serializable {

    private String userId;
    private int rank; // Current rank (1-5)
    private int totalScore; // Total score earned across ALL ranks

    public Stats() {
    }

    public Stats(String userId, int rank, int totalScore) {
        this.userId = userId;
        this.rank = rank;
        this.totalScore = totalScore;
    }

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
    public String toString() {
        return "Stats{" +
                "userId='" + userId + '\'' +
                ", rank=" + rank +
                ", totalScore=" + totalScore +
                '}';
    }
}