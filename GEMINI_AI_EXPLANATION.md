# Gemini AI 오셀로 게임 원리 설명

## 📋 개요

이 문서는 오셀로 게임에서 Gemini AI가 어떻게 수를 두는지 설명합니다.

---

## 🔄 전체 흐름도

```
게임 진행 중 (AI 턴)
    ↓
GameView.handleAITurn()
    ↓
AIPlayer.getBestMove(difficulty)
    ↓
1. 프롬프트 생성 (buildPrompt)
    ↓
2. Gemini API 호출 (callGeminiApi)
    ↓
3. 응답 파싱 (parseMoveFromResponse)
    ↓
4. 유효한 수 검증
    ↓
게임 보드에 수 두기
```

---

## 📝 1단계: 프롬프트 생성 (`buildPrompt`)

### 위치
```78:108:src/main/java/org/example/service/AIPlayer.java
private String buildPrompt(GameModel.Difficulty difficulty, List<int[]> validMoves) {
    int[][] board = model.getBoard();
    int aiColor = model.getAIColor();
    String aiColorName = (aiColor == 1) ? "Black" : "White";

    StringBuilder sb = new StringBuilder();
    sb.append("You are playing Othello as ").append(aiColorName).append(".\n");
    sb.append("Current Board (0=Empty, 1=Black, 2=White):\n");

    for (int y = 0; y < 8; y++) {
        sb.append(Arrays.toString(board[y])).append("\n");
    }

    sb.append("Valid moves: ").append(validMoves.stream()
                    .map(pos -> "[" + pos[0] + ", " + pos[1] + "]")
                    .collect(Collectors.joining(", ")))
            .append(".\n");

    if (difficulty == GameModel.Difficulty.MEDIUM) {
        sb.append("Strategy: Pick a move that flips many pieces.\n");
    } else {
        sb.append("Strategy: Play like an expert. Prioritize corners and stable discs.\n");
    }

    // 핵심 수정: AI에게 답변 형식을 강제합니다.
    sb.append("\nIMPORTANT: You can think step-by-step, but at the very end of your response, you MUST output the final move in this exact format:\n");
    sb.append("MOVE: X, Y\n");
    sb.append("Example:\nSome reasoning...\nMOVE: 3, 4");

    return sb.toString();
}
```

### 프롬프트 구성 요소

#### 1. 게임 상황 설명
```
"You are playing Othello as Black."
```
- AI가 어떤 색상인지 알려줌

#### 2. 현재 보드 상태
```
Current Board (0=Empty, 1=Black, 2=White):
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 2, 1, 0, 0, 0]
[0, 0, 0, 1, 2, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
```
- 8x8 배열로 보드 상태 전달
- 0 = 빈 칸, 1 = 흑돌, 2 = 백돌

#### 3. 유효한 수 목록
```
Valid moves: [2, 3], [3, 2], [4, 5], [5, 4]
```
- AI가 둘 수 있는 위치만 알려줌 (게임 규칙 검증 완료된 수)

#### 4. 난이도별 전략
- **MEDIUM**: "많은 돌을 뒤집는 수를 선택하세요"
- **HARD**: "전문가처럼 플레이. 모서리와 안정적인 돌을 우선시하세요"

#### 5. 응답 형식 강제
```
IMPORTANT: ... you MUST output the final move in this exact format:
MOVE: X, Y
```
- AI가 반드시 "MOVE: 3, 4" 형식으로 답변하도록 지시
- 파싱을 쉽게 하기 위함

### 예시 프롬프트 (전체)

```
You are playing Othello as Black.
Current Board (0=Empty, 1=Black, 2=White):
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 2, 1, 0, 0, 0]
[0, 0, 0, 1, 2, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
Valid moves: [2, 3], [3, 2], [4, 5], [5, 4].
Strategy: Play like an expert. Prioritize corners and stable discs.

IMPORTANT: You can think step-by-step, but at the very end of your response, you MUST output the final move in this exact format:
MOVE: X, Y
Example:
Some reasoning...
MOVE: 3, 4
```

---

## 🌐 2단계: Gemini API 호출 (`callGeminiApi`)

### 위치
```165:204:src/main/java/org/example/service/AIPlayer.java
private String callGeminiApi(String prompt) throws Exception {
    // ... API 호출 코드
}
```

### API 호출 과정

#### 1. URL 구성
```
https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=YOUR_API_KEY
```

#### 2. HTTP 요청
```json
POST /v1beta/models/gemini-2.0-flash:generateContent?key=...
Content-Type: application/json

{
  "contents": [{
    "parts": [{
      "text": "You are playing Othello as Black.\nCurrent Board..."
    }]
  }]
}
```

#### 3. API 응답 (성공 시)
```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "Looking at the board, I see that corner positions are valuable...\nMOVE: 3, 2"
      }]
    },
    "finishReason": "STOP"
  }]
}
```

#### 4. 텍스트 추출
- `response.candidates[0].content.parts[0].text` 추출
- 예: `"Looking at the board...\nMOVE: 3, 2"`

---

## 🔍 3단계: 응답 파싱 (`parseMoveFromResponse`)

