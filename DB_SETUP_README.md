# 오셀로 게임 - 데이터베이스 설정 가이드

## 📋 필수 요구사항

- MySQL 8.0 이상
- Java 21
- Maven

## 🗄️ 데이터베이스 설정

### 1. MySQL 설치

Windows:
```bash
# winget 사용
winget install Oracle.MySQL

# 또는 직접 다운로드
# https://dev.mysql.com/downloads/installer/
```

### 2. MySQL 서버 시작

```bash
# Windows
net start MySQL80

# 또는 MySQL Workbench 사용
```

### 3. 데이터베이스 생성

```bash
# MySQL 접속
mysql -u root -p

# SQL 파일 실행
mysql -u root -p < database_setup.sql
```

또는 MySQL Workbench에서:
1. `database_setup.sql` 파일 열기
2. 전체 선택 후 실행 (Ctrl + Shift + Enter)

### 4. 데이터베이스 연결 정보 수정

`OthelloGame/src/main/java/org/example/service/DatabaseService.java` 파일을 열고 다음 정보를 수정하세요:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/othello_db?useSSL=false&serverTimezone=UTC";
private static final String DB_USER = "root";              // ← 본인의 MySQL 사용자명
private static final String DB_PASSWORD = "your_password";  // ← 본인의 MySQL 비밀번호
```

## ✅ 연결 테스트

데이터베이스가 정상적으로 설정되었는지 확인:

```bash
mysql -u root -p

USE othello_db;
SHOW TABLES;

# 다음과 같이 표시되어야 함:
# +----------------------+
# | Tables_in_othello_db |
# +----------------------+
# | GameRecords          |
# | Users                |
# +----------------------+
```

## 🎮 게임 실행

데이터베이스 설정 후:

```bash
cd OthelloGame
mvn clean compile
mvn javafx:run
```

## 📊 데이터베이스 스키마

### Users 테이블
```sql
user_id         VARCHAR(50)   PRIMARY KEY
password_hash   VARCHAR(100)  NOT NULL (SHA-256 해시)
win_count       INT           DEFAULT 0
loss_count      INT           DEFAULT 0
draw_count      INT           DEFAULT 0
created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
```

### GameRecords 테이블
```sql
record_id             INT           AUTO_INCREMENT PRIMARY KEY
player1_id            VARCHAR(50)   FK
player2_id            VARCHAR(50)   FK
winner_id             VARCHAR(50)
match_date            DATETIME      DEFAULT CURRENT_TIMESTAMP
final_score_black     INT
final_score_white     INT
move_sequence_json    TEXT          (리플레이용 수순)
```

## 🔒 보안 참고사항

- 실제 배포 시에는 DB 비밀번호를 환경 변수로 관리하세요
- `DatabaseService.java`에서 비밀번호는 SHA-256으로 해싱됩니다
- 프로덕션 환경에서는 HTTPS와 추가 보안 조치가 필요합니다

## ❗ 문제 해결

### 연결 실패 시
1. MySQL 서비스가 실행 중인지 확인
2. 포트 3306이 열려있는지 확인
3. 방화벽 설정 확인
4. DB_USER와 DB_PASSWORD가 올바른지 확인

### 테이블이 안 보일 때
```sql
USE othello_db;
SOURCE database_setup.sql;
```

## 🚀 DB 없이 실행

DB 없이도 게임을 플레이할 수 있습니다 (게스트 모드):
- 로그인하지 않고 게임 플레이 가능
- 전적이 기록되지 않음
- AI, 로컬, 온라인 모드 모두 사용 가능

