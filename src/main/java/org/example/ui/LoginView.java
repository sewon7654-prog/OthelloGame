package org.example.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.model.User;
import org.example.service.DatabaseService;
import org.example.service.ButtonEffectService;
import org.example.service.PixelArtUIService;

/**
 * 로그인 및 회원가입 UI
 */
public class LoginView {

    private Stage primaryStage;
    private DatabaseService dbService;
    private Runnable onLoginSuccess;
    private Runnable onBackToMenu;
    private User currentUser;

    public LoginView(Stage stage) {
        this.primaryStage = stage;
        this.dbService = DatabaseService.getInstance();
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    public void setOnBackToMenu(Runnable callback) {
        this.onBackToMenu = callback;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * 로그인 화면 표시
     */
    public void show() {
        // 픽셀 아트 배경 생성 (메뉴 화면과 동일한 크기)
        double screenWidth = 1600;
        double screenHeight = 1000;
        StackPane backgroundPane = PixelArtUIService.createPixelArtBackground(screenWidth, screenHeight);
        
        VBox mainLayout = new VBox(30);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(60));
        mainLayout.getStyleClass().add("login-container");
        mainLayout.setStyle("-fx-background-color: transparent;");
        
        // 배경과 로그인 화면을 스택으로 합치기
        StackPane rootPane = new StackPane();
        rootPane.getChildren().addAll(backgroundPane, mainLayout);

        // 타이틀 (픽셀 아트 로고)
        StackPane titleLogo = PixelArtUIService.createLargePixelArtLogo("오셀로 게임", 800, 200);
        titleLogo.setAlignment(Pos.CENTER);
        
        Label subtitle = new Label("로그인");
        subtitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f4e5b7; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 4, 0, 2, 2);");

        // 로그인 폼
        GridPane loginForm = createLoginForm();

        // 회원가입 버튼 (픽셀 아트 스타일)
        StackPane btnRegisterPane = PixelArtUIService.createPixelArtButton("회원가입", 300, 70);
        Button btnRegister = new Button();
        btnRegister.setGraphic(btnRegisterPane);
        btnRegister.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btnRegister.setPrefSize(300, 70);
        ButtonEffectService.addPixelArtButtonEffects(btnRegister);
        ButtonEffectService.addClickParticleEffect(btnRegister);
        btnRegister.setOnAction(e -> showRegisterView());

        // 뒤로가기 버튼 (픽셀 아트 스타일)
        StackPane btnBackPane = PixelArtUIService.createPixelArtButton("← 메뉴로", 250, 70);
        Button btnBack = new Button();
        btnBack.setGraphic(btnBackPane);
        btnBack.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btnBack.setPrefSize(250, 70);
        ButtonEffectService.addPixelArtButtonEffects(btnBack);
        ButtonEffectService.addClickParticleEffect(btnBack);
        btnBack.setOnAction(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        HBox buttonBox = new HBox(20, btnRegister, btnBack);
        buttonBox.setAlignment(Pos.CENTER);

        mainLayout.getChildren().addAll(titleLogo, subtitle, loginForm, buttonBox);

        Scene scene = new Scene(rootPane, screenWidth, screenHeight);
        scene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("로그인");
        
        // 창 모드 설정
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setFullScreen(false);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(screenWidth);
        primaryStage.setMinHeight(screenHeight);
    }

    /**
     * 로그인 폼 생성
     */
    private GridPane createLoginForm() {
        VBox formContainer = new VBox(25);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(40));
        formContainer.getStyleClass().add("form-container");

        Label lblUserId = new Label("아이디");
        lblUserId.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f4e5b7; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 3, 0, 1, 1);");
        
        TextField tfUserId = new TextField();
        tfUserId.setPromptText("아이디를 입력하세요");
        tfUserId.setStyle("-fx-font-size: 16px; -fx-pref-width: 350px; -fx-pref-height: 40px; -fx-background-color: #f4e5b7; -fx-background-radius: 5px; -fx-border-color: #8B4513; -fx-border-width: 2px; -fx-border-radius: 5px;");

