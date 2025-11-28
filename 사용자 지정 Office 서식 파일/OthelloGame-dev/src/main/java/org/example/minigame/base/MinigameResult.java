package org.example.minigame.base;

/**
 * 미니게임 결과 데이터 클래스
 */
public class MinigameResult {
    private final boolean success;      // 성공 여부
    private final int score;            // 점수
    private final long timeElapsed;     // 소요 시간 (초)
    private final MinigameType type;    // 게임 타입
    
    public MinigameResult(boolean success, int score, long timeElapsed, MinigameType type) {
        this.success = success;
        this.score = score;
        this.timeElapsed = timeElapsed;
        this.type = type;
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public int getScore() {
        return score;
    }
    
    public long getTimeElapsed() {
        return timeElapsed;
    }
    
    public MinigameType getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return String.format("MinigameResult{type=%s, success=%b, score=%d, time=%ds}", 
            type, success, score, timeElapsed);
    }
}

