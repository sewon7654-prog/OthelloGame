package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.service.DatabaseService;
import org.example.service.PixelArtUIService;

import java.util.List;

/**
 * 내 전적 화면
 */
public class StatsView {

    private final Stage primaryStage;
    private final DatabaseService dbService;
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
     * 내 전적 화면 표시
     */
    public void show() {
        double screenWidth = 1600;
        double screenHeight = 1000;

        // 로그인/메뉴와 동일한 픽셀아트 배경
        StackPane backgroundPane = PixelArtUIService.createPixelArtBackground(screenWidth, screenHeight);

        // 카드 컨테이너
        BorderPane cardPane = new BorderPane();
        cardPane.getStyleClass().add("card-panel");
        cardPane.setPadding(new Insets(24));
        cardPane.setMaxWidth(950);

        // 제목
        Label title = new Label("내 전적");
        title.getStyleClass().add("screen-title");
        BorderPane.setAlignment(title, Pos.CENTER);
        cardPane.setTop(title);

        // 최신 사용자 정보 갱신
        User freshUserInfo = dbService.getUserInfo(currentUser.getUserId());
        if (freshUserInfo != null) {
            currentUser = freshUserInfo;
        }

        // 통계/히스토리
        VBox statsBox = createStatsBox();
        cardPane.setCenter(statsBox);

        VBox historyBox = createHistoryBox();
        historyBox.setPadding(new Insets(16, 0, 0, 0));
        cardPane.setBottom(historyBox);
        BorderPane.setAlignment(historyBox, Pos.CENTER_LEFT);

        VBox content = new VBox(20, cardPane);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: transparent;");

        Button btnBack = new Button("메뉴로 돌아가기");
        btnBack.getStyleClass().add("back-to-menu-button");
        btnBack.setOnAction(e -> {
            if (onBackToMenu != null) {
                onBackToMenu.run();
            }
        });
        content.getChildren().add(btnBack);

        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundPane, content);

        Scene scene = new Scene(root, screenWidth, screenHeight);
        scene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/game.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("내 전적 - " + currentUser.getUserId());
    }

    /**
     * 통계 영역
     */
    private VBox createStatsBox() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("card-section");

        Label lblUserId = new Label("플레이어: " + currentUser.getUserId());
        lblUserId.getStyleClass().add("stat-label-strong");

        Label lblWin = new Label("승리: " + currentUser.getWinCount() + "회");
        lblWin.getStyleClass().add("stat-win");

        Label lblLoss = new Label("패배: " + currentUser.getLossCount() + "회");
        lblLoss.getStyleClass().add("stat-loss");

        Label lblDraw = new Label("무승부: " + currentUser.getDrawCount() + "회");
        lblDraw.getStyleClass().add("stat-draw");

        Label lblTotal = new Label("총 게임: " + currentUser.getTotalGames() + "회");
        lblTotal.getStyleClass().add("stat-normal");

        Label lblWinRate = new Label(String.format("승률: %.1f%%", currentUser.getWinRate()));
        lblWinRate.getStyleClass().add("stat-rate");

        Separator separator = new Separator();
        separator.getStyleClass().add("thin-separator");

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
     * 최근 게임 기록
     */
    private VBox createHistoryBox() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("최근 게임 기록");
        title.getStyleClass().add("section-title");

        List<String> history = dbService.getUserGameHistory(currentUser.getUserId(), 10);

        if (history.isEmpty()) {
            Label noData = new Label("아직 게임 기록이 없습니다.");
            noData.getStyleClass().add("stat-empty");
            box.getChildren().addAll(title, noData);
        } else {
            ListView<String> listView = new ListView<>();
            listView.getItems().addAll(history);
            listView.setPrefHeight(220);
            listView.getStyleClass().add("history-list");
            box.getChildren().addAll(title, listView);
        }

        return box;
    }
}
