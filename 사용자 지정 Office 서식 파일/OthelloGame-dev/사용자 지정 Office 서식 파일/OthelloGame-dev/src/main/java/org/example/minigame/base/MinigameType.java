package org.example.minigame.base;

/**
 * 미니게임 타입 enum
 */
public enum MinigameType {
    REACTION("반응속도 게임", "빠르게 버튼을 클릭하세요!"),
    MEMORY("기억력 게임", "패턴을 기억하고 따라하세요!"),
    DODGE("회피 게임", "장애물을 피하세요!");
    
    private final String displayName;
    private final String description;
    
    MinigameType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}

