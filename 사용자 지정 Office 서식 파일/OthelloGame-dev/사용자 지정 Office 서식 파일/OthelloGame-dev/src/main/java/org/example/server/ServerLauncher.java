package org.example.server;

import org.example.network.NetworkServer;

/**
 * 서버 전용 실행 클래스
 * 이 클래스는 서버만 실행합니다 (UI 없음)
 * 
 * 사용법:
 * 1. 이 클래스를 실행하면 서버만 시작됩니다
 * 2. 클라이언트는 Main.java를 실행합니다
 */
public class ServerLauncher {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   오셀로 게임 서버 시작");
        System.out.println("========================================");
        System.out.println("서버가 실행 중입니다...");
        System.out.println("클라이언트는 이 서버의 IP 주소로 접속할 수 있습니다.");
        System.out.println("포트: 8080");
        System.out.println("========================================");
        
        // NetworkServer의 main 메서드 호출
        NetworkServer.main(args);
    }
}

