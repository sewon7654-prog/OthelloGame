package org.example.minigame.manager;

import javafx.stage.Stage;
import org.example.minigame.base.MinigameBase;
import org.example.minigame.base.MinigameCallback;
import org.example.minigame.base.MinigameType;
import org.example.minigame.games.reaction.ReactionGame;
import org.example.minigame.games.memory.MemoryGame;
import org.example.minigame.games.dodge.DodgeGame;

import java.util.Random;

/**
 * 미니게임 생성 및 관리 클래스
 */
public class MinigameManager {
    private static final Random random = new Random();
    private MinigameBase currentGame;
    
    /**
     * 랜덤 미니게임 생성
     */
    public MinigameBase createRandomGame() {
        MinigameType[] types = MinigameType.values();
        MinigameType randomType = types[random.nextInt(types.length)];
        return createGame(randomType);
    }
    
    /**
     * 특정 타입의 미니게임 생성
     */
    public MinigameBase createGame(MinigameType type) {
        switch (type) {
            case REACTION:
                return new ReactionGame();
            case MEMORY:
                return new MemoryGame();
            case DODGE:
                return new DodgeGame();
            default:
                throw new IllegalArgumentException("Unknown game type: " + type);
        }
    }
    
    /**
     * 플레이어 모드로 미니게임 시작
     */
    public void startPlayerMode(Stage parentStage, MinigameType type, MinigameCallback callback) {
        currentGame = createGame(type);
        currentGame.startPlayerMode(parentStage, callback);
    }
    
    /**
     * 관전 모드로 미니게임 시작
     */
    public void startSpectatorMode(Stage parentStage, MinigameType type) {
        currentGame = createGame(type);
        currentGame.startSpectatorMode(parentStage);
    }
    
    /**
     * 현재 게임의 상태 반환
     */
    public String getCurrentGameState() {
        if (currentGame != null) {
            return currentGame.getStateJson();
        }
        return "{}";
    }
    
    /**
     * 현재 게임 상태 업데이트 (관전자용)
     */
    public void updateCurrentGame(String stateJson) {
        if (currentGame != null) {
            currentGame.updateFromJson(stateJson);
        }
    }
    
    /**
     * 현재 게임 가져오기
     */
    public MinigameBase getCurrentGame() {
        return currentGame;
    }
}

