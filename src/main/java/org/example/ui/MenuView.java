package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.GameModel;
import org.example.model.User;
import org.example.service.ConfigService;

import java.util.Optional;

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
        btnOnline.setOnAction(e -> startOnlineMatch());
        btnAI.setOnAction(e -> showAIDifficultyMenu());

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

    /**
     * AI 난이도 선택 메뉴 표시
     */
    private void showAIDifficultyMenu() {
        VBox menuBox = new VBox(25);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(40));
        menuBox.getStyleClass().add("menu-container");

        Label title = new Label("AI 난이도 선택");
        title.getStyleClass().add("menu-title");

        Button btnEasy = new Button("쉬움 (Easy)");
        Button btnMedium = new Button("중간 (Medium)");
        Button btnHard = new Button("어려움 (Hard)");
        Button btnBack = new Button("← 뒤로가기");

        btnEasy.getStyleClass().add("game-mode-button");
        btnMedium.getStyleClass().add("game-mode-button");
        btnHard.getStyleClass().add("game-mode-button");
        btnBack.getStyleClass().add("account-button");

        btnEasy.setOnAction(e -> {
            gameView.setAIDifficulty(GameModel.Difficulty.EASY);
            gameView.show(GameModel.Mode.AI);
        });
        btnMedium.setOnAction(e -> {
            gameView.setAIDifficulty(GameModel.Difficulty.MEDIUM);
            gameView.show(GameModel.Mode.AI);
        });
        btnHard.setOnAction(e -> {
            gameView.setAIDifficulty(GameModel.Difficulty.HARD);
            gameView.show(GameModel.Mode.AI);
        });
        btnBack.setOnAction(e -> show());

        menuBox.getChildren().addAll(title, btnEasy, btnMedium, btnHard, btnBack);

        Scene scene = new Scene(menuBox, 500, 600);
        scene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/menu.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("AI 난이도 선택");
    }

    /**
     * 온라인 매칭 시작 (IP 주소와 포트 번호 입력 다이얼로그 포함)
     */
    private void startOnlineMatch() {
        // IP와 포트 입력을 위한 커스텀 다이얼로그 생성
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("서버 접속");
        dialog.setHeaderText("playit.gg에서 받은 IP 주소와 포트 번호를 입력하세요.");

        // 기본값 설정
        String defaultIP = ConfigService.getServerIP();
        if (defaultIP == null || defaultIP.isEmpty()) {
            defaultIP = "127.0.0.1";
        }
        String defaultPort = String.valueOf(ConfigService.getServerPort());

        // 다이얼로그 버튼 설정
        ButtonType connectButtonType = new ButtonType("연결", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

        // 입력 필드 생성
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField ipField = new TextField();
        ipField.setPromptText("예: 203.234.62.84");
        ipField.setText(defaultIP);
        
        TextField portField = new TextField();
        portField.setPromptText("예: 8080");
        portField.setText(defaultPort);

        grid.add(new Label("IP 주소:"), 0, 0);
        grid.add(ipField, 1, 0);
        grid.add(new Label("포트 번호:"), 0, 1);
        grid.add(portField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // 연결 버튼 활성화/비활성화 처리
        Button connectButton = (Button) dialog.getDialogPane().lookupButton(connectButtonType);
        connectButton.setDefaultButton(true);

        // 입력값 검증
        ipField.textProperty().addListener((observable, oldValue, newValue) -> {
            connectButton.setDisable(ipField.getText().trim().isEmpty() || portField.getText().trim().isEmpty());
        });
        portField.textProperty().addListener((observable, oldValue, newValue) -> {
            connectButton.setDisable(ipField.getText().trim().isEmpty() || portField.getText().trim().isEmpty());
        });

        // 결과 변환
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                return new String[]{ipField.getText().trim(), portField.getText().trim()};
            }
            return null;
        });

        // 입력 대기
        Optional<String[]> result = dialog.showAndWait();

        // 확인 버튼을 눌렀을 때만 실행
        if (result.isPresent() && result.get() != null) {
            String[] connectionInfo = result.get();
            String ipAddress = connectionInfo[0];
            String portString = connectionInfo[1];
            
            // 포트 번호 유효성 검사
            int port;
            try {
                port = Integer.parseInt(portString);
                if (port < 1 || port > 65535) {
                    showAlert(Alert.AlertType.ERROR, "입력 오류", "포트 번호는 1-65535 사이의 숫자여야 합니다.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "입력 오류", "포트 번호는 숫자여야 합니다.");
                return;
            }
            
            // GameView의 startOnlineMatch 메서드에 IP와 포트 전달
            gameView.startOnlineMatch(ipAddress, port);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

