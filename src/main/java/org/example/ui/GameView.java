package org.example.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.model.GameModel;
import org.example.model.User;
import org.example.network.NetworkClient;
import org.example.service.AIPlayer;
import org.example.service.DatabaseService;
import org.example.service.SoundService;
import org.example.service.EffectService;

import java.util.List;
import java.util.Map;

/**
 * 게임 화면 UI 및 게임 로직을 담당하는 클래스
 * 추후 UI 변경 시 이 클래스만 수정하면 됨
 */
public class GameView {

    private static final int TILE_SIZE = 85;
    private static final int WIDTH = 8;

    // Core Game Components
    private GameModel gameModel;
    private NetworkClient networkClient;
    private AIPlayer aiPlayer;
    private int myColor = 0; // 1: Black, 2: White, 0: Not assigned
    private User currentUser; // 현재 로그인한 사용자
    private String opponentUserId; // 온라인 모드에서 상대방 사용자 ID
    private DatabaseService dbService;
    private SoundService soundService;
    
    // 커스텀 색상 설정 (기본값)
    private Color customBlackColor = Color.BLACK;
    private Color customWhiteColor = Color.WHITE;

    // GUI Components
    private Stage primaryStage;
    private BorderPane mainLayout;
    private GridPane boardView;
    private Label scoreLabel;
    private Runnable onBackToMenu;
    private VBox matchingScreen; // 매칭 중 화면
    private Label matchingLabel; // 매칭 상태 표시 레이블
    
    // 커스텀 폰트
    private static javafx.scene.text.Font cinzelFont;
    private static javafx.scene.text.Font orbitronFont;
    
    // 찬스카드 관련
    private VBox memoryCard; // 기억력 카드
    private VBox reactionCard; // 반응속도 카드
    private VBox dodgeCard; // 회피 카드
    private boolean[] cardUsed = new boolean[3]; // 카드 사용 여부

    static {
        try {
            // Cinzel - 제목용 (세리프 폰트, 로마 비문 스타일)
            cinzelFont = javafx.scene.text.Font.loadFont(
                GameView.class.getResourceAsStream("/fonts/Cinzel-Bold.ttf"), 24
            );
            
            // Orbitron - 본문용 (기하학적 레트로 폰트)
            orbitronFont = javafx.scene.text.Font.loadFont(
                GameView.class.getResourceAsStream("/fonts/Orbitron-Bold.ttf"), 18
            );
            
            if (cinzelFont == null || orbitronFont == null) {
                System.err.println("[폰트 로드] 커스텀 폰트 로드 실패, 기본 폰트 사용");
                cinzelFont = javafx.scene.text.Font.font("Times New Roman", javafx.scene.text.FontWeight.BOLD, 24);
                orbitronFont = javafx.scene.text.Font.font("Consolas", javafx.scene.text.FontWeight.BOLD, 18);
            }
        } catch (Exception e) {
            System.err.println("[폰트 로드] 오류: " + e.getMessage());
            cinzelFont = javafx.scene.text.Font.font("Times New Roman", javafx.scene.text.FontWeight.BOLD, 24);
            orbitronFont = javafx.scene.text.Font.font("Consolas", javafx.scene.text.FontWeight.BOLD, 18);
        }
    }

    public GameView(Stage stage, GameModel model, AIPlayer aiPlayer) {
        this.primaryStage = stage;
        this.gameModel = model;
        this.aiPlayer = aiPlayer;
        this.dbService = DatabaseService.getInstance();
        this.soundService = SoundService.getInstance();
    }

    public void setOnBackToMenu(Runnable callback) {
        this.onBackToMenu = callback;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        // 사용자 설정 불러오기
        if (user != null) {
            loadUserSettings();
        }
    }
    
    /**
     * 사용자 설정 불러오기
     */
    private void loadUserSettings() {
        if (currentUser == null) return;
        
        Map<String, String> settings = dbService.getUserSettings(currentUser.getUserId());
        if (settings != null && !settings.isEmpty()) {
            if (settings.containsKey("blackColor")) {
                try {
                    customBlackColor = Color.web(settings.get("blackColor"));
                } catch (Exception e) {
                    customBlackColor = Color.BLACK;
                }
            }
            if (settings.containsKey("whiteColor")) {
                try {
                    customWhiteColor = Color.web(settings.get("whiteColor"));
                } catch (Exception e) {
                    customWhiteColor = Color.WHITE;
                }
            }
        }
    }

