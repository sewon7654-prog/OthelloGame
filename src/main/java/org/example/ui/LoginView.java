package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.service.DatabaseService;

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
        VBox mainLayout = new VBox(25);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(40));
        mainLayout.getStyleClass().add("login-container");

        // 타이틀
        Label title = new Label("오셀로 게임");
        title.getStyleClass().add("login-title");
        
        Label subtitle = new Label("로그인");
        subtitle.getStyleClass().add("login-subtitle");

        // 로그인 폼
        GridPane loginForm = createLoginForm();

        // 회원가입 버튼
        Button btnRegister = new Button("회원가입");
        btnRegister.getStyleClass().add("register-button");
        btnRegister.setOnAction(e -> showRegisterView());

        // 뒤로가기 버튼
        Button btnBack = new Button("← 메뉴로");
        btnBack.getStyleClass().add("back-button");
        btnBack.setOnAction(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        HBox buttonBox = new HBox(15, btnRegister, btnBack);
        buttonBox.setAlignment(Pos.CENTER);

        mainLayout.getChildren().addAll(title, subtitle, loginForm, buttonBox);

        Scene scene = new Scene(mainLayout, 450, 500);
        scene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("로그인");
    }

    /**
     * 로그인 폼 생성
     */
    private GridPane createLoginForm() {
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(30));
        formContainer.getStyleClass().add("form-container");

        Label lblUserId = new Label("아이디");
        lblUserId.getStyleClass().add("form-label");
        
        TextField tfUserId = new TextField();
        tfUserId.setPromptText("아이디를 입력하세요");
        tfUserId.getStyleClass().add("form-input");

        Label lblPassword = new Label("비밀번호");
        lblPassword.getStyleClass().add("form-label");
        
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("비밀번호를 입력하세요");
        pfPassword.getStyleClass().add("form-input");

        Button btnLogin = new Button("로그인");
        btnLogin.getStyleClass().add("login-submit-button");
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
        VBox mainLayout = new VBox(25);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(40));
        mainLayout.getStyleClass().add("login-container");

        // 타이틀
        Label title = new Label("오셀로 게임");
        title.getStyleClass().add("login-title");
        
        Label subtitle = new Label("회원가입");
        subtitle.getStyleClass().add("login-subtitle");

        // 회원가입 폼
        GridPane registerForm = createRegisterForm();

        // 뒤로가기 버튼
        Button btnBack = new Button("← 로그인 화면으로");
        btnBack.getStyleClass().add("back-button");
        btnBack.setOnAction(e -> show());

        mainLayout.getChildren().addAll(title, subtitle, registerForm, btnBack);

        Scene scene = new Scene(mainLayout, 500, 650);
        scene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("회원가입");
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
        lblUserId.getStyleClass().add("form-label");
        
        HBox idBox = new HBox(10);
        idBox.setAlignment(Pos.CENTER);
        
        TextField tfUserId = new TextField();
        tfUserId.setPromptText("아이디 입력 (영문, 숫자)");
        tfUserId.getStyleClass().add("id-input");
        
        Button btnCheckId = new Button("중복 확인");
        btnCheckId.getStyleClass().add("check-id-button");
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
        lblPassword.getStyleClass().add("form-label");
        
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("비밀번호 입력 (4자 이상)");
        pfPassword.getStyleClass().add("form-input");

        Label lblPasswordConfirm = new Label("비밀번호 확인");
        lblPasswordConfirm.getStyleClass().add("form-label");
        
        PasswordField pfPasswordConfirm = new PasswordField();
        pfPasswordConfirm.setPromptText("비밀번호 재입력");
        pfPasswordConfirm.getStyleClass().add("form-input");

        Button btnRegister = new Button("가입하기");
        btnRegister.getStyleClass().add("register-submit-button");
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
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