        Label lblPassword = new Label("비밀번호");
        lblPassword.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f4e5b7; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 3, 0, 1, 1);");
        
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("비밀번호를 입력하세요");
        pfPassword.setStyle("-fx-font-size: 16px; -fx-pref-width: 350px; -fx-pref-height: 40px; -fx-background-color: #f4e5b7; -fx-background-radius: 5px; -fx-border-color: #8B4513; -fx-border-width: 2px; -fx-border-radius: 5px;");

        // 로그인 버튼 (픽셀 아트 스타일)
        StackPane btnLoginPane = PixelArtUIService.createPixelArtButton("로그인", 350, 70);
        Button btnLogin = new Button();
        btnLogin.setGraphic(btnLoginPane);
        btnLogin.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btnLogin.setPrefSize(350, 70);
        ButtonEffectService.addPixelArtButtonEffects(btnLogin);
        ButtonEffectService.addClickParticleEffect(btnLogin);
        btnLogin.setOnAction(e -> handleLogin(tfUserId.getText(), pfPassword.getText()));

        // Enter 키로 로그인
        pfPassword.setOnAction(e -> handleLogin(tfUserId.getText(), pfPassword.getText()));

        formContainer.getChildren().addAll(lblUserId, tfUserId, lblPassword, pfPassword, btnLogin);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.add(formContainer, 0, 0);

        return grid;
    }

    /**
     * 로그인 처리
     */
    private void handleLogin(String userId, String password) {
        if (userId.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "입력 오류", "아이디와 비밀번호를 입력해주세요.");
            return;
        }

        if (!dbService.isConnected()) {
            showAlert(Alert.AlertType.ERROR, "DB 연결 오류", 
                "데이터베이스에 연결할 수 없습니다.\n설정을 확인해주세요.");
            return;
        }

        User user = dbService.loginUser(userId, password);
        if (user != null) {
            currentUser = user;
            showAlert(Alert.AlertType.INFORMATION, "로그인 성공", 
                user.getUserId() + "님 환영합니다!");
            if (onLoginSuccess != null) onLoginSuccess.run();
        } else {
            showAlert(Alert.AlertType.ERROR, "로그인 실패", 
                "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    /**
     * 회원가입 화면 표시
     */
    private void showRegisterView() {
        // 픽셀 아트 배경 생성 (메뉴 화면과 동일한 크기)
        double screenWidth = 1600;
        double screenHeight = 1000;
        StackPane backgroundPane = PixelArtUIService.createPixelArtBackground(screenWidth, screenHeight);
        
        VBox mainLayout = new VBox(30);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(50));
        mainLayout.getStyleClass().add("login-container");
        mainLayout.setStyle("-fx-background-color: transparent;");
        
        // 배경과 회원가입 화면을 스택으로 합치기
        StackPane rootPane = new StackPane();
        rootPane.getChildren().addAll(backgroundPane, mainLayout);

        // 타이틀 (픽셀 아트 로고)
        StackPane titleLogo = PixelArtUIService.createLargePixelArtLogo("오셀로 게임", 800, 180);
        titleLogo.setAlignment(Pos.CENTER);
        
        Label subtitle = new Label("회원가입");
        subtitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f4e5b7; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 4, 0, 2, 2);");

        // 회원가입 폼
        GridPane registerForm = createRegisterForm();

        // 뒤로가기 버튼 (픽셀 아트 스타일)
        StackPane btnBackPane = PixelArtUIService.createPixelArtButton("← 로그인 화면으로", 300, 70);
        Button btnBack = new Button();
        btnBack.setGraphic(btnBackPane);
        btnBack.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btnBack.setPrefSize(300, 70);
        ButtonEffectService.addPixelArtButtonEffects(btnBack);
        ButtonEffectService.addClickParticleEffect(btnBack);
        btnBack.setOnAction(e -> show());

        mainLayout.getChildren().addAll(titleLogo, subtitle, registerForm, btnBack);

        Scene scene = new Scene(rootPane, screenWidth, screenHeight);
        scene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("회원가입");
        
        // 창 모드 설정
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setFullScreen(false);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(screenWidth);
        primaryStage.setMinHeight(screenHeight);
    }

    /**
     * 회원가입 폼 생성
     */
    private GridPane createRegisterForm() {
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(30));
        formContainer.getStyleClass().add("form-container");

        Label lblUserId = new Label("아이디");
        lblUserId.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f4e5b7; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 3, 0, 1, 1);");
        
        HBox idBox = new HBox(10);
        idBox.setAlignment(Pos.CENTER);
        
        TextField tfUserId = new TextField();
        tfUserId.setPromptText("아이디 입력 (영문, 숫자)");
        tfUserId.setStyle("-fx-font-size: 16px; -fx-pref-width: 250px; -fx-pref-height: 40px; -fx-background-color: #f4e5b7; -fx-background-radius: 5px; -fx-border-color: #8B4513; -fx-border-width: 2px; -fx-border-radius: 5px;");
        
        // 중복 확인 버튼 (픽셀 아트 스타일)
        StackPane btnCheckIdPane = PixelArtUIService.createPixelArtButton("중복 확인", 150, 50);
        Button btnCheckId = new Button();
        btnCheckId.setGraphic(btnCheckIdPane);
        btnCheckId.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btnCheckId.setPrefSize(150, 50);
        ButtonEffectService.addPixelArtButtonEffects(btnCheckId);
        ButtonEffectService.addClickParticleEffect(btnCheckId);
        btnCheckId.setOnAction(e -> {
            String userId = tfUserId.getText();
            if (userId.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "입력 오류", "아이디를 입력해주세요.");
                return;
            }
            if (dbService.isUserIdExists(userId)) {
                showAlert(Alert.AlertType.WARNING, "중복 확인", "이미 사용 중인 아이디입니다.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "중복 확인", "사용 가능한 아이디입니다.");
            }
        });
        
        idBox.getChildren().addAll(tfUserId, btnCheckId);

        Label lblPassword = new Label("비밀번호");
        lblPassword.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f4e5b7; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 3, 0, 1, 1);");
        
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("비밀번호 입력 (4자 이상)");
        pfPassword.setStyle("-fx-font-size: 16px; -fx-pref-width: 350px; -fx-pref-height: 40px; -fx-background-color: #f4e5b7; -fx-background-radius: 5px; -fx-border-color: #8B4513; -fx-border-width: 2px; -fx-border-radius: 5px;");

        Label lblPasswordConfirm = new Label("비밀번호 확인");
        lblPasswordConfirm.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f4e5b7; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 3, 0, 1, 1);");
        
        PasswordField pfPasswordConfirm = new PasswordField();
        pfPasswordConfirm.setPromptText("비밀번호 재입력");
        pfPasswordConfirm.setStyle("-fx-font-size: 16px; -fx-pref-width: 350px; -fx-pref-height: 40px; -fx-background-color: #f4e5b7; -fx-background-radius: 5px; -fx-border-color: #8B4513; -fx-border-width: 2px; -fx-border-radius: 5px;");

        // 가입하기 버튼 (픽셀 아트 스타일)
        StackPane btnRegisterPane = PixelArtUIService.createPixelArtButton("가입하기", 350, 70);
        Button btnRegister = new Button();
        btnRegister.setGraphic(btnRegisterPane);
        btnRegister.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btnRegister.setPrefSize(350, 70);
        ButtonEffectService.addPixelArtButtonEffects(btnRegister);
        ButtonEffectService.addClickParticleEffect(btnRegister);
        btnRegister.setOnAction(e -> handleRegister(
            tfUserId.getText(), 
            pfPassword.getText(), 
            pfPasswordConfirm.getText()
        ));

        formContainer.getChildren().addAll(lblUserId, idBox, lblPassword, pfPassword, lblPasswordConfirm, pfPasswordConfirm, btnRegister);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.add(formContainer, 0, 0);

        return grid;
    }

    /**
     * 회원가입 처리
     */
    private void handleRegister(String userId, String password, String passwordConfirm) {
        // 입력 검증
        if (userId.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "입력 오류", "모든 항목을 입력해주세요.");
            return;
        }

        if (!password.equals(passwordConfirm)) {
            showAlert(Alert.AlertType.WARNING, "비밀번호 불일치", "비밀번호가 일치하지 않습니다.");
            return;
        }

        if (userId.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "입력 오류", "아이디는 3자 이상이어야 합니다.");
            return;
        }

        if (password.length() < 4) {
            showAlert(Alert.AlertType.WARNING, "입력 오류", "비밀번호는 4자 이상이어야 합니다.");
            return;
        }

        if (!dbService.isConnected()) {
            showAlert(Alert.AlertType.ERROR, "DB 연결 오류", 
                "데이터베이스에 연결할 수 없습니다.");
            return;
        }

        // 중복 확인
        if (dbService.isUserIdExists(userId)) {
            showAlert(Alert.AlertType.WARNING, "가입 실패", "이미 사용 중인 아이디입니다.");
            return;
        }

        // 회원가입 처리
        if (dbService.registerUser(userId, password)) {
            showAlert(Alert.AlertType.INFORMATION, "가입 완료", 
                "회원가입이 완료되었습니다!\n로그인해주세요.");
            show(); // 로그인 화면으로 돌아가기
        } else {
            showAlert(Alert.AlertType.ERROR, "가입 실패", 
                "회원가입 중 오류가 발생했습니다.");
        }
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

