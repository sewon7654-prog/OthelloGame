package org.example.minigame.games.memory;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.minigame.base.MinigameBase;
import org.example.minigame.base.MinigameCallback;
import org.example.minigame.base.MinigameResult;
import org.example.minigame.base.MinigameType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * 기억력 카드 매칭 게임
 */
public class MemoryGame implements MinigameBase {
    private static final int GRID_SIZE = 4;
    private static final int TOTAL_PAIRS = (GRID_SIZE * GRID_SIZE) / 2;
    private static final int GAME_TIME_LIMIT = 30; // seconds

    private int timeRemaining = GAME_TIME_LIMIT;
    private int pairsFound = 0;
    private boolean gameOver = false;
    private boolean success = false;
    private long startTime;

    private MemoryGameView view;
    private final List<Button> cards = new ArrayList<>();
    private Button firstCard;
    private Button secondCard;
    private boolean isChecking = false;
    private boolean gameStarted = false;
    private boolean isSpectator = false;
    private boolean applyingState = false;

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

        view = new MemoryGameView();
        view.createGameWindow(parentStage, "Memory Card Match");
        view.getGameStage().setOnCloseRequest(e -> stopTimer());

        VBox root = view.createGameUI(GAME_TIME_LIMIT, TOTAL_PAIRS, this::startGame, this::resetGame);
        initializeCards();
        view.showScene(root);
        showAllCards();
        broadcastState();
    }

    @Override
    public void startSpectatorMode(Stage parentStage) {
        isSpectator = true;
        startPlayerMode(parentStage, null);
        view.getCardGrid().setDisable(true);
        VBox root = (VBox) view.getGameStage().getScene().getRoot();
        view.addSpectatorLabel(root);
    }

    private void startGame() {
        gameStarted = true;
        if (view.getStartButton() != null) {
            view.getStartButton().setVisible(false);
        }
        for (Button card : cards) {
            if (!isMatched(card)) {
                view.updateCardStyle(card, false, false);
            }
        }
        startTimer();
        broadcastState();
    }

    private void initializeCards() {
        cards.clear();
        view.getCardGrid().getChildren().clear();

        List<Integer> values = new ArrayList<>();
        for (int i = 1; i <= TOTAL_PAIRS; i++) {
            values.add(i);
            values.add(i);
        }
        Collections.shuffle(values, new Random());

        int index = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int value = values.get(index++);
                Button card = view.createCardButton(value, null);
                card.setOnAction(e -> onCardClick(card));
                cards.add(card);
                view.getCardGrid().add(card, col, row);
            }
        }
    }

    private void showAllCards() {
        for (Button card : cards) {
            view.updateCardStyle(card, true, false);
        }
    }

    private void resetGame() {
        pairsFound = 0;
        firstCard = null;
        secondCard = null;
        isChecking = false;
        gameStarted = false;
        gameOver = false;
        success = false;

        stopTimer();
        timeRemaining = GAME_TIME_LIMIT;
        updatePairsLabel();
        updateTimeLabel();

        initializeCards();
        if (view.getStartButton() != null) {
            view.getStartButton().setVisible(true);
        }
        showAllCards();
        broadcastState();
    }

    private void onCardClick(Button card) {
        if (!gameStarted || gameOver || isChecking || isFlipped(card) || isMatched(card)) {
            return;
        }

        view.updateCardStyle(card, true, false);
        broadcastState();

        if (firstCard == null) {
            firstCard = card;
        } else if (secondCard == null) {
            secondCard = card;
            isChecking = true;
            PauseTransition pause = new PauseTransition(Duration.millis(800));
            pause.setOnFinished(e -> checkMatch());
            pause.play();
        }
    }

    private void checkMatch() {
        if (getCardValue(firstCard) == getCardValue(secondCard)) {
            view.updateCardStyle(firstCard, true, true);
            view.updateCardStyle(secondCard, true, true);
            playMatchPulseAnimation(firstCard);
            playMatchPulseAnimation(secondCard);

            pairsFound++;
            updatePairsLabel();
            broadcastState();

            if (pairsFound == TOTAL_PAIRS) {
                success = true;
                finishGame();
                return;
            }
        } else {
            view.updateCardStyle(firstCard, false, false);
            view.updateCardStyle(secondCard, false, false);
        }

        firstCard = null;
        secondCard = null;
        isChecking = false;
        broadcastState();
    }

    private void playMatchPulseAnimation(Button card) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(600), card);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private void startTimer() {
        stopTimer();
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            updateTimeLabel();
            broadcastState();
            if (timeRemaining <= 0) {
                finishGame();
            }
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    private boolean isFlipped(Button card) {
        return card.getStyleClass().contains("memory-card-front") ||
               card.getStyleClass().contains("memory-card-matched");
    }

    private boolean isMatched(Button card) {
        return card.getStyleClass().contains("memory-card-matched");
    }

    private int getCardValue(Button card) {
        Object data = card.getUserData();
        if (data instanceof Integer) {
            return (Integer) data;
        }
        return 0;
    }

    private void updateTimeLabel() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        view.getTimeLabel().setText(String.format("%d:%02d", minutes, seconds));
        if (timeRemaining <= 10) {
            view.getTimeLabel().getStyleClass().clear();
            view.getTimeLabel().getStyleClass().add("memory-time-value-warning");
        }
    }

    private void updatePairsLabel() {
        view.getPairsLabel().setText(pairsFound + "/" + TOTAL_PAIRS);
    }

    private void finishGame() {
        gameOver = true;
        stopTimer();

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        StackPane overlay = view.createResultOverlay(success, pairsFound, TOTAL_PAIRS, elapsed);
        StackPane root = new StackPane();
        root.getChildren().addAll(view.getGameStage().getScene().getRoot(), overlay);
        view.getGameStage().getScene().setRoot(root);
        broadcastState();

        PauseTransition closeDelay = new PauseTransition(Duration.seconds(3));
        closeDelay.setOnFinished(e -> {
            closeGame();
            if (callback != null) {
                MinigameResult result = new MinigameResult(success, pairsFound, elapsed, MinigameType.MEMORY);
                callback.onComplete(result);
            }
        });
        closeDelay.play();
    }

    @Override
    public String getStateJson() {
        StringBuilder values = new StringBuilder();
        StringBuilder flipped = new StringBuilder();
        StringBuilder matched = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            Button card = cards.get(i);
            values.append(getCardValue(card));
            flipped.append(isFlipped(card) ? 1 : 0);
            matched.append(isMatched(card) ? 1 : 0);
            if (i < cards.size() - 1) {
                values.append(",");
                flipped.append(",");
                matched.append(",");
            }
        }
        return String.format(
            "{\"pairsFound\":%d,\"timeRemaining\":%d,\"gameOver\":%b,\"success\":%b,\"started\":%b,\"values\":[%s],\"flipped\":[%s],\"matched\":[%s]}",
            pairsFound, timeRemaining, gameOver, success, gameStarted, values, flipped, matched
        );
    }

    @Override
    public void updateFromJson(String json) {
        applyingState = true;
        pairsFound = parseInt(json, "pairsFound", pairsFound);
        timeRemaining = parseInt(json, "timeRemaining", timeRemaining);
        gameOver = parseBoolean(json, "gameOver", gameOver);
        success = parseBoolean(json, "success", success);
        gameStarted = parseBoolean(json, "started", gameStarted);

        int[] values = parseIntArray(json, "values");
        boolean[] flipped = parseBooleanArray(json, "flipped");
        boolean[] matched = parseBooleanArray(json, "matched");

        for (int i = 0; i < cards.size(); i++) {
            Button card = cards.get(i);
            if (i < values.length) {
                card.setUserData(values[i]);
            }
            boolean flipState = i < flipped.length && flipped[i];
            boolean matchState = i < matched.length && matched[i];
            view.updateCardStyle(card, flipState || matchState, matchState);
        }

        updatePairsLabel();
        updateTimeLabel();

        if (gameOver) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            StackPane overlay = view.createResultOverlay(success, pairsFound, TOTAL_PAIRS, elapsed);
            StackPane root = new StackPane();
            root.getChildren().addAll(view.getGameStage().getScene().getRoot(), overlay);
            view.getGameStage().getScene().setRoot(root);
        }

        applyingState = false;
    }

    @Override
    public boolean isFinished() {
        return gameOver;
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
        return MinigameType.MEMORY;
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

    private int[] parseIntArray(String json, String key) {
        String[] parts = extractArray(json, key);
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i].trim());
            } catch (Exception e) {
                result[i] = 0;
            }
        }
        return result;
    }

    private boolean[] parseBooleanArray(String json, String key) {
        String[] parts = extractArray(json, key);
        boolean[] result = new boolean[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String v = parts[i].trim();
            result[i] = "1".equals(v) || "true".equalsIgnoreCase(v);
        }
        return result;
    }

    private String[] extractArray(String json, String key) {
        String token = "\"" + key + "\":";
        int idx = json.indexOf(token);
        if (idx == -1) return new String[0];
        int start = json.indexOf('[', idx);
        int end = json.indexOf(']', start);
        if (start == -1 || end == -1) return new String[0];
        String body = json.substring(start + 1, end).trim();
        if (body.isEmpty()) return new String[0];
        return body.split(",");
    }
}
