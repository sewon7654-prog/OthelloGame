package org.example.minigame.games.memory;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.minigame.base.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 기억력 게임 - 카드 매칭 게임
 * 규칙:
 * 1. 60초 제한시간
 * 2. 카드를 뒤집어서 같은 쌍 찾기
 * 3. 두 카드가 다르면 자동으로 다시 뒤집힘
 * 4. 모든 쌍을 찾으면 성공
 */
public class MemoryGame implements MinigameBase {
    private static final int GRID_SIZE = 4; // 4x4 = 16장 (8쌍)
    private static final int TOTAL_PAIRS = (GRID_SIZE * GRID_SIZE) / 2;
    private static final int GAME_TIME_LIMIT = 30; // 게임 제한 시간 (초)
    
    // 게임 상태
    private int timeRemaining = GAME_TIME_LIMIT;
    private int pairsFound = 0;
    private boolean gameOver = false;
    private boolean success = false;
    private long startTime;
    
    // UI (View 분리)
    private MemoryGameView view;
    
    // 게임 로직
    private List<Button> cards = new ArrayList<>();
    private Button firstCard = null;
    private Button secondCard = null;
    private boolean isChecking = false;
    private boolean gameStarted = false;
    
    // 게임 상태
    private Timeline gameTimer;
    private MinigameCallback callback;

    @Override
    public void startPlayerMode(Stage parentStage, MinigameCallback callback) {
        this.callback = callback;
        this.startTime = System.currentTimeMillis();
        
        // View 생성 및 초기화
        view = new MemoryGameView();
        view.createGameWindow(parentStage, "기억력 게임 - 카드 매칭");
        view.getGameStage().setOnCloseRequest(e -> stopTimer());
        
        // UI 생성
        VBox root = view.createGameUI(GAME_TIME_LIMIT, TOTAL_PAIRS, 
            this::startGame, this::resetGame);
        
        // 카드 초기화
        initializeCards();
        
        // Scene 표시
        view.showScene(root);
        
        // 모든 카드를 앞면으로 보여줌 (미리 보기)
        showAllCards();
    }
    
    /**
     * 게임 시작 (시작 버튼 클릭 시)
     */
    private void startGame() {
        gameStarted = true;
        view.getStartButton().setVisible(false);
        
        // 모든 카드를 뒤집음
        for (Button card : cards) {
            if (!isMatched(card)) {
                view.updateCardStyle(card, false, false);
            }
        }
        
        // 타이머 시작
        startTimer();
    }
    
    /**
     * 모든 카드를 앞면으로 보여줌 (게임 시작 전)
     */
    private void showAllCards() {
        for (Button card : cards) {
            view.updateCardStyle(card, true, false);
        }
    }
    
