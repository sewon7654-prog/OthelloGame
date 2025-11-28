package org.example.minigame.games.reaction;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.minigame.base.MinigameBase;
import org.example.minigame.base.MinigameCallback;
import org.example.minigame.base.MinigameResult;
import org.example.minigame.base.MinigameType;

import java.util.Random;
import java.util.function.Consumer;

/**
 * 반응속도 게임
 */
public class ReactionGame implements MinigameBase {
    private static final long SUCCESS_THRESHOLD = 500; // 0.5초 이내 클릭 시 성공 (밀리초)
    private static final long MIN_WAIT_TIME = 1000; // 최소 대기 시간 1초
    private static final long MAX_WAIT_TIME = 3000; // 최대 대기 시간 3초

    private ReactionGameView view;
    private boolean finished = false;
    private boolean success = false;
    private boolean gameStarted = false;
    private boolean isSpectator = false;
    private boolean applyingState = false;

    private long startTime;
    private long buttonAppearTime;
    private long reactionTime;
    private long waitTime;

    private Timeline gameTimer;
    private MinigameCallback callback;
    private Consumer<String> updatePublisher;

    public void setUpdatePublisher(Consumer<String> publisher) {
        this.updatePublisher = publisher;
    }

    @Override
    public void startPlayerMode(Stage parentStage, MinigameCallback callback) {
        this.callback = callback;
        this.startTime = System.currentTimeMillis();

        view = new ReactionGameView();
        view.createGameWindow(parentStage, "Reaction Speed Game");
        view.getGameStage().setOnCloseRequest(e -> stopTimer());

        VBox root = view.createGameUI(this::startGame, this::onTargetClick);
        view.showScene(root);
        broadcastState();
    }

    @Override
    public void startSpectatorMode(Stage parentStage) {
        isSpectator = true;
        startPlayerMode(parentStage, null);
        view.getTargetButton().setDisable(true);
        view.getStartButton().setDisable(true);
        VBox root = (VBox) view.getGameStage().getScene().getRoot();
        view.addSpectatorLabel(root);
    }

    private void startGame() {
        if (gameStarted) return;
        
        gameStarted = true;
        finished = false;
        success = false;
        
        view.getStartButton().setVisible(false);
        view.getStatusLabel().setText("WAIT FOR THE BUTTON...");
        view.getTimeLabel().setText("--");
        view.getReactionTimeLabel().setText("--");
        view.getTargetButton().setVisible(false);
        
        // 랜덤 대기 시간 설정
        Random random = new Random();
        long range = MAX_WAIT_TIME - MIN_WAIT_TIME;
        waitTime = MIN_WAIT_TIME + Math.abs(random.nextLong() % range);
        
        // 대기 시간 카운트다운
        startWaitTimer();
        
        // 랜덤 시간 후 버튼 표시
        PauseTransition waitTransition = new PauseTransition(Duration.millis(waitTime));
        waitTransition.setOnFinished(e -> showTargetButton());
        waitTransition.play();
        
        broadcastState();
    }

    private void startWaitTimer() {
        stopTimer();
        long startWait = System.currentTimeMillis();
        gameTimer = new Timeline(new KeyFrame(Duration.millis(10), e -> {
            long elapsed = System.currentTimeMillis() - startWait;
            if (elapsed < waitTime) {
                view.getTimeLabel().setText(String.format("%.2f", (waitTime - elapsed) / 1000.0));
            } else {
                view.getTimeLabel().setText("0.00");
            }
            broadcastState();
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    private void showTargetButton() {
        if (finished) return;
        
        buttonAppearTime = System.currentTimeMillis();
        view.getTargetButton().setVisible(true);
        view.getStatusLabel().setText("CLICK NOW!");
        stopTimer();
        view.getTimeLabel().setText("0.00");
        
        // 반응 시간 측정 시작
        startReactionTimer();
        broadcastState();
    }

    private void startReactionTimer() {
        stopTimer();
        long startReaction = System.currentTimeMillis();
        gameTimer = new Timeline(new KeyFrame(Duration.millis(10), e -> {
            if (!finished && view.getTargetButton().isVisible()) {
                long elapsed = System.currentTimeMillis() - startReaction;
                view.getReactionTimeLabel().setText(String.format("%.3f", elapsed / 1000.0));
                broadcastState();
            }
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    private void onTargetClick() {
        if (finished || !view.getTargetButton().isVisible()) return;
        
        long clickTime = System.currentTimeMillis();
        reactionTime = clickTime - buttonAppearTime;
        finished = true;
        success = reactionTime <= SUCCESS_THRESHOLD;
        
        stopTimer();
        view.getTargetButton().setVisible(false);
        view.getStatusLabel().setText(success ? "SUCCESS!" : "TOO SLOW!");
        
        finishGame();
        broadcastState();
    }

    private void finishGame() {
        stopTimer();
        
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        StackPane overlay = view.createResultOverlay(success, reactionTime);
        StackPane root = new StackPane();
        root.getChildren().addAll(view.getGameStage().getScene().getRoot(), overlay);
        view.getGameStage().getScene().setRoot(root);
        
        PauseTransition closeDelay = new PauseTransition(Duration.seconds(3));
        closeDelay.setOnFinished(e -> {
            closeGame();
            if (callback != null) {
                MinigameResult result = new MinigameResult(success, success ? 100 : 0, elapsed, MinigameType.REACTION);
                callback.onComplete(result);
            }
        });
        closeDelay.play();
    }

    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    @Override
    public String getStateJson() {
        return String.format(
            "{\"finished\":%b,\"success\":%b,\"started\":%b,\"reactionTime\":%d,\"waitTime\":%d,\"buttonVisible\":%b}",
            finished, success, gameStarted, reactionTime, waitTime, 
            view != null && view.getTargetButton() != null && view.getTargetButton().isVisible()
        );
    }

    @Override
    public void updateFromJson(String json) {
        applyingState = true;
        finished = parseBoolean(json, "finished", finished);
        success = parseBoolean(json, "success", success);
        gameStarted = parseBoolean(json, "started", gameStarted);
        reactionTime = parseInt(json, "reactionTime", reactionTime);
        waitTime = parseInt(json, "waitTime", waitTime);
        boolean buttonVisible = parseBoolean(json, "buttonVisible", false);
        
        if (view != null) {
            if (finished) {
                finishGame();
            } else if (buttonVisible && view.getTargetButton() != null) {
                view.getTargetButton().setVisible(true);
                view.getStatusLabel().setText("CLICK NOW!");
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
        stopTimer();
        if (view != null && view.getGameStage() != null) {
            view.getGameStage().close();
        }
    }

    @Override
    public MinigameType getType() {
        return MinigameType.REACTION;
    }

    private void broadcastState() {
        if (updatePublisher != null && !applyingState && !isSpectator) {
            updatePublisher.accept(getStateJson());
        }
    }

    private int parseInt(String json, String key, long defaultValue) {
        String token = "\"" + key + "\":";
        int idx = json.indexOf(token);
        if (idx == -1) return (int) defaultValue;
        idx += token.length();
        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(idx, end));
        } catch (Exception e) {
            return (int) defaultValue;
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
