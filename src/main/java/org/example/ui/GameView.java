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

    private static final int TILE_SIZE = 70;
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
        modeLabel.getStyleClass().add("mode-label");
        
        VBox topPanel = new VBox(8);
        topPanel.setPadding(new Insets(15));
        topPanel.setAlignment(Pos.CENTER);
        topPanel.getStyleClass().add("game-top-panel");
        topPanel.getChildren().addAll(modeLabel, scoreLabel);

        // 하단 패널 (점수 및 버튼)
        HBox bottomPanel = new HBox(15);
        bottomPanel.setPadding(new Insets(15));
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.getStyleClass().add("game-bottom-panel");
        bottomPanel.getChildren().add(backButton);

        // 보드를 중앙 정렬하기 위한 컨테이너
        StackPane boardContainer = new StackPane();
        boardContainer.setAlignment(Pos.CENTER);
        boardContainer.getChildren().add(boardView);

        mainLayout = new BorderPane();
        mainLayout.setTop(topPanel);
        mainLayout.setCenter(boardContainer);
        mainLayout.setBottom(bottomPanel);
        mainLayout.getStyleClass().add("game-container");

        // AI 모드 선공일 경우 바로 AI 턴 시작
        if (mode == GameModel.Mode.AI && gameModel.getCurrentTurn() == gameModel.getAIColor()) {
            Platform.runLater(this::handleAITurn);
        }

        drawBoard();
        drawValidMoves();
        updateScoreDisplay();

        Scene gameScene = new Scene(mainLayout, WIDTH * TILE_SIZE + 40, WIDTH * TILE_SIZE + 180);
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
            scoreLabel.getStyleClass().clear();
            scoreLabel.getStyleClass().add("score-label-game-over");
            // 게임 종료 사운드 재생
            soundService.playGameOverSound();
        } else {
            scoreLabel.setText(String.format("⚫ 흑: %d  ⚪ 백: %d  |  현재 턴: %s", black, white, turn));
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
}

