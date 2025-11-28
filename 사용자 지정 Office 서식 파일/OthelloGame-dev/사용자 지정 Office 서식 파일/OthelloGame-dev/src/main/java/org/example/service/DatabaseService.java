package org.example.service;

import org.example.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 데이터베이스 연결 및 쿼리를 담당하는 서비스 클래스
 * MySQL 연동
 */
public class DatabaseService {

    // DB 연결 정보 (추후 application.properties로 분리 가능)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/othello_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234"; // 실제 비밀번호로 변경 필요

    private static DatabaseService instance;
    private Connection connection;

    private DatabaseService() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connect();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /**
     * DB 연결
     */
    private void connect() {
        try {
            // 먼저 데이터베이스가 없으면 생성
            createDatabaseIfNotExists();
            
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connected successfully!");
            
            // 테이블 자동 생성
            initializeTables();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    /**
     * 데이터베이스가 없으면 생성
     */
    private void createDatabaseIfNotExists() {
        try {
            String urlWithoutDb = "jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            Connection tempConnection = DriverManager.getConnection(urlWithoutDb, DB_USER, DB_PASSWORD);
            Statement stmt = tempConnection.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS othello_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            stmt.close();
            tempConnection.close();
            System.out.println("Database 'othello_db' checked/created successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to create database: " + e.getMessage());
        }
    }

    /**
     * 테이블 자동 생성 (앱 시작 시 실행)
     */
    private void initializeTables() {
        if (!isConnected()) return;
        
        try {
            Statement stmt = connection.createStatement();
            
            // Users 테이블 생성
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS Users (
                    user_id VARCHAR(50) PRIMARY KEY,
                    password_hash VARCHAR(100) NOT NULL,
                    win_count INT DEFAULT 0,
                    loss_count INT DEFAULT 0,
                    draw_count INT DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            stmt.executeUpdate(createUsersTable);
            System.out.println("Users table checked/created successfully!");
            
            // GameRecords 테이블 생성
            String createGameRecordsTable = """
                CREATE TABLE IF NOT EXISTS GameRecords (
                    record_id INT AUTO_INCREMENT PRIMARY KEY,
                    player1_id VARCHAR(50),
                    player2_id VARCHAR(50),
                    winner_id VARCHAR(50),
                    match_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                    final_score_black INT,
                    final_score_white INT,
                    move_sequence_json TEXT,
                    FOREIGN KEY (player1_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (player2_id) REFERENCES Users(user_id) ON DELETE CASCADE
                )
                """;
            stmt.executeUpdate(createGameRecordsTable);
            System.out.println("GameRecords table checked/created successfully!");
            
            // UserSettings 테이블 생성
            String createUserSettingsTable = """
                CREATE TABLE IF NOT EXISTS UserSettings (
                    user_id VARCHAR(50) PRIMARY KEY,
                    settings_json TEXT,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
                )
            """;
            stmt.executeUpdate(createUserSettingsTable);
            System.out.println("UserSettings table checked/created successfully!");
            
            // 인덱스 생성 (이미 있으면 무시됨)
            try {
                stmt.executeUpdate("CREATE INDEX idx_match_date ON GameRecords(match_date)");
                stmt.executeUpdate("CREATE INDEX idx_player1 ON GameRecords(player1_id)");
                stmt.executeUpdate("CREATE INDEX idx_player2 ON GameRecords(player2_id)");
            } catch (SQLException e) {
                // 인덱스가 이미 존재하는 경우 무시
            }
            
            stmt.close();
            System.out.println("Database tables initialized successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to initialize tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * DB 연결 상태 확인
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 비밀번호 해싱 (SHA-256)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // ========== User 관련 메서드 ==========

    /**
     * 회원가입
     */
    public boolean registerUser(String userId, String password) {
        if (!isConnected()) connect();
        
        String sql = "INSERT INTO Users (user_id, password_hash, win_count, loss_count, draw_count) VALUES (?, ?, 0, 0, 0)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, hashPassword(password));
            pstmt.executeUpdate();
            System.out.println("User registered: " + userId);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to register user: " + e.getMessage());
            return false;
        }
    }

    /**
     * 로그인
     */
    public User loginUser(String userId, String password) {
        if (!isConnected()) connect();
        
        String sql = "SELECT * FROM Users WHERE user_id = ? AND password_hash = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, hashPassword(password));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getString("user_id"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setWinCount(rs.getInt("win_count"));
                user.setLossCount(rs.getInt("loss_count"));
                user.setDrawCount(rs.getInt("draw_count"));
                System.out.println("User logged in: " + userId);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Failed to login: " + e.getMessage());
        }
        return null;
    }

    /**
     * 사용자 ID 중복 확인
     */
    public boolean isUserIdExists(String userId) {
        if (!isConnected()) connect();
        
        String sql = "SELECT COUNT(*) FROM Users WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to check user ID: " + e.getMessage());
        }
        return false;
    }

    /**
     * 사용자 정보 조회
     */
    public User getUserInfo(String userId) {
        if (!isConnected()) connect();
        
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getString("user_id"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setWinCount(rs.getInt("win_count"));
                user.setLossCount(rs.getInt("loss_count"));
                user.setDrawCount(rs.getInt("draw_count"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Failed to get user info: " + e.getMessage());
        }
        return null;
    }

    /**
     * 전적 업데이트 (승리)
     */
    public void updateWin(String userId) {
        updateStats(userId, "win_count");
    }

    /**
     * 전적 업데이트 (패배)
     */
    public void updateLoss(String userId) {
        updateStats(userId, "loss_count");
    }

    /**
     * 전적 업데이트 (무승부)
     */
    public void updateDraw(String userId) {
        updateStats(userId, "draw_count");
    }

    private void updateStats(String userId, String column) {
        if (!isConnected()) connect();
        
        String sql = "UPDATE Users SET " + column + " = " + column + " + 1 WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
            System.out.println("Updated " + column + " for user: " + userId);
        } catch (SQLException e) {
            System.err.println("Failed to update stats: " + e.getMessage());
        }
    }

    // ========== GameRecords 관련 메서드 ==========

    /**
     * 게임 결과 저장
     */
    public void saveGameRecord(String player1Id, String player2Id, String winnerId, 
                               int finalScoreBlack, int finalScoreWhite, String moveSequenceJson) {
        if (!isConnected()) connect();
        
        String sql = "INSERT INTO GameRecords (player1_id, player2_id, winner_id, final_score_black, final_score_white, move_sequence_json) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player1Id);
            pstmt.setString(2, player2Id);
            pstmt.setString(3, winnerId);
            pstmt.setInt(4, finalScoreBlack);
            pstmt.setInt(5, finalScoreWhite);
            pstmt.setString(6, moveSequenceJson);
            pstmt.executeUpdate();
            System.out.println("Game record saved");
        } catch (SQLException e) {
            System.err.println("Failed to save game record: " + e.getMessage());
        }
    }

    /**
     * 사용자의 최근 게임 기록 조회
     */
    public List<String> getUserGameHistory(String userId, int limit) {
        if (!isConnected()) connect();
        
        List<String> history = new ArrayList<>();
        String sql = "SELECT * FROM GameRecords WHERE player1_id = ? OR player2_id = ? ORDER BY match_date DESC LIMIT ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            pstmt.setInt(3, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String player1 = rs.getString("player1_id");
                String player2 = rs.getString("player2_id");
                String winner = rs.getString("winner_id");
                int blackScore = rs.getInt("final_score_black");
                int whiteScore = rs.getInt("final_score_white");
                Timestamp matchDate = rs.getTimestamp("match_date");
                
                String record = String.format("%s vs %s | 승자: %s | %d:%d | %s", 
                    player1, player2, winner, blackScore, whiteScore, matchDate);
                history.add(record);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get game history: " + e.getMessage());
        }
        return history;
    }

    /**
     * 사용자 설정 저장
     */
    public boolean saveUserSettings(String userId, Map<String, String> settings) {
        if (!isConnected()) connect();
        
        // Map을 JSON 형식으로 변환 (간단한 구현)
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        json.append("}");
        
        String sql = """
            INSERT INTO UserSettings (user_id, settings_json) 
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE settings_json = ?, updated_at = CURRENT_TIMESTAMP
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, json.toString());
            pstmt.setString(3, json.toString());
            pstmt.executeUpdate();
            System.out.println("User settings saved for: " + userId);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to save user settings: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 사용자 설정 불러오기
     */
    public Map<String, String> getUserSettings(String userId) {
        if (!isConnected()) connect();
        
        String sql = "SELECT settings_json FROM UserSettings WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String json = rs.getString("settings_json");
                return parseSettingsJson(json);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load user settings: " + e.getMessage());
        }
        return new HashMap<>();
    }
    
    /**
     * 간단한 JSON 파싱 (실제로는 JSON 라이브러리 사용 권장)
     */
    private Map<String, String> parseSettingsJson(String json) {
        Map<String, String> settings = new HashMap<>();
        if (json == null || json.isEmpty()) return settings;
        
        try {
            // 간단한 파싱: {"key":"value","key2":"value2"}
            json = json.trim().replace("{", "").replace("}", "");
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replace("\"", "");
                    String value = keyValue[1].trim().replace("\"", "");
                    settings.put(key, value);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse settings JSON: " + e.getMessage());
        }
        return settings;
    }
    
    /**
     * 연결 종료
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close connection: " + e.getMessage());
        }
    }
}
