package org.example.service;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;

/**
 * 픽셀 아트 스타일 UI 요소를 생성하는 서비스
 * Canvas를 사용하여 픽셀 단위로 그리기
 */
public class PixelArtUIService {
    
    // 픽셀 아트 색상 팔레트
    private static final Color SKY_BLUE = Color.rgb(135, 206, 235); // #87CEEB
    private static final Color CLOUD_WHITE = Color.rgb(255, 255, 255);
    private static final Color GRASS_GREEN = Color.rgb(34, 139, 34); // #228B22
    private static final Color DIRT_BROWN = Color.rgb(139, 90, 43); // #8B5A2B
    private static final Color WOOD_LIGHT = Color.rgb(160, 82, 45); // #A0522D
    private static final Color WOOD_DARK = Color.rgb(139, 69, 19); // #8B4513
    private static final Color WOOD_DARKER = Color.rgb(101, 67, 33); // #654321
    private static final Color METAL_GRAY = Color.rgb(169, 169, 169); // #A9A9A9
    private static final Color METAL_DARK = Color.rgb(128, 128, 128); // #808080
    private static final Color PANEL_GRAY = Color.rgb(105, 105, 105); // #696969
    private static final Color PANEL_DARK = Color.rgb(64, 64, 64); // #404040
    
    /**
     * 픽셀 아트 스타일의 하늘 배경 생성
     */
    public static Canvas createSkyBackground(double width, double height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // 하늘 배경
        gc.setFill(SKY_BLUE);
        gc.fillRect(0, 0, width, height);
        
        // 구름 그리기 (픽셀 아트 스타일)
        drawPixelCloud(gc, width * 0.2, height * 0.1, 60);
        drawPixelCloud(gc, width * 0.6, height * 0.15, 80);
        drawPixelCloud(gc, width * 0.8, height * 0.08, 50);
        drawPixelCloud(gc, width * 0.3, height * 0.2, 70);
        
        return canvas;
    }
    
    /**
     * 픽셀 아트 스타일의 구름 그리기
     */
    private static void drawPixelCloud(GraphicsContext gc, double x, double y, double size) {
        gc.setFill(CLOUD_WHITE);
        // 픽셀 아트 스타일 구름 (직사각형 블록으로)
        int pixelSize = 8;
        for (int i = 0; i < size / pixelSize; i++) {
            for (int j = 0; j < size / pixelSize / 2; j++) {
                if ((i + j) % 3 != 0) { // 패턴 생성
                    gc.fillRect(x + i * pixelSize, y + j * pixelSize, pixelSize, pixelSize);
                }
            }
        }
    }
    
    /**
     * 픽셀 아트 스타일의 땅 배경 생성
     */
    public static Canvas createGroundBackground(double width, double height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // 땅 배경 (초록색 + 갈색)
        int groundHeight = (int)(height * 0.3);
        int grassHeight = groundHeight / 2;
        
        // 초록색 풀
        gc.setFill(GRASS_GREEN);
        gc.fillRect(0, height - groundHeight, width, grassHeight);
        
        // 갈색 흙
        gc.setFill(DIRT_BROWN);
        gc.fillRect(0, height - groundHeight + grassHeight, width, groundHeight - grassHeight);
        
        // 픽셀 패턴 (지그재그 가장자리)
        drawPixelGrassEdge(gc, width, height - groundHeight, grassHeight);
        
        return canvas;
    }
    
    /**
     * 픽셀 아트 스타일의 풀 가장자리 그리기
     */
    private static void drawPixelGrassEdge(GraphicsContext gc, double width, double y, double height) {
        int pixelSize = 4;
        gc.setFill(GRASS_GREEN);
        for (int i = 0; i < width / pixelSize; i++) {
            int offset = (int)(Math.sin(i * 0.3) * 2) * pixelSize;
            gc.fillRect(i * pixelSize, y + offset, pixelSize, height);
        }
    }
    
