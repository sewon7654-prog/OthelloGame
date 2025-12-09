package org.example.minigame.games.memory;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.minigame.base.MinigameResult;

/**
 * MemoryGame 단독 테스트용 클래스
 * 이 파일을 실행하면 게임만 단독으로 테스트할 수 있습니다.
 */
public class MemoryGameTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("테스트 창");
        primaryStage.show();
        
        // 게임 시작
        MemoryGame game = new MemoryGame();
        game.startPlayerMode(primaryStage, this::onGameComplete);
    }
    
    private void onGameComplete(MinigameResult result) {
        System.out.println("게임 종료!");
        System.out.println("결과: " + result);
        System.out.println("성공 여부: " + result.isSuccess());
        System.out.println("맞춘 쌍: " + result.getScore());
        System.out.println("소요 시간: " + result.getTimeElapsed() + "초");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