    /**
     * 게임 화면을 표시합니다
     */
    public void show(GameModel.Mode mode) {
        gameModel.setGameMode(mode);
        gameModel.initializeBoard();

        boardView = createBoardView();
        scoreLabel = new Label();
        scoreLabel.getStyleClass().add("score-label");

        Button backButton = new Button("← 메뉴로 돌아가기");
        backButton.getStyleClass().add("back-to-menu-button");
        backButton.setOnAction(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        // 상단 패널 (모드 정보 및 현재 턴)
        Label modeLabel = new Label();
        String modeText = switch(mode) {
            case LOCAL -> "로컬 2인 대전";
            case ONLINE -> "온라인 1:1 대전";
            case AI -> "AI 대전";
        };
        modeLabel.setText(modeText);
        modeLabel.setFont(cinzelFont);
        modeLabel.getStyleClass().add("mode-label");
        
        VBox topPanel = new VBox(8);
        topPanel.setPadding(new Insets(12));
        topPanel.setAlignment(Pos.CENTER);
        topPanel.getStyleClass().add("game-top-panel");
        topPanel.getChildren().addAll(modeLabel, scoreLabel);

        // 보드를 중앙 정렬하기 위한 컨테이너
        StackPane boardContainer = new StackPane();
        boardContainer.setAlignment(Pos.CENTER);
        boardContainer.getChildren().add(boardView);

        mainLayout = new BorderPane();
        mainLayout.setTop(topPanel);
        mainLayout.setCenter(boardContainer);
        mainLayout.getStyleClass().add("game-container");
        
        // 로컬/온라인 모드: 찬스카드를 오른쪽에 배치
        if (mode == GameModel.Mode.LOCAL || mode == GameModel.Mode.ONLINE) {
            VBox rightPanel = createRightPanel(backButton);
            mainLayout.setRight(rightPanel);
        } else {
            // AI 모드: 버튼만 하단에 표시
            HBox bottomPanel = new HBox();
            bottomPanel.setPadding(new Insets(15));
            bottomPanel.setAlignment(Pos.CENTER);
            bottomPanel.getChildren().add(backButton);
            mainLayout.setBottom(bottomPanel);
        }

        // AI 모드 선공일 경우 바로 AI 턴 시작
        if (mode == GameModel.Mode.AI && gameModel.getCurrentTurn() == gameModel.getAIColor()) {
            Platform.runLater(this::handleAITurn);
        }

        drawBoard();
        drawValidMoves();
        updateScoreDisplay();

        // 화면 크기 최적화 (화면에 맞게)
        int boardSize = WIDTH * TILE_SIZE + 20; // 680 + 20 = 700
        int rightPanelWidth = 280; // 오른쪽 패널
        int sceneWidth = boardSize + rightPanelWidth + 40; // 700 + 280 + 40 = 1020
        int sceneHeight = boardSize + 180; // 700 + 180 = 880
        Scene gameScene = new Scene(mainLayout, sceneWidth, sceneHeight);
        gameScene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        gameScene.getStylesheets().add(getClass().getResource("/css/game.css").toExternalForm());
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Othello Game - " + modeText);
    }

    /**
     * 온라인 매칭을 시작합니다 (IP 주소와 포트 번호 지정)
     */
    public void startOnlineMatch(String serverIp, int serverPort) {
        gameModel.setGameMode(GameModel.Mode.ONLINE);
        opponentUserId = null; // 상대방 ID 초기화
        
        // 매칭 중 화면 표시
        showMatchingScreen();

        if (gameModel.isOnlineMode() && networkClient != null && networkClient.isAlive()) return;

        networkClient = new NetworkClient(this, currentUser != null ? currentUser.getUserId() : "Guest", serverIp, serverPort);
        if (networkClient.connect()) {
            networkClient.start();
            updateMatchingStatus("서버(" + serverIp + ":" + serverPort + ")에 연결되었습니다. 상대방을 기다리는 중...");
        } else {
            showAlert("Connection Failed", "서버(" + serverIp + ":" + serverPort + ") 접속에 실패했습니다. NetworkServer를 실행했는지 확인하세요.");
            if (onBackToMenu != null) onBackToMenu.run();
        }
    }
    
    /**
     * 온라인 매칭을 시작합니다 (IP 주소만 지정, 포트는 설정 파일에서 읽음)
     */
    public void startOnlineMatch(String serverIp) {
        startOnlineMatch(serverIp, org.example.service.ConfigService.getServerPort());
    }
    
    /**
     * 온라인 매칭을 시작합니다 (기본 IP와 포트 사용)
     */
    public void startOnlineMatch() {
        startOnlineMatch(org.example.service.ConfigService.getServerIP(), org.example.service.ConfigService.getServerPort());
    }
    
    /**
     * AI 난이도 설정
     */
    public void setAIDifficulty(GameModel.Difficulty difficulty) {
        gameModel.setAIDifficulty(difficulty);
    }
    
    /**
     * 매칭 중 화면 표시
     */
    private void showMatchingScreen() {
        matchingScreen = new VBox(30);
        matchingScreen.setAlignment(Pos.CENTER);
        matchingScreen.setPadding(new Insets(40));
        matchingScreen.setStyle("-fx-background-color: linear-gradient(to bottom, #4A5D4A, #2F4F2F);");
        
        Label titleLabel = new Label("온라인 매칭");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 3);");
        
        matchingLabel = new Label("서버에 연결 중...");
        matchingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #A8D5BA; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 1);");
        
        // 로딩 애니메이션 (점 3개)
        Label loadingDots = new Label("...");
        loadingDots.setStyle("-fx-font-size: 24px; -fx-text-fill: #A8D5BA; -fx-font-weight: bold;");
        
        // 간단한 로딩 애니메이션
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(500), e -> loadingDots.setText(".")),
            new KeyFrame(Duration.millis(1000), e -> loadingDots.setText("..")),
            new KeyFrame(Duration.millis(1500), e -> loadingDots.setText("...")),
            new KeyFrame(Duration.millis(2000), e -> loadingDots.setText(""))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        
        Button cancelButton = new Button("취소");
        cancelButton.setStyle("""
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-min-width: 120px;
            -fx-min-height: 35px;
            -fx-background-color: linear-gradient(to bottom, #6B8E6B, #4A5D4A);
            -fx-text-fill: white;
            -fx-background-radius: 8px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);
            -fx-cursor: hand;
            -fx-border-color: #2F4F2F;
            -fx-border-width: 1.5px;
            -fx-border-radius: 8px;
        """);
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("""
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-min-width: 120px;
            -fx-min-height: 35px;
            -fx-background-color: linear-gradient(to bottom, #7CB68C, #556B55);
            -fx-text-fill: white;
            -fx-background-radius: 8px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 7, 0, 0, 3);
            -fx-cursor: hand;
            -fx-border-color: #2F4F2F;
            -fx-border-width: 1.5px;
            -fx-border-radius: 8px;
        """));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle("""
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-min-width: 120px;
            -fx-min-height: 35px;
            -fx-background-color: linear-gradient(to bottom, #6B8E6B, #4A5D4A);
            -fx-text-fill: white;
            -fx-background-radius: 8px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);
            -fx-cursor: hand;
            -fx-border-color: #2F4F2F;
            -fx-border-width: 1.5px;
            -fx-border-radius: 8px;
        """));
        cancelButton.setOnAction(e -> {
            if (networkClient != null && networkClient.isAlive()) {
                try {
                    networkClient.interrupt();
                } catch (Exception ex) {}
            }
            if (onBackToMenu != null) onBackToMenu.run();
        });
        
        matchingScreen.getChildren().addAll(titleLabel, matchingLabel, loadingDots, cancelButton);
        
        mainLayout = new BorderPane();
        mainLayout.setCenter(matchingScreen);
        
        Scene matchingScene = new Scene(mainLayout, 500, 400);
        matchingScene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        primaryStage.setScene(matchingScene);
        primaryStage.setTitle("온라인 매칭 중...");
    }
    
    /**
     * 매칭 상태 업데이트
     */
    public void updateMatchingStatus(String message) {
        Platform.runLater(() -> {
            if (matchingLabel != null) {
                matchingLabel.setText(message);
            }
        });
    }
    
    public void setOpponentUserId(String userId) {
        this.opponentUserId = userId;
    }

    // --- 게임 로직 및 UI 상호작용 ---

    private GridPane createBoardView() {
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("board-grid");
        gridPane.setHgap(2);
        gridPane.setVgap(2);

        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                StackPane stackPane = createTile(x, y);
                gridPane.add(stackPane, x, y);

                final int finalX = x;
                final int finalY = y;

                stackPane.setOnMouseClicked(e -> handleTileClick(finalX, finalY));
            }
        }
        return gridPane;
    }

    private void handleTileClick(int x, int y) {
        if (gameModel.isGameOver()) {
            showAlert("Game Over", "게임이 종료되었습니다! " + getWinnerMessage());
            return;
        }

        // 턴 제어
        if (gameModel.isAIMode() && gameModel.getCurrentTurn() == gameModel.getAIColor()) {
            showAlert("Wait", "AI의 턴입니다. 기다려 주세요.");
            return;
        }
        if (gameModel.isOnlineMode() && gameModel.getCurrentTurn() != myColor) {
            showAlert("Wait", "상대방의 턴입니다. 잠시 기다려 주세요.");
            return;
        }

        boolean flipped = gameModel.placePieceAndFlip(x, y);

        if (flipped) {
            // 사운드 효과 재생
            soundService.playPlaceSound();
            
            // 그래픽 효과 적용
            StackPane clickedTile = (StackPane) boardView.getChildren().get(y * WIDTH + x);
            if (clickedTile.getChildren().size() > 1) {
                javafx.scene.Node piece = clickedTile.getChildren().get(clickedTile.getChildren().size() - 1);
                if (piece instanceof Circle) {
                    Animation placeAnim = EffectService.createPlaceAnimation(piece);
                    placeAnim.play();
                    
                    // 파티클 효과 (타일의 중심 좌표 계산)
                    Color pieceColor = gameModel.getCurrentTurn() == 1 ? customBlackColor : customWhiteColor;
                    double tileCenterX = x * (TILE_SIZE + 2) + TILE_SIZE / 2;
                    double tileCenterY = y * (TILE_SIZE + 2) + TILE_SIZE / 2;
                    EffectService.createParticleEffect(boardView, tileCenterX, tileCenterY, pieceColor);
                }
            }
            
            if (gameModel.isOnlineMode()) {
                networkClient.sendMove(x, y);
            }

            updateGameViewAfterMove();

            // AI 턴 처리
            if (gameModel.isAIMode() && !gameModel.isGameOver()) {
                Platform.runLater(this::handleAITurn);
            }
        } else {
            showAlert("Invalid Move", "유효한 위치가 아닙니다.");
        }
    }

    /**
     * AI 모드 턴 처리 (AIPlayer 클래스를 호출)
     */
    private void handleAITurn() {
        if (gameModel.getCurrentTurn() != gameModel.getAIColor()) return;

        // AI가 수를 계산하는 동안 UI 멈춤 방지를 위해 쓰레드 사용
        new Thread(() -> {
            try {
                // 약간의 딜레이 추가 (AI가 생각하는 것처럼 보이게)
                Thread.sleep(700);
                
                // AI에게 현재 보드 상태를 넘기고 최적의 수를 요청 (난이도 포함)
                int[] move = aiPlayer.getBestMove(gameModel.getAIDifficulty());

                // UI 업데이트는 Platform.runLater로 메인 스레드에서 실행
                Platform.runLater(() -> {
                    if (move != null) {
                        gameModel.placePieceAndFlip(move[0], move[1]);
                        updateGameViewAfterMove();
                    } else {
                        // AI도 둘 곳이 없는 경우 (패스)
                        gameModel.switchTurn();
                        checkPassConditions();
                        updateGameViewAfterMove();
                        showAlert("AI Pass", "AI도 둘 곳이 없어 당신에게 턴이 돌아왔습니다.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("AI Error", "AI 계산 중 오류 발생: " + e.getMessage()));
            }
        }).start();
    }

    private void updateGameViewAfterMove() {
        gameModel.switchTurn();
        checkPassConditions();
        drawBoard();
        drawValidMoves();
        updateScoreDisplay();
    }

    // --- 온라인 대전 관련 메서드 (NetworkClient가 호출) ---

    public void processOpponentMove(int x, int y) {
        Platform.runLater(() -> {
            boolean flipped = gameModel.placePieceAndFlip(x, y);
            if (flipped) {
                // 사운드 효과 재생
                soundService.playPlaceSound();
                
                // 그래픽 효과 적용
                StackPane clickedTile = (StackPane) boardView.getChildren().get(y * WIDTH + x);
                if (clickedTile.getChildren().size() > 1) {
                    javafx.scene.Node piece = clickedTile.getChildren().get(clickedTile.getChildren().size() - 1);
                    if (piece instanceof Circle) {
                        Animation placeAnim = EffectService.createPlaceAnimation(piece);
                        placeAnim.play();
                    }
                }
                
                updateGameViewAfterMove();
                showAlert("Your Turn", "상대방이 수를 두었습니다. 이제 당신 차례입니다.");
            } else {
                showAlert("Sync Error", "상대방의 수 처리 중 오류 발생.");
            }
        });
    }

    public void setPlayerColor(String color) {
        Platform.runLater(() -> {
            gameModel.initializeBoard();

            if (color.equals("BLACK")) {
                myColor = 1;
                updateMatchingStatus("매칭 성공! 당신은 흑돌(Black)입니다.");
            } else if (color.equals("WHITE")) {
                myColor = 2;
                updateMatchingStatus("매칭 성공! 당신은 백돌(White)입니다.");
            }
            
            // 매칭 성공 후 잠시 대기 후 게임 화면으로 전환
            new Thread(() -> {
                try {
                    Thread.sleep(1500); // 1.5초 대기
                    Platform.runLater(() -> {
                        show(GameModel.Mode.ONLINE);
                        if (color.equals("BLACK")) {
                            showAlert("Game Start", "매칭 성공! 당신은 흑돌(Black)입니다. 선공하세요.");
                        } else if (color.equals("WHITE")) {
                            showAlert("Game Start", "매칭 성공! 당신은 백돌(White)입니다. 상대방 수를 기다리세요.");
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }

    // --- 게임 상태 체크 ---

    private void checkPassConditions() {
        if (gameModel.getValidMoves().isEmpty()) {
            showAlert("Pass", gameModel.getCurrentPlayerName() + " (현재 턴)은 둘 곳이 없어 패스합니다.");
            gameModel.switchTurn();

            if (gameModel.getValidMoves().isEmpty()) {
                gameModel.setGameOver(true);
                
                // 게임 결과 저장
                saveGameResult();
                
                showAlert("Game Over", getWinnerMessage());
            }
        }
    }
    
    /**
     * 게임 결과를 DB에 저장
     */
    private void saveGameResult() {
        // GameModel을 통해 게임 결과 저장 (아키텍처 개선: GUI가 DB를 직접 호출하지 않음)
        if (currentUser == null) {
            return;
        }
        
        // GameModel의 saveGameResult 메서드 호출
        gameModel.saveGameResult(
            currentUser.getUserId(), 
            opponentUserId, 
            myColor
        );
    }

    private String getWinnerMessage() {
        int black = gameModel.getScore(1);
        int white = gameModel.getScore(2);

        if (black > white) {
            return "흑돌 (" + black + ") 승리!";
        } else if (white > black) {
            return "백돌 (" + white + ") 승리!";
        } else {
            return "무승부입니다!";
        }
    }

    // --- UI 렌더링 메서드 ---

    private void drawValidMoves() {
        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                StackPane stackPane = (StackPane) boardView.getChildren().get(y * WIDTH + x);
                stackPane.getChildren().removeIf(node -> node instanceof Circle && node.getStyleClass().contains("valid-move"));
            }
        }

        if (!gameModel.isGameOver()) {
            List<int[]> validMoves = gameModel.getValidMoves();
            for (int[] pos : validMoves) {
                int x = pos[0];
                int y = pos[1];
                StackPane stackPane = (StackPane) boardView.getChildren().get(y * WIDTH + x);

                Circle hint = new Circle(TILE_SIZE * 0.15);
                hint.setFill(gameModel.getCurrentTurn() == 1 ? Color.DARKRED : Color.NAVY);
                hint.setOpacity(0.7);
                hint.getStyleClass().add("valid-move");

                stackPane.getChildren().add(hint);
            }
        }
    }

    private void updateScoreDisplay() {
        int black = gameModel.getScore(1);
        int white = gameModel.getScore(2);
        String turn = gameModel.getCurrentPlayerName();

        if (gameModel.isGameOver()) {
            scoreLabel.setText("🎮 게임 종료 | " + getWinnerMessage());
            scoreLabel.setFont(cinzelFont);
            scoreLabel.getStyleClass().clear();
            scoreLabel.getStyleClass().add("score-label-game-over");
            // 게임 종료 사운드 재생
            soundService.playGameOverSound();
        } else {
            scoreLabel.setText(String.format("⚫ 흑: %d  ⚪ 백: %d  |  현재 턴: %s", black, white, turn));
            scoreLabel.setFont(orbitronFont);
            scoreLabel.getStyleClass().clear();
            scoreLabel.getStyleClass().add("score-label");
        }
    }

    private void drawBoard() {
        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                StackPane stackPane = (StackPane) boardView.getChildren().get(y * WIDTH + x);
                stackPane.getChildren().removeIf(node -> node instanceof Circle);

                int piece = gameModel.getBoard()[y][x];
                if (piece != 0) {
                    stackPane.getChildren().add(createPiece(getColorForPiece(piece)));
                }
            }
        }
    }

    private StackPane createTile(int x, int y) {
        Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
        
        // 이미지 기반 바둑판 디자인 - 녹색 체크무늬 패턴
        if ((x + y) % 2 == 0) {
            // 밝은 연두색 타일 - 왼쪽 상단에서 오른쪽 하단으로 그라데이션
            javafx.scene.paint.LinearGradient lightGreenGradient = new javafx.scene.paint.LinearGradient(
                0, 0, 1, 1, true, null,
                new javafx.scene.paint.Stop(0, Color.web("#A8D5BA")), // 왼쪽 상단 - 밝은 연두색
                new javafx.scene.paint.Stop(0.5, Color.web("#8FBC8F")), // 중앙
                new javafx.scene.paint.Stop(1, Color.web("#7CB68C"))  // 오른쪽 하단 - 약간 어두운 연두색
            );
            tile.setFill(lightGreenGradient);
        } else {
            // 어두운 녹색 타일 - 왼쪽 상단에서 오른쪽 하단으로 그라데이션
            javafx.scene.paint.LinearGradient darkGreenGradient = new javafx.scene.paint.LinearGradient(
                0, 0, 1, 1, true, null,
                new javafx.scene.paint.Stop(0, Color.web("#6B8E6B")), // 왼쪽 상단 - 밝은 녹색
                new javafx.scene.paint.Stop(0.5, Color.web("#556B55")), // 중앙
                new javafx.scene.paint.Stop(1, Color.web("#4A5D4A"))  // 오른쪽 하단 - 어두운 녹색
            );
            tile.setFill(darkGreenGradient);
        }
        
        // 테두리 - 어두운 녹색, 얇은 선
        tile.setStroke(Color.web("#2F4F2F"));
        tile.setStrokeWidth(1);
        tile.setArcWidth(2);
        tile.setArcHeight(2);
        
        // 타일 사이 구분선 효과를 위한 그림자
        javafx.scene.effect.DropShadow tileShadow = new javafx.scene.effect.DropShadow();
        tileShadow.setRadius(1);
        tileShadow.setColor(Color.web("#FFFFFF22")); // 밝은 선 효과
        tileShadow.setOffsetX(0.5);
        tileShadow.setOffsetY(0.5);
        tile.setEffect(tileShadow);
        
        return new StackPane(tile);
    }

    private Circle createPiece(Color color) {
        Circle piece = new Circle(TILE_SIZE * 0.4);
        
        // 방사형 그라데이션으로 강한 3D 효과
        if (color == Color.BLACK || color.equals(customBlackColor)) {
            // 흑돌 - 중앙 상단 하이라이트에서 바깥쪽으로 어두워지는 방사형 그라데이션
            javafx.scene.paint.RadialGradient blackGradient = new javafx.scene.paint.RadialGradient(
                0,  // focusAngle
                0,  // focusDistance
                0.3,  // centerX (약간 위쪽)
                0.3,  // centerY (약간 위쪽)
                0.5,  // radius
                true,  // proportional
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.web("#4A4A4A")), // 중앙 상단 - 어두운 회색 하이라이트
                new javafx.scene.paint.Stop(0.3, Color.web("#2C2C2C")), // 중간
                new javafx.scene.paint.Stop(0.6, Color.web("#1A1A1A")), // 바깥쪽
                new javafx.scene.paint.Stop(1, Color.web("#000000"))  // 가장자리 - 깊은 검은색
            );
            piece.setFill(blackGradient);
            piece.setStroke(Color.web("#0A0A0A"));
        } else {
            // 백돌 - 중앙 상단 하이라이트에서 바깥쪽으로 어두워지는 방사형 그라데이션
            javafx.scene.paint.RadialGradient whiteGradient = new javafx.scene.paint.RadialGradient(
                0,  // focusAngle
                0,  // focusDistance
                0.3,  // centerX (약간 위쪽)
                0.3,  // centerY (약간 위쪽)
                0.5,  // radius
                true,  // proportional
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.web("#FFFFFF")), // 중앙 상단 - 밝은 흰색 하이라이트
                new javafx.scene.paint.Stop(0.3, Color.web("#F5F5F5")), // 중간
                new javafx.scene.paint.Stop(0.6, Color.web("#E0E0E0")), // 바깥쪽
                new javafx.scene.paint.Stop(1, Color.web("#C0C0C0"))  // 가장자리 - 부드러운 회색
            );
            piece.setFill(whiteGradient);
            piece.setStroke(Color.web("#BDBDBD"));
        }
        
        piece.setStrokeWidth(1.5);
        
        // 부드러운 그림자 효과 - 돌이 보드 위에 떠 있는 느낌
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setRadius(4);
        shadow.setColor(Color.web("#00000088")); // 더 진한 그림자
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        piece.setEffect(shadow);
        
        return piece;
    }

    private Color getColorForPiece(int piece) {
        if (piece == 1) return customBlackColor;
        if (piece == 2) return customWhiteColor;
        return Color.TRANSPARENT;
    }
    
    /**
     * 돌 색상 커스텀 설정 (추후 확장 가능)
     */
    public void setCustomPieceColors(Color blackColor, Color whiteColor) {
        this.customBlackColor = blackColor;
        this.customWhiteColor = whiteColor;
        // 보드 다시 그리기
        if (boardView != null) {
            drawBoard();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * 오른쪽 사이드바 패널 생성 (카드 + 버튼)
     */
    private VBox createRightPanel(Button backButton) {
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(20, 15, 20, 15));
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setMinWidth(220);
        
        // 카드 제목
        Label cardTitle = new Label("🎴 찬스카드");
        cardTitle.setFont(cinzelFont);
        cardTitle.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #f4e5b7; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 4, 0, 2, 2);"
        );
        
        // 기억력 게임 카드
        memoryCard = createSingleCard("🎲", "기억력", "MEMORY", 0);
        
        // 반응속도 게임 카드
        reactionCard = createSingleCard("⚡", "반응속도", "REACTION", 1);
        
        // 회피 게임 카드
        dodgeCard = createSingleCard("🎯", "회피게임", "DODGE", 2);
        
        // 구분선
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        spacer.setPrefHeight(15);
        
        rightPanel.getChildren().addAll(
            cardTitle,
            memoryCard, 
            reactionCard, 
            dodgeCard,
            spacer,
            backButton
        );
        
        return rightPanel;
    }
    
    /**
     * 단일 카드 생성
     */
    private VBox createSingleCard(String icon, String name, String gameType, int cardIndex) {
        // 메인 카드 컨테이너
        javafx.scene.layout.StackPane cardStack = new javafx.scene.layout.StackPane();
        cardStack.setPrefSize(180, 230);
        
        // 카드 배경
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(180, 230);
        card.getStyleClass().addAll("game-card", "card-" + gameType.toLowerCase());
        
        // 장식용 내부 테두리
        javafx.scene.shape.Rectangle innerBorder = new javafx.scene.shape.Rectangle(
            180 - 20, 230 - 20
        );
        innerBorder.setFill(Color.TRANSPARENT);
        innerBorder.setStroke(Color.web("#d4a024", 0.4));
        innerBorder.setStrokeWidth(2);
        innerBorder.setArcWidth(8);
        innerBorder.setArcHeight(8);
        
        // 상단 장식 (다이아몬드)
        Label topDecoration = new Label("◆");
        topDecoration.getStyleClass().add("card-top-decoration");
        javafx.scene.layout.StackPane.setAlignment(topDecoration, Pos.TOP_CENTER);
        javafx.scene.layout.StackPane.setMargin(topDecoration, new Insets(15, 0, 0, 0));
        
        // 카드 컨텐츠 컨테이너
        VBox cardContent = new VBox(15);
        cardContent.setAlignment(Pos.CENTER);
        
        // 카드 아이콘 (Float 애니메이션 추가)
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("card-icon");
        
        // Float 애니메이션
        javafx.animation.Timeline floatAnimation = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.ZERO,
                new javafx.animation.KeyValue(iconLabel.translateYProperty(), 0)
            ),
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(1),
                new javafx.animation.KeyValue(iconLabel.translateYProperty(), -8)
            ),
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(2),
                new javafx.animation.KeyValue(iconLabel.translateYProperty(), 0)
            )
        );
        floatAnimation.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        floatAnimation.play();
        
        // 카드 이름
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("card-name");
        
        cardContent.getChildren().addAll(iconLabel, nameLabel);
        
        // 모든 요소를 스택에 추가
        cardStack.getChildren().addAll(card, innerBorder, topDecoration, cardContent);
        
        // VBox로 래핑 (기존 코드와 호환성 유지)
        VBox wrapper = new VBox(cardStack);
        wrapper.setAlignment(Pos.CENTER);
        
        // 클릭 이벤트
        cardStack.setOnMouseClicked(e -> {
            if (!cardUsed[cardIndex]) {
                useChanceCard(gameType, cardIndex);
            }
        });
        
        // 호버 효과 (카드 올라가기 + 그림자 증가)
        javafx.animation.ScaleTransition scaleUp = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(200), cardStack
        );
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        
        javafx.animation.TranslateTransition moveUp = new javafx.animation.TranslateTransition(
            javafx.util.Duration.millis(200), cardStack
        );
        moveUp.setToY(-10);
        
        javafx.animation.ScaleTransition scaleDown = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(200), cardStack
        );
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        
        javafx.animation.TranslateTransition moveDown = new javafx.animation.TranslateTransition(
            javafx.util.Duration.millis(200), cardStack
        );
        moveDown.setToY(0);
        
        cardStack.setOnMouseEntered(e -> {
            if (!cardUsed[cardIndex]) {
                scaleUp.play();
                moveUp.play();
            }
        });
        
        cardStack.setOnMouseExited(e -> {
            scaleDown.play();
            moveDown.play();
        });
        
        // 카드 인덱스 저장 (업데이트용)
        wrapper.setUserData(new CardData(cardStack, iconLabel, floatAnimation));
        
        return wrapper;
    }
    
    /**
     * 카드 데이터 저장용 내부 클래스
     */
    private static class CardData {
        javafx.scene.layout.StackPane cardStack;
        Label iconLabel;
        javafx.animation.Timeline floatAnimation;
        
        CardData(javafx.scene.layout.StackPane cardStack, Label iconLabel, javafx.animation.Timeline floatAnimation) {
            this.cardStack = cardStack;
            this.iconLabel = iconLabel;
            this.floatAnimation = floatAnimation;
        }
    }
    
    /**
     * 찬스카드 사용
     */
    private void useChanceCard(String gameType, int cardIndex) {
        if (cardUsed[cardIndex]) {
            showAlert("카드 사용됨", "이미 사용한 카드입니다!");
            return;
        }
        
        if (gameModel.isGameOver()) {
            showAlert("게임 종료", "게임이 이미 종료되었습니다!");
            return;
        }
        
        // 온라인 모드: 상대방 턴일 때만 사용 가능
        if (gameModel.getGameMode() == GameModel.Mode.ONLINE) {
            int currentTurn = gameModel.getCurrentTurn();
            if (currentTurn == myColor) {
                showAlert("사용 불가", "상대방의 턴일 때만 찬스카드를 사용할 수 있습니다!");
                return;
            }
        }
        
        // 카드 사용 처리
        cardUsed[cardIndex] = true;
        updateCardAppearance(cardIndex);
        
        // 미니게임 실행
        startMinigame(gameType);
    }
    
    /**
     * 카드 외관 업데이트 (사용된 카드)
     */
    private void updateCardAppearance(int cardIndex) {
        VBox wrapper = null;
        switch (cardIndex) {
            case 0: wrapper = memoryCard; break;
            case 1: wrapper = reactionCard; break;
            case 2: wrapper = dodgeCard; break;
        }
        
        if (wrapper != null && wrapper.getUserData() instanceof CardData) {
            CardData data = (CardData) wrapper.getUserData();
            
            // Float 애니메이션 중지
            data.floatAnimation.stop();
            data.iconLabel.setTranslateY(0);
            data.iconLabel.setOpacity(0.5);
            
            // 카드를 회색으로 변경
            javafx.scene.layout.StackPane cardStack = data.cardStack;
            
            // card-used 스타일 추가
            if (cardStack.getChildren().size() > 0 && cardStack.getChildren().get(0) instanceof VBox) {
                VBox card = (VBox) cardStack.getChildren().get(0);
                card.getStyleClass().add("card-used");
            }
            
            // USED 스탬프 추가
            Label usedStamp = new Label("USED");
            usedStamp.getStyleClass().add("used-stamp");
            javafx.scene.layout.StackPane.setAlignment(usedStamp, Pos.CENTER);
            cardStack.getChildren().add(usedStamp);
            
            // 마우스 커서 변경
            cardStack.setDisable(true);
        }
    }
    
    /**
     * 미니게임 시작
     */
    private void startMinigame(String gameType) {
        org.example.minigame.base.MinigameBase minigame = null;
        
        switch (gameType) {
            case "MEMORY":
                minigame = new org.example.minigame.games.memory.MemoryGame();
                break;
            case "REACTION":
                minigame = new org.example.minigame.games.reaction.ReactionGame();
                break;
            case "DODGE":
                minigame = new org.example.minigame.games.dodge.DodgeGame();
                break;
            default:
                showAlert("오류", "알 수 없는 게임 타입입니다.");
                return;
        }
        
        // 온라인 모드: 상대방에게 관전 모드 알림
        if (gameModel.getGameMode() == GameModel.Mode.ONLINE && networkClient != null) {
            String startMessage = org.example.minigame.network.MinigameProtocol.createStartMessage(gameType);
            networkClient.sendMinigameStart(startMessage);
        }
        
        minigame.startPlayerMode(primaryStage, result -> {
            // 미니게임 결과 처리
            if (result.isSuccess()) {
                handleMinigameSuccess(result);
            } else {
                showAlert("미니게임 실패", 
                    "아쉽게도 미니게임에 실패했습니다.\n" +
                    "점수: " + result.getScore() + "\n" +
                    "다음 기회에 도전하세요!");
                
                // 온라인 모드: 결과 전송
                if (gameModel.getGameMode() == GameModel.Mode.ONLINE && networkClient != null) {
                    String resultMessage = org.example.minigame.network.MinigameProtocol
                        .createResultMessage(false, result.getScore(), result.getTimeElapsed());
                    networkClient.sendMinigameResult(resultMessage);
                }
            }
        });
    }
    
    /**
     * 미니게임 성공 처리
     */
    private void handleMinigameSuccess(org.example.minigame.base.MinigameResult result) {
        showAlert("미니게임 성공!", 
            "축하합니다! 미니게임에 성공했습니다!\n" +
            "점수: " + result.getScore() + "\n" +
            "소요 시간: " + result.getTimeElapsed() + "초\n\n" +
            "찬스 효과: 상대방의 다음 턴을 랜덤으로 둡니다!");
        
        // 온라인 모드: 결과 전송 및 상대방 턴 랜덤 처리
        if (gameModel.getGameMode() == GameModel.Mode.ONLINE && networkClient != null) {
            String resultMessage = org.example.minigame.network.MinigameProtocol
                .createResultMessage(true, result.getScore(), result.getTimeElapsed());
            networkClient.sendMinigameResult(resultMessage);
            
            // 상대방 턴을 랜덤으로 처리하도록 서버에 요청
            networkClient.requestRandomMove();
        } else {
            // 로컬 모드: 한 턴 스킵 (현재 턴 유지)
            // 현재 플레이어가 다시 놓을 수 있음
        }
    }
    
    /**
     * 상대방이 미니게임을 시작했을 때 관전 모드 표시
     */
    public void showMinigameSpectator(String gameType) {
        Platform.runLater(() -> {
            org.example.minigame.base.MinigameBase minigame = null;
            
            switch (gameType) {
                case "MEMORY":
                    minigame = new org.example.minigame.games.memory.MemoryGame();
                    break;
                case "REACTION":
                    minigame = new org.example.minigame.games.reaction.ReactionGame();
                    break;
                case "DODGE":
                    minigame = new org.example.minigame.games.dodge.DodgeGame();
                    break;
                default:
                    return;
            }
            
            minigame.startSpectatorMode(primaryStage);
        });
    }
    
    /**
     * 미니게임 성공으로 인한 랜덤 수 처리
     */
    public void handleRandomMove() {
        Platform.runLater(() -> {
            List<int[]> validMoves = gameModel.getValidMoves();
            if (validMoves.isEmpty()) {
                // 유효한 수가 없으면 턴 패스
                gameModel.switchTurn();
                drawBoard();
                updateScoreDisplay();
                return;
            }
            
            // 랜덤으로 하나 선택
            java.util.Random random = new java.util.Random();
            int[] randomMove = validMoves.get(random.nextInt(validMoves.size()));
            
            // 자동으로 수 놓기
            gameModel.placePieceAndFlip(randomMove[0], randomMove[1]);
            drawBoard();
            updateScoreDisplay();
            
            // 게임 종료 확인
            if (gameModel.isGameOver()) {
                handleGameOver();
            }
        });
    }
    
    /**
     * 게임 종료 처리
     */
    private void handleGameOver() {
        int blackScore = gameModel.getScore(1); // 1 = BLACK
        int whiteScore = gameModel.getScore(2); // 2 = WHITE
        
        String winner;
        if (blackScore > whiteScore) {
            winner = "흑돌 승리!";
        } else if (whiteScore > blackScore) {
            winner = "백돌 승리!";
        } else {
            winner = "무승부!";
        }
        
        showAlert("게임 종료", 
            winner + "\n\n" +
            "흑: " + blackScore + " vs 백: " + whiteScore);
    }
}

