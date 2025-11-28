package org.example.minigame.games.dodge;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * 회피 게임 JavaFX view
 */
public class DodgeGameView {
    private Stage gameStage;
    private Label timeLabel;
    private Label scoreLabel;
    private Pane gameArea;
    private Circle player;
    private Button startButton;
    private Label instructionLabel;
    private Label statusLabel;

    private static Font cinzelFont;
    private static Font orbitronFont;

    static {
        try {
            cinzelFont = Font.loadFont(
                DodgeGameView.class.getResourceAsStream("/fonts/Cinzel-Bold.ttf"), 32
            );
            orbitronFont = Font.loadFont(
                DodgeGameView.class.getResourceAsStream("/fonts/Orbitron-Bold.ttf"), 14
            );
        } catch (Exception ignored) {}
    }

    public void createGameWindow(Stage parentStage, String title) {
        gameStage = new Stage();
        gameStage.initModality(Modality.WINDOW_MODAL);
        gameStage.initOwner(parentStage);
        gameStage.setTitle(title);
    }

    public VBox createGameUI(Runnable onStart, Consumer<Double> onMove) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("dodge-game-root");

        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("dodge-header");

        Label titleLabel = new Label("DODGE GAME");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.getStyleClass().add("dodge-game-title");
        if (cinzelFont != null) {
            titleLabel.setFont(Font.font(cinzelFont.getFamily(), 32));
        }

        HBox infoPanel = createInfoPanel();
        header.getChildren().addAll(titleLabel, infoPanel);

        // 게임 영역
        gameArea = new Pane();
        gameArea.setPrefSize(500, 400);
        gameArea.getStyleClass().add("dodge-game-area");
        gameArea.setFocusTraversable(true);

        // 플레이어 생성
        player = new Circle(15, Color.web("#4a90e2"));
        player.setLayoutX(250);
        player.setLayoutY(350);
        player.getStyleClass().add("dodge-player");
        gameArea.getChildren().add(player);

        startButton = createStartButton(onStart);
        startButton.getStyleClass().add("dodge-start-button");
        if (orbitronFont != null) {
            startButton.setFont(Font.font(orbitronFont.getFamily(), 14));
        }

        statusLabel = new Label("READY TO START");
        statusLabel.getStyleClass().add("dodge-status-label");
        if (orbitronFont != null) {
            statusLabel.setFont(Font.font(orbitronFont.getFamily(), 16));
        }

        instructionLabel = new Label("USE ARROW KEYS TO DODGE OBSTACLES!");
        instructionLabel.getStyleClass().add("dodge-instruction");
        if (orbitronFont != null) {
            instructionLabel.setFont(Font.font(orbitronFont.getFamily(), 13));
        }

        VBox centerArea = new VBox(20);
        centerArea.setAlignment(Pos.CENTER);
        centerArea.getChildren().addAll(startButton, statusLabel);

        StackPane gameContainer = new StackPane();
        gameContainer.getChildren().addAll(gameArea, centerArea);

