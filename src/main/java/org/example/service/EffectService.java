package org.example.service;

import javafx.animation.*;
import javafx.animation.Interpolator;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * 그래픽 효과를 관리하는 클래스
 */
public class EffectService {
    
    /**
     * 돌 놓기 애니메이션 효과 생성
     */
    public static Animation createPlaceAnimation(javafx.scene.Node node) {
        // 스케일 애니메이션 (작게 시작해서 커지기)
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), node);
        scaleTransition.setFromX(0.0);
        scaleTransition.setFromY(0.0);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        
        // 글로우 효과
        Glow glow = new Glow(0.8);
        glow.setInput(new DropShadow(10, Color.WHITE));
        node.setEffect(glow);
        
        // 글로우 효과 제거 애니메이션
        Timeline glowTimeline = new Timeline(
            new KeyFrame(Duration.millis(0), e -> node.setEffect(glow)),
            new KeyFrame(Duration.millis(200), e -> node.setEffect(null))
        );
        
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(scaleTransition, glowTimeline);
        
        return parallelTransition;
    }
    
    /**
     * 돌 뒤집기 애니메이션 효과 생성
     */
    public static Animation createFlipAnimation(javafx.scene.Node node) {
        // Y축 회전 애니메이션 (뒤집는 효과)
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), node);
        rotateTransition.setAxis(javafx.scene.transform.Rotate.Y_AXIS);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(180);
        rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
        
        // 스케일 애니메이션 (약간 작아졌다가 커지기)
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), node);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(0.3);
        scaleTransition.setToY(1.0);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(rotateTransition, scaleTransition);
        
        return parallelTransition;
    }
    
    /**
     * 파티클 효과 생성 (돌 놓을 때 주변에 작은 점들)
     */
    public static void createParticleEffect(javafx.scene.layout.Pane parent, double x, double y, Color color) {
        for (int i = 0; i < 8; i++) {
            javafx.scene.shape.Circle particle = new javafx.scene.shape.Circle(3);
            particle.setFill(color);
            
            double angle = (360.0 / 8) * i;
            double radians = Math.toRadians(angle);
            double distance = 30;
            
            particle.setLayoutX(x);
            particle.setLayoutY(y);
            
            parent.getChildren().add(particle);
            
            // 파티클 이동 애니메이션
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), particle);
            translateTransition.setByX(Math.cos(radians) * distance);
            translateTransition.setByY(Math.sin(radians) * distance);
            
            // 파티클 페이드 아웃
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), particle);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            
            // 파티클 제거
            fadeTransition.setOnFinished(e -> parent.getChildren().remove(particle));
            
            ParallelTransition parallelTransition = new ParallelTransition();
            parallelTransition.getChildren().addAll(translateTransition, fadeTransition);
            parallelTransition.play();
        }
    }
    
    /**
     * 유효한 수 위치 하이라이트 효과
     */
    public static void createValidMoveHighlight(javafx.scene.Node node) {
        Glow glow = new Glow(0.5);
        glow.setInput(new DropShadow(5, Color.CYAN));
        node.setEffect(glow);
        
        // 펄스 애니메이션
        Timeline pulseTimeline = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(1000), node);
                st.setFromX(1.0);
                st.setFromY(1.0);
                st.setToX(1.1);
                st.setToY(1.1);
                st.setAutoReverse(true);
                st.setCycleCount(Animation.INDEFINITE);
                st.play();
            })
        );
        pulseTimeline.play();
    }
}

