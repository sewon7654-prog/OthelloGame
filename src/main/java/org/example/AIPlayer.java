package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * AI 대전 기능을 위한 클래스입니다. (F-09의 고급 기능 확장)
 * Gemini API를 HTTP 통신으로 호출하여 수를 계산합니다.
 */
public class AIPlayer {

    private GameModel model;

    // Gemini API 설정 (환경 변수 사용 권장)
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1/models/";

    public AIPlayer(GameModel model) {
        this.model = model;
    }

    /**
     * Gemini API를 호출하여 최적의 다음 수 (x, y)를 반환합니다.
     */
    public int[] getBestMove() throws Exception {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new RuntimeException("GEMINI_API_KEY 환경 변수가 설정되지 않았습니다. AI 기능을 사용할 수 없습니다.");
        }

        // 1. 보드 상태 및 유효한 수를 문자열로 준비
        String prompt = buildPrompt();

        // 2. API 호출
        String responseText = callGeminiApi(prompt);

        // 3. 응답에서 좌표 파싱 (예: "3, 4" 또는 "[3, 4]" 형태)
        return parseMoveFromResponse(responseText);
    }

    // --- 프롬프트 구성 및 로직 ---

    private String buildPrompt() {
        int[][] board = model.getBoard();
        List<int[]> validMoves = model.getValidMoves();
        int aiColor = model.getAIColor();
        String aiColorName = (aiColor == 1) ? "Black" : "White";

        StringBuilder sb = new StringBuilder();
        sb.append("You are playing Othello (Reversi) as ").append(aiColorName).append(" (").append(aiColor).append(").\n");
        sb.append("Current Board (0=Empty, 1=Black, 2=White):\n");

        // 보드 상태를 텍스트로 표현
        for (int y = 0; y < 8; y++) {
            sb.append(Arrays.toString(board[y])).append("\n");
        }

        // 유효한 수 목록을 프롬프트에 포함 (AI에게 힌트 제공)
        sb.append("Your valid moves are: ").append(validMoves.stream()
                        .map(pos -> "[" + pos[0] + ", " + pos[1] + "]")
                        .collect(java.util.stream.Collectors.joining(", ")))
                .append(".\n");

        sb.append("Your task is to choose the single best valid move (X, Y) to maximize your score and strategic position.\n");
        sb.append("Respond ONLY with the X and Y coordinates separated by a comma. Example: 3, 4\n");
        sb.append("Do not include any other text, explanation, or brackets.\n");

        return sb.toString();
    }

    private int[] parseMoveFromResponse(String response) {
        // 응답에서 숫자만 추출 (예: "3, 4" 또는 "The best move is 3, 4.")
        response = response.replaceAll("[^0-9,]+", "").trim();

        if (response.contains(",")) {
            try {
                String[] parts = response.split(",");
                if (parts.length == 2) {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());

                    // 0~7 범위 검사
                    if (x >= 0 && x < 8 && y >= 0 && y < 8) {
                        return new int[]{x, y};
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
        // 파싱 실패 시, 무작위 유효한 수 반환 (안전 장치)
        System.err.println("Gemini 파싱 실패. 무작위 수 반환.");
        List<int[]> validMoves = model.getValidMoves();
        if (!validMoves.isEmpty()) {
            Random random = new Random();
            return validMoves.get(random.nextInt(validMoves.size()));
        }
        return null;
    }

    // --- HTTP 통신 로직 (제공해주신 코드 기반) ---

    private String callGeminiApi(String prompt) throws Exception {
        String urlString = API_BASE_URL + MODEL_NAME + ":generateContent?key=" + API_KEY;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                prompt.replace("\n", "\\n").replace("\"", "\\\"")
        );

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JSONObject jsonResponse = new JSONObject(response.toString());

                if (jsonResponse.has("candidates")) {
                    JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    if (candidates.length() > 0) {
                        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        if (parts.length() > 0) {
                            return parts.getJSONObject(0).getString("text");
                        }
                    }
                }
                return "No text found.";

            }
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                String errorResponse = br.lines().collect(java.util.stream.Collectors.joining("\n"));
                throw new RuntimeException("API Call Failed: HTTP code " + responseCode + ". Response: " + errorResponse);
            }
        }
    }
}