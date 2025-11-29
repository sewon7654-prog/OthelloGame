package org.example.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.service.DatabaseService;
import org.example.service.PixelArtUIService;
import org.example.service.SoundService;

import java.util.HashMap;
import java.util.Map;

/**
 * 플레이어 설정 화면
 */
public class SettingsView {

    private final Stage primaryStage;
    private User currentUser;
    private Runnable onBackToMenu;
    private final DatabaseService dbService;

    private Color customBlackColor = Color.BLACK;
    private Color customWhiteColor = Color.WHITE;

    private ColorPicker blackColorPicker;
    private ColorPicker whiteColorPicker;
    private CheckBox soundEnabledCheckBox;

    public SettingsView(Stage stage, User user) {
        this.primaryStage = stage;
        this.currentUser = user;
        this.dbService = DatabaseService.getInstance();
        loadSettings();
    }

    public void setOnBackToMenu(Runnable callback) {
        this.onBackToMenu = callback;
    }

    /**
     * 설정 화면 표시
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
        cardPane.setMaxWidth(520);

        // 제목
        Label title = new Label("플레이어 설정");
        title.getStyleClass().add("screen-title");
        BorderPane.setAlignment(title, Pos.CENTER);
        cardPane.setTop(title);

        // 설정 영역
        VBox contentBox = new VBox(18);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.getStyleClass().add("card-section");

        // 색상 설정
        Label colorSection = new Label("돌 색상 설정");
        colorSection.getStyleClass().add("section-title");

        HBox blackColorBox = new HBox(12);
        blackColorBox.setAlignment(Pos.CENTER_LEFT);
        Label blackLabel = new Label("흑돌 색상:");
        blackLabel.getStyleClass().add("stat-label-strong");
        blackColorPicker = new ColorPicker(customBlackColor);
        blackColorPicker.getStyleClass().add("color-picker-small");
        Circle blackPreview = createPreviewCircle(customBlackColor);
        blackColorPicker.setOnAction(e -> {
            customBlackColor = blackColorPicker.getValue();
            blackPreview.setFill(customBlackColor);
        });
        blackColorBox.getChildren().addAll(blackLabel, blackColorPicker, blackPreview);

        HBox whiteColorBox = new HBox(12);
        whiteColorBox.setAlignment(Pos.CENTER_LEFT);
        Label whiteLabel = new Label("백돌 색상:");
        whiteLabel.getStyleClass().add("stat-label-strong");
        whiteColorPicker = new ColorPicker(customWhiteColor);
        whiteColorPicker.getStyleClass().add("color-picker-small");
        Circle whitePreview = createPreviewCircle(customWhiteColor);
        whiteColorPicker.setOnAction(e -> {
            customWhiteColor = whiteColorPicker.getValue();
            whitePreview.setFill(customWhiteColor);
        });
        whiteColorBox.getChildren().addAll(whiteLabel, whiteColorPicker, whitePreview);

        Separator sep1 = new Separator();
        sep1.getStyleClass().add("thin-separator");

        // 사운드 설정
        Label soundSection = new Label("사운드 설정");
        soundSection.getStyleClass().add("section-title");
        HBox soundBox = new HBox(12);
        soundBox.setAlignment(Pos.CENTER_LEFT);
        Label soundLabel = new Label("사운드 효과:");
        soundLabel.getStyleClass().add("stat-label-strong");
        soundEnabledCheckBox = new CheckBox("사운드 사용");
        soundEnabledCheckBox.getStyleClass().add("stat-normal");
        soundEnabledCheckBox.setSelected(SoundService.getInstance().isSoundEnabled());
        soundEnabledCheckBox.setOnAction(e -> SoundService.getInstance().setSoundEnabled(soundEnabledCheckBox.isSelected()));
        soundBox.getChildren().addAll(soundLabel, soundEnabledCheckBox);

        // 버튼 영역
        Separator sep2 = new Separator();
        sep2.getStyleClass().add("thin-separator");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveButton = new Button("저장");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(e -> saveSettings());

        Button resetButton = new Button("기본값");
        resetButton.getStyleClass().add("secondary-button");
        resetButton.setOnAction(e -> resetSettings());

        Button backButton = new Button("뒤로가기");
        backButton.getStyleClass().add("back-to-menu-button");
        backButton.setOnAction(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        buttonBox.getChildren().addAll(saveButton, resetButton, backButton);

        contentBox.getChildren().addAll(
            colorSection,
            blackColorBox,
            whiteColorBox,
            sep1,
            soundSection,
            soundBox,
            sep2,
            buttonBox
        );

        cardPane.setCenter(contentBox);

        VBox layout = new VBox(20, cardPane);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: transparent;");

        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundPane, layout);

        Scene scene = new Scene(root, screenWidth, screenHeight);
        scene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/game.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("플레이어 설정");
    }

    private Circle createPreviewCircle(Color fill) {
        Circle c = new Circle(18);
        c.setFill(fill);
        c.setStroke(Color.web("#2F4F2F"));
        c.setStrokeWidth(2);
        return c;
    }

    /**
     * 설정 불러오기
     */
    private void loadSettings() {
        if (currentUser == null) return;

        Map<String, String> settings = dbService.getUserSettings(currentUser.getUserId());

        if (settings != null && !settings.isEmpty()) {
            if (settings.containsKey("blackColor")) {
                try {
                    customBlackColor = Color.web(settings.get("blackColor"));
                } catch (Exception e) {
                    customBlackColor = Color.BLACK;
                }
            }
            if (settings.containsKey("whiteColor")) {
                try {
                    customWhiteColor = Color.web(settings.get("whiteColor"));
                } catch (Exception e) {
                    customWhiteColor = Color.WHITE;
                }
            }
            if (settings.containsKey("soundEnabled")) {
                boolean soundEnabled = Boolean.parseBoolean(settings.get("soundEnabled"));
                SoundService.getInstance().setSoundEnabled(soundEnabled);
            }
        }
    }

    /**
     * 설정 저장
     */
    private void saveSettings() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "실패", "로그인이 필요합니다.");
            return;
        }

        Map<String, String> settings = new HashMap<>();
        settings.put("blackColor", colorToHex(customBlackColor));
        settings.put("whiteColor", colorToHex(customWhiteColor));
        settings.put("soundEnabled", String.valueOf(SoundService.getInstance().isSoundEnabled()));

        if (dbService.saveUserSettings(currentUser.getUserId(), settings)) {
            showAlert(Alert.AlertType.INFORMATION, "완료", "설정이 저장되었습니다.");
        } else {
            showAlert(Alert.AlertType.ERROR, "실패", "설정 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 설정 초기화
     */
    private void resetSettings() {
        customBlackColor = Color.BLACK;
        customWhiteColor = Color.WHITE;

        if (blackColorPicker != null) blackColorPicker.setValue(customBlackColor);
        if (whiteColorPicker != null) whiteColorPicker.setValue(customWhiteColor);
        if (soundEnabledCheckBox != null) {
            soundEnabledCheckBox.setSelected(true);
            SoundService.getInstance().setSoundEnabled(true);
        }

        showAlert(Alert.AlertType.INFORMATION, "완료", "설정이 기본값으로 초기화되었습니다.");
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type, content, javafx.scene.control.ButtonType.OK);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
