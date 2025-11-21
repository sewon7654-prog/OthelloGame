package org.example.service;

import org.example.model.GameModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI 대전 기능을 위한 클래스입니다. (F-09의 고급 기능 확장)
 * Gemini API를 HTTP 통신으로 호출하여 수를 계산합니다.
 */
public class AIPlayer {

    private GameModel model;
    private Random random;

    // Gemini API 설정 (환경 변수 또는 config.properties에서 읽음)
    private static final String API_KEY = org.example.service.ConfigService.getGeminiApiKey();
    private static final String MODEL_NAME = "gemini-2.0-flash";
    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    public AIPlayer(GameModel model) {
        this.model = model;
        this.random = new Random();
    }

    /**
     * Gemini API를 호출하여 최적의 다음 수 (x, y)를 반환합니다.
     * @param difficulty AI 난이도 (EASY, MEDIUM, HARD)
     */
    public int[] getBestMove(GameModel.Difficulty difficulty) throws Exception {
        List<int[]> validMoves = model.getValidMoves();
        if (validMoves.isEmpty()) return null;

        // 쉬움 난이도: 랜덤 선택 (즉시)
        if (difficulty == GameModel.Difficulty.EASY) {
            System.out.println("[AI] 쉬움 난이도: 랜덤 수 선택");
            return validMoves.get(random.nextInt(validMoves.size()));
        }
        
        // 중급 난이도: 가장 많이 뒤집는 수 선택 (로컬 계산, 빠름)
        if (difficulty == GameModel.Difficulty.MEDIUM) {
            System.out.println("[AI] 중급 난이도: 로컬 알고리즘으로 계산 중...");
            return getBestMoveByFlipCount(validMoves);
        }

        // API 키 확인 및 검증
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("========================================");
            System.err.println("[AI] ⚠️  API Key가 설정되지 않았습니다!");
            System.err.println("[AI] 중급/고급 난이도는 Gemini API가 필요합니다.");
            System.err.println("[AI]");
            System.err.println("[AI] 설정 방법:");
            System.err.println("[AI]   1. Windows PowerShell:");
            System.err.println("[AI]      $env:GEMINI_API_KEY=\"YOUR_API_KEY\"");
            System.err.println("[AI]");
            System.err.println("[AI]   2. Windows CMD:");
            System.err.println("[AI]      set GEMINI_API_KEY=YOUR_API_KEY");
            System.err.println("[AI]");
            System.err.println("[AI]   3. IDE에서 실행 시:");
            System.err.println("[AI]      Run → Edit Configurations → Environment variables");
            System.err.println("[AI]      GEMINI_API_KEY=YOUR_API_KEY 추가");
            System.err.println("[AI]");
            System.err.println("[AI] API 키 발급: https://makersuite.google.com/app/apikey");
            System.err.println("========================================");
            System.err.println("[AI] 랜덤 수로 대체합니다.");
            return validMoves.get(random.nextInt(validMoves.size()));
        }
        
        if (API_KEY.length() < 20 || !API_KEY.startsWith("AIzaSy")) {
            System.err.println("[AI] ⚠️  API Key 형식이 올바르지 않습니다.");
            System.err.println("[AI]   - 길이: " + API_KEY.length() + "자 (최소 20자 필요)");
            System.err.println("[AI]   - 시작: " + (API_KEY.length() > 5 ? API_KEY.substring(0, 5) : "N/A") + " (AIzaSy로 시작해야 함)");
            System.err.println("[AI] 랜덤 수로 대체합니다.");
            return validMoves.get(random.nextInt(validMoves.size()));
        }
        
        System.out.println("[AI] ✓ API Key 확인 완료 (길이: " + API_KEY.length() + "자)");
        System.out.println("[AI] 고급 난이도: Gemini API 호출 중... (2-5초 소요)");

        try {
            String prompt = buildPrompt(difficulty, validMoves);
            System.out.println("[AI] 프롬프트 생성 완료. API 호출 중...");
            String responseText = callGeminiApi(prompt);
            System.out.println("[AI] API 응답 받음: " + (responseText.length() > 100 ? responseText.substring(0, 100) + "..." : responseText));
            int[] move = parseMoveFromResponse(responseText, validMoves);
            if (move != null) {
                System.out.println("[AI] 수 결정: (" + move[0] + ", " + move[1] + ")");
                return move;
            } else {
                System.err.println("[AI] 응답 파싱 실패 - 유효한 수를 찾지 못함");
                return validMoves.get(random.nextInt(validMoves.size()));
            }
        } catch (Exception e) {
            System.err.println("[AI] 호출 실패 상세 정보:");
            System.err.println("  - 예외 타입: " + e.getClass().getSimpleName());
            System.err.println("  - 메시지: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("  - 원인: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            System.err.println("[AI] 랜덤 수로 대체합니다.");
            return validMoves.get(random.nextInt(validMoves.size()));
        }
    }
    