    /**
     * 카드 초기화 및 배치
     */
    private void initializeCards() {
        cards.clear();
        view.getCardGrid().getChildren().clear();
        
        // 8쌍의 카드 생성 (1~8 숫자 2개씩)
        List<Integer> values = new ArrayList<>();
        for (int i = 1; i <= TOTAL_PAIRS; i++) {
            values.add(i);
            values.add(i);
        }
        
        // 카드 섞기
        Collections.shuffle(values);
        
        // 카드 배치
        int index = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int value = values.get(index);
                Button card = view.createCardButton(value, null);
                final Button finalCard = card;
                card.setOnAction(e -> onCardClick(finalCard));
                cards.add(card);
                view.getCardGrid().add(card, col, row);
                index++;
            }
        }
    }
    
    /**
     * 카드가 뒤집혀있는지 확인
     */
    private boolean isFlipped(Button card) {
        return card.getStyleClass().contains("memory-card-front") || 
               card.getStyleClass().contains("memory-card-matched");
    }
    
    /**
     * 카드가 매칭되었는지 확인
     */
    private boolean isMatched(Button card) {
        return card.getStyleClass().contains("memory-card-matched");
    }
    
    /**
     * 카드 값 가져오기
     */
    private int getCardValue(Button card) {
        return (int) card.getUserData();
    }
    
    /**
     * 게임 초기화
     */
    private void resetGame() {
        pairsFound = 0;
        firstCard = null;
        secondCard = null;
        isChecking = false;
        gameStarted = false;
        
        // 타이머 정지
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        // 시간 초기화
        timeRemaining = GAME_TIME_LIMIT;
        
        updatePairsLabel();
        updateTimeLabel();
        
        // 카드 재생성
        initializeCards();
        
        // 시작 버튼 다시 표시
        if (view != null && view.getStartButton() != null) {
            view.getStartButton().setVisible(true);
        }
        
        // 모든 카드 앞면으로
        showAllCards();
    }
    
    /**
     * 카드 클릭 처리
     */
    private void onCardClick(Button card) {
        // 게임이 시작되지 않았으면 클릭 무시
        if (!gameStarted) {
            return;
        }
        
        // 게임 오버 또는 이미 뒤집어진 카드는 무시
        if (gameOver || isChecking || isFlipped(card) || isMatched(card)) {
            return;
        }
        
        // 카드 뒤집기
        view.updateCardStyle(card, true, false);
        
        if (firstCard == null) {
            // 첫 번째 카드 선택
            firstCard = card;
        } else if (secondCard == null) {
            // 두 번째 카드 선택
            secondCard = card;
            isChecking = true;
            
            // 매칭 확인 (약간의 딜레이 후)
            PauseTransition pause = new PauseTransition(Duration.millis(800));
            pause.setOnFinished(e -> checkMatch());
            pause.play();
        }
    }
    
    /**
     * 카드 매칭 확인
     */
    private void checkMatch() {
        if (getCardValue(firstCard) == getCardValue(secondCard)) {
            // 매칭 성공!
            view.updateCardStyle(firstCard, true, true);
            view.updateCardStyle(secondCard, true, true);
            
            // 펄스 애니메이션 (matchPulse 효과)
            playMatchPulseAnimation(firstCard);
            playMatchPulseAnimation(secondCard);
            
            pairsFound++;
            updatePairsLabel();
            
            // 모든 쌍을 찾았는지 확인
            if (pairsFound == TOTAL_PAIRS) {
                success = true;
                finishGame();
            }
        } else {
            // 매칭 실패 - 다시 뒤집기
            view.updateCardStyle(firstCard, false, false);
            view.updateCardStyle(secondCard, false, false);
        }
        
        // 상태 초기화
        firstCard = null;
        secondCard = null;
        isChecking = false;
    }
    
    /**
     * 매칭 성공 펄스 애니메이션
     * 0.6초 동안 1.0 → 1.08 → 1.0 크기 변화
     */
    private void playMatchPulseAnimation(Button card) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(600), card);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setCycleCount(2); // 0→50%→100% = 1 사이클, 총 2회 (왕복)
        pulse.setAutoReverse(true); // 자동으로 되돌아오기
        pulse.play();
    }
    
    /**
     * 타이머 시작
     */
    private void startTimer() {
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            updateTimeLabel();
            
            if (timeRemaining <= 0) {
                finishGame();
            }
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }
    
    /**
     * 타이머 정지
     */
    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
    
    /**
     * UI 업데이트
     */
    private void updateTimeLabel() {
        // 남은 시간을 분:초 형식으로 표시
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        view.getTimeLabel().setText(String.format("%d:%02d", minutes, seconds));
        
        // 10초 이하 남았을 때 강조 스타일 변경
        if (timeRemaining <= 10) {
            view.getTimeLabel().getStyleClass().clear();
            view.getTimeLabel().getStyleClass().add("memory-time-value-warning");
        }
    }
    
    private void updatePairsLabel() {
        view.getPairsLabel().setText(pairsFound + "/" + TOTAL_PAIRS);
    }
    
    /**
     * 게임 종료
     */
    private void finishGame() {
        gameOver = true;
        stopTimer();
        
        // 결과 화면 생성
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        StackPane overlay = view.createResultOverlay(success, pairsFound, TOTAL_PAIRS, elapsed);
        
        // 씬에 오버레이 추가
        StackPane root = new StackPane();
        root.getChildren().addAll(view.getGameStage().getScene().getRoot(), overlay);
        view.getGameStage().getScene().setRoot(root);
        
        // 3초 후 창 닫기
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
    public void startSpectatorMode(Stage parentStage) {
        startPlayerMode(parentStage, null);
        view.getCardGrid().setDisable(true); // 입력 비활성화
        
        // 관전 표시 추가
        VBox root = (VBox) view.getGameStage().getScene().getRoot();
        view.addSpectatorLabel(root);
    }

    @Override
    public String getStateJson() {
        return String.format(
            "{\"pairsFound\":%d,\"timeRemaining\":%d,\"gameOver\":%b,\"success\":%b}",
            pairsFound, timeRemaining, gameOver, success
        );
    }

    @Override
    public void updateFromJson(String json) {
        // TODO: JSON 파싱 및 관전자 화면 동기화
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
}
