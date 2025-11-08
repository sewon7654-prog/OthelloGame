package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.service.DatabaseService;

import java.util.List;

/**
 * 전적 조회 UI
 */
public class StatsView {

    private Stage primaryStage;
    private DatabaseService dbService;
    private User currentUser;
    private Runnable onBackToMenu;

    public StatsView(Stage stage, User user) {
        this.primaryStage = stage;
        this.currentUser = user;
        this.dbService = DatabaseService.getInstance();
    }

    public void setOnBackToMenu(Runnable callback) {
        this.onBackToMenu = callback;
    }

    /**
     * 전적 조회 화면 표시
     */
    public void show() {
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(30));

        // 제목
        Label title = new Label("내 전적");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 사용자 정보 갱신
        User freshUserInfo = dbService.getUserInfo(currentUser.getUserId());
        if (freshUserInfo != null) {
            currentUser = freshUserInfo;
        }

        // 전적 정보 표시
        VBox statsBox = createStatsBox();

        // 최근 게임 기록
        VBox historyBox = createHistoryBox();

        // 뒤로가기 버튼
        Button btnBack = new Button("메뉴로 돌아가기");
        btnBack.setStyle("-fx-font-size: 14px;");
        btnBack.setOnAction(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        mainLayout.getChildren().addAll(title, statsBox, historyBox, btnBack);

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 500, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("내 전적 - " + currentUser.getUserId());
    }

    /**
     * 전적 통계 박스 생성
     */
    private VBox createStatsBox() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20; -fx-background-radius: 10;");

        Label lblUserId = new Label("플레이어: " + currentUser.getUserId());
        lblUserId.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label lblWin = new Label("승리: " + currentUser.getWinCount() + "회");
        lblWin.setStyle("-fx-font-size: 16px; -fx-text-fill: green;");

        Label lblLoss = new Label("패배: " + currentUser.getLossCount() + "회");
        lblLoss.setStyle("-fx-font-size: 16px; -fx-text-fill: red;");

        Label lblDraw = new Label("무승부: " + currentUser.getDrawCount() + "회");
        lblDraw.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");

        Label lblTotal = new Label("총 게임: " + currentUser.getTotalGames() + "회");
        lblTotal.setStyle("-fx-font-size: 16px;");

        Label lblWinRate = new Label(String.format("승률: %.1f%%", currentUser.getWinRate()));
        lblWinRate.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: blue;");

        Separator separator = new Separator();

        box.getChildren().addAll(
            lblUserId, 
            separator,
            lblWin, 
            lblLoss, 
            lblDraw, 
            lblTotal,
            lblWinRate
        );

        return box;
    }

    /**
     * 최근 게임 기록 박스 생성
     */
    private VBox createHistoryBox() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("최근 게임 기록");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 최근 10개 게임 기록 조회
        List<String> history = dbService.getUserGameHistory(currentUser.getUserId(), 10);

        if (history.isEmpty()) {
            Label noData = new Label("아직 게임 기록이 없습니다.");
            noData.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            box.getChildren().addAll(title, noData);
        } else {
            ListView<String> listView = new ListView<>();
            listView.getItems().addAll(history);
            listView.setPrefHeight(200);
            listView.setStyle("-fx-font-size: 12px;");

            box.getChildren().addAll(title, listView);
        }

        return box;
    }
}

