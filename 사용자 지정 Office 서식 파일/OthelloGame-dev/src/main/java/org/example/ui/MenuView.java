package org.example.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.GameModel;
import org.example.model.User;
import org.example.service.ConfigService;
import org.example.service.ButtonEffectService;
import org.example.service.PixelArtUIService;

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
        // 픽셀 아트 배경 생성 (컴퓨터 화면 크기에 맞게)
        double screenWidth = 1600;
        double screenHeight = 1000;
        StackPane backgroundPane = PixelArtUIService.createPixelArtBackground(screenWidth, screenHeight);
        
        VBox mainLayout = new VBox(35);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(60));
        mainLayout.getStyleClass().add("menu-container");
        mainLayout.setStyle("-fx-background-color: transparent;");
        
        // 배경과 메뉴를 스택으로 합치기
        StackPane rootPane = new StackPane();
        rootPane.getChildren().addAll(backgroundPane, mainLayout);

        // 타이틀 (픽셀 아트 로고 - 이미지처럼 큰 픽셀 폰트)
        StackPane titleLogo = PixelArtUIService.createLargePixelArtLogo("오셀로 게임", 1200, 280);
        titleLogo.setAlignment(Pos.CENTER);

        // 로그인 상태 표시
        Label statusLabel;
        if (currentUser != null) {
            statusLabel = new Label("환영합니다, " + currentUser.getUserId() + "님!");
            statusLabel.getStyleClass().add("status-label-logged-in");
        } else {
            statusLabel = new Label("게스트 모드 (로그인하면 전적이 기록됩니다)");
            statusLabel.getStyleClass().add("status-label-guest");
        }

        // 게임 모드 버튼들 (픽셀 아트 스타일 - 더 크게)
        StackPane btnLocal = PixelArtUIService.createPixelArtButton("로컬 2인 대전", 650, 100);
        StackPane btnOnline = PixelArtUIService.createPixelArtButton("온라인 1:1 대전", 650, 100);
        StackPane btnAI = PixelArtUIService.createPixelArtButton("AI와 대전", 650, 100);
        
        // 클릭 이벤트를 위한 래퍼
        javafx.scene.control.Button btnLocalWrapper = new javafx.scene.control.Button();
        btnLocalWrapper.setGraphic(btnLocal);
        btnLocalWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        
        javafx.scene.control.Button btnOnlineWrapper = new javafx.scene.control.Button();
        btnOnlineWrapper.setGraphic(btnOnline);
        btnOnlineWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        
        javafx.scene.control.Button btnAIWrapper = new javafx.scene.control.Button();
        btnAIWrapper.setGraphic(btnAI);
        btnAIWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // 세련된 클릭 효과 추가
        ButtonEffectService.addPixelArtButtonEffects(btnLocalWrapper);
        ButtonEffectService.addPixelArtButtonEffects(btnOnlineWrapper);
        ButtonEffectService.addPixelArtButtonEffects(btnAIWrapper);
        ButtonEffectService.addClickParticleEffect(btnLocalWrapper);
        ButtonEffectService.addClickParticleEffect(btnOnlineWrapper);
        ButtonEffectService.addClickParticleEffect(btnAIWrapper);

        // 버튼 클릭 이벤트
        btnLocalWrapper.setOnAction(e -> gameView.show(GameModel.Mode.LOCAL));
        btnOnlineWrapper.setOnAction(e -> startOnlineMatch());
        btnAIWrapper.setOnAction(e -> showAIDifficultyMenu());

        // 계정 관련 버튼들
        HBox accountButtons = new HBox(15);
        accountButtons.setAlignment(Pos.CENTER);

        if (currentUser == null) {
            // 로그인 전
            StackPane btnLoginPane = PixelArtUIService.createPixelArtButton("로그인 / 회원가입", 400, 80);
            javafx.scene.control.Button btnLoginWrapper = new javafx.scene.control.Button();
            btnLoginWrapper.setGraphic(btnLoginPane);
            btnLoginWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
            btnLoginWrapper.setPrefSize(400, 80);
            ButtonEffectService.addPixelArtButtonEffects(btnLoginWrapper);
            ButtonEffectService.addClickParticleEffect(btnLoginWrapper);
            btnLoginWrapper.setOnAction(e -> loginView.show());
            accountButtons.getChildren().add(btnLoginWrapper);
        } else {
            // 로그인 후
            StackPane btnStatsPane = PixelArtUIService.createPixelArtButton("내 전적", 250, 80);
            StackPane btnSettingsPane = PixelArtUIService.createPixelArtButton("설정", 250, 80);
            StackPane btnLogoutPane = PixelArtUIService.createPixelArtButton("로그아웃", 250, 80);
            
            javafx.scene.control.Button btnStatsWrapper = new javafx.scene.control.Button();
            btnStatsWrapper.setGraphic(btnStatsPane);
            btnStatsWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
            btnStatsWrapper.setPrefSize(250, 80);
            
            javafx.scene.control.Button btnSettingsWrapper = new javafx.scene.control.Button();
            btnSettingsWrapper.setGraphic(btnSettingsPane);
            btnSettingsWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
            btnSettingsWrapper.setPrefSize(250, 80);
            
            javafx.scene.control.Button btnLogoutWrapper = new javafx.scene.control.Button();
            btnLogoutWrapper.setGraphic(btnLogoutPane);
            btnLogoutWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
            btnLogoutWrapper.setPrefSize(250, 80);

            // 세련된 클릭 효과 추가
            ButtonEffectService.addPixelArtButtonEffects(btnStatsWrapper);
            ButtonEffectService.addPixelArtButtonEffects(btnSettingsWrapper);
            ButtonEffectService.addPixelArtButtonEffects(btnLogoutWrapper);
            ButtonEffectService.addClickParticleEffect(btnStatsWrapper);
            ButtonEffectService.addClickParticleEffect(btnSettingsWrapper);
            ButtonEffectService.addClickParticleEffect(btnLogoutWrapper);

            btnStatsWrapper.setOnAction(e -> showStats());
            btnSettingsWrapper.setOnAction(e -> showSettings());
            btnLogoutWrapper.setOnAction(e -> {
                currentUser = null;
                gameView.setCurrentUser(null);
                showAlert(Alert.AlertType.INFORMATION, "로그아웃", "로그아웃되었습니다.");
                show();
            });

            accountButtons.getChildren().addAll(btnStatsWrapper, btnSettingsWrapper, btnLogoutWrapper);
        }

        mainLayout.getChildren().addAll(
            titleLogo,
            statusLabel,
            btnLocalWrapper,
            btnOnlineWrapper,
            btnAIWrapper,
            accountButtons
        );

        Scene menuScene = new Scene(rootPane, screenWidth, screenHeight);
        menuScene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        menuScene.getStylesheets().add(getClass().getResource("/css/menu.css").toExternalForm());
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Othello Game - 메인 메뉴");
        primaryStage.setMaximized(true); // 창 최대화
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
        // 픽셀 아트 배경 생성 (컴퓨터 화면 크기에 맞게)
        double aiScreenWidth = 1600;
        double aiScreenHeight = 1000;
        StackPane backgroundPane = PixelArtUIService.createPixelArtBackground(aiScreenWidth, aiScreenHeight);
        
        VBox menuBox = new VBox(35);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(60));
        menuBox.getStyleClass().add("menu-container");
        menuBox.setStyle("-fx-background-color: transparent;");
        
        // 배경과 메뉴를 스택으로 합치기
        StackPane rootPane = new StackPane();
        rootPane.getChildren().addAll(backgroundPane, menuBox);

        // 타이틀 로고
        StackPane titleLogo = PixelArtUIService.createLargePixelArtLogo("AI 난이도 선택", 600, 140);
        titleLogo.setAlignment(Pos.CENTER);

        // 버튼들 (픽셀 아트 스타일)
        StackPane btnEasyPane = PixelArtUIService.createPixelArtButton("쉬움 (Easy)", 500, 80);
        StackPane btnMediumPane = PixelArtUIService.createPixelArtButton("중간 (Medium)", 500, 80);
        StackPane btnHardPane = PixelArtUIService.createPixelArtButton("어려움 (Hard)", 500, 80);
        StackPane btnBackPane = PixelArtUIService.createPixelArtButton("← 뒤로가기", 300, 60);
        
        javafx.scene.control.Button btnEasyWrapper = new javafx.scene.control.Button();
        btnEasyWrapper.setGraphic(btnEasyPane);
        btnEasyWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btnEasyWrapper.setPrefSize(650, 100);
        
        javafx.scene.control.Button btnMediumWrapper = new javafx.scene.control.Button();
        btnMediumWrapper.setGraphic(btnMediumPane);
        btnMediumWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btnMediumWrapper.setPrefSize(650, 100);
        
        javafx.scene.control.Button btnHardWrapper = new javafx.scene.control.Button();
        btnHardWrapper.setGraphic(btnHardPane);
        btnHardWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btnHardWrapper.setPrefSize(650, 100);
        
        javafx.scene.control.Button btnBackWrapper = new javafx.scene.control.Button();
        btnBackWrapper.setGraphic(btnBackPane);
        btnBackWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btnBackWrapper.setPrefSize(400, 80);

        // 세련된 클릭 효과 추가
        ButtonEffectService.addPixelArtButtonEffects(btnEasyWrapper);
        ButtonEffectService.addPixelArtButtonEffects(btnMediumWrapper);
        ButtonEffectService.addPixelArtButtonEffects(btnHardWrapper);
        ButtonEffectService.addPixelArtButtonEffects(btnBackWrapper);
        ButtonEffectService.addClickParticleEffect(btnEasyWrapper);
        ButtonEffectService.addClickParticleEffect(btnMediumWrapper);
        ButtonEffectService.addClickParticleEffect(btnHardWrapper);
        ButtonEffectService.addClickParticleEffect(btnBackWrapper);

        btnEasyWrapper.setOnAction(e -> {
            gameView.setAIDifficulty(GameModel.Difficulty.EASY);
            gameView.show(GameModel.Mode.AI);
        });
        btnMediumWrapper.setOnAction(e -> {
            gameView.setAIDifficulty(GameModel.Difficulty.MEDIUM);
            gameView.show(GameModel.Mode.AI);
        });
        btnHardWrapper.setOnAction(e -> {
            gameView.setAIDifficulty(GameModel.Difficulty.HARD);
            gameView.show(GameModel.Mode.AI);
        });
        btnBackWrapper.setOnAction(e -> show());

        menuBox.getChildren().addAll(titleLogo, btnEasyWrapper, btnMediumWrapper, btnHardWrapper, btnBackWrapper);

        Scene scene = new Scene(rootPane, aiScreenWidth, aiScreenHeight);
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
        Platform.runLater(() -> {
            Alert alert = new Alert(type, content, javafx.scene.control.ButtonType.OK);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}