### 위치
```110:161:src/main/java/org/example/service/AIPlayer.java
private int[] parseMoveFromResponse(String response, List<int[]> validMoves) {
    // ... 파싱 코드
}
```

### 파싱 방법 (3단계)

#### 방법 1: 정규표현식으로 "MOVE: X, Y" 찾기
```java
Pattern pattern = Pattern.compile("MOVE:\\s*(\\d+)\\s*,\\s*(\\d+)");
Matcher matcher = pattern.matcher(response);

while (matcher.find()) {
    int x = Integer.parseInt(matcher.group(1));  // 첫 번째 숫자
    int y = Integer.parseInt(matcher.group(2));  // 두 번째 숫자
    // 유효한 수인지 검증
}
```

**예시:**
- 응답: `"I think the best move is...\nMOVE: 3, 2"`
- 추출: `x = 3, y = 2`

#### 방법 2: 괄호 형식 "[X, Y]" 찾기
```java
Pattern bracketPattern = Pattern.compile("\\[(\\d+)\\s*,\\s*(\\d+)\\]");
```

**예시:**
- 응답: `"The move [3, 2] looks good"`
- 추출: `x = 3, y = 2`

#### 방법 3: 숫자만 추출하여 뒤에서부터 찾기
```java
String clean = response.replaceAll("[^0-9,\\s]", "");
// "3, 2" 같은 패턴 찾기
```

**예시:**
- 응답: `"Move at position 3, 2"`
- 추출: `x = 3, y = 2`

### 유효성 검증

파싱한 좌표가 실제로 유효한 수인지 확인:

```java
for (int[] move : validMoves) {
    if (move[0] == x && move[1] == y) {
        return move;  // 유효한 수!
    }
}
```

**왜 필요한가?**
- AI가 잘못된 좌표를 반환할 수 있음
- 게임 규칙에 맞는 수만 사용해야 함

---

## 🎯 전체 예시 시나리오

### 시나리오: AI가 수를 두는 과정

#### 1. 게임 상황
```
보드 상태:
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 2, 1, 0, 0, 0]
[0, 0, 0, 1, 2, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]
[0, 0, 0, 0, 0, 0, 0, 0]

AI 색상: Black (1)
유효한 수: [2, 3], [3, 2], [4, 5], [5, 4]
난이도: HARD
```

#### 2. 프롬프트 생성
```
You are playing Othello as Black.
Current Board (0=Empty, 1=Black, 2=White):
[0, 0, 0, 0, 0, 0, 0, 0]
...
Valid moves: [2, 3], [3, 2], [4, 5], [5, 4].
Strategy: Play like an expert. Prioritize corners and stable discs.
IMPORTANT: ... MOVE: X, Y
```

#### 3. Gemini API 호출
```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=...
```

#### 4. Gemini 응답
```
"Looking at the board, I can see that position [3, 2] would flip the white piece at [3, 3] and give me control of the left side. This is a strategic move that sets up future corner opportunities.

MOVE: 3, 2"
```

#### 5. 파싱
```
정규표현식 매칭: "MOVE: 3, 2"
추출: x = 3, y = 2
검증: [3, 2]가 validMoves에 있음 ✓
```

#### 6. 게임에 적용
```
gameModel.placePieceAndFlip(3, 2);
→ 보드에 수가 둬짐!
```

---

## ⚠️ 에러 처리

### 1. API 호출 실패
- **원인**: 네트워크 오류, API 키 오류, 할당량 초과
- **처리**: 상세한 에러 로그 출력 후 랜덤 수로 대체

### 2. 응답 파싱 실패
- **원인**: AI가 형식을 지키지 않음
- **처리**: 여러 파싱 방법 시도 후 랜덤 수로 대체

### 3. 유효하지 않은 수
- **원인**: AI가 잘못된 좌표 반환
- **처리**: 유효한 수 목록과 비교하여 검증

---

## 🔧 개선 사항 (최신 버전)

### 1. 상세한 로깅
- 각 단계마다 `[AI]` 태그로 로그 출력
- 문제 발생 시 원인 파악 용이

### 2. 타임아웃 설정
- 연결 타임아웃: 10초
- 읽기 타임아웃: 30초

### 3. 다중 파싱 방법
- 정규표현식 → 괄호 형식 → 숫자 추출
- 하나 실패해도 다른 방법 시도

### 4. 안전한 JSON 파싱
- `candidates` 배열 존재 확인
- `finishReason` 확인 (차단 여부 체크)

---

## 📊 성능 및 제한사항

### API 호출 시간
- 평균: 1-3초
- 최대: 30초 (타임아웃)

### 비용
- Gemini API 무료 할당량 사용
- 요청당 토큰 수에 따라 비용 발생 가능

### 제한사항
- 네트워크 연결 필요
- API 키 필요
- API 할당량 제한 가능

---

## 💡 요약

1. **프롬프트 생성**: 게임 상태를 텍스트로 변환하여 AI에게 전달
2. **API 호출**: Gemini API에 HTTP 요청 전송
3. **응답 파싱**: AI 응답에서 좌표 추출
4. **검증**: 추출한 좌표가 유효한 수인지 확인
5. **적용**: 게임 보드에 수 두기

이 과정을 통해 AI가 오셀로 게임에서 지능적으로 수를 둘 수 있습니다!

