package org.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 설정 파일을 읽는 유틸리티 클래스
 */
public class ConfigService {
    private static final String CONFIG_FILE = "/config.properties";
    private static Properties properties;
    
    static {
        loadConfig();
    }
    
    /**
     * 설정 파일 로드
     */
    private static void loadConfig() {
        properties = new Properties();
        try (InputStream input = ConfigService.class.getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("설정 파일을 찾을 수 없습니다. 기본값을 사용합니다.");
            }
        } catch (IOException e) {
            System.err.println("설정 파일 로드 중 오류 발생: " + e.getMessage());
        }
    }
    
    /**
     * 서버 IP 주소 가져오기
     */
    public static String getServerIP() {
        String ip = properties.getProperty("server.ip", "127.0.0.1");
        return ip;
    }
    
    /**
     * 서버 포트 가져오기
     */
    public static int getServerPort() {
        String port = properties.getProperty("server.port", "8080");
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return 8080;
        }
    }
    
    /**
     * 서버 IP 주소 설정 (런타임에 변경 가능)
     */
    public static void setServerIP(String ip) {
        properties.setProperty("server.ip", ip);
    }
    
    /**
     * Gemini API 키 반환
     */
    public static String getGeminiApiKey() {
        // 1순위: 환경 변수
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            return envKey;
        }
        
        // 2순위: config.properties
        String configKey = properties.getProperty("gemini.api.key", "");
        if (configKey != null && !configKey.isEmpty()) {
            return configKey;
        }
        
        return null;
    }
}

