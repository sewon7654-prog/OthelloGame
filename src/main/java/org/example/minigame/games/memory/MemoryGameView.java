package org.example.minigame.games.memory;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 기억력 게임 UI 생성 클래스
 * UI 생성 로직만 담당
 */
public class MemoryGameView {
    private Stage gameStage;
    private Label timeLabel;
    private Label pairsLabel;
    private Label movesValueLabel;
    private GridPane cardGrid;
    private Button startButton;
    private Button resetButton;
    
    // 커스텀 폰트
    private static Font cinzelFont;
    private static Font orbitronFont;
    
    static {
        try {
            // Cinzel - 제목용 (세리프 폰트, 로마 비문 스타일)
            cinzelFont = Font.loadFont(
                MemoryGameView.class.getResourceAsStream("/fonts/Cinzel-Bold.ttf"), 32
            );
            
            // Orbitron - 본문용 (기하학적 레트로 폰트)
            orbitronFont = Font.loadFont(
                MemoryGameView.class.getResourceAsStream("/fonts/Orbitron-Bold.ttf"), 14
            );
            
            System.out.println("[폰트 로드] Cinzel: " + (cinzelFont != null ? "성공" : "실패"));
            System.out.println("[폰트 로드] Orbitron: " + (orbitronFont != null ? "성공" : "실패"));
        } catch (Exception e) {
            System.err.println("[폰트 로드 실패] " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 게임 창 생성
     */
    public void createGameWindow(Stage parentStage, String title) {
        gameStage = new Stage();
        gameStage.initModality(Modality.WINDOW_MODAL);
        gameStage.initOwner(parentStage);
        gameStage.setTitle(title);
    }
    
    /**
     * 게임 UI 생성
     */
    public VBox createGameUI(int gameTimeLimit, int totalPairs, 
                             Runnable onStart, Runnable onReset) {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("memory-game-root");
        
        // 헤더 (제목 + 정보 패널)
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("memory-header");
        
        // 제목 (Cinzel 폰트 적용)
        Label titleLabel = new Label("◈ MEMORY GAME ◈");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.getStyleClass().add("memory-game-title");
        if (cinzelFont != null) {
            titleLabel.setFont(Font.font(cinzelFont.getFamily(), 32));
        }
        
        // 정보 패널
        HBox infoPanel = createInfoPanel(gameTimeLimit, totalPairs);
        
        header.getChildren().addAll(titleLabel, infoPanel);
        
        // 카드 그리드
        cardGrid = new GridPane();
        cardGrid.getStyleClass().add("memory-card-grid");
        
        // 버튼들
        startButton = createStartButton(onStart);
        resetButton = createResetButton(onReset);
        
        // 버튼 패널 - 중앙 정렬
        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getStyleClass().add("memory-button-panel");
        buttonPanel.getChildren().addAll(startButton, resetButton);
        
        // 지시사항
        Label instructionLabel = new Label("◆ FIND ALL MATCHING PAIRS! ◆");
        instructionLabel.getStyleClass().add("memory-instruction");
        if (orbitronFont != null) {
            instructionLabel.setFont(Font.font(orbitronFont.getFamily(), 13));
        }
        
        root.getChildren().addAll(header, cardGrid, buttonPanel, instructionLabel);
        return root;
    }
    
    /**
     * 정보 패널 생성
     */
    private HBox createInfoPanel(int gameTimeLimit, int totalPairs) {
        HBox infoPanel = new HBox(20);
        infoPanel.setAlignment(Pos.CENTER);
        infoPanel.getStyleClass().add("memory-info-panel");
        
        // MOVES 박스
        VBox movesBox = new VBox(5);
        movesBox.setAlignment(Pos.CENTER);
        movesBox.getStyleClass().add("memory-stat-box");
        Label movesTitle = new Label("MOVES");
        movesTitle.getStyleClass().add("memory-stat-title");
        if (orbitronFont != null) {
            movesTitle.setFont(Font.font(orbitronFont.getFamily(), 11));
        }
        movesValueLabel = new Label("0");
        movesValueLabel.getStyleClass().add("memory-moves-value");
        if (orbitronFont != null) {
            movesValueLabel.setFont(Font.font(orbitronFont.getFamily(), 18));
        }
        movesBox.getChildren().addAll(movesTitle, movesValueLabel);
        
        // MATCHES 박스
        VBox pairsBox = new VBox(5);
        pairsBox.setAlignment(Pos.CENTER);
        pairsBox.getStyleClass().add("memory-stat-box");
        Label pairsTitle = new Label("MATCHES");
        pairsTitle.getStyleClass().add("memory-stat-title");
        if (orbitronFont != null) {
            pairsTitle.setFont(Font.font(orbitronFont.getFamily(), 11));
        }
        pairsLabel = new Label("0/" + totalPairs);
        pairsLabel.getStyleClass().add("memory-pairs-value");
        if (orbitronFont != null) {
            pairsLabel.setFont(Font.font(orbitronFont.getFamily(), 18));
        }
        pairsBox.getChildren().addAll(pairsTitle, pairsLabel);
        
        // TIME 박스
        VBox timeBox = new VBox(5);
        timeBox.setAlignment(Pos.CENTER);
        timeBox.getStyleClass().add("memory-stat-box");
        Label timeTitle = new Label("TIME");
        timeTitle.getStyleClass().add("memory-stat-title");
        if (orbitronFont != null) {
            timeTitle.setFont(Font.font(orbitronFont.getFamily(), 11));
        }
        timeLabel = new Label("0:30");
        timeLabel.getStyleClass().add("memory-time-value");
        if (orbitronFont != null) {
            timeLabel.setFont(Font.font(orbitronFont.getFamily(), 18));
        }
        timeBox.getChildren().addAll(timeTitle, timeLabel);
        
        infoPanel.getChildren().addAll(movesBox, pairsBox, timeBox);
        return infoPanel;
    }
    
    /**
     * 시작 버튼 생성
     */
    private Button createStartButton(Runnable onStart) {
        Button button = new Button("▶ NEW GAME");
        button.getStyleClass().add("memory-start-button");
        if (orbitronFont != null) {
            button.setFont(Font.font(orbitronFont.getFamily(), 14));
        }
        button.setOnAction(e -> onStart.run());
        return button;
    }
    
    /**
     * 초기화 버튼 생성
     */
    private Button createResetButton(Runnable onReset) {
        Button button = new Button("⚙ OPTIONS");
        button.getStyleClass().add("memory-reset-button");
        if (orbitronFont != null) {
            button.setFont(Font.font(orbitronFont.getFamily(), 14));
        }
        button.setOnAction(e -> onReset.run());
        button.setVisible(false);
        return button;
    }
    
    /**
     * 결과 화면 생성
     */
    public StackPane createResultOverlay(boolean success, int pairsFound, int totalPairs, long elapsed) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("memory-result-overlay");
        overlay.setPrefSize(750, 700);
        
        VBox resultBox = new VBox(20);
        resultBox.setAlignment(Pos.CENTER);
        
        Label resultLabel = new Label(success ? "★ YOU WIN! ★" : "⏰ TIME'S UP!");
        resultLabel.getStyleClass().add(success ? "memory-result-label-success" : "memory-result-label-fail");
        if (cinzelFont != null) {
            resultLabel.setFont(Font.font(cinzelFont.getFamily(), 42));
        }
        
        Label scoreLabel = new Label("MATCHES: " + pairsFound + "/" + totalPairs);
        scoreLabel.getStyleClass().add("memory-result-score");
        if (orbitronFont != null) {
            scoreLabel.setFont(Font.font(orbitronFont.getFamily(), 20));
        }
        
        // 실제 소요 시간을 계산 (30초 - 남은 시간)
        int actualTime = 30 - (int)elapsed;
        int minutes = actualTime / 60;
        int seconds = actualTime % 60;
        Label timeLabel = new Label(String.format("TIME: %d:%02d", minutes, seconds));
        timeLabel.getStyleClass().add("memory-result-time");
        if (orbitronFont != null) {
            timeLabel.setFont(Font.font(orbitronFont.getFamily(), 18));
        }
        
        resultBox.getChildren().addAll(resultLabel, scoreLabel, timeLabel);
        overlay.getChildren().add(resultBox);
        
        return overlay;
    }
    
    /**
     * 관전 모드 표시 추가
     */
    public void addSpectatorLabel(VBox root) {
        Label spectatorLabel = new Label("◆ SPECTATOR MODE ◆");
        spectatorLabel.getStyleClass().add("memory-spectator-label");
        root.getChildren().add(1, spectatorLabel);
    }
    
    /**
     * Scene 생성 및 표시
     */
    public void showScene(VBox root) {
        Scene scene = new Scene(root, 750, 700);
        scene.getStylesheets().add(getClass().getResource("/css/minigame/memory.css").toExternalForm());
        gameStage.setScene(scene);
        gameStage.show();
    }
    
    // Getters
    public Stage getGameStage() {
        return gameStage;
    }
    
    public Label getTimeLabel() {
        return timeLabel;
    }
    
    public Label getPairsLabel() {
        return pairsLabel;
    }
    
    public Label getMovesValueLabel() {
        return movesValueLabel;
    }
    
    public GridPane getCardGrid() {
        return cardGrid;
    }
    
    public Button getStartButton() {
        return startButton;
    }
    
    public Button getResetButton() {
        return resetButton;
    }
    
    /**
     * 카드 버튼 생성
     */
    public Button createCardButton(int value, Runnable onClick) {
        Button button = new Button();
        button.setPrefSize(120, 120);
        button.getStyleClass().addAll("memory-card", "memory-card-back");
        button.setText("?");
        // 카드 뒷면에 Cinzel 폰트 적용
        if (cinzelFont != null) {
            button.setFont(Font.font(cinzelFont.getFamily(), 48));
        }
        if (onClick != null) {
            button.setOnAction(e -> onClick.run());
        }
        
        // 카드 값을 userData에 저장
        button.setUserData(value);
        
        return button;
    }
    
    /**
     * 카드 스타일 업데이트
     */
    public void updateCardStyle(Button button, boolean flipped, boolean matched) {
        button.getStyleClass().clear();
        button.getStyleClass().add("memory-card");
        
        int value = (int) button.getUserData();
        
        if (matched) {
            button.setText(getCardSymbol(value));
            button.getStyleClass().add("memory-card-matched");
            // 매칭된 카드는 Orbitron 폰트
            if (orbitronFont != null) {
                button.setFont(Font.font(orbitronFont.getFamily(), 48));
            }
        } else if (flipped) {
            button.setText(getCardSymbol(value));
            button.getStyleClass().add("memory-card-front");
            // 앞면 카드는 Orbitron 폰트
            if (orbitronFont != null) {
                button.setFont(Font.font(orbitronFont.getFamily(), 48));
            }
        } else {
            button.setText("?");
            button.getStyleClass().add("memory-card-back");
            // 뒷면 카드는 Cinzel 폰트
            if (cinzelFont != null) {
                button.setFont(Font.font(cinzelFont.getFamily(), 48));
            }
        }
    }
    
    /**
     * 숫자를 이모지/심볼로 변환
     */
    private String getCardSymbol(int value) {
        switch (value) {
            case 1: return "⭐";
            case 2: return "❤️";
            case 3: return "🌟";
            case 4: return "🎵";
            case 5: return "🔥";
            case 6: return "💎";
            case 7: return "🌈";
            case 8: return "🎯";
            default: return String.valueOf(value);
        }
    }
}

