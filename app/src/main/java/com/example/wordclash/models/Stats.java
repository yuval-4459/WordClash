package com.example.wordclash.models;

import java.io.Serializable;

/**
 * Model class for user statistics
 * Tracks user's rank, practice count, and scores
 */
public class Stats implements Serializable {

    private String userId;
    private int rank; // 1-5
    private int practiceCount; // Number of completed practice sessions
    private int totalScore; // Total points earned
    private boolean hasReviewedWords; // Has user reviewed words for current rank

    public Stats() {
    }

    public Stats(String userId, int rank, int practiceCount, int totalScore, boolean hasReviewedWords) {
        this.userId = userId;
        this.rank = rank;
        this.practiceCount = practiceCount;
        this.totalScore = totalScore;
        this.hasReviewedWords = hasReviewedWords;
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

    public int getPracticeCount() {
        return practiceCount;
    }

    public void setPracticeCount(int practiceCount) {
        this.practiceCount = practiceCount;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public boolean isHasReviewedWords() {
        return hasReviewedWords;
    }

    public void setHasReviewedWords(boolean hasReviewedWords) {
        this.hasReviewedWords = hasReviewedWords;
    }

    /**
     * Get required practice count to rank up
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

    /**
     * Check if user can rank up
     */
    public boolean canRankUp() {
        return rank < 5 && practiceCount >= getRequiredPracticeCount();
    }

    @Override
    public String toString() {
        return "Stats{" +
                "userId='" + userId + '\'' +
                ", rank=" + rank +
                ", practiceCount=" + practiceCount +
                ", totalScore=" + totalScore +
                ", hasReviewedWords=" + hasReviewedWords +
                '}';
    }
}