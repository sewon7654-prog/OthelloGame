-- 오셀로 게임 데이터베이스 설정 파일
-- MySQL 8.0+ 기준

-- 1. 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS othello_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE othello_db;

-- 2. Users 테이블 생성
CREATE TABLE IF NOT EXISTS Users (
    user_id VARCHAR(50) PRIMARY KEY,
    password_hash VARCHAR(100) NOT NULL, -- 비밀번호는 해시 값으로 저장 (SHA-256)
    win_count INT DEFAULT 0,
    loss_count INT DEFAULT 0,
    draw_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. GameRecords 테이블 생성
CREATE TABLE IF NOT EXISTS GameRecords (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    player1_id VARCHAR(50), -- FK
    player2_id VARCHAR(50), -- FK
    winner_id VARCHAR(50),
    match_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    final_score_black INT,
    final_score_white INT,
    -- A-06 리플레이를 위한 전체 수순 (JSON 형식으로 저장)
    move_sequence_json TEXT,
    FOREIGN KEY (player1_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (player2_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 4. 인덱스 생성 (성능 향상)
CREATE INDEX idx_match_date ON GameRecords(match_date);
CREATE INDEX idx_player1 ON GameRecords(player1_id);
CREATE INDEX idx_player2 ON GameRecords(player2_id);

-- 5. 테스트 데이터 (선택사항)
-- INSERT INTO Users (user_id, password_hash, win_count, loss_count, draw_count) 
-- VALUES ('testuser', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 5, 3, 1);
-- 비밀번호: 123 (SHA-256 해시)

-- 확인
SELECT 'Database setup completed!' AS Status;
SHOW TABLES;

