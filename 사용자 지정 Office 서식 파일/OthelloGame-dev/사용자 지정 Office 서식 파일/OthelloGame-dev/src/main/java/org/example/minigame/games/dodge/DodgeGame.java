package org.example.minigame.games.dodge;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.minigame.base.MinigameBase;
import org.example.minigame.base.MinigameCallback;
import org.example.minigame.base.MinigameResult;
import org.example.minigame.base.MinigameType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * 회피 게임
 */
public class DodgeGame implements MinigameBase {
    private static final int GAME_DURATION = 13; // 13초로 고정
    private static final double PLAYER_SPEED = 5.0;
    private static final double OBSTACLE_SPEED = 9.0; // 장애물 속도 대폭 증가 (7.5 -> 9.0) - 난이도 상승
    private static final double OBSTACLE_SPAWN_RATE = 0.12; // 매 프레임마다 생성 확률 대폭 증가 (0.08 -> 0.12) - 난이도 상승

    private DodgeGameView view;
    private boolean finished = false;
    private boolean success = false;
    private boolean gameStarted = false;
    private boolean isSpectator = false;
    private boolean applyingState = false;

    private long startTime;
    private int timeRemaining = GAME_DURATION;
    private int score = 0;
    private double playerX = 250;

    private List<Rectangle> obstacles = new ArrayList<>();
    private Timeline gameTimer;
    private Timeline obstacleSpawnTimer;
    private MinigameCallback callback;
    private Consumer<String> updatePublisher;
    private Random random = new Random();

    public void setUpdatePublisher(Consumer<String> publisher) {
        this.updatePublisher = publisher;
    }

    @Override
    public void startPlayerMode(Stage parentStage, MinigameCallback callback) {
        this.callback = callback;
        this.startTime = System.currentTimeMillis();

        view = new DodgeGameView();
        view.createGameWindow(parentStage, "Dodge Game");
        view.getGameStage().setOnCloseRequest(e -> stopTimers());

        VBox root = view.createGameUI(this::startGame, this::handlePlayerMove);
        setupKeyControls();
        view.showScene(root);
        broadcastState();
    }

    @Override
    public void startSpectatorMode(Stage parentStage) {
        isSpectator = true;
        startPlayerMode(parentStage, null);
        view.getGameArea().setDisable(true);
        VBox root = (VBox) view.getGameStage().getScene().getRoot();
        view.addSpectatorLabel(root);
    }

    private void setupKeyControls() {
        view.getGameArea().setOnKeyPressed(e -> {
            if (!gameStarted || finished) return;
            handleKeyPress(e.getCode());
        });
    }

