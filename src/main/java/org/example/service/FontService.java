package org.example.service;

import javafx.scene.text.Font;

/**
 * 픽셀 폰트를 관리하는 서비스
 * 폰트 파일이 없으면 기본 폰트를 사용
 */
public class FontService {
    
    private static Font pixelFont;
    private static boolean fontLoaded = false;
    
    // 폰트 파일 경로 (resources/fonts/ 폴더에 폰트 파일을 넣어주세요)
    private static final String[] FONT_PATHS = {
        "/fonts/Mulmaru.ttf",  // Mulmaru 폰트 (우선순위 1)
        "/fonts/Mulmaru.otf",  // Mulmaru 폰트 (OpenType 형식)
        "/fonts/NeoDunggeunmo.ttf",
        "/fonts/DungGeunmo.ttf",
        "/fonts/pixel-font.ttf"
    };
    
    /**
     * 픽셀 폰트 로드 시도
     */
    static {
        loadPixelFont();
    }
    
    /**
     * 픽셀 폰트 로드
     */
    private static void loadPixelFont() {
        for (String fontPath : FONT_PATHS) {
            try {
                java.io.InputStream fontStream = FontService.class.getResourceAsStream(fontPath);
                if (fontStream != null) {
                    pixelFont = Font.loadFont(fontStream, 16);
                    fontLoaded = true;
                    fontStream.close();
                    System.out.println("픽셀 폰트 로드 성공: " + fontPath + " (폰트명: " + pixelFont.getFamily() + ")");
                    break;
                }
            } catch (Exception e) {
                // 다음 폰트 파일 시도
                continue;
            }
        }
        
        // 폰트 로드 실패 시 기본 폰트 사용
        if (!fontLoaded) {
            pixelFont = Font.font("Courier New", 16);
            System.out.println("픽셀 폰트 로드 실패, 기본 폰트 사용: Courier New");
        }
    }
    
    /**
     * 픽셀 폰트 반환 (크기 지정)
     */
    public static Font getPixelFont(double size) {
        if (fontLoaded) {
            return Font.font(pixelFont.getFamily(), size);
        } else {
            return Font.font("Courier New", size);
        }
    }
    
    /**
     * 픽셀 폰트 패밀리 이름 반환
     */
    public static String getPixelFontFamily() {
        if (fontLoaded) {
            return pixelFont.getFamily();
        } else {
            return "Courier New";
        }
    }
    
    /**
     * 폰트가 성공적으로 로드되었는지 확인
     */
    public static boolean isFontLoaded() {
        return fontLoaded;
    }
}