        root.getChildren().addAll(header, gameContainer, instructionLabel);
        return root;
    }

    private HBox createInfoPanel() {
        HBox infoPanel = new HBox(20);
        infoPanel.setAlignment(Pos.CENTER);
        infoPanel.getStyleClass().add("dodge-info-panel");

        VBox timeBox = new VBox(5);
        timeBox.setAlignment(Pos.CENTER);
        timeBox.getStyleClass().add("dodge-stat-box");
        Label timeTitle = new Label("TIME");
        timeTitle.getStyleClass().add("dodge-stat-title");
        if (orbitronFont != null) {
            timeTitle.setFont(Font.font(orbitronFont.getFamily(), 11));
        }
        timeLabel = new Label("0:10");
        timeLabel.getStyleClass().add("dodge-time-value");
        if (orbitronFont != null) {
            timeLabel.setFont(Font.font(orbitronFont.getFamily(), 18));
        }
        timeBox.getChildren().addAll(timeTitle, timeLabel);

        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.getStyleClass().add("dodge-stat-box");
        Label scoreTitle = new Label("SCORE");
        scoreTitle.getStyleClass().add("dodge-stat-title");
        if (orbitronFont != null) {
            scoreTitle.setFont(Font.font(orbitronFont.getFamily(), 11));
        }
        scoreLabel = new Label("0");
        scoreLabel.getStyleClass().add("dodge-time-value");
        if (orbitronFont != null) {
            scoreLabel.setFont(Font.font(orbitronFont.getFamily(), 18));
        }
        scoreBox.getChildren().addAll(scoreTitle, scoreLabel);

        infoPanel.getChildren().addAll(timeBox, scoreBox);
        return infoPanel;
    }

    private Button createStartButton(Runnable onStart) {
        Button button = new Button("START GAME");
        button.setOnAction(e -> onStart.run());
        return button;
    }

    public void addObstacle(Rectangle obstacle) {
        gameArea.getChildren().add(obstacle);
    }

    public void removeObstacle(Rectangle obstacle) {
        gameArea.getChildren().remove(obstacle);
    }

    public void clearObstacles() {
        gameArea.getChildren().removeIf(node -> node instanceof Rectangle);
    }

    public void updatePlayerPosition(double x) {
        if (player != null) {
            player.setLayoutX(Math.max(15, Math.min(485, x)));
        }
    }

    public void showStartScreen(boolean show) {
        if (startButton != null) {
            startButton.setVisible(show);
        }
        if (statusLabel != null) {
            statusLabel.setVisible(show);
        }
    }

    public StackPane createResultOverlay(boolean success, int score, long timeElapsed) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dodge-result-overlay");
        overlay.setPrefSize(750, 700);

        VBox resultBox = new VBox(20);
        resultBox.setAlignment(Pos.CENTER);

        Label resultLabel = new Label(success ? "SUCCESS!" : "GAME OVER!");
        resultLabel.getStyleClass().add(success ? "dodge-result-label-success" : "dodge-result-label-fail");
        if (cinzelFont != null) {
            resultLabel.setFont(Font.font(cinzelFont.getFamily(), 42));
        }

        Label scoreLabel = new Label("SCORE: " + score);
        scoreLabel.getStyleClass().add("dodge-result-score");
        if (orbitronFont != null) {
            scoreLabel.setFont(Font.font(orbitronFont.getFamily(), 20));
        }

        Label timeLabel = new Label(String.format("TIME: %d s", timeElapsed));
        timeLabel.getStyleClass().add("dodge-result-time");
        if (orbitronFont != null) {
            timeLabel.setFont(Font.font(orbitronFont.getFamily(), 18));
        }

        resultBox.getChildren().addAll(resultLabel, scoreLabel, timeLabel);
        overlay.getChildren().add(resultBox);
        return overlay;
    }

    public void addSpectatorLabel(VBox root) {
        Label spectatorLabel = new Label("SPECTATOR MODE");
        spectatorLabel.getStyleClass().add("dodge-spectator-label");
        root.getChildren().add(1, spectatorLabel);
    }

    public void showScene(VBox root) {
        Scene scene = new Scene(root, 750, 700);
        scene.getStylesheets().add(getClass().getResource("/css/minigame/dodge.css").toExternalForm());
        gameStage.setScene(scene);
        gameStage.show();
        gameArea.requestFocus();
    }

    public Stage getGameStage() {
        return gameStage;
    }

    public Label getTimeLabel() {
        return timeLabel;
    }

    public Label getScoreLabel() {
        return scoreLabel;
    }

    public Pane getGameArea() {
        return gameArea;
    }

    public Circle getPlayer() {
        return player;
    }

    public Button getStartButton() {
        return startButton;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public Label getInstructionLabel() {
        return instructionLabel;
    }
}

