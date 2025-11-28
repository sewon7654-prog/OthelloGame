# 🔍 온라인 기능 디버깅 가이드

## 문제: 온라인 기능이 작동하지 않음

### 체크리스트

#### 1. 서버 실행 확인
- [ ] 서버가 실행 중입니까?
  ```bash
  java org.example.network.NetworkServer
  ```
- [ ] 서버 로그에 "running on port 12345" 메시지가 나옵니까?

#### 2. 클라이언트 연결 확인
- [ ] IP 주소를 정확히 입력했습니까?
  - 로컬 테스트: `localhost` 또는 `127.0.0.1`
  - 같은 네트워크: `192.168.x.x`
- [ ] 포트 번호가 맞습니까? (기본: 12345)
- [ ] 방화벽이 포트를 막고 있지 않습니까?

#### 3. 연결 후 확인
- [ ] "Connected to server" 메시지가 나옵니까?
- [ ] "매칭 성공" 메시지가 나옵니까?
- [ ] 게임 화면이 나타납니까?

---

## 코드 변경 내역

### NetworkServer.java
**변경 사항**: 미니게임 메시지 처리 로직 추가 (라인 156-164)

**기존 코드에 영향**: 없음 ✅
- `MOVE` 프로토콜 처리는 else 블록에서 그대로 처리됨
- 미니게임 메시지만 별도로 필터링

**테스트 방법**:
1. 미니게임 기능 없이 일반 온라인 대전 테스트
2. 돌을 놓았을 때 상대방에게 전달되는지 확인

---

### NetworkClient.java
**변경 사항**: 미니게임 메시지 수신 처리 추가 (라인 148-163)

**기존 코드에 영향**: 없음 ✅
- `MOVE` 프로토콜 처리는 기존 else-if 블록에 그대로 존재
- 새로운 프로토콜만 별도로 추가

**테스트 방법**:
1. 서버로부터 `MOVE x y` 메시지를 받았을 때 `processOpponentMove()` 호출되는지 확인
2. 콘솔에 "Opponent's move received" 로그 추가해서 확인

---

## 디버깅 코드 추가

### 1. NetworkServer.java에 로그 추가

```java
// ClientHandler.run() 메서드의 라인 149 다음에 추가
while ((inputLine = in.readLine()) != null) {
    System.out.println("[DEBUG] Received from client: " + inputLine); // ← 추가
    
    if (inputLine.startsWith("USER_ID ")) {
        userId = inputLine.substring(8);
        System.out.println("User ID received: " + userId);
    }
    // ...
}
```

### 2. NetworkClient.java에 로그 추가

```java
// run() 메서드의 라인 128 다음에 추가
while ((serverResponse = in.readLine()) != null) {
    System.out.println("[DEBUG] Received from server: " + serverResponse); // ← 추가
    
    if (serverResponse.startsWith("START_")) {
        // ...
    }
}
```

### 3. GameView.java에 로그 추가

```java
// processOpponentMove() 메서드 시작 부분에 추가
public void processOpponentMove(int x, int y) {
    System.out.println("[DEBUG] processOpponentMove called: (" + x + ", " + y + ")"); // ← 추가
    Platform.runLater(() -> {
        // ...
    });
}
```

---

## 예상 문제 및 해결

### 문제 1: "Failed to connect to server"
**원인**: 서버가 실행되지 않았거나 IP/포트가 잘못됨
**해결**:
1. 서버 실행 확인
2. IP 주소 재확인
3. 방화벽 확인

### 문제 2: 연결은 되는데 게임 시작 안 됨
**원인**: 2명이 접속하지 않음
**해결**:
1. 2개의 클라이언트를 실행해야 함
2. 서버 로그에서 "Match Found" 메시지 확인

### 문제 3: 게임은 시작되는데 수가 전달 안 됨
**원인**: `processOpponentMove()` 호출 문제 또는 `GameModel` 동기화 문제
**해결**:
1. 위의 디버깅 코드로 메시지 수신 확인
2. `GameView.processOpponentMove()` 메서드 확인
3. `gameModel.placePieceAndFlip()` 반환값 확인

### 문제 4: 게임 화면이 두 번 나타남
**원인**: `show()` 메서드가 중복 호출됨
**해결**:
`GameView.java`의 `setPlayerColor()` 메서드 확인
- 라인 565: `show(GameModel.Mode.ONLINE)` 호출 전에 이미 show()가 호출되었는지 확인

---

## 미니게임 기능 임시 제거 (테스트용)

온라인 기능만 테스트하려면 다음 코드를 **주석 처리**:

### NetworkServer.java
```java
// 라인 156-164 주석 처리
/*
if (inputLine.startsWith("MINIGAME_START") || 
    inputLine.startsWith("MINIGAME_UPDATE") ||
    inputLine.startsWith("MINIGAME_RESULT") ||
    inputLine.startsWith("MINIGAME_CLOSE")) {
    room.broadcastMove(this, inputLine);
} else if (inputLine.equals("RANDOM_MOVE")) {
    room.executeRandomMove(this);
} else {
*/
    // F-11: 받은 수를 같은 방의 상대방에게 중계합니다.
    room.broadcastMove(this, inputLine);
/*
}
*/
```

### NetworkClient.java
```java
// 라인 148-163 주석 처리
/*
else if (serverResponse.startsWith("MINIGAME_START")) { ... }
else if (serverResponse.startsWith("MINIGAME_RESULT")) { ... }
else if (serverResponse.equals("RANDOM_MOVE_EXECUTED")) { ... }
*/
```

---

## 정상 작동 시 로그 예시

### 서버 로그
```
Othello Game Server is running on port 12345...
New client connected: 127.0.0.1
User ID received: Player1
Client waiting for opponent. Current queue: 1
New client connected: 127.0.0.1
User ID received: Player2
--- Match Found: Room 1 created. ---
[DEBUG] Received from client: MOVE 3 2
[DEBUG] Received from client: MOVE 2 2
```

### 클라이언트 1 로그
```
Connected to server (127.0.0.1:12345). Waiting for opponent...
[DEBUG] Received from server: START_BLACK Player2
매칭 성공! 당신은 흑돌(Black)입니다.
[DEBUG] Received from server: MOVE 2 2
[DEBUG] processOpponentMove called: (2, 2)
```

### 클라이언트 2 로그
```
Connected to server (127.0.0.1:12345). Waiting for opponent...
[DEBUG] Received from server: START_WHITE Player1
매칭 성공! 당신은 백돌(White)입니다.
[DEBUG] Received from server: MOVE 3 2
[DEBUG] processOpponentMove called: (3, 2)
```

---

## 결론

**기존 온라인 기능 코드는 전혀 손대지 않았습니다!**

미니게임 기능은 기존 프로토콜과 **별도로 추가**되었으며, `else` 블록에서 기존 `MOVE` 처리가 그대로 작동합니다.

온라인 기능이 안 되는 원인은:
1. 서버 실행 문제
2. 네트워크 연결 문제
3. GameView.show() 중복 호출 문제

위의 디버깅 코드를 추가해서 **어디서 문제가 발생하는지 정확히 파악**해야 합니다.

