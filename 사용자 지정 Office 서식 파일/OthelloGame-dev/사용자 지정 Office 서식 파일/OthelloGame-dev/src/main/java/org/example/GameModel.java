package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameModel {

    public enum Mode { LOCAL, ONLINE, AI }
    // 난이도 Enum 추가
    public enum Difficulty { EASY, MEDIUM, HARD }

    private int[][] board;
    private int currentTurn;
    private final int SIZE = 8;
    private boolean isGameOver;
    private Mode gameMode;
    private int aiColor;
    private Difficulty aiDifficulty; // 난이도 저장 변수

    private final int[] DY = {-1, 1, 0, 0, -1, -1, 1, 1};
    private final int[] DX = {0, 0, -1, 1, -1, 1, -1, 1};

    public GameModel() {
        gameMode = Mode.LOCAL;
        aiDifficulty = Difficulty.MEDIUM; // 기본값
        initializeBoard();
    }

    public void initializeBoard() {
        board = new int[SIZE][SIZE];
        board[3][3] = 2; board[4][4] = 2;
        board[3][4] = 1; board[4][3] = 1;
        currentTurn = 1;
        isGameOver = false;

        if (gameMode == Mode.AI) {
            Random random = new Random();
            aiColor = random.nextBoolean() ? 1 : 2;
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

            while (ny >= 0 && ny < SIZE && nx >= 0 && nx < SIZE && board[ny][nx] == opponent) {
                potentialFlip.add(new int[]{ny, nx});
                ny += DY[i];
                nx += DX[i];
            }

            if (ny >= 0 && ny < SIZE && nx >= 0 && nx < SIZE && board[ny][nx] == player) {
                piecesToFlip.addAll(potentialFlip);
            }
        }

        return piecesToFlip;
    }

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

    public void switchTurn() {
        currentTurn = (currentTurn == 1) ? 2 : 1;
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
    public boolean isAIMode() { return gameMode == Mode.AI; }
    public boolean isOnlineMode() { return gameMode == Mode.ONLINE; }

    public int getAIColor() { return aiColor; }

    // 난이도 관련 메소드 추가
    public void setAIDifficulty(Difficulty difficulty) { this.aiDifficulty = difficulty; }
    public Difficulty getAIDifficulty() { return this.aiDifficulty; }
}