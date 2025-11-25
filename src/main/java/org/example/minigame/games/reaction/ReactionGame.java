package org.example.minigame.games.reaction;

import javafx.stage.Stage;
import org.example.minigame.base.*;

/**
 * 반응속도 게임
 * TODO: React 코드를 보고 구현
 */
public class ReactionGame implements MinigameBase {
    private Stage gameStage;
    private boolean finished = false;
    private boolean success = false;
    
    @Override
    public void startPlayerMode(Stage parentStage, MinigameCallback callback) {
        // TODO: 게임 UI 생성 및 로직 구현
        System.out.println("반응속도 게임 시작 - 구현 필요");
    }
    
    @Override
    public void startSpectatorMode(Stage parentStage) {
        // TODO: 관전 모드 구현
        startPlayerMode(parentStage, null);
    }
    
    @Override
    public String getStateJson() {
        return "{}";
    }
    
    @Override
    public void updateFromJson(String json) {
        // TODO: JSON 파싱 및 상태 업데이트
    }
    
    @Override
    public boolean isFinished() {
        return finished;
    }
    
    @Override
    public boolean isSuccess() {
        return success;
    }
    
    @Override
    public void closeGame() {
        if (gameStage != null) {
            gameStage.close();
        }
    }
    
    @Override
    public MinigameType getType() {
        return MinigameType.REACTION;
    }
}

