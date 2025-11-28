package org.example.minigame.base;

/**
 * 미니게임 종료 시 호출될 콜백 인터페이스
 */
@FunctionalInterface
public interface MinigameCallback {
    /**
     * 미니게임 완료 시 호출됩니다.
     * @param result 게임 결과
     */
    void onComplete(MinigameResult result);
}