    private void handleKeyPress(KeyCode code) {
        if (code == KeyCode.LEFT || code == KeyCode.A) {
            playerX -= PLAYER_SPEED;
        } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
            playerX += PLAYER_SPEED;
        }
        playerX = Math.max(15, Math.min(485, playerX));
        view.updatePlayerPosition(playerX);
        broadcastState();
    }

    private void handlePlayerMove(Double deltaX) {
        if (!gameStarted || finished) return;
        playerX += deltaX;
        playerX = Math.max(15, Math.min(485, playerX));
        view.updatePlayerPosition(playerX);
        broadcastState();
    }

    private void startGame() {
        if (gameStarted) return;
        
        gameStarted = true;
        finished = false;
        success = false;
        timeRemaining = GAME_DURATION;
        score = 0;
        playerX = 250;
        obstacles.clear();
        
        view.getStartButton().setVisible(false);
        view.getStatusLabel().setVisible(false);
        view.getStatusLabel().setText("DODGE!");
        view.clearObstacles();
        view.updatePlayerPosition(playerX);
        view.getGameArea().requestFocus();
        
        startGameTimer();
        startObstacleSpawner();
        broadcastState();
    }

    private void startGameTimer() {
        stopTimers();
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            view.getTimeLabel().setText(String.format("0:%02d", timeRemaining));
            
            if (timeRemaining <= 0) {
                success = true;
                finishGame();
            } else if (timeRemaining <= 3) {
                view.getTimeLabel().getStyleClass().clear();
                view.getTimeLabel().getStyleClass().add("dodge-time-value-warning");
            }
            broadcastState();
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    private void startObstacleSpawner() {
        obstacleSpawnTimer = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            if (finished) return;
            
            // 장애물 생성
            if (random.nextDouble() < OBSTACLE_SPAWN_RATE) {
                spawnObstacle();
            }
            
            // 장애물 이동 및 충돌 검사
            updateObstacles();
        }));
        obstacleSpawnTimer.setCycleCount(Timeline.INDEFINITE);
        obstacleSpawnTimer.play();
    }

    private void spawnObstacle() {
        double x = 50 + random.nextDouble() * 400;
        Rectangle obstacle = new Rectangle(30, 30, Color.web("#ff6b6b"));
        obstacle.setLayoutX(x);
        obstacle.setLayoutY(-30);
        obstacle.getStyleClass().add("dodge-obstacle");
        obstacles.add(obstacle);
        view.addObstacle(obstacle);
    }

    private void updateObstacles() {
        List<Rectangle> toRemove = new ArrayList<>();
        
        for (Rectangle obstacle : obstacles) {
            double y = obstacle.getLayoutY() + OBSTACLE_SPEED;
            obstacle.setLayoutY(y);
            
            // 화면 밖으로 나가면 제거
            if (y > 450) {
                toRemove.add(obstacle);
                score += 10;
                view.getScoreLabel().setText(String.valueOf(score));
            }
            // 플레이어와 충돌 검사
            else if (checkCollision(obstacle)) {
                finished = true;
                success = false;
                finishGame();
                return;
            }
        }
        
        for (Rectangle obstacle : toRemove) {
            obstacles.remove(obstacle);
            view.removeObstacle(obstacle);
        }
    }

    private boolean checkCollision(Rectangle obstacle) {
        double obstacleX = obstacle.getLayoutX() + 15; // 장애물 중심
        double obstacleY = obstacle.getLayoutY() + 15;
        double playerY = view.getPlayer().getLayoutY();
        
        double distance = Math.sqrt(
            Math.pow(obstacleX - playerX, 2) + 
            Math.pow(obstacleY - playerY, 2)
        );
        
        return distance < 30; // 플레이어 반지름(15) + 장애물 반지름(약15)
    }

    private void finishGame() {
        stopTimers();
        
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        StackPane overlay = view.createResultOverlay(success, score, elapsed);
        StackPane root = new StackPane();
        root.getChildren().addAll(view.getGameStage().getScene().getRoot(), overlay);
        view.getGameStage().getScene().setRoot(root);
        
        javafx.animation.PauseTransition closeDelay = new javafx.animation.PauseTransition(Duration.seconds(3));
        closeDelay.setOnFinished(e -> {
            closeGame();
            if (callback != null) {
                MinigameResult result = new MinigameResult(success, score, elapsed, MinigameType.DODGE);
                callback.onComplete(result);
            }
        });
        closeDelay.play();
    }

    private void stopTimers() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (obstacleSpawnTimer != null) {
            obstacleSpawnTimer.stop();
        }
    }

    @Override
    public String getStateJson() {
        return String.format(
            "{\"finished\":%b,\"success\":%b,\"started\":%b,\"timeRemaining\":%d,\"score\":%d,\"playerX\":%.2f}",
            finished, success, gameStarted, timeRemaining, score, playerX
        );
    }

    @Override
    public void updateFromJson(String json) {
        applyingState = true;
        finished = parseBoolean(json, "finished", finished);
        success = parseBoolean(json, "success", success);
        gameStarted = parseBoolean(json, "started", gameStarted);
        timeRemaining = parseInt(json, "timeRemaining", timeRemaining);
        score = parseInt(json, "score", score);
        playerX = parseDouble(json, "playerX", playerX);
        
        if (view != null) {
            view.getTimeLabel().setText(String.format("0:%02d", timeRemaining));
            view.getScoreLabel().setText(String.valueOf(score));
            view.updatePlayerPosition(playerX);
            
            if (finished) {
                finishGame();
            }
        }
        
        applyingState = false;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public void closeGame() {
        stopTimers();
        if (view != null && view.getGameStage() != null) {
            view.getGameStage().close();
        }
    }

    @Override
    public MinigameType getType() {
        return MinigameType.DODGE;
    }

    private void broadcastState() {
        if (updatePublisher != null && !applyingState && !isSpectator) {
            updatePublisher.accept(getStateJson());
        }
    }

    private int parseInt(String json, String key, int defaultValue) {
        String token = "\"" + key + "\":";
        int idx = json.indexOf(token);
        if (idx == -1) return defaultValue;
        idx += token.length();
        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(idx, end));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double parseDouble(String json, String key, double defaultValue) {
        String token = "\"" + key + "\":";
        int idx = json.indexOf(token);
        if (idx == -1) return defaultValue;
        idx += token.length();
        int end = idx;
        while (end < json.length() && 
               (Character.isDigit(json.charAt(end)) || 
                json.charAt(end) == '.' || 
                json.charAt(end) == '-' || 
                json.charAt(end) == 'E' || 
                json.charAt(end) == 'e')) {
            end++;
        }
        try {
            return Double.parseDouble(json.substring(idx, end));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean parseBoolean(String json, String key, boolean defaultValue) {
        String token = "\"" + key + "\":";
        int idx = json.indexOf(token);
        if (idx == -1) return defaultValue;
        idx += token.length();
        String tail = json.substring(idx).trim().toLowerCase();
        if (tail.startsWith("true")) return true;
        if (tail.startsWith("false")) return false;
        return defaultValue;
    }
}
