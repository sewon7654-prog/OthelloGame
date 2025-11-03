package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * F-10, F-11: 온라인 대전을 위한 클라이언트 클래스 (게임 내 통신 관리).
 */
public class NetworkClient extends Thread {

    private static final String SERVER_IP = "127.0.0.1"; // Localhost
    private static final int SERVER_PORT = 8080;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Main gameController;

    public NetworkClient(Main gameController) {
        this.gameController = gameController;
    }

    /**
     * F-10: 서버에 연결을 시도하고 성공 여부를 반환합니다.
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
                    String color = serverResponse.substring(6);
                    gameController.setPlayerColor(color);
                }
                else if (serverResponse.startsWith("MOVE")) {
                    // Opponent's move received (F-11 Synchronization)
                    String[] parts = serverResponse.split(" ");
                    if (parts.length == 3) {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        gameController.processOpponentMove(x, y);
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