    /**
     * 난이도 없이 호출 시 기본값(MEDIUM) 사용
     */
    public int[] getBestMove() throws Exception {
        return getBestMove(GameModel.Difficulty.MEDIUM);
    }
    
    /**
     * 중급 난이도: 가장 많이 뒤집는 수를 선택 (로컬 알고리즘)
     */
    private int[] getBestMoveByFlipCount(List<int[]> validMoves) {
        if (validMoves.isEmpty()) return null;
        
        int[] bestMove = validMoves.get(0); // 기본값: 첫 번째 수
        int maxFlips = -1;
        
        for (int[] move : validMoves) {
            int x = move[0];
            int y = move[1];
            
            // 이 수를 두면 몇 개를 뒤집을 수 있는지 계산
            List<int[]> flips = model.getPiecesToFlip(x, y, model.getCurrentTurn());
            int flipCount = flips.size();
            
            if (flipCount > maxFlips) {
                maxFlips = flipCount;
                bestMove = move;
            }
        }
        
        System.out.println("[AI] 중급: 최대 " + maxFlips + "개 뒤집기 가능한 수 선택: (" + bestMove[0] + ", " + bestMove[1] + ")");
        return bestMove;
    }

    // --- 프롬프트 구성 및 로직 ---

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

    private int[] parseMoveFromResponse(String response, List<int[]> validMoves) {
        System.out.println("[AI] 응답 파싱 시작...");
        System.out.println("[AI] 전체 응답: " + response);
        
        try {
            // 방법 1: 정규표현식으로 "MOVE: 숫자, 숫자" 패턴 찾기
            Pattern pattern = Pattern.compile("MOVE:\\s*(\\d+)\\s*,\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(response);

            int[] finalMove = null;
            int matchCount = 0;

            // 텍스트에서 패턴을 찾습니다. (여러 개 있다면 마지막 것을 사용)
            while (matcher.find()) {
                matchCount++;
                try {
                    int x = Integer.parseInt(matcher.group(1));
                    int y = Integer.parseInt(matcher.group(2));
                    
                    System.out.println("[AI] 패턴 매칭 발견 #" + matchCount + ": (" + x + ", " + y + ")");

                    // 유효한 수인지 검증
                    for (int[] move : validMoves) {
                        if (move[0] == x && move[1] == y) {
                            finalMove = move;
                            System.out.println("[AI] 유효한 수 확인됨: (" + x + ", " + y + ")");
                            break;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("[AI] 숫자 파싱 실패: " + matcher.group(1) + ", " + matcher.group(2));
                }
            }

            if (finalMove != null) {
                System.out.println("[AI] 최종 선택된 수: (" + finalMove[0] + ", " + finalMove[1] + ")");
                return finalMove;
            }

            // 방법 2: MOVE: 패턴을 못 찾았다면, 괄호 안의 좌표 찾기 [x, y]
            System.out.println("[AI] MOVE: 패턴 없음, [x, y] 형식 검색 중...");
            Pattern bracketPattern = Pattern.compile("\\[(\\d+)\\s*,\\s*(\\d+)\\]");
            Matcher bracketMatcher = bracketPattern.matcher(response);
            
            while (bracketMatcher.find()) {
                try {
                    int x = Integer.parseInt(bracketMatcher.group(1));
                    int y = Integer.parseInt(bracketMatcher.group(2));
                    
                    System.out.println("[AI] 괄호 형식 발견: [" + x + ", " + y + "]");
                    
                    for (int[] move : validMoves) {
                        if (move[0] == x && move[1] == y) {
                            System.out.println("[AI] 유효한 수 확인됨: [" + x + ", " + y + "]");
                            return move;
                        }
                    }
                } catch (NumberFormatException e) {
                    // 무시하고 계속
                }
            }

            // 방법 3: 숫자만 추출하여 뒤에서부터 유효한 좌표 찾기
            System.out.println("[AI] 패턴 매칭 실패, 숫자 추출 방식 시도...");
            String clean = response.replaceAll("[^0-9,\\s]", "");
            System.out.println("[AI] 정제된 텍스트: " + clean);
            
            if (clean.contains(",")) {
                String[] parts = clean.split("[,,\\s]+");
                System.out.println("[AI] 추출된 숫자 조각: " + Arrays.toString(parts));
                
                // 뒤에서부터 2개씩 짝지어 유효한 좌표인지 확인
                for (int i = parts.length - 2; i >= 0; i--) {
                    try {
                        int x = Integer.parseInt(parts[i].trim());
                        int y = Integer.parseInt(parts[i+1].trim());
                        
                        // 0~7 범위 체크
                        if (x >= 0 && x < 8 && y >= 0 && y < 8) {
                            System.out.println("[AI] 숫자 조합 시도: (" + x + ", " + y + ")");
                            for (int[] move : validMoves) {
                                if (move[0] == x && move[1] == y) {
                                    System.out.println("[AI] 유효한 수 확인됨: (" + x + ", " + y + ")");
                                    return move;
                                }
                            }
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        // 무시하고 계속
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("[AI] 파싱 중 예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }

        // 모든 방법 실패
        System.err.println("[AI] 모든 파싱 방법 실패. 유효한 수 목록: " + 
            validMoves.stream()
                .map(m -> "(" + m[0] + "," + m[1] + ")")
                .collect(Collectors.joining(", ")));
        System.err.println("[AI] 랜덤 수로 대체합니다.");
        return validMoves.get(random.nextInt(validMoves.size()));
    }

    // --- HTTP 통신 로직 (제공해주신 코드 기반) ---

    private String callGeminiApi(String prompt) throws Exception {
        String urlString = API_BASE_URL + MODEL_NAME + ":generateContent?key=" + API_KEY;
        System.out.println("[AI] API URL: " + API_BASE_URL + MODEL_NAME + ":generateContent?key=" + (API_KEY.length() > 10 ? API_KEY.substring(0, 10) + "..." : "INVALID"));
        
        URL url = java.net.URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(5000); // 5초 연결 타임아웃 (빠르게)
        conn.setReadTimeout(15000); // 15초 읽기 타임아웃 (빠르게)
        conn.setDoOutput(true);

        // JSON 이스케이프 처리 개선
        String escapedPrompt = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
        
        String jsonInputString = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                escapedPrompt
        );

        System.out.println("[AI] 요청 전송 중... (프롬프트 길이: " + prompt.length() + "자)");
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        System.out.println("[AI] HTTP 응답 코드: " + responseCode);
        
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                String responseStr = response.toString();
                System.out.println("[AI] 응답 본문 길이: " + responseStr.length() + "자");
                
                JSONObject jsonResponse = new JSONObject(responseStr);
                
                // 응답 구조 확인 및 안전하게 파싱
                if (!jsonResponse.has("candidates")) {
                    throw new RuntimeException("API 응답에 'candidates' 필드가 없습니다: " + responseStr);
                }
                
                if (jsonResponse.getJSONArray("candidates").length() == 0) {
                    throw new RuntimeException("API 응답에 후보가 없습니다: " + responseStr);
                }
                
                JSONObject candidate = jsonResponse.getJSONArray("candidates").getJSONObject(0);
                
                // safetyRatings 확인 (차단되었는지 확인)
                if (candidate.has("finishReason") && !candidate.getString("finishReason").equals("STOP")) {
                    String reason = candidate.getString("finishReason");
                    throw new RuntimeException("API 응답이 차단되었습니다. 이유: " + reason);
                }
                
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                
                if (parts.length() == 0) {
                    throw new RuntimeException("API 응답에 'parts'가 없습니다: " + responseStr);
                }
                
                String text = parts.getJSONObject(0).getString("text");
                System.out.println("[AI] 추출된 텍스트: " + (text.length() > 200 ? text.substring(0, 200) + "..." : text));
                return text;
            }
        } else {
            // 에러 응답 상세 출력
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
            } catch (Exception e) {
                errorResponse.append("에러 스트림 읽기 실패: " + e.getMessage());
            }
            
            String errorMsg = errorResponse.toString();
            System.err.println("[AI] API 에러 응답:");
            System.err.println("  - HTTP 코드: " + responseCode);
            System.err.println("  - 응답 내용: " + errorMsg);
            
            // JSON 에러 응답 파싱 시도
            try {
                JSONObject errorJson = new JSONObject(errorMsg);
                if (errorJson.has("error")) {
                    JSONObject error = errorJson.getJSONObject("error");
                    String message = error.has("message") ? error.getString("message") : "알 수 없는 오류";
                    String status = error.has("status") ? error.getString("status") : "UNKNOWN";
                    throw new RuntimeException("Gemini API 오류 [" + status + "]: " + message);
                }
            } catch (Exception e) {
                // JSON 파싱 실패 시 원본 메시지 사용
            }
            
            throw new RuntimeException("HTTP Error " + responseCode + ": " + errorMsg);
        }
    }
}

