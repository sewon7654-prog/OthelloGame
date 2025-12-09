package org.example.service;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;

/**
 * 픽셀 아트 스타일 배경을 생성하는 서비스
 */
public class PixelArtBackgroundService {
    
    /**
     * 픽셀 아트 스타일의 나무 텍스처 배경을 반환
     */
    public static Paint createWoodTexture() {
        // 나무 질감을 위한 그라데이션
        return new LinearGradient(
            0, 0, 1, 0,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.rgb(139, 69, 19)),   // #8B4513
            new Stop(0.5, Color.rgb(160, 82, 45)),   // #A0522D
            new Stop(1.0, Color.rgb(139, 69, 19))    // #8B4513
        );
    }
    
    /**
     * 픽셀 아트 스타일의 하늘 배경을 반환
     */
    public static Paint createSkyBackground() {
        // 하늘색 배경
        return Color.rgb(135, 206, 235); // #87CEEB
    }
    
    /**
     * 픽셀 아트 스타일의 땅 배경을 반환
     */
    public static Paint createGroundBackground() {
        // 초록색 땅
        return Color.rgb(34, 139, 34); // #228B22
    }
    
    /**
     * Region에 픽셀 아트 배경 적용
     */
    public static void applyPixelArtBackground(Region region, Paint background) {
        region.setBackground(new javafx.scene.layout.Background(
            new javafx.scene.layout.BackgroundFill(
                background,
                null,
                null
            )
        ));
    }
}

