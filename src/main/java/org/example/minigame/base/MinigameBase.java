package org.example.minigame.base;

import javafx.stage.Stage;

/**
 * 미니게임 기본 인터페이스
 * 모든 미니게임은 이 인터페이스를 구현해야 합니다.
 */
public interface MinigameBase {
    
    /**
     * 플레이어 모드로 게임 시작 (조작 가능)
     * @param parentStage 부모 Stage (오셀로 게임 창)
     * @param callback 게임 종료 시 호출될 콜백
     */
    void startPlayerMode(Stage parentStage, MinigameCallback callback);
    
    /**
     * 관전 모드로 게임 시작 (화면만 보임)
     * @param parentStage 부모 Stage
     */
    void startSpectatorMode(Stage parentStage);
    
    /**
     * 현재 게임 상태를 JSON 문자열로 반환
     * 네트워크 동기화에 사용됩니다.
     * @return JSON 형식의 게임 상태
     */
    String getStateJson();
    
    /**
     * JSON 문자열로부터 게임 상태 업데이트
     * 관전자 화면을 동기화합니다.
     * @param json 게임 상태 JSON
     */
    void updateFromJson(String json);
    
    /**
     * 게임 종료 여부 확인
     * @return 게임이 종료되었으면 true
     */
    boolean isFinished();
    
    /**
     * 게임 성공 여부 확인
     * @return 게임에 성공했으면 true
     */
    boolean isSuccess();
    
    /**
     * 게임 창 닫기
     */
    void closeGame();
    
    /**
     * 미니게임 타입 반환
     * @return 게임 타입
     */
    MinigameType getType();
}

