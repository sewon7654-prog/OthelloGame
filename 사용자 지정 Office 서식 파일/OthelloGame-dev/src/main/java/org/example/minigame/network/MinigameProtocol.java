package org.example.minigame.network;

/**
 * 미니게임 네트워크 프로토콜 정의
 */
public class MinigameProtocol {
    // 프로토콜 헤더
    public static final String MINIGAME_START = "MINIGAME_START";
    public static final String MINIGAME_UPDATE = "MINIGAME_UPDATE";
    public static final String MINIGAME_RESULT = "MINIGAME_RESULT";
    public static final String MINIGAME_CLOSE = "MINIGAME_CLOSE";
    
    /**
     * 미니게임 시작 메시지 생성
     * 형식: MINIGAME_START REACTION
     */
    public static String createStartMessage(String gameType) {
        return MINIGAME_START + " " + gameType;
    }
    
    /**
     * 미니게임 상태 업데이트 메시지 생성
     * 형식: MINIGAME_UPDATE {"score":10,"time":15}
     */
    public static String createUpdateMessage(String stateJson) {
        return MINIGAME_UPDATE + " " + stateJson;
    }
    
    /**
     * 미니게임 결과 메시지 생성
     * 형식: MINIGAME_RESULT SUCCESS 10 45
     */
    public static String createResultMessage(boolean success, int score, long time) {
        return createResultMessage(success, score, time, -1, -1);
    }
    
    /**
     * 미니게임 결과 메시지 (강제 수 좌표 포함 버전)
     * 형식: MINIGAME_RESULT SUCCESS 10 45 3 4
     */
    public static String createResultMessage(boolean success, int score, long time, int forcedX, int forcedY) {
        String status = success ? "SUCCESS" : "FAIL";
        return MINIGAME_RESULT + " " + status + " " + score + " " + time + " " + forcedX + " " + forcedY;
    }
    
    /**
     * 미니게임 종료 메시지 생성
     * 형식: MINIGAME_CLOSE
     */
    public static String createCloseMessage() {
        return MINIGAME_CLOSE;
    }
    
    /**
     * 메시지 타입 확인
     */
    public static boolean isMinigameMessage(String message) {
        return message.startsWith(MINIGAME_START) ||
               message.startsWith(MINIGAME_UPDATE) ||
               message.startsWith(MINIGAME_RESULT) ||
               message.startsWith(MINIGAME_CLOSE);
    }
}

