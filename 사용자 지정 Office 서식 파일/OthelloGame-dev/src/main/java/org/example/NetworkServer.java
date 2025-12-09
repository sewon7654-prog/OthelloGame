package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * F-10, F-11: 온라인 대전을 위한 서버 클래스 (백엔드 역할).
 */
public class NetworkServer {

    private static final int PORT = 8080;
    private static List<ClientHandler> waitingClients = new ArrayList<>();
    private static List<GameRoom> activeRooms = new ArrayList<>();
    // 쓰레드 풀을 사용하여 다중 접속 처리
    private static ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("Othello Game Server is running on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // F-10: 클라이언트의 연결 요청을 대기
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler newClient = new ClientHandler(clientSocket);
                pool.execute(newClient);

                matchPlayers(newClient);
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            // 서버 종료 시 풀도 종료
            pool.shutdown();
        }
    }

    /**
     * F-10: 매칭 로직 (아주 간단한 1:1 매칭)
     */
    private static synchronized void matchPlayers(ClientHandler newClient) {
        try {
            // 쓰레드가 완전히 시작될 때까지 잠시 대기
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        if (newClient.isValid() && !waitingClients.contains(newClient)) {
            if (!waitingClients.isEmpty()) {
                ClientHandler player1 = waitingClients.remove(0);
                GameRoom room = new GameRoom(player1, newClient);
                activeRooms.add(room);
                System.out.println("--- Match Found: Room " + room.getId() + " created. ---");
                room.startGame();
            } else {
                waitingClients.add(newClient);
                System.out.println("Client waiting for opponent. Current queue: 1");
            }
        }
    }

    /**
     * 게임 방 관리 클래스.
     */
    private static class GameRoom {
        private static int nextRoomId = 1;
        private final int id;
        private ClientHandler player1; // Black
        private ClientHandler player2; // White

        public GameRoom(ClientHandler p1, ClientHandler p2) {
            this.id = nextRoomId++;
            this.player1 = p1;
            this.player2 = p2;
            player1.setRoom(this);
            player2.setRoom(this);
        }

        public int getId() { return id; }

        // F-11: 상대방에게 수를 중계합니다.
        public void broadcastMove(ClientHandler sender, String moveData) {
            if (sender == player1) {
                player2.sendMessage(moveData);
            } else if (sender == player2) {
                player1.sendMessage(moveData);
            }
        }

        public void startGame() {
            // 흑돌(Player1)에게는 'START_BLACK', 백돌(Player2)에게는 'START_WHITE' 메시지를 보냅니다.
            player1.sendMessage("START_BLACK");
            player2.sendMessage("START_WHITE");
        }
    }

    /**
     * 클라이언트와의 개별 통신을 처리하는 핸들러.
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private GameRoom room;
        private BufferedReader in;
        private PrintWriter out;
        private boolean connected = true;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                connected = false;
            }
        }

        public boolean isValid() { return connected; }
        public void setRoom(GameRoom room) { this.room = room; }

        public void sendMessage(String message) {
            if (connected) {
                out.println(message);
            }
        }

        @Override
        public void run() {
            if (!connected) return;
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (room != null) {
                        // F-11: 받은 수를 같은 방의 상대방에게 중계합니다.
                        room.broadcastMove(this, inputLine);
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + socket.getInetAddress().getHostAddress());
            } finally {
                // 자원 정리
                try {
                    socket.close();
                } catch (IOException ignored) {}
                waitingClients.remove(this);
                // GameRoom도 여기서 종료/제거 처리가 필요합니다.
            }
        }
    }
}