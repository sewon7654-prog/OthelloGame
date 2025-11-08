package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.GameModel;
import org.example.model.User;
import org.example.network.NetworkClient;
import org.example.service.ConfigService;

/**
 * 게임 시작 메뉴 UI를 담당하는 클래스
 * 로그인, 전적 조회 기능 포함
 */
public class MenuView {

    private Stage primaryStage;
    private GameView gameView;
    private LoginView loginView;
    private User currentUser; // 현재 로그인한 사용자

    public MenuView(Stage stage, GameView gameView) {
        this.primaryStage = stage;
        this.gameView = gameView;
        this.loginView = new LoginView(stage);
        
        // 로그인 성공 시 메뉴로 돌아오기
        loginView.setOnLoginSuccess(() -> {
            currentUser = loginView.getCurrentUser();
            gameView.setCurrentUser(currentUser);
            show();
        });
        
        // 로그인 화면에서 뒤로가기
        loginView.setOnBackToMenu(this::show);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * 시작 메뉴를 표시합니다
     */
    public void show() {
        VBox mainLayout = new VBox(25);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(40));
        mainLayout.getStyleClass().add("menu-container");

        // 타이틀
        Label title = new Label("오셀로 게임");
        title.getStyleClass().add("menu-title");

        // 로그인 상태 표시
        Label statusLabel;
        if (currentUser != null) {
            statusLabel = new Label("환영합니다, " + currentUser.getUserId() + "님!");
            statusLabel.getStyleClass().add("status-label-logged-in");
        } else {
            statusLabel = new Label("게스트 모드 (로그인하면 전적이 기록됩니다)");
            statusLabel.getStyleClass().add("status-label-guest");
        }

        // 게임 모드 버튼들
        Button btnLocal = new Button("로컬 2인 대전");
        Button btnOnline = new Button("온라인 1:1 대전");
        Button btnAI = new Button("AI와 대전");
        
        btnLocal.getStyleClass().add("game-mode-button");
        btnOnline.getStyleClass().add("game-mode-button");
        btnAI.getStyleClass().add("game-mode-button");

        // 버튼 클릭 이벤트
        btnLocal.setOnAction(e -> gameView.show(GameModel.Mode.LOCAL));
        btnOnline.setOnAction(e -> {
            // 설정 파일에서 서버 IP 읽어서 바로 매칭 시작
            String serverIP = ConfigService.getServerIP();
            NetworkClient.setServerIP(serverIP);
            gameView.startOnlineMatch();
        });
        btnAI.setOnAction(e -> gameView.show(GameModel.Mode.AI));

        // 계정 관련 버튼들
        HBox accountButtons = new HBox(15);
        accountButtons.setAlignment(Pos.CENTER);

        if (currentUser == null) {
            // 로그인 전
            Button btnLogin = new Button("로그인 / 회원가입");
            btnLogin.getStyleClass().add("login-button");
            btnLogin.setOnAction(e -> loginView.show());
            accountButtons.getChildren().add(btnLogin);
        } else {
            // 로그인 후
            Button btnStats = new Button("내 전적");
            Button btnSettings = new Button("설정");
            Button btnLogout = new Button("로그아웃");
            
            btnStats.getStyleClass().add("account-button");
            btnSettings.getStyleClass().add("account-button");
            btnLogout.getStyleClass().add("account-button");

            btnStats.setOnAction(e -> showStats());
            btnSettings.setOnAction(e -> showSettings());
            btnLogout.setOnAction(e -> {
                currentUser = null;
                gameView.setCurrentUser(null);
                showAlert(Alert.AlertType.INFORMATION, "로그아웃", "로그아웃되었습니다.");
                show();
            });

            accountButtons.getChildren().addAll(btnStats, btnSettings, btnLogout);
        }

        mainLayout.getChildren().addAll(
            title,
            statusLabel,
            btnLocal,
            btnOnline,
            btnAI,
            accountButtons
        );

        Scene menuScene = new Scene(mainLayout, 500, 600);
        menuScene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        menuScene.getStylesheets().add(getClass().getResource("/css/menu.css").toExternalForm());
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Othello Game - 메인 메뉴");
        primaryStage.show();
    }

    /**
     * 전적 조회 화면 표시
     */
    private void showStats() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "로그인 필요", "로그인 후 이용 가능합니다.");
            return;
        }

        StatsView statsView = new StatsView(primaryStage, currentUser);
        statsView.setOnBackToMenu(this::show);
        statsView.show();
    }
    
    /**
     * 설정 화면 표시
     */
    private void showSettings() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "로그인 필요", "로그인 후 이용 가능합니다.");
            return;
        }

        SettingsView settingsView = new SettingsView(primaryStage, currentUser);
        settingsView.setOnBackToMenu(this::show);
        settingsView.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

