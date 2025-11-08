package org.example.network;

import org.example.ui.GameView;
import org.example.service.ConfigService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * F-10, F-11: 온라인 대전을 위한 클라이언트 클래스 (게임 내 통신 관리).
 */
public class NetworkClient extends Thread {

    private static String SERVER_IP = ConfigService.getServerIP(); // 설정 파일에서 읽어옴
    private static int SERVER_PORT = ConfigService.getServerPort(); // 설정 파일에서 읽어옴

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameView gameView;
    private String userId;

    public NetworkClient(GameView gameView, String userId) {
        this.gameView = gameView;
        this.userId = userId;
    }
    
    /**
     * 서버 IP 주소 설정 (외부 접속용)
     */
    public static void setServerIP(String ip) {
        SERVER_IP = ip;
    }
    
    /**
     * 현재 설정된 서버 IP 주소 반환
     */
    public static String getServerIP() {
        return SERVER_IP;
    }

    /**
     * F-10: 서버에 연결을 시도하고 성공 여부를 반환합니다.
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // 연결 시 사용자 ID 전송
            out.println("USER_ID " + userId);
            System.out.println("Connected to server. Waiting for opponent...");
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }

    /**
     * F-11: 자신의 수를 서버로 전송합니다.
     */
    public void sendMove(int x, int y) {
        if (out != null) {
            // Protocol: "MOVE X Y"
            out.println("MOVE " + x + " " + y);
        }
    }

    /**
     * F-11: 서버로부터 메시지를 수신하고 게임에 반영합니다.
     */
    @Override
    public void run() {
        try {
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                if (serverResponse.startsWith("START_")) {
                    // Game start and color assignment
                    // Format: "START_BLACK opponentId" or "START_WHITE opponentId"
                    String[] parts = serverResponse.split(" ", 2);
                    String color = parts[0].substring(6);
                    if (parts.length > 1 && !parts[1].isEmpty()) {
                        gameView.setOpponentUserId(parts[1]);
                    }
                    gameView.setPlayerColor(color);
                }
                else if (serverResponse.startsWith("MOVE")) {
                    // Opponent's move received (F-11 Synchronization)
                    String[] parts = serverResponse.split(" ");
                    if (parts.length == 3) {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        gameView.processOpponentMove(x, y);
                    }
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
}

