package com.example.wordclash.models;

import java.io.Serializable;

/**
 * Model class for user statistics
 * Tracks user's CURRENT rank and total score
 * Note: practiceCount and hasReviewedWords are now per-rank in rank_progress
 */
public class Stats implements Serializable {

    private String userId;
    private int rank; // Current rank (1-5)
    private int totalScore; // Total points earned across ALL ranks

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

    /**
     * Get required practice count to rank up from current rank
     */
    public int getRequiredPracticeCount() {
        switch (rank) {
            case 1: return 15;
            case 2: return 25;
            case 3: return 40;
            case 4: return 60;
            case 5: return Integer.MAX_VALUE; // Infinite
            default: return 15;
        }
    }

    /**
     * Get questions per practice session for current rank
     */
    public int getQuestionsPerPractice() {
        switch (rank) {
            case 1: return 10;
            case 2: return 13;
            case 3: return 16;
            case 4: return 20;
            case 5: return 25;
            default: return 10;
        }
    }

    @Override
    public String toString() {
        return "Stats{" +
                "userId='" + userId + '\'' +
                ", rank=" + rank +
                ", totalScore=" + totalScore +
                '}';
    }
}