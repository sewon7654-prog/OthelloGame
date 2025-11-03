package org.example;

import javafx.application.Application;
import javafx.application.Platform;
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

import java.util.List;

public class Main extends Application {

    private static final int TILE_SIZE = 70;
    private static final int WIDTH = 8;

    // Core Game Components
    private GameModel gameModel;
    private NetworkClient networkClient;
    private AIPlayer aiPlayer; // AI Player Instance
    private int myColor = 0; // 1: Black, 2: White, 0: Not assigned

    // GUI Components
    private Stage primaryStage;
    private BorderPane mainLayout;
    private GridPane boardView;
    private Label scoreLabel;

    private Color getColorForPiece(int piece) {
        if (piece == 1) return Color.BLACK;
        if (piece == 2) return Color.WHITE;
        return Color.TRANSPARENT;
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        gameModel = new GameModel();
        aiPlayer = new AIPlayer(gameModel); // AI Player 초기화 (Gemini 호출 클래스)

        showStartMenu();
    }

    // --- 최종 목표: 게임 시작 메뉴 구현 ---

    private void showStartMenu() {
        Label title = new Label("오셀로 게임 모드 선택");
        title.setStyle("-fx-font-size: 24px; -fx-padding: 20px;");

        Button btnLocal = new Button("로컬 2인 대전");
        Button btnOnline = new Button("온라인 1:1 대전");
        Button btnAI = new Button("AI와 대전 (Gemini)");

        btnLocal.setOnAction(e -> startGame(GameModel.Mode.LOCAL));
        btnOnline.setOnAction(e -> startOnlineMatch());
        btnAI.setOnAction(e -> startGame(GameModel.Mode.AI));

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.getChildren().addAll(title, btnLocal, btnOnline, btnAI);

        Scene menuScene = new Scene(menuBox, 400, 400);
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Othello Game - Mode Selection");
        primaryStage.show();
    }

    // --- 게임 시작 (Local/AI) ---

    private void startGame(GameModel.Mode mode) {
        gameModel.setGameMode(mode);
        gameModel.initializeBoard();

        boardView = createBoardView();
        scoreLabel = new Label();

        Button backButton = new Button("메뉴로 돌아가기");
        backButton.setOnAction(e -> showStartMenu());

        mainLayout = new BorderPane();
        HBox topControls = new HBox(10, backButton);
        topControls.setAlignment(Pos.CENTER_LEFT);

        GridPane bottomPanel = new GridPane();
        bottomPanel.add(scoreLabel, 0, 0);
        bottomPanel.add(topControls, 1, 0);

        mainLayout.setCenter(boardView);
        mainLayout.setBottom(bottomPanel);

        // AI 모드 선공일 경우 바로 AI 턴 시작
        if (mode == GameModel.Mode.AI && gameModel.getCurrentTurn() == gameModel.getAIColor()) {
            Platform.runLater(this::handleAITurn);
        }

        drawBoard();
        drawValidMoves();
        updateScoreDisplay();

        Scene gameScene = new Scene(mainLayout, WIDTH * TILE_SIZE, WIDTH * TILE_SIZE + 100);
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Othello Game - " + mode.name() + " Mode");
    }

    // --- F-10 온라인 매칭 시작 ---

    public void startOnlineMatch() {
        gameModel.setGameMode(GameModel.Mode.ONLINE);
        startGame(GameModel.Mode.ONLINE);

        if (gameModel.isOnlineMode() && networkClient != null && networkClient.isAlive()) return;

        networkClient = new NetworkClient(this);
        if (networkClient.connect()) {
            networkClient.start();
            showAlert("Matching", "서버에 연결되었습니다. 상대방을 기다리는 중...");
        } else {
            showAlert("Connection Failed", "서버 접속에 실패했습니다. NetworkServer를 실행했는지 확인하세요.");
            showStartMenu();
        }
    }

    // --- F-03/F-04 Game Logic and UI Interaction ---

    private GridPane createBoardView() {
        GridPane gridPane = new GridPane();
        gridPane.setStyle("-fx-background-color: #333333;");

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
                // AI에게 현재 보드 상태를 넘기고 최적의 수를 요청
                int[] move = aiPlayer.getBestMove();

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

    // --- 이하 코드는 이전과 거의 동일 (UI/Logic 업데이트) ---

    private void updateGameViewAfterMove() {
        gameModel.switchTurn();
        checkPassConditions();
        drawBoard();
        drawValidMoves();
        updateScoreDisplay();
    }

    public void processOpponentMove(int x, int y) {
        Platform.runLater(() -> {
            boolean flipped = gameModel.placePieceAndFlip(x, y);
            if (flipped) {
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
                showAlert("Game Start", "매칭 성공! 당신은 흑돌(Black)입니다. 선공하세요.");
            } else if (color.equals("WHITE")) {
                myColor = 2;
                showAlert("Game Start", "매칭 성공! 당신은 백돌(White)입니다. 상대방 수를 기다리세요.");
            }
            drawBoard();
            drawValidMoves();
            updateScoreDisplay();
        });
    }

    private void checkPassConditions() {
        if (gameModel.getValidMoves().isEmpty()) {
            showAlert("Pass", gameModel.getCurrentPlayerName() + " (현재 턴)은 둘 곳이 없어 패스합니다.");
            gameModel.switchTurn();

            if (gameModel.getValidMoves().isEmpty()) {
                gameModel.setGameOver(true);
                showAlert("Game Over", getWinnerMessage());
            }
        }
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
            scoreLabel.setText("GAME OVER | " + getWinnerMessage());
        } else {
            scoreLabel.setText(String.format("Score: Black (%d) vs White (%d). Current Turn: %s", black, white, turn));
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
        if ((x + y) % 2 == 0) {
            tile.setFill(Color.web("#2ECC71"));
        } else {
            tile.setFill(Color.web("#27AE60"));
        }
        tile.setStroke(Color.DARKGRAY);
        tile.setStrokeWidth(1.5);
        return new StackPane(tile);
    }

    private Circle createPiece(Color color) {
        Circle piece = new Circle(TILE_SIZE * 0.4);
        piece.setFill(color);
        piece.setStroke(color == Color.BLACK ? Color.GRAY : Color.DARKGRAY);
        piece.setStrokeWidth(2);
        return piece;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}