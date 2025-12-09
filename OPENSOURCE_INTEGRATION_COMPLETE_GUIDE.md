# 오픈소스 미니게임 완벽 통합 가이드 🎮

## 📋 목차
1. [오픈소스 선정 기준](#1-오픈소스-선정-기준)
2. [GitHub에서 다운로드](#2-github에서-다운로드)
3. [폴더 구조 분석](#3-폴더-구조-분석)
4. [파일 복사 및 배치](#4-파일-복사-및-배치)
5. [코드 수정](#5-코드-수정)
6. [테스트](#6-테스트)
7. [실전 예시](#7-실전-예시)

---

## 1. 오픈소스 선정 기준

### ✅ 필수 조건
- **JavaFX 기반** (Java Swing 제외)
- **Java 11 이상** 호환
- **단독 실행 가능** (복잡한 의존성 없음)
- **MIT/Apache 라이선스** (자유롭게 수정 가능)

### ⭐ 추천 조건
- **최근 1년 이내 업데이트**
- **README가 상세함**
- **Star 10개 이상**
- **코드가 깔끔하고 간단함**

### 🔍 검색 키워드
```
GitHub 검색:
"javafx game"
"javafx reaction game"
"javafx avoid game"
"javafx whack a mole"
"javafx flappy bird"
"javafx dodge game"
```

---

## 2. GitHub에서 다운로드

### 방법 A: ZIP 다운로드 (추천)

```
1. GitHub 페이지 접속
2. 우측 상단 "Code" 버튼 클릭
3. "Download ZIP" 선택
4. 다운로드 폴더에서 압축 해제
```

### 방법 B: Git Clone

```bash
git clone https://github.com/사용자명/프로젝트명.git
```

---

## 3. 폴더 구조 분석

### 일반적인 JavaFX 프로젝트 구조

```
downloaded-project/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/game/
│       │       ├── Main.java              ← 메인 클래스 (시작점)
│       │       ├── GameController.java    ← 게임 로직
│       │       ├── GameObject.java        ← 게임 오브젝트
│       │       └── utils/
│       │           └── Helper.java
│       └── resources/
│           ├── images/                    ← 이미지 파일
│           ├── sounds/                    ← 사운드 파일
│           └── styles/                    ← CSS 파일
├── pom.xml                                ← 의존성 확인
└── README.md
```

### 🔎 중요 파일 찾기

#### Step 1: 메인 클래스 찾기
```
src/main/java/ 폴더에서 찾기:
- "extends Application" 포함
- "public static void main" 포함
- "Main", "App", "Game" 이름 포함
```

#### Step 2: 의존 파일 확인
```java
// Main.java 내부에서
import com.example.game.GameController;  ← 이것도 필요
import com.example.game.GameObject;      ← 이것도 필요
```

#### Step 3: 리소스 파일 확인
```
src/main/resources/ 폴더:
- images/ 폴더 전체
- sounds/ 폴더 전체
- *.css 파일들
- *.fxml 파일들
```

---

## 4. 파일 복사 및 배치

### 🎯 오셀로 프로젝트 폴더 구조

```
OthelloGame/
├── src/main/java/org/example/minigame/games/
│   ├── memory/           ← 기억력 게임 (이미 완성)
│   │   ├── MemoryGame.java
│   │   └── MemoryCard.java
│   │
│   ├── reaction/         ← 반응속도 게임 (오픈소스 넣을 곳)
│   │   ├── ReactionGame.java
│   │   ├── Target.java
│   │   └── 기타 필요한 클래스들...
│   │
│   └── dodge/            ← 회피 게임 (오픈소스 넣을 곳)
│       ├── DodgeGame.java
│       ├── Player.java
│       ├── Obstacle.java
│       └── 기타 필요한 클래스들...
│
└── src/main/resources/minigame/
    ├── reaction/
    │   └── images/       ← 반응속도 게임 이미지
    └── dodge/
        └── images/       ← 회피 게임 이미지
```

### 📂 복사 규칙

#### Java 파일
```
오픈소스:
downloaded-project/src/main/java/com/example/game/Main.java

↓ 복사 ↓

내 프로젝트:
OthelloGame/src/main/java/org/example/minigame/games/reaction/ReactionGame.java
```

#### 리소스 파일
```
오픈소스:
downloaded-project/src/main/resources/images/button.png

↓ 복사 ↓

내 프로젝트:
OthelloGame/src/main/resources/minigame/reaction/images/button.png
```

---

## 5. 코드 수정

### 수정 1: 패키지명 변경

```java
// 원본
package com.example.game;

// ↓ 수정 ↓

package org.example.minigame.games.reaction;
```

### 수정 2: 클래스명 변경 (선택)

```java
// 원본
public class Main extends Application {

// ↓ 수정 ↓

public class ReactionGame extends Application {
```

### 수정 3: Application 상속 제거

```java
// ===== 원본 =====
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // 게임 UI 생성
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Game");
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

// ===== 수정 =====
public class ReactionGame implements MinigameBase {
    private Stage gameStage;
    private boolean finished = false;
    private boolean success = false;
    
    @Override
    public void startPlayerMode(Stage parentStage, MinigameCallback callback) {
        gameStage = new Stage();
        gameStage.initModality(Modality.WINDOW_MODAL);
        gameStage.initOwner(parentStage);
        
        // 원본의 start() 코드를 여기로 이동
        Scene scene = new Scene(root, 800, 600);
        gameStage.setScene(scene);
        gameStage.setTitle("Reaction Game");
        gameStage.show();
        
        // 60초 타이머 추가
        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(60), e -> {
            finished = true;
            success = checkSuccess();
            gameStage.close();
            if (callback != null) {
                callback.onComplete(new MinigameResult(success, score, 60, MinigameType.REACTION));
            }
        }));
        timer.play();
    }
    
    @Override
    public void startSpectatorMode(Stage parentStage) {
        startPlayerMode(parentStage, null);
        // 입력 비활성화
    }
    
    @Override
    public String getStateJson() {
        return "{}";
    }
    
    @Override
    public void updateFromJson(String json) {
        // 관전자 동기화
    }
    
    @Override
    public boolean isFinished() {
        return finished;
    }
    
    @Override
    public boolean isSuccess() {
        return success;
    }
    
    @Override
    public void closeGame() {
        if (gameStage != null) {
            gameStage.close();
        }
    }
    
    @Override
    public MinigameType getType() {
        return MinigameType.REACTION;
    }
    
    // main() 메서드 삭제 또는 주석 처리
}
```

### 수정 4: 리소스 경로 수정

```java
// 원본
Image img = new Image("/images/button.png");
Image bg = new Image("background.jpg");

// ↓ 수정 ↓

Image img = new Image("/minigame/reaction/images/button.png");
Image bg = new Image("/minigame/reaction/images/background.jpg");
```

### 수정 5: import 문 추가

```java
// 파일 상단에 추가
package org.example.minigame.games.reaction;

import org.example.minigame.base.MinigameBase;
import org.example.minigame.base.MinigameCallback;
import org.example.minigame.base.MinigameResult;
import org.example.minigame.base.MinigameType;
import javafx.stage.Modality;
import javafx.stage.Stage;
// ... 기타 필요한 import
```

---

## 6. 테스트

### Step 1: 컴파일 확인

IntelliJ에서:
```
1. 수정한 파일 열기
2. Ctrl + F9 (빌드)
3. 오류 없으면 OK
```

### Step 2: 단독 테스트

테스트 파일 생성:
```java
// ReactionGameTest.java
package org.example.minigame.games.reaction;

import javafx.application.Application;
import javafx.stage.Stage;

public class ReactionGameTest extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.show();
        
        ReactionGame game = new ReactionGame();
        game.startPlayerMode(primaryStage, result -> {
            System.out.println("게임 결과: " + result.isSuccess());
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

실행:
```
1. ReactionGameTest.java 우클릭
2. "Run 'ReactionGameTest.main()'"
3. 게임 창이 정상적으로 열리면 성공!
```

### Step 3: pom.xml 수정하여 실행

```xml
<!-- 임시로 mainClass 변경 -->
<configuration>
    <mainClass>org.example.minigame.games.reaction.ReactionGameTest</mainClass>
</configuration>
```

Maven javafx:run 실행

---

## 7. 실전 예시

### 예시 1: Whack-a-Mole 게임 통합

#### 📥 다운로드
```
GitHub: "javafx whack a mole"
예시: https://github.com/user/whacka-mole-javafx
```

#### 📂 원본 구조
```
whacka-mole-javafx/
├── src/
│   └── WhackAMole.java       ← 메인 파일
│   └── Mole.java             ← 두더지 클래스
└── resources/
    └── images/
        ├── mole.png
        └── hole.png
```

#### ✂️ 복사
```
WhackAMole.java → OthelloGame/src/main/java/org/example/minigame/games/reaction/ReactionGame.java
Mole.java       → OthelloGame/src/main/java/org/example/minigame/games/reaction/Mole.java

images/ 전체   → OthelloGame/src/main/resources/minigame/reaction/images/
```

#### ✏️ 수정

**ReactionGame.java:**
```java
// 1. 패키지 변경
package org.example.minigame.games.reaction;

// 2. import 추가
import org.example.minigame.base.*;

// 3. 클래스 수정
public class ReactionGame implements MinigameBase {
    // MinigameBase 인터페이스 구현
}
```

**Mole.java:**
```java
// 1. 패키지 변경
package org.example.minigame.games.reaction;

// 2. 이미지 경로 수정
Image moleImg = new Image("/minigame/reaction/images/mole.png");
Image holeImg = new Image("/minigame/reaction/images/hole.png");
```

#### 🧪 테스트
```
1. ReactionGameTest.java 생성 (위의 템플릿 사용)
2. 실행
3. 정상 작동 확인
```

---

### 예시 2: Flappy Bird 스타일 회피 게임

#### 📥 다운로드
```
GitHub: "javafx flappy bird"
예시: https://github.com/user/flappy-javafx
```

#### 📂 원본 구조
```
flappy-javafx/
├── src/
│   ├── FlappyBird.java       ← 메인
│   ├── Bird.java             ← 새
│   ├── Pipe.java             ← 장애물
│   └── GameLoop.java         ← 게임 루프
└── resources/
    └── images/
        ├── bird.png
        ├── pipe.png
        └── background.png
```

#### ✂️ 복사
```
FlappyBird.java → DodgeGame.java
Bird.java       → Player.java
Pipe.java       → Obstacle.java
GameLoop.java   → (필요시 복사)

images/ → minigame/dodge/images/
```

#### ✏️ 수정

**DodgeGame.java:**
```java
package org.example.minigame.games.dodge;

import org.example.minigame.base.*;

public class DodgeGame implements MinigameBase {
    private Player player;
    private List<Obstacle> obstacles;
    private int score = 0;
    private boolean finished = false;
    
    @Override
    public void startPlayerMode(Stage parentStage, MinigameCallback callback) {
        gameStage = new Stage();
        gameStage.initModality(Modality.WINDOW_MODAL);
        gameStage.initOwner(parentStage);
        
        // 원본의 게임 초기화 코드
        player = new Player();
        obstacles = new ArrayList<>();
        
        // 게임 루프 시작
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
                
                // 충돌 체크
                if (checkCollision()) {
                    finished = true;
                    stop();
                    gameStage.close();
                    callback.onComplete(new MinigameResult(false, score, 60, MinigameType.DODGE));
                }
            }
        };
        gameLoop.start();
        
        // 60초 타이머
        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(60), e -> {
            finished = true;
            gameLoop.stop();
            gameStage.close();
            callback.onComplete(new MinigameResult(true, score, 60, MinigameType.DODGE));
        }));
        timer.play();
        
        gameStage.show();
    }
    
    private void update() {
        // 원본의 update 로직
        player.update();
        for (Obstacle obs : obstacles) {
            obs.update();
        }
    }
    
    private void render() {
        // 원본의 render 로직
    }
    
    private boolean checkCollision() {
        // 원본의 충돌 체크 로직
        return false;
    }
    
    // 나머지 MinigameBase 인터페이스 구현
}
```

---

## 🔧 자주 발생하는 문제 해결

### 문제 1: 패키지 충돌
```
오류: The import com.example conflicts with a type defined in the same file

해결: import 문에서 같은 패키지 내부 클래스는 import 불필요
      (같은 폴더에 있으면 자동으로 인식됨)
```

### 문제 2: 리소스 파일 못 찾음
```
오류: java.lang.IllegalArgumentException: Invalid URL

해결: 
1. resources 폴더에 파일이 있는지 확인
2. 경로가 /minigame/xxx/images/로 시작하는지 확인
3. 파일 이름 대소문자 정확히 확인 (Linux는 구분함)
```

### 문제 3: FXML 로딩 실패
```
오류: javafx.fxml.LoadException

해결:
1. FXML 파일도 resources 폴더에 복사
2. FXMLLoader 경로 수정:
   FXMLLoader loader = new FXMLLoader(
       getClass().getResource("/minigame/reaction/game.fxml")
   );
```

### 문제 4: CSS 적용 안됨
```
해결:
scene.getStylesheets().add(
    getClass().getResource("/minigame/reaction/style.css").toExternalForm()
);
```

### 문제 5: 이미지가 안 보임
```
해결:
1. Maven 빌드:
   Ctrl + F9 (IntelliJ)
   
2. target/classes/minigame/reaction/images/ 에 파일이 복사되었는지 확인

3. 안되면 Maven clean:
   mvn clean compile
```

---

## 📝 체크리스트

### 파일 복사 단계
- [ ] Java 파일 복사 완료
- [ ] 패키지명 변경 완료
- [ ] 클래스명 변경 (필요시)
- [ ] images/ 폴더 복사 완료
- [ ] sounds/ 폴더 복사 (있으면)
- [ ] CSS 파일 복사 (있으면)
- [ ] FXML 파일 복사 (있으면)

### 코드 수정 단계
- [ ] `Application` 상속 제거
- [ ] `MinigameBase` 인터페이스 구현
- [ ] `startPlayerMode()` 메서드 작성
- [ ] `startSpectatorMode()` 메서드 작성
- [ ] 나머지 인터페이스 메서드 구현
- [ ] 리소스 경로 수정 완료
- [ ] import 문 정리 완료
- [ ] main() 메서드 삭제 또는 주석

### 테스트 단계
- [ ] 컴파일 에러 없음
- [ ] 단독 테스트 성공
- [ ] 60초 타이머 작동
- [ ] 성공/실패 판정 정상
- [ ] 결과 콜백 작동
- [ ] 이미지/사운드 정상 로딩

### 통합 단계
- [ ] GameView에서 호출 테스트
- [ ] 찬스카드 버튼으로 실행 성공
- [ ] 결과가 오셀로로 정상 반환
- [ ] 여러 번 실행해도 문제없음

---

## 🎯 다음 단계

1. **GitHub에서 게임 찾기**
   - "javafx" + 원하는 게임 종류 검색
   - 라이선스 확인 (MIT/Apache)
   - README 읽기

2. **ZIP 다운로드**
   - Code → Download ZIP
   - 압축 해제

3. **파일 구조 파악**
   - 메인 클래스 찾기
   - 의존 클래스 확인
   - 리소스 파일 확인

4. **복사 및 수정**
   - 이 가이드대로 복사
   - 패키지/클래스명 변경
   - MinigameBase 구현

5. **테스트**
   - 단독 테스트
   - 오셀로 통합 테스트

---

## 💡 팁

### Tip 1: 간단한 게임부터 시작
- 복잡한 게임보다 단순한 게임을 먼저 통합
- Whack-a-Mole, Snake 같은 게임 추천

### Tip 2: 파일 개수가 적은 것 선택
- 파일이 1~5개 정도면 관리하기 쉬움
- 10개 이상이면 복잡할 수 있음

### Tip 3: 최근 업데이트된 프로젝트
- JavaFX 21과 호환성 좋음
- 최신 문법 사용

### Tip 4: 라이선스 확인 필수
- MIT, Apache: 자유롭게 수정 가능 ✅
- GPL: 전체 프로젝트도 GPL이 됨 ⚠️
- 라이선스 없음: 사용 금지 ❌

---

## 📞 도움 요청 시

다음 정보를 함께 알려주세요:
1. **오픈소스 GitHub 링크**
2. **어디까지 진행했는지** (복사/수정/테스트)
3. **오류 메시지** (있다면 전체 스택 트레이스)
4. **파일 구조** (src 폴더 내부)

그러면 정확한 해결책을 드릴 수 있습니다!

---

## ✅ 완료!

이 가이드대로 하면 어떤 JavaFX 오픈소스 게임도 오셀로 프로젝트에 통합할 수 있습니다! 🎮✨

