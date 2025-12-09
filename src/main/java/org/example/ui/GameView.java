package org.example.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
import org.example.service.ButtonEffectService;
import org.example.service.DatabaseService;
import org.example.service.SoundService;
import org.example.service.EffectService;
import org.example.service.PixelArtBackgroundService;
import org.example.service.PixelArtUIService;

import java.util.List;
import java.util.Map;

/**
 * 게임 화면 UI 및 게임 로직을 담당하는 클래스
 * 추후 UI 변경 시 이 클래스만 수정하면 됨
 */
public class GameView {

    private static final int TILE_SIZE = 75; // 화면 비율에 맞게 조정
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
    private boolean isProcessingMove = false; // 빠른 연속 클릭 방지
    
    // 커스텀 색상 설정 (기본값)
    private Color customBlackColor = Color.BLACK;
    private Color customWhiteColor = Color.WHITE;

    // GUI Components
    private Stage primaryStage;
    private BorderPane mainLayout;
    private StackPane gameRootPane; // 픽셀 아트 배경을 포함한 루트
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
    private org.example.minigame.base.MinigameBase activeMinigame;
    private int minigameOwnerColor = 0;

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
        ButtonEffectService.addPixelArtButtonEffects(backButton);
        ButtonEffectService.addClickParticleEffect(backButton);
        backButton.setOnAction(e -> {
            // 게임이 종료된 상태면 종료 사운드 재생
            if (gameModel.isGameOver()) {
                try {
                    System.out.println("[게임 종료] 종료 사운드 재생 시도");
                    soundService.playGameOverSound();
                    System.out.println("[게임 종료] 종료 사운드 재생 완료");
                } catch (Exception ex) {
                    System.err.println("게임 종료 사운드 재생 실패: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                // 게임이 진행 중이어도 메뉴로 돌아갈 때 종료 사운드 재생
                try {
                    System.out.println("[메뉴 복귀] 종료 사운드 재생 시도");
                    soundService.playGameOverSound();
                    System.out.println("[메뉴 복귀] 종료 사운드 재생 완료");
                } catch (Exception ex) {
                    System.err.println("게임 종료 사운드 재생 실패: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            soundService.stopBGM();
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
        // 픽셀 아트 배경 적용
        javafx.scene.paint.Paint woodPaint = PixelArtBackgroundService.createWoodTexture();
        PixelArtBackgroundService.applyPixelArtBackground(topPanel, woodPaint);
        topPanel.getChildren().addAll(modeLabel, scoreLabel);

        // 보드를 중앙 정렬하기 위한 컨테이너
        StackPane boardContainer = new StackPane();
        boardContainer.setAlignment(Pos.CENTER);
        boardContainer.getChildren().add(boardView);
        boardContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        BorderPane.setAlignment(boardContainer, Pos.CENTER);

        // 화면 크기 계산 (메뉴 화면과 동일한 크기로 고정)
        double screenWidth = 1600;
        double screenHeight = 1000;

        // 픽셀 아트 배경 생성 (메뉴 화면과 동일한 크기)
        StackPane backgroundPane = PixelArtUIService.createPixelArtBackground(screenWidth, screenHeight);

        mainLayout = new BorderPane();
        mainLayout.setTop(topPanel);
        mainLayout.setCenter(boardContainer);
        mainLayout.getStyleClass().add("game-container");
        mainLayout.setStyle("-fx-background-color: transparent;");
        
        // 배경과 게임을 스택으로 합치기
        this.gameRootPane = new StackPane();
        this.gameRootPane.getChildren().addAll(backgroundPane, mainLayout);
        
        // 로컬/온라인 모드: 찬스카드를 오른쪽에 배치
        if (mode == GameModel.Mode.LOCAL || mode == GameModel.Mode.ONLINE) {
            VBox rightPanel = createRightPanel(backButton);
            BorderPane.setAlignment(rightPanel, Pos.TOP_CENTER); // 상단 정렬
            mainLayout.setRight(rightPanel);
            resetChanceCards();
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

        // 화면 크기는 메뉴 화면과 동일하게 고정
        Scene gameScene = new Scene(gameRootPane, screenWidth, screenHeight);
        gameScene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        gameScene.getStylesheets().add(getClass().getResource("/css/game.css").toExternalForm());
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Othello Game - " + modeText);
        
        // 창 모드 설정 (창 제어 버튼 표시)
        // initStyle은 App 시작 시 1회만 설정 가능하므로 여기서는 호출하지 않는다
        primaryStage.setFullScreen(false); // 전체화면 아님
        primaryStage.setResizable(true); // 크기 조절 가능
        primaryStage.setMinWidth(screenWidth);
        primaryStage.setMinHeight(screenHeight);
        
        // 전체화면 전환 단축키 (F11)
        gameScene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.F11) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
            }
        });
        
        primaryStage.show();
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
            soundService.stopBGM();
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
            soundService.stopBGM();
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
        this.boardView = gridPane;
        gridPane.getStyleClass().add("board-grid");
        gridPane.setHgap(0);
        gridPane.setVgap(0);
        gridPane.setPadding(Insets.EMPTY);

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

        // 빠른 연속 클릭 방지
        if (isProcessingMove) {
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

        // 유효한 수인지 먼저 확인
        List<int[]> validMoves = gameModel.getValidMoves();
        boolean isValidMove = false;
        for (int[] move : validMoves) {
            if (move[0] == x && move[1] == y) {
                isValidMove = true;
                break;
            }
        }

        if (!isValidMove) {
            showAlert("Invalid Move", "유효한 위치가 아닙니다.");
            return;
        }

        // 플래그 설정
        isProcessingMove = true;

        // 방금 놓을 돌의 색상 저장 (턴이 바뀌기 전)
        int currentTurnBeforeMove = gameModel.getCurrentTurn();
        Color pieceColor = currentTurnBeforeMove == 1 ? customBlackColor : customWhiteColor;

        // 돌 놓기
        boolean flipped = gameModel.placePieceAndFlip(x, y);

        if (flipped) {
            // 사운드 효과 재생
            soundService.playPlaceSound();
            
            if (gameModel.isOnlineMode()) {
                networkClient.sendMove(x, y);
            }

            // 보드 업데이트 (돌이 그려진 후)
            updateGameViewAfterMove();

            // 보드 업데이트 후 효과 적용 (drawBoard() 이후에 돌이 그려져 있음)
            Platform.runLater(() -> {
                StackPane clickedTile = (StackPane) boardView.getChildren().get(y * WIDTH + x);
                
                // 돌 찾기 (가장 마지막 Circle이 방금 놓은 돌)
                Circle placedPiece = null;
                for (int i = clickedTile.getChildren().size() - 1; i >= 0; i--) {
                    javafx.scene.Node node = clickedTile.getChildren().get(i);
                    if (node instanceof Circle && !node.getStyleClass().contains("valid-move")) {
                        placedPiece = (Circle) node;
                        break;
                    }
                }
                
                if (placedPiece != null) {
                    // 돌 놓기 애니메이션
                    Animation placeAnim = EffectService.createPlaceAnimation(placedPiece);
                    placeAnim.play();
                    
                    // 타일 내부에 파티클 컨테이너 추가
                    javafx.scene.layout.Pane particleContainer = new javafx.scene.layout.Pane();
                    particleContainer.setPrefSize(TILE_SIZE, TILE_SIZE);
                    particleContainer.setMouseTransparent(true);
                    clickedTile.getChildren().add(particleContainer);
                    
                    // 타일 중심에 파티클 효과 적용
                    double tileCenterX = TILE_SIZE / 2;
                    double tileCenterY = TILE_SIZE / 2;
                    EffectService.createParticleEffect(particleContainer, tileCenterX, tileCenterY, pieceColor);
                }
                
                // 플래그 해제 (애니메이션 시간 후)
                Timeline delay = new Timeline(new KeyFrame(Duration.millis(500), e -> {
                    isProcessingMove = false;
                }));
                delay.play();
            });

            // AI 턴 처리
            if (gameModel.isAIMode() && !gameModel.isGameOver()) {
                Platform.runLater(this::handleAITurn);
            }
        } else {
            isProcessingMove = false;
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
        
        // 픽셀 아트 스타일 - 밝은 색상의 단색 타일
        if ((x + y) % 2 == 0) {
            // 밝은 연두색 타일 (픽셀 아트 스타일 - 단색)
            tile.setFill(Color.web("#A8E6CF")); // 밝은 연두색
        } else {
            // 밝은 녹색 타일 (픽셀 아트 스타일 - 단색)
            tile.setFill(Color.web("#7FCDBB")); // 밝은 청록색
        }
        
        // 픽셀 아트 스타일 테두리 - 명확한 검은색 선
        tile.setStroke(Color.web("#2D5016")); // 어두운 녹색 테두리
        tile.setStrokeWidth(2); // 두꺼운 테두리로 픽셀 아트 느낌
        tile.setArcWidth(0); // 각진 모서리 (픽셀 아트 스타일)
        tile.setArcHeight(0); // 각진 모서리
        
        return new StackPane(tile);
    }

                private Circle createPiece(Color color) {
        Circle piece = new Circle(TILE_SIZE * 0.4);

        // 전달된 색상을 그대로 사용하고, 테두리는 약간 어둡게 적용
        piece.setFill(color);
        piece.setStroke(color.darker());
        piece.setStrokeWidth(3);

        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setRadius(3);
        shadow.setOffsetX(1);
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

    private void showAlert(String title, String message) {
        GameDialog.showInfo(primaryStage, title, message);
    }
    
    /**
     * 오른쪽 사이드바 패널 생성 (카드 + 버튼)
     */
    private VBox createRightPanel(Button backButton) {
        VBox rightPanel = new VBox(8);
        rightPanel.setPadding(new Insets(8, 10, 8, 10));
        rightPanel.setAlignment(Pos.TOP_CENTER); // 상단 정렬로 변경
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setMinWidth(220);
        rightPanel.setMinHeight(800); // 최소 높이 명시적 설정
        
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
        cardStack.setPrefSize(170, 210);
        
        // 카드 배경
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(170, 210);
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
        iconLabel.setStyle("-fx-font-size: 56px;"); // 아이콘(이모지) 크기 확대
        
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
    
    private void resetChanceCards() {
        cardUsed = new boolean[]{false, false, false};
        resetSingleCard(memoryCard);
        resetSingleCard(reactionCard);
        resetSingleCard(dodgeCard);
    }

    private void resetSingleCard(VBox wrapper) {
        if (wrapper == null || !(wrapper.getUserData() instanceof CardData data)) {
            return;
        }

        javafx.scene.layout.StackPane cardStack = data.cardStack;
        cardStack.setDisable(false);
        data.iconLabel.setOpacity(1.0);
        data.iconLabel.setTranslateY(0);
        if (data.floatAnimation != null) {
            data.floatAnimation.stop();
            data.floatAnimation.playFromStart();
        }

        if (cardStack.getChildren().size() > 0 && cardStack.getChildren().get(0) instanceof VBox) {
            VBox card = (VBox) cardStack.getChildren().get(0);
            card.getStyleClass().remove("card-used");
        }

        cardStack.getChildren().removeIf(node ->
            (node instanceof Label && "USED".equals(((Label) node).getText())) ||
            (node.getStyleClass() != null && node.getStyleClass().contains("used-stamp"))
        );
    }

    /**
     * 미니게임 시작
     */
    private void startMinigame(String gameType) {
        // 찬스카드 사용자의 색을 올바르게 저장
        // 찬스카드는 상대방 턴일 때 사용하므로, 찬스카드 사용자는 현재 턴의 반대편
        if (gameModel.getGameMode() == GameModel.Mode.ONLINE) {
            // 온라인 모드: 찬스카드를 사용한 사람은 나(myColor)
            minigameOwnerColor = myColor;
        } else {
            // 로컬 모드: 상대방 턴일 때 찬스카드 사용하므로, 찬스카드 사용자는 현재 턴의 반대편
            minigameOwnerColor = gameModel.getCurrentTurn() == 1 ? 2 : 1;
        }
        System.out.println("[미니게임 시작] minigameOwnerColor(찬스카드 사용자): " + minigameOwnerColor + 
                          ", 현재 턴: " + gameModel.getCurrentTurn());
        org.example.minigame.base.MinigameBase minigame;
        switch (gameType) {
            case "MEMORY" -> minigame = new org.example.minigame.games.memory.MemoryGame();
            case "REACTION" -> minigame = new org.example.minigame.games.reaction.ReactionGame();
            case "DODGE" -> minigame = new org.example.minigame.games.dodge.DodgeGame();
            default -> {
                showAlert("오류", "지원하지 않는 게임 타입입니다.");
                return;
            }
        }

        if (gameModel.getGameMode() == GameModel.Mode.ONLINE && networkClient != null) {
            String startMessage = org.example.minigame.network.MinigameProtocol.createStartMessage(gameType);
            networkClient.sendMinigameStart(startMessage);
        }

        activeMinigame = minigame;
        if (gameModel.getGameMode() == GameModel.Mode.ONLINE && networkClient != null) {
            if (minigame instanceof org.example.minigame.games.memory.MemoryGame memoryGame) {
                memoryGame.setUpdatePublisher(state ->
                    networkClient.sendMinigameUpdate(
                        org.example.minigame.network.MinigameProtocol.createUpdateMessage(state)
                    )
                );
            } else if (minigame instanceof org.example.minigame.games.reaction.ReactionGame reactionGame) {
                reactionGame.setUpdatePublisher(state ->
                    networkClient.sendMinigameUpdate(
                        org.example.minigame.network.MinigameProtocol.createUpdateMessage(state)
                    )
                );
            } else if (minigame instanceof org.example.minigame.games.dodge.DodgeGame dodgeGame) {
                dodgeGame.setUpdatePublisher(state ->
                    networkClient.sendMinigameUpdate(
                        org.example.minigame.network.MinigameProtocol.createUpdateMessage(state)
                    )
                );
            }
        }

        minigame.startPlayerMode(primaryStage, result -> {
            activeMinigame = null;
            if (result.isSuccess()) {
                handleMinigameSuccess(result, minigameOwnerColor);
            } else {
                // 미니게임 패배 사운드 재생 (게임 종료 사운드 사용)
                Platform.runLater(() -> {
                    try {
                        System.out.println("[미니게임] 패배 사운드 재생 시도 (게임 종료 사운드 사용)");
                        soundService.playGameOverSound();
                        System.out.println("[미니게임] 패배 사운드 재생 완료");
                    } catch (Exception e) {
                        System.err.println("미니게임 패배 사운드 재생 실패: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    showAlert("미니게임 실패",
                        "아쉽게도 미니게임에 실패했습니다.\n" +
                        "점수: " + result.getScore() + "\n" +
                        "다음 기회를 노려보세요.");

                    if (gameModel.getGameMode() == GameModel.Mode.ONLINE && networkClient != null) {
                        String resultMessage = org.example.minigame.network.MinigameProtocol
                            .createResultMessage(false, result.getScore(), result.getTimeElapsed(), -1, -1);
                        networkClient.sendMinigameResult(resultMessage);
                    }
                });
            }
            minigameOwnerColor = 0;
        });
    }

    private void handleMinigameSuccess(org.example.minigame.base.MinigameResult result, int ownerColor) {
        // 미니게임 승리 사운드 재생 (즉시 재생)
        try {
            soundService.playMinigameWinSound();
        } catch (Exception e) {
            System.err.println("미니게임 승리 사운드 재생 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Platform.runLater로 UI 스레드에서 실행 보장 (실시간 업데이트)
        Platform.runLater(() -> {
            
            // 1) 먼저 이점 적용: 상대 돌 강제 수 + 턴 유지
            int opponentColor = ownerColor == 1 ? 2 : 1;
            
            // 디버깅: 현재 상태 확인
            System.out.println("[찬스카드 성공] ownerColor(찬스카드 사용자): " + ownerColor + 
                              ", opponentColor(상대방): " + opponentColor + 
                              ", 현재 턴: " + gameModel.getCurrentTurn());
            
            int[] forcedMove = pickRandomMoveFor(opponentColor);
            if (forcedMove != null) {
                System.out.println("[찬스카드 성공] 상대방 랜덤 수: (" + forcedMove[0] + ", " + forcedMove[1] + ")");
                applyForcedMove(opponentColor, ownerColor, forcedMove);
            } else {
                System.out.println("[찬스카드 성공] 상대방의 유효한 수가 없습니다.");
                // 유효한 수가 없으면 턴만 찬스카드 사용자로 설정
                gameModel.setCurrentTurn(ownerColor);
                drawBoard();
                updateScoreDisplay();
                drawValidMoves();
            }

            // 2) 온라인이면 좌표 포함해 결과 전송
            if (gameModel.getGameMode() == GameModel.Mode.ONLINE && networkClient != null) {
                String resultMessage = org.example.minigame.network.MinigameProtocol
                    .createResultMessage(true, result.getScore(), result.getTimeElapsed(),
                            forcedMove != null ? forcedMove[0] : -1,
                            forcedMove != null ? forcedMove[1] : -1);
                networkClient.sendMinigameResult(resultMessage);
            }

            // 3) 미니게임 성공 애니메이션 효과 추가
            if (gameRootPane != null) {
                EffectService.createMinigameSuccessEffect(gameRootPane);
            } else if (mainLayout != null) {
                EffectService.createMinigameSuccessEffect(mainLayout);
            }
            
            // 4) 알림은 마지막에 안전하게 표시
            showAlert("미니게임 성공!",
                "축하합니다! 미니게임에 성공했습니다.\n" +
                "점수: " + result.getScore() + "\n" +
                "소요 시간: " + result.getTimeElapsed() + "초\n\n" +
                "찬스 효과: 상대의 돌을 강제 랜덤 수로 둔 뒤 내 턴을 유지합니다.");
        });
    }

    public void showMinigameSpectator(String gameType) {
        Platform.runLater(() -> {
            // 상대가 미니게임을 시작했으므로 관전자는 상대 색을 owner로 기록
            minigameOwnerColor = (myColor == 1) ? 2 : 1;
            org.example.minigame.base.MinigameBase minigame;
            switch (gameType) {
                case "MEMORY" -> minigame = new org.example.minigame.games.memory.MemoryGame();
                case "REACTION" -> minigame = new org.example.minigame.games.reaction.ReactionGame();
                case "DODGE" -> minigame = new org.example.minigame.games.dodge.DodgeGame();
                default -> { return; }
            }
            activeMinigame = minigame;
            minigame.startSpectatorMode(primaryStage);
        });
    }

    public void onMinigameUpdate(String json) {
        if (activeMinigame != null) {
            Platform.runLater(() -> activeMinigame.updateFromJson(json));
        }
    }

    public void handleMinigameResultFromNetwork(boolean success, int score, long time, int forcedX, int forcedY) {
        Platform.runLater(() -> {
            if (activeMinigame != null) {
                activeMinigame.closeGame();
                activeMinigame = null;
            }
            int ownerColor = minigameOwnerColor != 0 ? minigameOwnerColor : (myColor == 1 ? 2 : 1);
            int opponentColor = ownerColor == 1 ? 2 : 1;
            if (success) {
                int[] move = (forcedX >= 0 && forcedY >= 0) ? new int[]{forcedX, forcedY} : pickRandomMoveFor(opponentColor);
                if (move != null) {
                    applyForcedMove(opponentColor, ownerColor, move);
                }
            } else {
                gameModel.setCurrentTurn(opponentColor);
                drawValidMoves();
            }
            minigameOwnerColor = 0;
        });
    }

    public void handleRandomMove() {
        Platform.runLater(() -> {
            int moveColor = gameModel.getCurrentTurn();
            int returnColor = minigameOwnerColor != 0 ? minigameOwnerColor : (moveColor == 1 ? 2 : 1);
            int[] move = pickRandomMoveFor(moveColor);
            if (move != null) {
                applyForcedMove(moveColor, returnColor, move);
            } else {
                gameModel.setCurrentTurn(returnColor);
                drawBoard();
                updateScoreDisplay();
                drawValidMoves();
            }
            if (gameModel.isGameOver()) {
                handleGameOver();
            }
        });
    }

    private int[] pickRandomMoveFor(int playerColor) {
        java.util.List<int[]> validMoves = gameModel.getValidMovesFor(playerColor);
        if (validMoves == null || validMoves.isEmpty()) {
            return null;
        }
        java.util.Random random = new java.util.Random();
        return validMoves.get(random.nextInt(validMoves.size()));
    }

    private void applyForcedMove(int moveColor, int returnTurnColor, int[] move) {
        // 디버깅: 강제 수 실행 전 상태
        int beforeTurn = gameModel.getCurrentTurn();
        System.out.println("[강제 수 실행 전] 현재 턴: " + beforeTurn + 
                          ", moveColor(상대방 색): " + moveColor + 
                          ", returnTurnColor(찬스카드 사용자 색): " + returnTurnColor);
        
        // 상대방 색으로 턴 설정
        gameModel.setCurrentTurn(moveColor);
        System.out.println("[강제 수 실행] 턴을 상대방 색(" + moveColor + ")으로 설정");
        
        // 상대방 색으로 돌 놓기
        boolean success = gameModel.placePieceAndFlip(move[0], move[1]);
        System.out.println("[강제 수 실행] 돌 놓기 결과: " + (success ? "성공" : "실패") + 
                          ", 위치: (" + move[0] + ", " + move[1] + ")");
        
        if (success) {
            // 찬스카드 사용자 색으로 턴 복귀
            gameModel.setCurrentTurn(returnTurnColor);
            System.out.println("[강제 수 실행] 턴을 찬스카드 사용자 색(" + returnTurnColor + ")으로 복귀");
        } else {
            // 실패 시 원래 턴으로 복귀
            gameModel.setCurrentTurn(beforeTurn);
            System.out.println("[강제 수 실행] 실패 - 원래 턴(" + beforeTurn + ")으로 복귀");
        }
        
        // 모든 UI 업데이트를 한 번에 실행 (동기화 보장)
        Platform.runLater(() -> {
            drawBoard();
            updateScoreDisplay();
            drawValidMoves();
            
            System.out.println("[강제 수 실행 후] 최종 턴: " + gameModel.getCurrentTurn());
            
            if (gameModel.isGameOver()) {
                handleGameOver();
            }
        });
    }

    private void handleGameOver() {
        // 게임 종료 사운드 재생
        try {
            soundService.playGameOverSound();
        } catch (Exception e) {
            System.err.println("게임 종료 사운드 재생 실패: " + e.getMessage());
        }
        // BGM 중지
        soundService.stopBGM();
        
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




