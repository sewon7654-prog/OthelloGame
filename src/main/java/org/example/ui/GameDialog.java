package org.example.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 공통 게임 스타일 알림창 유틸리티.
 * 모든 Alert 호출을 이 클래스를 통해 일관된 스타일로 보여준다.
 */
public final class GameDialog {

    private GameDialog() {}

    public static void showInfo(Stage owner, String title, String message) {
        show(Alert.AlertType.INFORMATION, owner, title, message);
    }

    public static void showWarning(Stage owner, String title, String message) {
        show(Alert.AlertType.WARNING, owner, title, message);
    }

    public static void showError(Stage owner, String title, String message) {
        show(Alert.AlertType.ERROR, owner, title, message);
    }

    private static void show(Alert.AlertType type, Stage owner, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            if (owner != null) {
                alert.initOwner(owner);
                alert.initModality(Modality.WINDOW_MODAL);
            }
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            // 버튼 한글 통일
            alert.getButtonTypes().setAll(ButtonType.OK);

            DialogPane pane = alert.getDialogPane();
            // 공통 CSS 적용
            pane.getStylesheets().add(GameDialog.class.getResource("/css/common.css").toExternalForm());
            pane.getStylesheets().add(GameDialog.class.getResource("/css/game.css").toExternalForm());
            pane.getStylesheets().add(GameDialog.class.getResource("/css/dialog.css").toExternalForm());
            pane.getStyleClass().add("game-dialog");

            Button okButton = (Button) pane.lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setText("확인");
                okButton.getStyleClass().add("primary-button");
            }

            alert.showAndWait();
        });
    }
}
