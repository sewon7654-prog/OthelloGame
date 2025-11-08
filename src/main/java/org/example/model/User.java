package org.example.model;

/**
 * 사용자 정보를 담는 모델 클래스
 */
public class User {
    private String userId;
    private String passwordHash;
    private int winCount;
    private int lossCount;
    private int drawCount;

    public User() {}

    public User(String userId, String passwordHash) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.winCount = 0;
        this.lossCount = 0;
        this.drawCount = 0;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public int getLossCount() {
        return lossCount;
    }

    public void setLossCount(int lossCount) {
        this.lossCount = lossCount;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public void setDrawCount(int drawCount) {
        this.drawCount = drawCount;
    }

    public int getTotalGames() {
        return winCount + lossCount + drawCount;
    }

    public double getWinRate() {
        int total = getTotalGames();
        return total > 0 ? (winCount * 100.0 / total) : 0.0;
    }

    @Override
    public String toString() {
        return String.format("%s (승: %d, 패: %d, 무: %d)", 
            userId, winCount, lossCount, drawCount);
    }
}