    /**
     * 나무 프레임과 메탈 코너가 있는 픽셀 아트 버튼 생성
     */
    public static StackPane createPixelArtButton(String text, double width, double height) {
        StackPane buttonPane = new StackPane();
        buttonPane.setPrefSize(width, height);
        buttonPane.setMaxSize(width, height);
        
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        int pixelSize = 4;
        int borderWidth = 8;
        int cornerSize = 16;
        
        // 나무 프레임 그리기
        drawWoodenFrame(gc, 0, 0, width, height, borderWidth, pixelSize);
        
        // 메탈 코너 장식
        drawMetalCorner(gc, 0, 0, cornerSize, pixelSize); // 좌상
        drawMetalCorner(gc, width - cornerSize, 0, cornerSize, pixelSize); // 우상
        drawMetalCorner(gc, 0, height - cornerSize, cornerSize, pixelSize); // 좌하
        drawMetalCorner(gc, width - cornerSize, height - cornerSize, cornerSize, pixelSize); // 우하
        
        // 중앙 패널 (다크 그레이)
        double panelX = borderWidth;
        double panelY = borderWidth;
        double panelWidth = width - borderWidth * 2;
        double panelHeight = height - borderWidth * 2;
        
        gc.setFill(PANEL_DARK);
        gc.fillRect(panelX, panelY, panelWidth, panelHeight);
        
        // 픽셀 패턴 추가
        drawPixelPattern(gc, panelX, panelY, panelWidth, panelHeight, pixelSize);
        
        buttonPane.getChildren().add(canvas);
        
        // 텍스트 레이블 추가
        javafx.scene.control.Label label = new javafx.scene.control.Label(text);
        label.setFont(FontService.getPixelFont(32));
        label.setStyle("""
            -fx-text-fill: #FFFFFF;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,1.0), 0, 0, 3, 3);
            -fx-stroke: #000000;
            -fx-stroke-width: 3px;
        """);
        label.setMouseTransparent(true); // 클릭 이벤트가 버튼으로 전달되도록
        buttonPane.getChildren().add(label);
        
        return buttonPane;
    }
    
    /**
     * 나무 프레임 그리기
     */
    private static void drawWoodenFrame(GraphicsContext gc, double x, double y, double width, double height, 
                                       int borderWidth, int pixelSize) {
        // 나무 색상
        gc.setFill(WOOD_DARK);
        gc.fillRect(x, y, width, height);
        
        // 나무 질감 패턴
        gc.setFill(WOOD_LIGHT);
        for (int i = 0; i < width / pixelSize; i++) {
            for (int j = 0; j < height / pixelSize; j++) {
                if ((i + j) % 4 == 0) {
                    gc.fillRect(x + i * pixelSize, y + j * pixelSize, pixelSize, pixelSize);
                }
            }
        }
        
        // 어두운 나무 라인
        gc.setFill(WOOD_DARKER);
        for (int i = 0; i < width / pixelSize; i += 8) {
            gc.fillRect(x + i * pixelSize, y, pixelSize, height);
        }
    }
    
    /**
     * 메탈 코너 장식 그리기 (rivets 포함)
     */
    private static void drawMetalCorner(GraphicsContext gc, double x, double y, double size, int pixelSize) {
        // 메탈 플레이트
        gc.setFill(METAL_GRAY);
        gc.fillRect(x, y, size, size);
        
        // 메탈 하이라이트
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, size, pixelSize * 2);
        gc.fillRect(x, y, pixelSize * 2, size);
        
        // 메탈 그림자
        gc.setFill(METAL_DARK);
        gc.fillRect(x + size - pixelSize * 2, y, pixelSize * 2, size);
        gc.fillRect(x, y + size - pixelSize * 2, size, pixelSize * 2);
        
