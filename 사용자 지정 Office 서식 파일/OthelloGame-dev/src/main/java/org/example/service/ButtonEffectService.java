package org.example.service;

import javafx.animation.*;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * 버튼 클릭 및 호버 효과를 관리하는 클래스 (픽셀 아트 스타일)
 */
public class ButtonEffectService {
    
    /**
     * 버튼에 세련된 픽셀 아트 스타일 클릭 효과 추가
     */
    public static void addPixelArtButtonEffects(Button button) {
        // 호버 효과: 버튼이 위로 살짝 올라가고 그림자 증가
        button.setOnMouseEntered(e -> {
            // 위로 이동
            TranslateTransition moveUp = new TranslateTransition(Duration.millis(150), button);
            moveUp.setToY(-5);
            moveUp.setInterpolator(Interpolator.EASE_OUT);
            
            // 스케일 증가
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), button);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);
            
            // 그림자 효과 강화
            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.rgb(0, 0, 0, 0.8));
            hoverShadow.setRadius(15);
            hoverShadow.setOffsetX(0);
            hoverShadow.setOffsetY(8);
            button.setEffect(hoverShadow);
            
            ParallelTransition hoverTransition = new ParallelTransition();
            hoverTransition.getChildren().addAll(moveUp, scaleUp);
            hoverTransition.play();
        });
        
        // 호버 해제 효과
        button.setOnMouseExited(e -> {
            // 원래 위치로
            TranslateTransition moveDown = new TranslateTransition(Duration.millis(150), button);
            moveDown.setToY(0);
            moveDown.setInterpolator(Interpolator.EASE_IN);
            
            // 원래 크기로
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), button);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.setInterpolator(Interpolator.EASE_IN);
            
            // 그림자 효과 원래대로
            DropShadow normalShadow = new DropShadow();
            normalShadow.setColor(Color.rgb(0, 0, 0, 0.6));
            normalShadow.setRadius(8);
            normalShadow.setOffsetX(0);
            normalShadow.setOffsetY(4);
            button.setEffect(normalShadow);
            
            ParallelTransition exitTransition = new ParallelTransition();
            exitTransition.getChildren().addAll(moveDown, scaleDown);
            exitTransition.play();
        });
        
        // 클릭 효과: 버튼이 눌리는 느낌
        button.setOnMousePressed(e -> {
            // 아래로 이동 (눌리는 효과)
            TranslateTransition pressDown = new TranslateTransition(Duration.millis(100), button);
            pressDown.setToY(2);
            pressDown.setInterpolator(Interpolator.EASE_OUT);
            
            // 약간 작아지는 효과
            ScaleTransition pressScale = new ScaleTransition(Duration.millis(100), button);
            pressScale.setToX(0.98);
            pressScale.setToY(0.98);
            pressScale.setInterpolator(Interpolator.EASE_OUT);
            
            // 그림자 감소 (눌린 느낌)
            DropShadow pressShadow = new DropShadow();
            pressShadow.setColor(Color.rgb(0, 0, 0, 0.4));
            pressShadow.setRadius(4);
            pressShadow.setOffsetX(0);
            pressShadow.setOffsetY(2);
            button.setEffect(pressShadow);
            
            ParallelTransition pressTransition = new ParallelTransition();
            pressTransition.getChildren().addAll(pressDown, pressScale);
            pressTransition.play();
        });
        
        // 클릭 해제 효과: 튕기는 느낌
        button.setOnMouseReleased(e -> {
            // 원래 위치로 (튕기는 효과)
            TranslateTransition releaseUp = new TranslateTransition(Duration.millis(150), button);
            releaseUp.setToY(0);
            releaseUp.setInterpolator(Interpolator.EASE_OUT);
            
            // 원래 크기로 (약간 튕김)
            ScaleTransition releaseScale = new ScaleTransition(Duration.millis(150), button);
            releaseScale.setToX(1.0);
            releaseScale.setToY(1.0);
            releaseScale.setInterpolator(Interpolator.EASE_OUT);
            
            // 그림자 복원
            DropShadow normalShadow = new DropShadow();
            normalShadow.setColor(Color.rgb(0, 0, 0, 0.6));
            normalShadow.setRadius(8);
            normalShadow.setOffsetX(0);
            normalShadow.setOffsetY(4);
            button.setEffect(normalShadow);
            
            ParallelTransition releaseTransition = new ParallelTransition();
            releaseTransition.getChildren().addAll(releaseUp, releaseScale);
            releaseTransition.play();
        });
        
        // 초기 그림자 설정
        DropShadow initialShadow = new DropShadow();
        initialShadow.setColor(Color.rgb(0, 0, 0, 0.6));
        initialShadow.setRadius(8);
        initialShadow.setOffsetX(0);
        initialShadow.setOffsetY(4);
        button.setEffect(initialShadow);
    }
    
    /**
     * 버튼 클릭 시 파티클 효과 추가
     */
    public static void addClickParticleEffect(Button button) {
        button.setOnAction(e -> {
            // 클릭 위치에서 작은 파티클 효과
            javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) button.getParent();
            if (parent != null) {
                javafx.geometry.Bounds bounds = button.localToScene(button.getBoundsInLocal());
                double centerX = bounds.getMinX() + bounds.getWidth() / 2;
                double centerY = bounds.getMinY() + bounds.getHeight() / 2;
                
                javafx.geometry.Bounds parentBounds = parent.localToScene(parent.getBoundsInLocal());
                double relativeX = centerX - parentBounds.getMinX();
                double relativeY = centerY - parentBounds.getMinY();
                
                // 작은 파티클 효과
                for (int i = 0; i < 8; i++) {
                    javafx.scene.shape.Circle particle = new javafx.scene.shape.Circle(3);
                    particle.setFill(Color.WHITE);
                    
                    double angle = (360.0 / 8) * i;
                    double radians = Math.toRadians(angle);
                    double distance = 20;
                    
                    particle.setLayoutX(relativeX);
                    particle.setLayoutY(relativeY);
                    
                    parent.getChildren().add(particle);
                    
                    TranslateTransition translate = new TranslateTransition(Duration.millis(300), particle);
                    translate.setByX(Math.cos(radians) * distance);
                    translate.setByY(Math.sin(radians) * distance);
                    
                    FadeTransition fade = new FadeTransition(Duration.millis(300), particle);
                    fade.setFromValue(1.0);
                    fade.setToValue(0.0);
                    
                    fade.setOnFinished(event -> parent.getChildren().remove(particle));
                    
                    ParallelTransition particleTransition = new ParallelTransition();
                    particleTransition.getChildren().addAll(translate, fade);
                    particleTransition.play();
                }
            }
        });
    }
}

