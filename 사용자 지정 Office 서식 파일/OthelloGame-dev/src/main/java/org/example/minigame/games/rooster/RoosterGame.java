package org.example.minigame.games.rooster;

import javafx.stage.Stage;
import org.example.minigame.base.MinigameBase;
import org.example.minigame.base.MinigameCallback;
import org.example.minigame.base.MinigameResult;
import org.example.minigame.base.MinigameType;

/**
 * 간단화된 Rooster 미니게임 로직/상태 관리.
 * 별도 Stage에서 실행하며, 게임 종료 시 콜백으로 결과를 반환한다.
 */
public class RoosterGame implements MinigameBase {

    private boolean finished = false;
    private boolean success = false;
    private int score = 0;
    private long elapsed = 0;
    private RoosterGameView view;
    private MinigameCallback callback;

    @Override
    public void startPlayerMode(Stage parentStage, MinigameCallback callback) {
        this.callback = callback;
        this.view = new RoosterGameView(this);
        view.show(parentStage);
        view.startGame();
    }

    @Override
    public void startSpectatorMode(Stage parentStage) {
        // 관전 모드: 입력 없이 화면만 표시
        this.view = new RoosterGameView(this);
        view.show(parentStage);
        view.startSpectator();
    }

    /** RoosterGameView가 호출: 게임 종료 시 결과 설정 후 콜백 */
    public void finishGame(boolean success, int score, long elapsedSeconds) {
        if (finished) return;
        this.finished = true;
        this.success = success;
        this.score = score;
        this.elapsed = elapsedSeconds;
        if (callback != null) {
            callback.onComplete(new MinigameResult(success, score, elapsedSeconds, MinigameType.REACTION));
        }
    }

    @Override
    public String getStateJson() {
        return "{\"finished\":" + finished + ",\"success\":" + success + ",\"score\":" + score + ",\"elapsed\":" + elapsed + "}";
    }

    @Override
    public void updateFromJson(String json) {
        // 관전 모드 동기화 필요 시 구현 (생략)
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
        if (view != null) {
            view.close();
        }
    }

    @Override
    public MinigameType getType() {
        return MinigameType.REACTION;
    }
}
