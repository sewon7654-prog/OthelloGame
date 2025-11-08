package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.model.GameModel;
import org.example.service.AIPlayer;
import org.example.service.DatabaseService;
import org.example.ui.GameView;
import org.example.ui.MenuView;

/**
 * 오셀로 게임 메인 진입점
 * 
 * 프로젝트 구조:
 * - model: 게임 로직 (GameModel)
 * - service: AI, DB 등 서비스 계층 (AIPlayer)
 * - network: 네트워크 통신 (NetworkClient, NetworkServer)
 * - ui: 사용자 인터페이스 (GameView, MenuView)
 */
public class Main extends Application {

    private GameModel gameModel;
    private AIPlayer aiPlayer;
    private GameView gameView;
    private MenuView menuView;

    @Override
    public void start(Stage primaryStage) {
        // 데이터베이스 초기화 (테이블 자동 생성)
        System.out.println("Initializing database...");
        DatabaseService dbService = DatabaseService.getInstance();
        if (dbService.isConnected()) {
            System.out.println("Database initialized successfully!");
        } else {
            System.err.println("Warning: Database connection failed. Some features may not work.");
        }
        
        // 게임 모델 및 서비스 초기화
        gameModel = new GameModel();
        aiPlayer = new AIPlayer(gameModel);
        
        // UI 초기화
        gameView = new GameView(primaryStage, gameModel, aiPlayer);
        menuView = new MenuView(primaryStage, gameView);
        
        // 게임 화면에서 메뉴로 돌아가기 콜백 설정
        gameView.setOnBackToMenu(menuView::show);
        
        // 시작 메뉴 표시
        menuView.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
