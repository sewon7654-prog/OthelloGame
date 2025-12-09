package org.example.model;

import org.example.service.DatabaseService;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameModel {

    public enum Mode { LOCAL, ONLINE, AI } // 게임 모드 정의
    // 난이도 Enum 추가
    public enum Difficulty { EASY, MEDIUM, HARD }

    private int[][] board;
    private int currentTurn;
    private final int SIZE = 8;
    private boolean isGameOver;
    private Mode gameMode;
    private int aiColor; // AI의 돌 색상
    private Difficulty aiDifficulty; // 난이도 저장 변수

    // 8방향 벡터
    private final int[] DY = {-1, 1, 0, 0, -1, -1, 1, 1};
    private final int[] DX = {0, 0, -1, 1, -1, 1, -1, 1};

    public GameModel() {
        gameMode = Mode.LOCAL;
        aiDifficulty = Difficulty.MEDIUM; // 기본값
        initializeBoard();
    }


    // 보드 초기화
    public void initializeBoard() {
        board = new int[SIZE][SIZE];
        board[3][3] = 2; board[4][4] = 2; //백돌 초기 배치
        board[3][4] = 1; board[4][3] = 1; //흑돌 초기 배치
        currentTurn = 1;
        isGameOver = false;

        if (gameMode == Mode.AI) {
            Random random = new Random();
            aiColor = random.nextBoolean() ? 1 : 2; // AI 색상 랜덤 지정
        } else {
            aiColor = 0;
        }
    }

    public boolean placePieceAndFlip(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || board[y][x] != 0) {
            return false;
        }

        List<int[]> piecesToFlip = getPiecesToFlip(x, y, currentTurn);

        if (piecesToFlip.isEmpty()) {
            return false;
        }

        board[y][x] = currentTurn;
        for (int[] pos : piecesToFlip) {
            board[pos[0]][pos[1]] = currentTurn;
        }

        return true;
    }




    // 돌 놓기 및 뒤집기기
    public List<int[]> getPiecesToFlip(int x, int y, int player) {
        List<int[]> piecesToFlip = new ArrayList<>();

        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || board[y][x] != 0) {
            return piecesToFlip;
        }

        int opponent = (player == 1) ? 2 : 1;

        for (int i = 0; i < 8; i++) {
            List<int[]> potentialFlip = new ArrayList<>();
            int ny = y + DY[i];
            int nx = x + DX[i];
            // 상대발 돌을 만나는 동안 계속 이동동
            while (ny >= 0 && ny < SIZE && nx >= 0 && nx < SIZE && board[ny][nx] == opponent) {
                potentialFlip.add(new int[]{ny, nx});
                ny += DY[i];
                nx += DX[i];
            }
            // 끝에 자신의 돌이 있으면 뒤집을 수 있음음
            if (ny >= 0 && ny < SIZE && nx >= 0 && nx < SIZE && board[ny][nx] == player) {
                piecesToFlip.addAll(potentialFlip);
            }
        }

        return piecesToFlip;
    }




    // 유효한 수 목록 반환
    public List<int[]> getValidMoves() {
        List<int[]> validMoves = new ArrayList<>();

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (board[y][x] == 0) {
                    if (!getPiecesToFlip(x, y, currentTurn).isEmpty()) {
                        validMoves.add(new int[]{x, y});
                    }
                }
            }
        }
        return validMoves;
    }

    // ?�정???�어�?용 가??자리 ???�기 (미니게임 ?�득 시 강제 ?�로 ???�음)
    public List<int[]> getValidMovesFor(int player) {
        int originalTurn = currentTurn;
        currentTurn = player;
        List<int[]> moves = getValidMoves();
        currentTurn = originalTurn;
        return moves;
    }

    public void switchTurn() {
        currentTurn = (currentTurn == 1) ? 2 : 1;
    }

    public void setCurrentTurn(int turn) {
        this.currentTurn = turn;
    }

    public int getScore(int player) {
        int count = 0;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (board[y][x] == player) {
                    count++;
                }
            }
        }
        return count;
    }

    public String getCurrentPlayerName() {
        if (gameMode == Mode.AI && currentTurn == aiColor) {
            return "AI (" + (currentTurn == 1 ? "Black" : "White") + ")";
        }
        return (currentTurn == 1) ? "Black" : "White";
    }

    // --- Getter & Setter ---
    public int[][] getBoard() { return board; }
    public int getCurrentTurn() { return currentTurn; }
    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }

    public void setGameMode(Mode mode) { this.gameMode = mode; }
    public Mode getGameMode() { return gameMode; }
    public boolean isAIMode() { return gameMode == Mode.AI; }
    public boolean isOnlineMode() { return gameMode == Mode.ONLINE; }
    public int getAIColor() { return aiColor; }
    
    // 난이도 관련 메소드 추가
    public void setAIDifficulty(Difficulty difficulty) { this.aiDifficulty = difficulty; }
    public Difficulty getAIDifficulty() { return this.aiDifficulty; }
    
    // --- 게임 결과 저장 관련 메서드 (아키텍처 개선: DB 접근을 GameModel에서 담당) ---
    
    /**
     * 게임 결과를 데이터베이스에 저장
     * @param player1 플레이어1 ID
     * @param player2 플레이어2 ID
     * @param winnerId 승자 ID (또는 "DRAW")
     * @param blackScore 흑돌 점수
     * @param whiteScore 백돌 점수
     * @param moveSequenceJson 수순 JSON (현재는 빈 배열)
     */
    public void saveGameRecord(String player1, String player2, String winnerId, 
                               int blackScore, int whiteScore, String moveSequenceJson) {
        DatabaseService dbService = DatabaseService.getInstance();
        if (dbService.isConnected()) {
            dbService.saveGameRecord(player1, player2, winnerId, blackScore, whiteScore, moveSequenceJson);
        }
    }
    
    /**
     * 플레이어 전적 업데이트
     * @param userId 사용자 ID
     * @param result 결과 ("WIN", "LOSS", "DRAW")
     */
    public void updatePlayerStats(String userId, String result) {
        DatabaseService dbService = DatabaseService.getInstance();
        if (!dbService.isConnected()) return;
        
        switch (result) {
            case "WIN" -> dbService.updateWin(userId);
            case "LOSS" -> dbService.updateLoss(userId);
            case "DRAW" -> dbService.updateDraw(userId);
        }
    }
    
    /**
     * 게임 결과를 저장 (게임 모드에 따라 자동 처리)
     * @param currentUserId 현재 사용자 ID
     * @param opponentUserId 상대방 ID (온라인 모드일 경우)
     * @param myColor 내 색상 (1: Black, 2: White)
     */
    public void saveGameResult(String currentUserId, String opponentUserId, int myColor) {
        DatabaseService dbService = DatabaseService.getInstance();
        if (currentUserId == null || !dbService.isConnected()) {
            return;
        }
        
        int blackScore = getScore(1);
        int whiteScore = getScore(2);
        String player1 = currentUserId;
        String player2;
        String winnerId;
        
        // 로컬 2인 대전 모드: 전적은 업데이트하지 않고 게임 기록만 저장
        if (gameMode == Mode.LOCAL) {
            if (blackScore > whiteScore) {
                winnerId = currentUserId + " (Black)";
            } else if (whiteScore > blackScore) {
                winnerId = currentUserId + " (White)";
            } else {
                winnerId = "DRAW";
            }
            player1 = currentUserId + " (Black)";
            player2 = currentUserId + " (White)";
            saveGameRecord(player1, player2, winnerId, blackScore, whiteScore, "[]");
            System.out.println("Local game record saved (no stats updated)");
            return;
        }
        
        // AI 모드 또는 온라인 모드
        if (gameMode == Mode.AI) {
            player2 = "AI";
            if (blackScore > whiteScore) {
                winnerId = currentUserId;
                updatePlayerStats(currentUserId, "WIN");
            } else if (whiteScore > blackScore) {
                winnerId = "AI";
                updatePlayerStats(currentUserId, "LOSS");
            } else {
                winnerId = "DRAW";
                updatePlayerStats(currentUserId, "DRAW");
            }
        } else if (gameMode == Mode.ONLINE) {
            player2 = (opponentUserId != null) ? opponentUserId : "Online_Opponent";
            if (blackScore > whiteScore) {
                // 흑돌 승리
                if (myColor == 1) {
                    winnerId = currentUserId;
                    updatePlayerStats(currentUserId, "WIN");
                } else {
                    winnerId = player2;
                    updatePlayerStats(currentUserId, "LOSS");
                }
            } else if (whiteScore > blackScore) {
                // 백돌 승리
                if (myColor == 2) {
                    winnerId = currentUserId;
                    updatePlayerStats(currentUserId, "WIN");
                } else {
                    winnerId = player2;
                    updatePlayerStats(currentUserId, "LOSS");
                }
            } else {
                winnerId = "DRAW";
                updatePlayerStats(currentUserId, "DRAW");
            }
        } else {
            return; // 알 수 없는 모드
        }
        
        saveGameRecord(player1, player2, winnerId, blackScore, whiteScore, "[]");
        System.out.println("Game result saved to database for user: " + currentUserId);
    }
}

