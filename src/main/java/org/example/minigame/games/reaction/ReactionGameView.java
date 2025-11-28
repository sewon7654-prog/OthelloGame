package org.example.minigame.games.reaction;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 반응속도 게임 JavaFX view
 */
public class ReactionGameView {
    private Stage gameStage;
    private Label timeLabel;
    private Label reactionTimeLabel;
    private Button targetButton;
    private Button startButton;
    private Label instructionLabel;
    private Label statusLabel;

    private static Font cinzelFont;
    private static Font orbitronFont;

    static {
        try {
            cinzelFont = Font.loadFont(
                ReactionGameView.class.getResourceAsStream("/fonts/Cinzel-Bold.ttf"), 32
            );
            orbitronFont = Font.loadFont(
                ReactionGameView.class.getResourceAsStream("/fonts/Orbitron-Bold.ttf"), 14
            );
        } catch (Exception ignored) {}
    }

    public void createGameWindow(Stage parentStage, String title) {
        gameStage = new Stage();
        gameStage.initModality(Modality.WINDOW_MODAL);
        gameStage.initOwner(parentStage);
        gameStage.setTitle(title);
    }

    public VBox createGameUI(Runnable onStart, Runnable onTargetClick) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("reaction-game-root");

        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("reaction-header");

        Label titleLabel = new Label("REACTION GAME");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.getStyleClass().add("reaction-game-title");
        if (cinzelFont != null) {
            titleLabel.setFont(Font.font(cinzelFont.getFamily(), 32));
        }

        HBox infoPanel = createInfoPanel();
        header.getChildren().addAll(titleLabel, infoPanel);

        // 게임 영역
        StackPane gameArea = new StackPane();
        gameArea.setPrefSize(400, 300);
        gameArea.getStyleClass().add("reaction-game-area");

        targetButton = createTargetButton(onTargetClick);
        targetButton.setVisible(false);
        gameArea.getChildren().add(targetButton);

        startButton = createStartButton(onStart);
        startButton.getStyleClass().add("reaction-start-button");
        if (orbitronFont != null) {
            startButton.setFont(Font.font(orbitronFont.getFamily(), 14));
        }

        statusLabel = new Label("READY TO START");
        statusLabel.getStyleClass().add("reaction-status-label");
        if (orbitronFont != null) {
            statusLabel.setFont(Font.font(orbitronFont.getFamily(), 16));
        }

        instructionLabel = new Label("CLICK THE BUTTON AS FAST AS YOU CAN!");
        instructionLabel.getStyleClass().add("reaction-instruction");
        if (orbitronFont != null) {
            instructionLabel.setFont(Font.font(orbitronFont.getFamily(), 13));
        }

        VBox centerArea = new VBox(20);
        centerArea.setAlignment(Pos.CENTER);
        centerArea.getChildren().addAll(startButton, statusLabel);

        gameArea.getChildren().add(centerArea);

        root.getChildren().addAll(header, gameArea, instructionLabel);
        return root;
    }

    private HBox createInfoPanel() {
        HBox infoPanel = new HBox(20);
        infoPanel.setAlignment(Pos.CENTER);
        infoPanel.getStyleClass().add("reaction-info-panel");

        VBox timeBox = new VBox(5);
        timeBox.setAlignment(Pos.CENTER);
        timeBox.getStyleClass().add("reaction-stat-box");
        Label timeTitle = new Label("WAIT TIME");
        timeTitle.getStyleClass().add("reaction-stat-title");
        if (orbitronFont != null) {
            timeTitle.setFont(Font.font(orbitronFont.getFamily(), 11));
        }
        timeLabel = new Label("--");
        timeLabel.getStyleClass().add("reaction-time-value");
        if (orbitronFont != null) {
            timeLabel.setFont(Font.font(orbitronFont.getFamily(), 18));
        }
        timeBox.getChildren().addAll(timeTitle, timeLabel);

        VBox reactionBox = new VBox(5);
        reactionBox.setAlignment(Pos.CENTER);
        reactionBox.getStyleClass().add("reaction-stat-box");
        Label reactionTitle = new Label("REACTION");
        reactionTitle.getStyleClass().add("reaction-stat-title");
        if (orbitronFont != null) {
            reactionTitle.setFont(Font.font(orbitronFont.getFamily(), 11));
        }
        reactionTimeLabel = new Label("--");
        reactionTimeLabel.getStyleClass().add("reaction-time-value");
        if (orbitronFont != null) {
            reactionTimeLabel.setFont(Font.font(orbitronFont.getFamily(), 18));
        }
        reactionBox.getChildren().addAll(reactionTitle, reactionTimeLabel);

        infoPanel.getChildren().addAll(timeBox, reactionBox);
        return infoPanel;
    }

    private Button createStartButton(Runnable onStart) {
        Button button = new Button("START GAME");
        button.setOnAction(e -> onStart.run());
        return button;
    }

    private Button createTargetButton(Runnable onClick) {
        Button button = new Button("CLICK!");
        button.setPrefSize(200, 200);
        button.getStyleClass().addAll("reaction-target-button");
        if (cinzelFont != null) {
            button.setFont(Font.font(cinzelFont.getFamily(), 36));
        }
        button.setOnAction(e -> onClick.run());
        return button;
    }

    public StackPane createResultOverlay(boolean success, long reactionTime) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("reaction-result-overlay");
        overlay.setPrefSize(750, 700);

        VBox resultBox = new VBox(20);
        resultBox.setAlignment(Pos.CENTER);

        Label resultLabel = new Label(success ? "SUCCESS!" : "TOO SLOW!");
        resultLabel.getStyleClass().add(success ? "reaction-result-label-success" : "reaction-result-label-fail");
        if (cinzelFont != null) {
            resultLabel.setFont(Font.font(cinzelFont.getFamily(), 42));
        }

        Label timeLabel = new Label(String.format("REACTION TIME: %.3f s", reactionTime / 1000.0));
        timeLabel.getStyleClass().add("reaction-result-time");
        if (orbitronFont != null) {
            timeLabel.setFont(Font.font(orbitronFont.getFamily(), 20));
        }

        resultBox.getChildren().addAll(resultLabel, timeLabel);
        overlay.getChildren().add(resultBox);
        return overlay;
    }

    public void addSpectatorLabel(VBox root) {
        Label spectatorLabel = new Label("SPECTATOR MODE");
        spectatorLabel.getStyleClass().add("reaction-spectator-label");
        root.getChildren().add(1, spectatorLabel);
    }

    public void showScene(VBox root) {
        Scene scene = new Scene(root, 750, 700);
        scene.getStylesheets().add(getClass().getResource("/css/minigame/reaction.css").toExternalForm());
        gameStage.setScene(scene);
        gameStage.show();
    }

    public Stage getGameStage() {
        return gameStage;
    }

    public Label getTimeLabel() {
        return timeLabel;
    }

    public Label getReactionTimeLabel() {
        return reactionTimeLabel;
    }

    public Button getTargetButton() {
        return targetButton;
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