        // Rivets (나사)
        int rivetSize = pixelSize * 2;
        int rivetOffset = pixelSize * 2;
        gc.setFill(METAL_DARK);
        // 좌상 rivet
        gc.fillOval(x + rivetOffset, y + rivetOffset, rivetSize, rivetSize);
        // 우하 rivet
        gc.fillOval(x + size - rivetOffset - rivetSize, y + size - rivetOffset - rivetSize, rivetSize, rivetSize);
    }
    
    /**
     * 픽셀 패턴 그리기 (더 명확한 픽셀 느낌)
     */
    private static void drawPixelPattern(GraphicsContext gc, double x, double y, double width, double height, int pixelSize) {
        gc.setFill(PANEL_GRAY);
        // 더 명확한 체크 패턴
        for (int i = 0; i < width / pixelSize; i++) {
            for (int j = 0; j < height / pixelSize; j++) {
                if ((i + j) % 3 == 0) {
                    gc.fillRect(x + i * pixelSize, y + j * pixelSize, pixelSize, pixelSize);
                }
            }
        }
    }
    
    /**
     * 픽셀 아트 스타일의 배너/패널 생성
     */
    public static StackPane createPixelArtBanner(String text, double width, double height) {
        StackPane bannerPane = new StackPane();
        bannerPane.setPrefSize(width, height);
        
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        int pixelSize = 4;
        int borderWidth = 12;
        int cornerSize = 20;
        
        // 나무 프레임
        drawWoodenFrame(gc, 0, 0, width, height, borderWidth, pixelSize);
        
        // 메탈 코너
        drawMetalCorner(gc, 0, 0, cornerSize, pixelSize);
        drawMetalCorner(gc, width - cornerSize, 0, cornerSize, pixelSize);
        drawMetalCorner(gc, 0, height - cornerSize, cornerSize, pixelSize);
        drawMetalCorner(gc, width - cornerSize, height - cornerSize, cornerSize, pixelSize);
        
        // 중앙 패널
        double panelX = borderWidth;
        double panelY = borderWidth;
        double panelWidth = width - borderWidth * 2;
        double panelHeight = height - borderWidth * 2;
        
        gc.setFill(PANEL_DARK);
        gc.fillRect(panelX, panelY, panelWidth, panelHeight);
        drawPixelPattern(gc, panelX, panelY, panelWidth, panelHeight, pixelSize);
        
        bannerPane.getChildren().add(canvas);
        
        // 텍스트 레이블
        javafx.scene.control.Label label = new javafx.scene.control.Label(text);
        label.setStyle("""
            -fx-font-family: "Courier New", monospace;
            -fx-font-size: 36px;
            -fx-font-weight: bold;
            -fx-text-fill: #FF6B6B;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,1.0), 0, 0, 3, 3);
            -fx-stroke: #000000;
            -fx-stroke-width: 3px;
        """);
        bannerPane.getChildren().add(label);
        
        return bannerPane;
    }
    
    /**
     * 픽셀 아트 스타일의 배경 레이어 생성 (하늘 + 땅)
     */
    public static StackPane createPixelArtBackground(double width, double height) {
        StackPane backgroundPane = new StackPane();
        backgroundPane.setPrefSize(width, height);
        
        // 하늘 배경
        Canvas skyCanvas = createSkyBackground(width, height);
        
        // 땅 배경
        Canvas groundCanvas = createGroundBackground(width, height);
        
        backgroundPane.getChildren().addAll(skyCanvas, groundCanvas);
        
        return backgroundPane;
    }
    
    /**
     * 픽셀 아트 스타일의 로고 생성 (이미지처럼 큰 픽셀 폰트)
     */
    public static Canvas createPixelArtLogo(String text, double width, double height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // 배경 투명
        gc.clearRect(0, 0, width, height);
        
        // 픽셀 크기 (큰 픽셀 아트 느낌)
        int pixelSize = 8;
        
        // 텍스트를 픽셀 아트로 그리기
        double startX = width / 2;
        double startY = height / 2;
        
        // 간단한 픽셀 폰트로 텍스트 그리기
        drawPixelText(gc, text, startX, startY, pixelSize, Color.WHITE);
        
        return canvas;
    }
    
    /**
     * 픽셀 아트 스타일의 텍스트 그리기 (큰 블록 폰트)
     */
    private static void drawPixelText(GraphicsContext gc, String text, double centerX, double centerY, int pixelSize, Color color) {
        gc.setFill(color);
        
        // 각 글자를 픽셀 패턴으로 그리기
        String[] lines = text.split("\n");
        double lineHeight = pixelSize * 12;
        double totalHeight = lines.length * lineHeight;
        double y = centerY - totalHeight / 2;
        
        for (String line : lines) {
            double lineWidth = calculatePixelTextWidth(line, pixelSize);
            double x = centerX - lineWidth / 2;
            drawPixelTextLine(gc, line, x, y, pixelSize, color);
            y += lineHeight;
        }
    }
    
    /**
     * 픽셀 텍스트 한 줄 그리기
     */
    private static void drawPixelTextLine(GraphicsContext gc, String text, double x, double y, int pixelSize, Color color) {
        // 기본 픽셀 폰트 패턴 (간단한 8x8 그리드)
        int charWidth = pixelSize * 8;
        double currentX = x;
        
        for (char c : text.toCharArray()) {
            drawPixelChar(gc, c, currentX, y, pixelSize, color);
            currentX += charWidth;
        }
    }
    
    /**
     * 픽셀 문자 그리기 (간단한 픽셀 폰트)
     */
    private static void drawPixelChar(GraphicsContext gc, char c, double x, double y, int pixelSize, Color color) {
        // 그림자 효과 (오른쪽 아래)
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        drawCharPattern(gc, c, x + pixelSize, y + pixelSize, pixelSize, Color.BLACK);
        
        // 메인 텍스트
        gc.setFill(color);
        drawCharPattern(gc, c, x, y, pixelSize, color);
    }
    
    /**
     * 문자 패턴 그리기 (8x8 픽셀 그리드)
     */
    private static void drawCharPattern(GraphicsContext gc, char c, double x, double y, int pixelSize, Color color) {
        int[][] pattern = getPixelCharPattern(c);
        if (pattern == null) return;
        
        gc.setFill(color);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (pattern[row][col] == 1) {
                    gc.fillRect(x + col * pixelSize, y + row * pixelSize, pixelSize, pixelSize);
                }
            }
        }
    }
    
    /**
     * 픽셀 텍스트 너비 계산
     */
    private static double calculatePixelTextWidth(String text, int pixelSize) {
        return text.length() * pixelSize * 8;
    }
    
    /**
     * 문자별 픽셀 패턴 반환 (8x8 그리드)
     */
    private static int[][] getPixelCharPattern(char c) {
        // 간단한 픽셀 폰트 패턴 (8x8)
        switch (Character.toUpperCase(c)) {
            case '오':
                return new int[][] {
                    {0,1,1,1,1,1,0,0},
                    {1,1,0,0,0,1,1,0},
                    {1,1,0,0,0,1,1,0},
                    {1,1,0,0,0,1,1,0},
                    {1,1,0,0,0,1,1,0},
                    {0,1,1,1,1,1,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0}
                };
            case '셀':
                return new int[][] {
                    {1,1,1,1,1,1,0,0},
                    {0,0,1,1,0,0,0,0},
                    {0,0,1,1,0,0,0,0},
                    {0,0,1,1,0,0,0,0},
                    {0,0,1,1,0,0,0,0},
                    {1,1,1,1,1,1,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0}
                };
            case '로':
                return new int[][] {
                    {1,1,0,0,0,1,1,0},
                    {1,1,0,0,0,1,1,0},
                    {1,1,0,0,0,1,1,0},
                    {1,1,0,0,0,1,1,0},
                    {1,1,0,0,0,1,1,0},
                    {0,1,1,1,1,1,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0}
                };
            case '게':
                return new int[][] {
                    {1,1,1,1,1,1,0,0},
                    {1,1,0,0,0,0,0,0},
                    {1,1,1,1,1,0,0,0},
                    {1,1,0,0,0,0,0,0},
                    {1,1,0,0,0,0,0,0},
                    {1,1,1,1,1,1,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0}
                };
            case '임':
                return new int[][] {
                    {0,1,1,1,1,1,0,0},
                    {1,1,0,0,0,1,1,0},
                    {1,1,0,0,0,1,1,0},
                    {0,1,1,1,1,1,0,0},
                    {0,0,1,1,0,0,0,0},
                    {0,0,1,1,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0}
                };
            case ' ':
                return new int[8][8];
            default:
                // 영문자나 기타 문자는 간단한 패턴
                return getSimpleCharPattern(c);
        }
    }
    
    /**
     * 간단한 영문자 패턴
     */
    private static int[][] getSimpleCharPattern(char c) {
        // 기본 패턴 (나중에 확장 가능)
        return new int[8][8];
    }
    
    /**
     * 픽셀 아트 스타일의 큰 로고 생성 (이미지처럼)
     */
    public static StackPane createLargePixelArtLogo(String text, double width, double height) {
        StackPane logoPane = new StackPane();
        logoPane.setPrefSize(width, height);
        logoPane.setAlignment(Pos.CENTER);
        
        // 그림자 레이블 (뒤에 - 어두운 갈색)
        javafx.scene.control.Label shadowLabel = new javafx.scene.control.Label(text);
        shadowLabel.setFont(FontService.getPixelFont(140));
        shadowLabel.setStyle("""
            -fx-text-fill: rgba(101, 67, 33, 0.8);
            -fx-translate-x: 8px;
            -fx-translate-y: 8px;
        """);
        
        // 메인 텍스트 레이블 (금색/오렌지 그라데이션 효과)
        javafx.scene.control.Label textLabel = new javafx.scene.control.Label(text);
        textLabel.setFont(FontService.getPixelFont(140));
        
        // 금색/오렌지 그라데이션을 위한 LinearGradient
        LinearGradient goldGradient = new LinearGradient(
            0, 0, 0, 1,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.rgb(255, 215, 0)),   // 밝은 금색 (상단)
            new Stop(0.3, Color.rgb(255, 200, 0)),   // 중간 금색
            new Stop(0.7, Color.rgb(255, 140, 0)),   // 오렌지
            new Stop(1.0, Color.rgb(200, 100, 0))   // 어두운 오렌지 (하단)
        );
        
        // 텍스트에 그라데이션 적용 (CSS로는 불가능하므로 Java 코드로)
        textLabel.setTextFill(goldGradient);
        textLabel.setStyle("""
            -fx-effect: dropshadow(gaussian, rgba(101, 67, 33, 0.8), 0, 0, 6, 6),
                        dropshadow(gaussian, rgba(0,0,0,0.6), 0, 0, 3, 3);
            -fx-stroke: rgba(101, 67, 33, 0.9);
            -fx-stroke-width: 4px;
            -fx-background-color: transparent;
        """);
        
        logoPane.getChildren().addAll(shadowLabel, textLabel);
        
        return logoPane;
    }
    
    /**
     * 간단한 픽셀 텍스트 그리기 (영문/숫자용 블록 스타일)
     */
    private static void drawSimplePixelText(GraphicsContext gc, String text, double centerX, double centerY, int pixelSize, Color color) {
        // 영문자나 숫자가 있으면 블록으로 그리기
        String[] lines = text.split("\n");
        double lineHeight = pixelSize * 8;
        double totalHeight = lines.length * lineHeight;
        double y = centerY - totalHeight / 2;
        
        for (String line : lines) {
            // 간단한 블록 배경 그리기 (픽셀 아트 느낌)
            double lineWidth = line.length() * pixelSize * 6;
            double x = centerX - lineWidth / 2;
            
            // 각 글자마다 블록 그리기
            for (int i = 0; i < line.length(); i++) {
                drawSimplePixelBlock(gc, x, y, pixelSize * 5, pixelSize * 6, pixelSize, color);
                x += pixelSize * 6;
            }
            
            y += lineHeight;
        }
    }
    
    /**
     * 간단한 픽셀 블록 그리기
     */
    private static void drawSimplePixelBlock(GraphicsContext gc, double x, double y, double width, double height, int pixelSize, Color color) {
        // 외곽선
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
        
        // 내부 패턴 (픽셀 아트 느낌)
        gc.setFill(Color.rgb(
            (int)(color.getRed() * 255 * 0.9),
            (int)(color.getGreen() * 255 * 0.9),
            (int)(color.getBlue() * 255 * 0.9)
        ));
        for (int i = 0; i < width / pixelSize; i += 2) {
            for (int j = 0; j < height / pixelSize; j += 2) {
                gc.fillRect(x + i * pixelSize, y + j * pixelSize, pixelSize, pixelSize);
            }
        }
    }
    
    
}

