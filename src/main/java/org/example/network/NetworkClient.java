package org.example.network;

import org.example.ui.GameView;
import org.example.service.ConfigService;
import org.example.minigame.network.MinigameProtocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 클라이언트 네트워크 스레드: 서버와 통신하며 턴/미니게임 정보를 전달한다.
 */
public class NetworkClient extends Thread {

    private String serverIp;
    private int serverPort;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final GameView gameView;
    private final String userId;

    public NetworkClient(GameView gameView, String userId, String serverIp, int serverPort) {
        this.gameView = gameView;
        this.userId = userId;
        this.serverIp = serverIp != null ? serverIp : ConfigService.getServerIP();
        this.serverPort = serverPort > 0 ? serverPort : ConfigService.getServerPort();
    }

    public NetworkClient(GameView gameView, String userId, String serverIp) {
        this(gameView, userId, serverIp, ConfigService.getServerPort());
    }

    public NetworkClient(GameView gameView, String userId) {
        this(gameView, userId, null, ConfigService.getServerPort());
    }

    public boolean connect() {
        try {
            socket = new Socket(serverIp, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("USER_ID " + userId);
            System.out.println("Connected to server (" + serverIp + ":" + serverPort + "). Waiting for opponent...");
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server (" + serverIp + ":" + serverPort + "): " + e.getMessage());
            return false;
        }
    }

    public void sendMove(int x, int y) {
        if (out != null) {
            out.println("MOVE " + x + " " + y);
        }
    }

    public void sendMinigameStart(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void sendMinigameResult(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void sendMinigameUpdate(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void requestRandomMove() {
        if (out != null) {
            out.println("RANDOM_MOVE");
        }
    }

    @Override
    public void run() {
        try {
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                if (serverResponse.startsWith("START_")) {
                    String[] parts = serverResponse.split(" ", 2);
                    String color = parts[0].substring(6);
                    if (parts.length > 1 && !parts[1].isEmpty()) {
                        gameView.setOpponentUserId(parts[1]);
                    }
                    gameView.setPlayerColor(color);
                }
                else if (serverResponse.startsWith("MOVE")) {
                    String[] parts = serverResponse.split(" ");
                    if (parts.length == 3) {
                        int x = safeParseInt(parts[1]);
                        int y = safeParseInt(parts[2]);
                        gameView.processOpponentMove(x, y);
                    }
                }
                else if (serverResponse.startsWith(MinigameProtocol.MINIGAME_START)) {
                    String[] parts = serverResponse.split(" ");
                    if (parts.length >= 2) {
                        String gameType = parts[1];
                        gameView.showMinigameSpectator(gameType);
                    }
                }
                else if (serverResponse.startsWith(MinigameProtocol.MINIGAME_UPDATE)) {
                    String json = serverResponse.substring(MinigameProtocol.MINIGAME_UPDATE.length()).trim();
                    gameView.onMinigameUpdate(json);
                }
                else if (serverResponse.startsWith(MinigameProtocol.MINIGAME_RESULT)) {
                    String[] parts = serverResponse.split(" ");
                    boolean success = parts.length > 1 && "SUCCESS".equalsIgnoreCase(parts[1]);
                    int score = parts.length > 2 ? safeParseInt(parts[2]) : 0;
                    long time = parts.length > 3 ? safeParseInt(parts[3]) : 0;
                    int x = parts.length > 4 ? safeParseInt(parts[4]) : -1;
                    int y = parts.length > 5 ? safeParseInt(parts[5]) : -1;
                    gameView.handleMinigameResultFromNetwork(success, score, time, x, y);
                }
                else if (serverResponse.equals("RANDOM_MOVE_EXECUTED")) {
                    gameView.handleRandomMove();
                }
            }
        } catch (IOException e) {
            System.out.println("Connection lost to server.");
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException ignored) {}
        }
    }

    private int safeParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
}
