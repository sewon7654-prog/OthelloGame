package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.service.DatabaseService;
import org.example.service.SoundService;

import java.util.HashMap;
import java.util.Map;

/**
 * 플레이어 설정 UI (개인정보 페이지)
 * 돌 커스터마이징, 사운드 설정 등
 */
public class SettingsView {
    
    private Stage primaryStage;
    private User currentUser;
    private Runnable onBackToMenu;
    private DatabaseService dbService;
    
    // 커스텀 색상
    private Color customBlackColor = Color.BLACK;
    private Color customWhiteColor = Color.WHITE;
    
    // 색상 선택기
    private ColorPicker blackColorPicker;
    private ColorPicker whiteColorPicker;
    
    // 사운드 설정
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
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(40));
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);");
        
        // 타이틀
        Label title = new Label("플레이어 설정");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        
        // 설정 컨테이너
        VBox settingsContainer = new VBox(25);
        settingsContainer.setAlignment(Pos.CENTER);
        settingsContainer.setPadding(new Insets(30));
        settingsContainer.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.95);
            -fx-background-radius: 15px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);
        """);
        settingsContainer.setMaxWidth(500);
        
        // 돌 커스터마이징 섹션
        Label customSectionTitle = new Label("돌 커스터마이징");
        customSectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        // 흑돌 색상 선택
        HBox blackColorBox = new HBox(15);
        blackColorBox.setAlignment(Pos.CENTER_LEFT);
        Label blackLabel = new Label("흑돌 색상:");
        blackLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        blackColorPicker = new ColorPicker(customBlackColor);
        blackColorPicker.setStyle("-fx-font-size: 14px;");
        
        // 미리보기
        Circle blackPreview = new Circle(20);
        blackPreview.setFill(customBlackColor);
        blackPreview.setStroke(Color.GRAY);
        blackPreview.setStrokeWidth(2);
        
        blackColorPicker.setOnAction(e -> {
            customBlackColor = blackColorPicker.getValue();
            blackPreview.setFill(customBlackColor);
        });
        
        blackColorBox.getChildren().addAll(blackLabel, blackColorPicker, blackPreview);
        
        // 백돌 색상 선택
        HBox whiteColorBox = new HBox(15);
        whiteColorBox.setAlignment(Pos.CENTER_LEFT);
        Label whiteLabel = new Label("백돌 색상:");
        whiteLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        whiteColorPicker = new ColorPicker(customWhiteColor);
        whiteColorPicker.setStyle("-fx-font-size: 14px;");
        
        // 미리보기
        Circle whitePreview = new Circle(20);
        whitePreview.setFill(customWhiteColor);
        whitePreview.setStroke(Color.GRAY);
        whitePreview.setStrokeWidth(2);
        
        whiteColorPicker.setOnAction(e -> {
            customWhiteColor = whiteColorPicker.getValue();
            whitePreview.setFill(customWhiteColor);
        });
        
        whiteColorBox.getChildren().addAll(whiteLabel, whiteColorPicker, whitePreview);
        
        // 사운드 설정 섹션
        Label soundSectionTitle = new Label("사운드 설정");
        soundSectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        HBox soundBox = new HBox(15);
        soundBox.setAlignment(Pos.CENTER_LEFT);
        Label soundLabel = new Label("사운드 효과:");
        soundLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        soundEnabledCheckBox = new CheckBox("사운드 활성화");
        soundEnabledCheckBox.setSelected(SoundService.getInstance().isSoundEnabled());
        soundEnabledCheckBox.setStyle("-fx-font-size: 14px;");
        
        soundEnabledCheckBox.setOnAction(e -> {
            SoundService.getInstance().setSoundEnabled(soundEnabledCheckBox.isSelected());
        });
        
        soundBox.getChildren().addAll(soundLabel, soundEnabledCheckBox);
        
        // 버튼들
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button saveButton = new Button("저장");
        saveButton.setStyle("""
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-pref-width: 120px;
            -fx-pref-height: 40px;
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-background-radius: 8px;
            -fx-cursor: hand;
        """);
        saveButton.setOnAction(e -> saveSettings());
        
        Button resetButton = new Button("기본값");
        resetButton.setStyle("""
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-pref-width: 120px;
            -fx-pref-height: 40px;
            -fx-background-color: #FF9800;
            -fx-text-fill: white;
            -fx-background-radius: 8px;
            -fx-cursor: hand;
        """);
        resetButton.setOnAction(e -> resetSettings());
        
        Button backButton = new Button("뒤로가기");
        backButton.setStyle("""
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-pref-width: 120px;
            -fx-pref-height: 40px;
            -fx-background-color: #757575;
            -fx-text-fill: white;
            -fx-background-radius: 8px;
            -fx-cursor: hand;
        """);
        backButton.setOnAction(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });
        
        buttonBox.getChildren().addAll(saveButton, resetButton, backButton);
        
        settingsContainer.getChildren().addAll(
            customSectionTitle,
            blackColorBox,
            whiteColorBox,
            new Separator(),
            soundSectionTitle,
            soundBox,
            new Separator(),
            buttonBox
        );
        
        mainLayout.getChildren().addAll(title, settingsContainer);
        
        Scene scene = new Scene(mainLayout, 600, 700);
        scene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("플레이어 설정");
    }
    
    /**
     * 설정 불러오기
     */
    private void loadSettings() {
        if (currentUser == null) return;
        
        // 데이터베이스에서 설정 불러오기
        Map<String, String> settings = dbService.getUserSettings(currentUser.getUserId());
        
        if (settings != null && !settings.isEmpty()) {
            // 흑돌 색상
            if (settings.containsKey("blackColor")) {
                try {
                    customBlackColor = Color.web(settings.get("blackColor"));
                    if (blackColorPicker != null) {
                        blackColorPicker.setValue(customBlackColor);
                    }
                } catch (Exception e) {
                    customBlackColor = Color.BLACK;
                }
            }
            
            // 백돌 색상
            if (settings.containsKey("whiteColor")) {
                try {
                    customWhiteColor = Color.web(settings.get("whiteColor"));
                    if (whiteColorPicker != null) {
                        whiteColorPicker.setValue(customWhiteColor);
                    }
                } catch (Exception e) {
                    customWhiteColor = Color.WHITE;
                }
            }
            
            // 사운드 설정
            if (settings.containsKey("soundEnabled")) {
                boolean soundEnabled = Boolean.parseBoolean(settings.get("soundEnabled"));
                SoundService.getInstance().setSoundEnabled(soundEnabled);
                if (soundEnabledCheckBox != null) {
                    soundEnabledCheckBox.setSelected(soundEnabled);
                }
            }
        }
    }
    
    /**
     * 설정 저장
     */
    private void saveSettings() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "저장 실패", "로그인이 필요합니다.");
            return;
        }
        
        Map<String, String> settings = new HashMap<>();
        settings.put("blackColor", colorToHex(customBlackColor));
        settings.put("whiteColor", colorToHex(customWhiteColor));
        settings.put("soundEnabled", String.valueOf(SoundService.getInstance().isSoundEnabled()));
        
        if (dbService.saveUserSettings(currentUser.getUserId(), settings)) {
            showAlert(Alert.AlertType.INFORMATION, "저장 완료", "설정이 저장되었습니다.");
        } else {
            showAlert(Alert.AlertType.ERROR, "저장 실패", "설정 저장 중 오류가 발생했습니다.");
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
        
        showAlert(Alert.AlertType.INFORMATION, "초기화 완료", "설정이 기본값으로 초기화되었습니다.");
    }
    
    /**
     * Color를 Hex 문자열로 변환
     */
    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
    
    /**
     * 설정된 색상 가져오기
     */
    public Color getCustomBlackColor() {
        return customBlackColor;
    }
    
    public Color getCustomWhiteColor() {
        return customWhiteColor;
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

