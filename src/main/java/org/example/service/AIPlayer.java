package org.example.service;

import org.example.model.GameModel;
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

    // Gemini API 설정 (환경 변수 사용 권장, 없으면 하드코딩된 키 사용)
    private static final String API_KEY = System.getenv("GEMINI_API_KEY") != null && !System.getenv("GEMINI_API_KEY").isEmpty() 
        ? System.getenv("GEMINI_API_KEY") 
        : "AIzaSyDbYbZO1M9MLe-P3j02mt9-7JTqjn-v1zU"; // 폴백용 키 (환경 변수가 없을 때)
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

        // 쉬움 난이도는 API 호출 안 함
        if (difficulty == GameModel.Difficulty.EASY) {
            return validMoves.get(random.nextInt(validMoves.size()));
        }

        // API 키 확인
        if (API_KEY == null || API_KEY.length() < 10 || API_KEY.startsWith("AIzaSy...")) {
            System.err.println("API Key가 설정되지 않았습니다. 랜덤 수로 대체합니다.");
            return validMoves.get(random.nextInt(validMoves.size()));
        }

        try {
            String prompt = buildPrompt(difficulty, validMoves);
            String responseText = callGeminiApi(prompt);
            return parseMoveFromResponse(responseText, validMoves);
        } catch (Exception e) {
            System.err.println("AI 호출 실패 (랜덤 착수): " + e.getMessage());
            return validMoves.get(random.nextInt(validMoves.size()));
        }
    }
    
    /**
     * 난이도 없이 호출 시 기본값(MEDIUM) 사용
     */
    public int[] getBestMove() throws Exception {
        return getBestMove(GameModel.Difficulty.MEDIUM);
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
        try {
            // 핵심 수정: 정규표현식으로 "MOVE: 숫자, 숫자" 패턴을 찾습니다.
            // AI가 앞에 무슨 말을 하든 상관없이 마지막에 나온 좌표를 가져옵니다.
            Pattern pattern = Pattern.compile("MOVE:\\s*(\\d+)\\s*,\\s*(\\d+)");
            Matcher matcher = pattern.matcher(response);

            int[] finalMove = null;

            // 텍스트에서 패턴을 찾습니다. (여러 개 있다면 마지막 것을 사용)
            while (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));

                // 유효한 수인지 검증
                for (int[] move : validMoves) {
                    if (move[0] == x && move[1] == y) {
                        finalMove = move;
                        break;
                    }
                }
            }

            if (finalMove != null) {
                return finalMove;
            }

            // 만약 MOVE: 패턴을 못 찾았다면, 기존 방식(숫자만 추출)으로 한 번 더 시도
            System.out.println("패턴 매칭 실패, 단순 파싱 시도: " + response);
            String clean = response.replaceAll("[^0-9,]", "");
            if (clean.contains(",")) {
                String[] parts = clean.split(",");
                // 뒤에서부터 2개씩 짝지어 유효한 좌표인지 확인 (설명에 포함된 숫자 무시)
                for (int i = parts.length - 2; i >= 0; i--) {
                    try {
                        int x = Integer.parseInt(parts[i].trim());
                        int y = Integer.parseInt(parts[i+1].trim());
                        for (int[] move : validMoves) {
                            if (move[0] == x && move[1] == y) return move;
                        }
                    } catch (Exception ignored) {}
                }
            }

        } catch (Exception e) {
            System.err.println("파싱 중 오류 발생: " + e.getMessage());
        }

        // 정말 안되면 랜덤
        System.err.println("AI 응답 해석 실패. 랜덤 수를 둡니다.");
        return validMoves.get(random.nextInt(validMoves.size()));
    }

    // --- HTTP 통신 로직 (제공해주신 코드 기반) ---

    private String callGeminiApi(String prompt) throws Exception {
        String urlString = API_BASE_URL + MODEL_NAME + ":generateContent?key=" + API_KEY;
        URL url = java.net.URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                prompt.replace("\n", "\\n").replace("\"", "\\\"")
        );

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);

                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0).getJSONObject("content")
                        .getJSONArray("parts").getJSONObject(0)
                        .getString("text");
            }
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) errorResponse.append(line);
                throw new RuntimeException("HTTP Error " + responseCode + ": " + errorResponse.toString());
            }
        }
    }
}

