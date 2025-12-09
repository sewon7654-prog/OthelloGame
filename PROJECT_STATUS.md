# 🎮 오셀로 게임 프로젝트 현황 보고서

**작성일**: 2025년 11월 25일  
**프로젝트**: JavaFX 기반 오셀로 게임 (AI, 온라인 대전, 미니게임 통합)

---

## 📋 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [완료된 기능](#완료된-기능)
3. [진행 중인 작업](#진행-중인-작업)
4. [향후 구현 예정](#향후-구현-예정)
5. [기술 스택](#기술-스택)
6. [폴더 구조](#폴더-구조)
7. [주요 파일 설명](#주요-파일-설명)
8. [알려진 이슈](#알려진-이슈)
9. [다음 단계 가이드](#다음-단계-가이드)

---

## 🎯 프로젝트 개요

### 목표
- **기본 오셀로 게임**: 8x8 보드, 표준 오셀로 규칙
- **AI 대전 모드**: Gemini API를 활용한 3가지 난이도 (Easy, Medium, Hard)
- **온라인 대전**: 2인 네트워크 대전 (클라이언트-서버 구조)
- **미니게임 시스템**: 3가지 찬스카드 게임으로 게임 내 이점 제공
- **데이터베이스 연동**: 사용자 정보, 게임 기록 저장

### 핵심 특징
- JavaFX 기반 GUI
- 모던한 UI/UX (커스텀 폰트, 그라데이션, 애니메이션)
- 실시간 네트워크 동기화
- 미니게임 관전 모드

---

## ✅ 완료된 기능

### 1. 핵심 게임 로직 ✅
- [x] 8x8 오셀로 보드 구현
- [x] 돌 놓기 및 뒤집기 로직
- [x] 유효한 수 검증
- [x] 게임 종료 판정
- [x] 점수 계산
- [x] 턴 관리

**주요 파일**: `GameModel.java`

---

### 2. 게임 모드 ✅

#### 2.1 로컬 2인 대전 ✅
- 한 컴퓨터에서 2명이 번갈아가며 플레이
- 실시간 점수 표시

#### 2.2 AI 대전 ✅
- **Easy (초급)**: 랜덤 수
- **Medium (중급)**: 로컬 알고리즘 (최대 뒤집기 개수 기준)
- **Hard (고급)**: Gemini API 호출 (전략적 판단)

**특징**:
- API 키 환경변수 관리 (`config.properties`)
- API 타임아웃 설정 (Medium: 3초, Hard: 5초)
- API 실패 시 로컬 알고리즘으로 폴백

**주요 파일**: `AIPlayer.java`, `ConfigService.java`

**API 키 설정**:
```properties
# config.properties
gemini.api.key=YOUR_GEMINI_API_KEY
```

#### 2.3 온라인 대전 ✅
- 클라이언트-서버 구조
- IP 주소 입력 방식
- 자동 매칭 (2명이 접속하면 게임 시작)
- 색상 자동 배정 (선착순: Black, 후착순: White)
- 실시간 수 동기화

**주요 파일**: `NetworkServer.java`, `NetworkClient.java`

---

### 3. 미니게임 시스템 🎮

#### 3.1 기억력 게임 (Memory Game) ✅ **완성**
- **게임 방식**: 
  - 카드 16개 (8쌍)
  - 처음 3초간 전체 카드 공개
  - "시작" 버튼 클릭 시 카드 뒤집기
  - 30초 내에 8쌍 모두 맞추면 성공
  
- **UI**:
  - 레트로 픽셀 게임 스타일
  - 커스텀 폰트 적용 (Cinzel, Orbitron)
  - 타이머, 움직임 카운터, 매치 카운터
  
- **구현 파일**:
  - `MemoryGame.java` (로직)
  - `MemoryGameView.java` (UI)
  - `memory.css` (스타일)

#### 3.2 반응속도 게임 (Reaction Game) 🟡 **템플릿만 존재**
- 현재 상태: 기본 구조만 생성됨
- 필요한 작업: 게임 로직 및 UI 구현

**파일**: `ReactionGame.java`, `ReactionGameView.java`

#### 3.3 회피 게임 (Dodge Game) 🟡 **템플릿만 존재**
- 현재 상태: 기본 구조만 생성됨
- 필요한 작업: 게임 로직 및 UI 구현

**파일**: `DodgeGame.java`, `DodgeGameView.java`

#### 3.4 미니게임 네트워크 기능 ✅
- **온라인 대전 중 미니게임 사용 가능**
- 상대방 턴에만 찬스카드 사용 가능
- 미니게임 진행 중 상대방은 실시간 관전
- 성공 시: 상대방 턴을 랜덤 수로 자동 진행
- 실패 시: 게임 계속 진행

**네트워크 프로토콜**:
- `MINIGAME_START`: 미니게임 시작 알림
- `MINIGAME_UPDATE`: 미니게임 상태 업데이트
- `MINIGAME_RESULT`: 미니게임 결과 전송
- `MINIGAME_CLOSE`: 미니게임 종료
- `RANDOM_MOVE_EXECUTED`: 랜덤 수 실행 알림

**주요 파일**: 
- `MinigameProtocol.java`
- `MinigameBase.java`
- `NetworkServer.java` (서버 측 중계)
- `NetworkClient.java` (클라이언트 측 수신)

---

### 4. UI/UX ✨

#### 4.1 메인 게임 화면 ✅
- **레이아웃**:
  - 상단: 모드 표시, 점수, 현재 턴
  - 중앙: 오셀로 보드 (680x680px)
  - 우측: 찬스카드 패널 (220px)

- **색상 테마**: 녹색/금색 고급 테마
  - 배경: 녹색 그라데이션 (`#2d6a4f` → `#1e5128`)
  - 보드: 금색 테두리 + 녹색 배경
  - 카드: 각 게임별 고유 색상

- **화면 크기**: 1020x880px (고정)

#### 4.2 찬스카드 UI ✅
- **카드 디자인**:
  - 크기: 180x230px
  - 각 카드별 고유 색상:
    - 기억력: 녹색 그라데이션
    - 반응속도: 금색 그라데이션
    - 회피게임: 빨간색 그라데이션
  
- **애니메이션**:
  - Float 효과: 아이콘이 2초마다 위아래로 8px 이동
  - Hover 효과: 카드가 위로 10px 올라가고 1.05배 확대
  
- **사용된 카드 효과**:
  - 회색 그라데이션 변환
  - "USED" 스탬프 표시 (빨간색, -15도 회전)
  - 애니메이션 중지
  - 클릭 비활성화

#### 4.3 커스텀 폰트 ✅
- **Cinzel Bold**: 제목용 (세리프 폰트)
- **Orbitron Bold**: 본문용 (기하학적 레트로 폰트)

**위치**: `src/main/resources/fonts/`

#### 4.4 보드 및 돌 디자인 ✅
- **보드 타일**: 녹색 체크무늬 패턴 (밝은/어두운 타일)
- **게임 돌**: 
  - 흑돌: 3D 방사형 그라데이션 (회색 → 검은색)
  - 백돌: 3D 방사형 그라데이션 (흰색 → 회색)
  - 부드러운 그림자 효과

#### 4.5 메뉴 화면 ✅
- 게임 모드 선택 (로컬, AI, 온라인)
- 로그인/회원가입 기능

---

### 5. 데이터베이스 연동 ✅
- **MySQL 연결**
- 사용자 인증 (로그인/회원가입)
- 게임 기록 저장
- 통계 조회

**주요 파일**: `DatabaseService.java`

**설정 파일**: `config.properties`
```properties
db.url=jdbc:mysql://localhost:3306/othello
db.username=root
db.password=YOUR_PASSWORD
```

---

## 🔄 진행 중인 작업

### 1. 카드 이미지 시스템 🟡
- **현재 상태**: 이미지 로딩 로직 구현 완료
- **필요한 작업**: 실제 카드 이미지 파일 추가

**이미지 위치**: `src/main/resources/images/cards/`
- `memory.png` (180x230px)
- `reaction.png` (180x230px)
- `dodge.png` (180x230px)

**현재**: 이미지가 없으면 이모지로 폴백 (🎲, ⚡, 🎯)

---

## 📅 향후 구현 예정

### 우선순위 1: 미니게임 완성 🎯

#### 1.1 반응속도 게임 (Reaction Game)
**게임 컨셉**:
- 화면에 랜덤 시간 후 신호 표시
- 최대한 빠르게 버튼 클릭
- 반응 시간이 일정 기준 이하면 성공

**구현 필요 사항**:
- 게임 로직:
  - 랜덤 대기 시간 (1-3초)
  - 신호 표시 (색상 변경 또는 아이콘)
  - 반응 시간 측정
  - 성공/실패 판정 (예: 0.5초 이내)
  
- UI:
  - 대기 화면
  - 신호 표시 화면
  - 결과 화면 (반응 시간 표시)
  
- 네트워크:
  - `getStateJson()`: 현재 게임 상태 (대기/신호/결과)
  - `updateFromJson()`: 상태 업데이트

**참고**: HTML 코드 제공된 디자인 참고

#### 1.2 회피 게임 (Dodge Game)
**게임 컨셉**:
- 화면에 떨어지는 장애물 회피
- 일정 시간 동안 생존하면 성공
- 마우스 또는 키보드로 캐릭터 이동

**구현 필요 사항**:
- 게임 로직:
  - 캐릭터 이동 (마우스/키보드)
  - 장애물 생성 및 이동
  - 충돌 감지
  - 생존 시간 측정
  - 성공/실패 판정 (예: 30초 생존)
  
- UI:
  - 게임 화면 (캐릭터 + 장애물)
  - 타이머 표시
  - 결과 화면
  
- 네트워크:
  - `getStateJson()`: 캐릭터 위치, 장애물 위치
  - `updateFromJson()`: 상태 업데이트

**참고**: HTML 코드 제공된 디자인 참고

#### 1.3 미니게임 공통 작업
- [ ] CSS 스타일 통일 (memory.css 참고)
- [ ] 커스텀 폰트 적용
- [ ] 애니메이션 효과 추가
- [ ] 관전 모드 UI 최적화
- [ ] 성공/실패 피드백 개선

---

### 우선순위 2: 게임 밸런스 조정 ⚖️

#### 2.1 미니게임 난이도
- [ ] 기억력 게임: 30초는 적절한지 테스트
- [ ] 반응속도 게임: 성공 기준 결정 (예: 0.5초 이내)
- [ ] 회피 게임: 생존 시간 결정 (예: 30초)

#### 2.2 찬스카드 밸런스
- [ ] 카드 사용 조건 검토 (현재: 상대방 턴에만 사용)
- [ ] 성공 시 이점 조정 (현재: 상대방 랜덤 수)
- [ ] 카드당 1회 제한은 적절한지 검토

---

### 우선순위 3: UI/UX 개선 🎨

#### 3.1 추가 애니메이션
- [ ] 돌 놓을 때 애니메이션 (Scale + Fade In)
- [ ] 돌 뒤집기 애니메이션 (Rotate)
- [ ] 게임 종료 화면 애니메이션
- [ ] 승리/패배 이펙트

#### 3.2 사운드 효과 (선택사항)
- [ ] 돌 놓는 소리
- [ ] 카드 사용 소리
- [ ] 미니게임 성공/실패 소리
- [ ] 배경 음악

#### 3.3 카드 이미지
- [ ] 전문 디자이너에게 의뢰 또는
- [ ] AI 이미지 생성 도구 활용 (DALL-E, Midjourney 등)
- [ ] 이미지 추가 후 테스트

---

### 우선순위 4: 기능 추가 🚀

#### 4.1 게임 내 기능
- [ ] 게임 저장/불러오기
- [ ] 기권 기능
- [ ] 무르기 기능 (로컬 전용)
- [ ] 플레이 리플레이

#### 4.2 통계 및 순위
- [ ] 승률 통계
- [ ] 온라인 랭킹 시스템
- [ ] 플레이어 프로필
- [ ] 게임 히스토리

#### 4.3 추가 게임 모드
- [ ] AI 토너먼트 모드
- [ ] 타임 어택 모드
- [ ] 핸디캡 모드

---

### 우선순위 5: 최적화 및 안정성 🔧

#### 5.1 성능 최적화
- [ ] AI 응답 시간 개선
- [ ] 네트워크 레이턴시 최적화
- [ ] 메모리 사용량 최적화
- [ ] UI 렌더링 최적화

#### 5.2 에러 처리
- [ ] 네트워크 끊김 처리
- [ ] API 오류 핸들링 강화
- [ ] 데이터베이스 연결 실패 처리
- [ ] 유효하지 않은 입력 처리

#### 5.3 테스트
- [ ] 단위 테스트 작성
- [ ] 통합 테스트
- [ ] 네트워크 테스트
- [ ] 부하 테스트

---

## 🛠️ 기술 스택

### 프레임워크 및 라이브러리
- **JavaFX 21**: GUI 프레임워크
- **Maven**: 빌드 도구 및 의존성 관리
- **MySQL Connector/J 8.0.33**: 데이터베이스 연동
- **JSON 20230227**: JSON 파싱 (API 통신)

### 외부 API
- **Google Gemini API**: AI 대전 (Hard 난이도)

### 데이터베이스
- **MySQL 8.0**: 사용자 정보, 게임 기록 저장

### 개발 환경
- **Java 17+**
- **IntelliJ IDEA / VSCode**
- **Git / GitHub**

---

## 📁 폴더 구조

```
OthelloGame/
├── src/main/
│   ├── java/org/example/
│   │   ├── Main.java                    # 애플리케이션 진입점
│   │   ├── model/
│   │   │   └── GameModel.java           # 게임 로직 (보드, 규칙, 점수)
│   │   ├── service/
│   │   │   ├── AIPlayer.java            # AI 플레이어 (Gemini API 연동)
│   │   │   ├── DatabaseService.java     # 데이터베이스 연동
│   │   │   └── ConfigService.java       # 설정 파일 관리
│   │   ├── network/
│   │   │   ├── NetworkServer.java       # 온라인 대전 서버
│   │   │   └── NetworkClient.java       # 온라인 대전 클라이언트
│   │   ├── ui/
│   │   │   ├── MenuView.java            # 메인 메뉴 화면
│   │   │   └── GameView.java            # 게임 화면 (보드, 카드, UI)
│   │   └── minigame/
│   │       ├── base/
│   │       │   ├── MinigameBase.java    # 미니게임 인터페이스
│   │       │   └── MinigameResult.java  # 미니게임 결과 데이터
│   │       ├── network/
│   │       │   └── MinigameProtocol.java # 미니게임 네트워크 프로토콜
│   │       └── games/
│   │           ├── memory/
│   │           │   ├── MemoryGame.java      # 기억력 게임 로직 ✅
│   │           │   └── MemoryGameView.java  # 기억력 게임 UI ✅
│   │           ├── reaction/
│   │           │   ├── ReactionGame.java    # 반응속도 게임 (템플릿) 🟡
│   │           │   └── ReactionGameView.java
│   │           └── dodge/
│   │               ├── DodgeGame.java       # 회피 게임 (템플릿) 🟡
│   │               └── DodgeGameView.java
│   └── resources/
│       ├── css/
│       │   ├── common.css               # 공통 스타일
│       │   ├── game.css                 # 게임 화면 스타일 ✅
│       │   └── minigame/
│       │       ├── common.css           # 미니게임 공통 스타일
│       │       └── memory.css           # 기억력 게임 스타일 ✅
│       ├── fonts/
│       │   ├── Cinzel-Bold.ttf          # 제목용 폰트 ✅
│       │   └── Orbitron-Bold.ttf        # 본문용 폰트 ✅
│       └── images/
│           └── cards/                    # 찬스카드 이미지 (추가 필요) 🟡
│               ├── memory.png            # (180x230px)
│               ├── reaction.png          # (180x230px)
│               └── dodge.png             # (180x230px)
├── config.properties                     # 설정 파일 (API 키, DB 정보)
├── pom.xml                               # Maven 설정
└── target/                               # 빌드 결과물 (자동 생성)
```

**범례**:
- ✅: 완성됨
- 🟡: 부분 완성 또는 템플릿만 존재
- ❌: 미구현

---

## 📝 주요 파일 설명

### 1. `GameModel.java`
**역할**: 오셀로 게임의 핵심 로직
- 8x8 보드 상태 관리 (`board[][]`)
- 돌 놓기 및 뒤집기 (`placePieceAndFlip()`)
- 유효한 수 검증 (`isValidMove()`, `getValidMoves()`)
- 게임 종료 판정 (`isGameOver()`)
- 점수 계산 (`getScore()`)

**주요 메서드**:
```java
public boolean placePieceAndFlip(int x, int y)
public List<int[]> getValidMoves()
public boolean isValidMove(int x, int y)
public boolean isGameOver()
public int getScore(int player)
```

---

### 2. `GameView.java`
**역할**: 게임 화면 UI 및 사용자 상호작용
- 보드 렌더링 (`drawBoard()`)
- 카드 UI 생성 (`createSingleCard()`)
- 미니게임 실행 (`startMinigame()`)
- 네트워크 통신 연동

**주요 메서드**:
```java
public void show(Stage primaryStage)
private void drawBoard()
private VBox createSingleCard(String icon, String name, String gameType, int cardIndex)
private void startMinigame(String gameType)
public void showMinigameSpectator(String gameType)
public void handleRandomMove()
```

**UI 구성**:
- `TILE_SIZE`: 85px
- `SCENE_WIDTH`: 1020px
- `SCENE_HEIGHT`: 880px

---

### 3. `AIPlayer.java`
**역할**: AI 플레이어 로직
- **Easy**: `getRandomMove()` - 유효한 수 중 랜덤 선택
- **Medium**: `getBestMoveByFlipCount()` - 가장 많이 뒤집는 수 선택 (로컬)
- **Hard**: `getBestMove()` - Gemini API 호출

**API 호출 흐름**:
1. 현재 보드 상태를 JSON으로 변환
2. Gemini API에 전략적 판단 요청
3. API 응답 파싱 (x, y 좌표)
4. 실패 시 Medium 알고리즘으로 폴백

**주요 설정**:
```java
MEDIUM_TIMEOUT = 3000ms
HARD_TIMEOUT = 5000ms
```

---

### 4. `NetworkServer.java` & `NetworkClient.java`
**역할**: 온라인 대전 네트워크 통신

**서버 (`NetworkServer.java`)**:
- 포트: `ConfigService.getServerPort()` (기본 12345)
- 매칭 로직: 2명 접속 시 자동 매칭
- 메시지 중계: `MOVE`, `MINIGAME_*`, `RANDOM_MOVE`

**클라이언트 (`NetworkClient.java`)**:
- 서버 연결 (IP 주소 입력)
- 수 전송: `sendMove(int x, int y)`
- 미니게임 메시지 전송: `sendMinigameStart()`, `sendMinigameResult()`

**프로토콜**:
- `START_BLACK opponentId`: 흑돌 시작
- `START_WHITE opponentId`: 백돌 시작
- `MOVE x y`: 수 전송
- `MINIGAME_START gameType`: 미니게임 시작
- `MINIGAME_RESULT success score time`: 미니게임 결과
- `RANDOM_MOVE_EXECUTED`: 랜덤 수 실행

---

### 5. `MemoryGame.java` & `MemoryGameView.java`
**역할**: 기억력 미니게임

**게임 로직 (`MemoryGame.java`)**:
- 카드 16개 (8쌍) 생성 및 셔플
- 카드 선택 및 매칭 검증
- 30초 타이머
- 성공/실패 판정

**UI (`MemoryGameView.java`)**:
- 4x4 그리드 레이아웃
- 카드 앞/뒤 전환 애니메이션
- 타이머, 움직임, 매치 표시

**상태 동기화**:
```java
public String getStateJson()  // JSON 형태로 현재 상태 반환
public void updateFromJson(String json)  // JSON으로 상태 업데이트
```

---

### 6. `MinigameProtocol.java`
**역할**: 미니게임 네트워크 프로토콜 정의

**메시지 포맷**:
```
MINIGAME_START gameType
MINIGAME_UPDATE gameType jsonState
MINIGAME_RESULT gameType success score time
MINIGAME_CLOSE
```

**주요 메서드**:
```java
public static String createStartMessage(String gameType)
public static String createResultMessage(boolean success, int score, long time)
public static boolean isMinigameMessage(String message)
```

---

### 7. `config.properties`
**역할**: 환경 설정 파일 (`.gitignore`에 추가됨)

**내용**:
```properties
# Gemini API
gemini.api.key=YOUR_GEMINI_API_KEY

# Database
db.url=jdbc:mysql://localhost:3306/othello
db.username=root
db.password=YOUR_DB_PASSWORD

# Server
server.port=12345
```

**주의**: 이 파일은 Git에 커밋되지 않음 (보안)

---

## ⚠️ 알려진 이슈

### 1. CSS 경고 (해결됨) ✅
**문제**: `border-style: double` 미지원  
**해결**: `border-style: solid`로 변경

### 2. 카드 이미지 미존재 🟡
**문제**: 카드 이미지 파일이 없음  
**현재**: 이모지로 폴백  
**해결 방법**: `src/main/resources/images/cards/`에 PNG 이미지 추가

### 3. 반응속도/회피 게임 미구현 🟡
**문제**: 템플릿만 존재, 실제 게임 로직 없음  
**해결 방법**: [향후 구현 예정](#우선순위-1-미니게임-완성-) 참고

### 4. API 키 노출 위험 ✅
**문제**: 하드코딩된 API 키가 GitHub에 노출됨  
**해결**: `config.properties` + `.gitignore` 사용

### 5. 네트워크 끊김 처리 🟡
**문제**: 온라인 대전 중 연결 끊김 시 에러 처리 부족  
**해결 방법**: try-catch 강화 및 재연결 로직 추가 필요

---

## 🚀 다음 단계 가이드

### 새로운 AI가 이 프로젝트를 이어받을 때

#### Step 1: 환경 설정
1. **Java 17+ 설치**
2. **Maven 설치** (IntelliJ는 내장)
3. **MySQL 8.0 설치** 및 데이터베이스 생성
4. **config.properties 생성**:
   ```properties
   gemini.api.key=YOUR_KEY
   db.url=jdbc:mysql://localhost:3306/othello
   db.username=root
   db.password=YOUR_PASSWORD
   server.port=12345
   ```

#### Step 2: 프로젝트 빌드
```bash
mvn clean install
```

#### Step 3: 실행
**방법 1: IntelliJ**
- `Main.java` 우클릭 → Run

**방법 2: Maven**
```bash
mvn javafx:run
```

**방법 3: JAR 파일**
```bash
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/OthelloGame.jar
```

#### Step 4: 서버 실행 (온라인 대전용)
**별도 터미널에서**:
```bash
java org.example.network.NetworkServer
```

---

### 미니게임 구현 가이드

#### 반응속도 게임 구현 예시

**1단계: `ReactionGame.java` 로직 구현**
```java
public class ReactionGame implements MinigameBase {
    private long startTime;
    private long reactionTime;
    private boolean gameStarted = false;
    private boolean signalShown = false;
    
    // 랜덤 대기 시간 후 신호 표시
    private void showSignalAfterDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(1000 + new Random().nextInt(2000)); // 1-3초
                signalShown = true;
                startTime = System.currentTimeMillis();
                // UI 업데이트
            } catch (InterruptedException e) {}
        }).start();
    }
    
    // 버튼 클릭 시
    public void onButtonClick() {
        if (!signalShown) return; // 신호 전 클릭은 무시
        reactionTime = System.currentTimeMillis() - startTime;
        gameStarted = false;
        // 성공/실패 판정
    }
    
    @Override
    public boolean isSuccess() {
        return reactionTime < 500; // 0.5초 이내
    }
}
```

**2단계: `ReactionGameView.java` UI 구현**
- 대기 화면: "신호를 기다리세요..."
- 신호 화면: 색상 변경 + "클릭!"
- 결과 화면: 반응 시간 표시

**3단계: CSS 스타일 (`reaction.css`)**
- `memory.css` 참고하여 스타일 작성

---

### 디버깅 팁

#### 1. 게임이 시작되지 않을 때
- `Main.java`에서 `launch()` 호출 확인
- JavaFX 런타임 모듈 로드 확인
- VM 옵션: `--add-modules javafx.controls,javafx.fxml`

#### 2. AI가 작동하지 않을 때
- `config.properties`에 API 키 확인
- API 호출 로그 확인: `System.out.println()` 추가
- 타임아웃 설정 확인

#### 3. 온라인 대전이 연결되지 않을 때
- 서버가 실행 중인지 확인
- 방화벽 설정 확인
- IP 주소가 올바른지 확인
- 포트 번호 확인 (기본 12345)

#### 4. 미니게임이 표시되지 않을 때
- CSS 파일 경로 확인
- `resources/` 폴더가 빌드에 포함되는지 확인 (`pom.xml`)
- 폰트 파일 로드 확인

---

## 📚 참고 자료

### 프로젝트 내 문서
- `DB_SETUP_README.md`: 데이터베이스 설정 가이드
- `DEPLOYMENT_GUIDE.md`: 배포 가이드
- `GEMINI_AI_EXPLANATION.md`: Gemini API 사용법
- `IMPLEMENTATION_STATUS.md`: 구현 상태 요약

### 외부 자료
- [JavaFX Documentation](https://openjfx.io/)
- [Gemini API Documentation](https://ai.google.dev/docs)
- [Maven Documentation](https://maven.apache.org/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

---

## 🎯 우선순위 요약

### 즉시 구현 가능 (1-2일)
1. ✅ 반응속도 게임 로직 및 UI
2. ✅ 회피 게임 로직 및 UI
3. ✅ 카드 이미지 추가

### 단기 목표 (1주일)
4. ⚖️ 게임 밸런스 조정 및 테스트
5. 🎨 애니메이션 효과 추가
6. 🔧 에러 처리 강화

### 중장기 목표 (2주 이상)
7. 🚀 추가 기능 구현 (저장/불러오기, 통계 등)
8. 🎵 사운드 효과 추가 (선택사항)
9. 📊 순위 시스템 구축

---

## 📞 연락처 및 기여

### 프로젝트 정보
- **GitHub**: (저장소 URL)
- **개발 기간**: 2025년 11월
- **개발자**: (이름)

### 기여 방법
1. 이 문서를 읽고 프로젝트 구조 파악
2. [향후 구현 예정](#향후-구현-예정) 섹션에서 작업 선택
3. 새 브랜치 생성: `git checkout -b feature/reaction-game`
4. 구현 후 커밋: `git commit -m "feat: implement reaction game"`
5. Pull Request 생성

---

## ✅ 체크리스트

### 프로젝트를 이어받는 개발자용

- [ ] 프로젝트 클론 및 빌드 성공
- [ ] `config.properties` 생성 및 설정
- [ ] 데이터베이스 연결 확인
- [ ] 로컬 게임 실행 테스트
- [ ] AI 대전 실행 테스트
- [ ] 온라인 대전 실행 테스트 (서버 + 2개 클라이언트)
- [ ] 기억력 미니게임 실행 테스트
- [ ] 이 문서 전체 읽기 ✅
- [ ] 다음 작업 선택 및 착수

---

**작성일**: 2025년 11월 25일  
**마지막 업데이트**: 2025년 11월 25일  
**문서 버전**: 1.0

이 문서는 프로젝트의 전체 현황을 파악하고 새로운 개발자가 빠르게 온보딩할 수 있도록 작성되었습니다.

