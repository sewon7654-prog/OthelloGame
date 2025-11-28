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
     * 돌 놓기 애니메이션 효과 생성 (강화된 픽셀 게임 스타일)
     */
    public static Animation createPlaceAnimation(javafx.scene.Node node) {
        // 더 강한 스케일 애니메이션 (작게 시작해서 커지고 약간 튕기는 효과)
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), node);
        scaleTransition.setFromX(0.0);
        scaleTransition.setFromY(0.0);
        scaleTransition.setToX(1.2);
        scaleTransition.setToY(1.2);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        
        // 튕기는 효과를 위한 추가 스케일
        ScaleTransition bounceTransition = new ScaleTransition(Duration.millis(150), node);
        bounceTransition.setFromX(1.2);
        bounceTransition.setFromY(1.2);
        bounceTransition.setToX(1.0);
        bounceTransition.setToY(1.0);
        bounceTransition.setInterpolator(Interpolator.EASE_IN);
        bounceTransition.setDelay(Duration.millis(300));
        
        // 강한 글로우 효과 (픽셀 게임 느낌)
        Glow glow = new Glow(1.2);
        glow.setInput(new DropShadow(20, Color.CYAN));
        node.setEffect(glow);
        
        // 글로우 효과 애니메이션 (펄스 효과)
        Timeline glowTimeline = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                Glow g = new Glow(1.2);
                g.setInput(new DropShadow(20, Color.CYAN));
                node.setEffect(g);
            }),
            new KeyFrame(Duration.millis(150), e -> {
                Glow g = new Glow(0.6);
                g.setInput(new DropShadow(15, Color.WHITE));
                node.setEffect(g);
            }),
            new KeyFrame(Duration.millis(300), e -> {
                Glow g = new Glow(0.3);
                g.setInput(new DropShadow(10, Color.WHITE));
                node.setEffect(g);
            }),
            new KeyFrame(Duration.millis(450), e -> node.setEffect(null))
        );
        
        SequentialTransition sequentialTransition = new SequentialTransition();
        sequentialTransition.getChildren().addAll(scaleTransition, bounceTransition);
        
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(sequentialTransition, glowTimeline);
        
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
     * 파티클 효과 생성 (돌 놓을 때 정확한 위치에만 작고 세련되게)
     */
    public static void createParticleEffect(javafx.scene.layout.Pane parent, double x, double y, Color color) {
        // 작고 세련된 파티클 효과 (돌 놓는 위치에만)
        int particleCount = 12; // 파티클 수 (적당히)
        
        for (int i = 0; i < particleCount; i++) {
            javafx.scene.shape.Circle particle = new javafx.scene.shape.Circle(2 + Math.random() * 3);
            particle.setFill(color);
            
            // 부드러운 글로우 효과
            Glow particleGlow = new Glow(0.6);
            particleGlow.setInput(new DropShadow(4, color));
            particle.setEffect(particleGlow);
            
            double angle = (360.0 / particleCount) * i + Math.random() * 20;
            double radians = Math.toRadians(angle);
            double distance = 20 + Math.random() * 30; // 타일 내부에서만 퍼짐
            
            // 파티클을 정확한 위치에 배치
            particle.setLayoutX(x);
            particle.setLayoutY(y);
            
            parent.getChildren().add(particle);
            
            // 파티클 이동 애니메이션 (짧고 가까운 거리)
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), particle);
            translateTransition.setByX(Math.cos(radians) * distance);
            translateTransition.setByY(Math.sin(radians) * distance);
            translateTransition.setInterpolator(Interpolator.EASE_OUT);
            
            // 스케일 애니메이션 (작아지면서 사라짐)
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(500), particle);
            scaleTransition.setFromX(1.0);
            scaleTransition.setFromY(1.0);
            scaleTransition.setToX(0.0);
            scaleTransition.setToY(0.0);
            
            // 파티클 페이드 아웃
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), particle);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            
            // 파티클 제거 (애니메이션 종료 후)
            fadeTransition.setOnFinished(e -> {
                if (parent.getChildren().contains(particle)) {
                    parent.getChildren().remove(particle);
                }
            });
            
            ParallelTransition parallelTransition = new ParallelTransition();
            parallelTransition.getChildren().addAll(translateTransition, scaleTransition, fadeTransition);
            parallelTransition.play();
        }
        
        // 타일 내부에만 제한된 파동 효과 추가 (화면 전체가 아닌)
        createRippleEffect(parent, x, y, color);
    }
    
    /**
     * 타일 내부에만 제한된 파동 효과 (돌 놓을 때)
     */
    private static void createRippleEffect(javafx.scene.layout.Pane parent, double centerX, double centerY, Color color) {
        // 작은 파동 원 생성 (타일 크기에 맞게)
        for (int i = 0; i < 3; i++) {
            javafx.scene.shape.Circle ripple = new javafx.scene.shape.Circle(0);
            ripple.setFill(Color.TRANSPARENT);
            ripple.setStroke(color);
            ripple.setStrokeWidth(3);
            
            // 글로우 효과
            Glow rippleGlow = new Glow(0.8);
            rippleGlow.setInput(new DropShadow(10, color));
            ripple.setEffect(rippleGlow);
            
            ripple.setLayoutX(centerX);
            ripple.setLayoutY(centerY);
            
            parent.getChildren().add(ripple);
            
            // 파동이 퍼지는 애니메이션 (타일 크기 내에서만)
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(600 + i * 100), ripple);
            scaleTransition.setFromX(0.0);
            scaleTransition.setFromY(0.0);
            scaleTransition.setToX(2.5); // 타일 크기의 약 1/4 정도로 제한
            scaleTransition.setToY(2.5);
            scaleTransition.setInterpolator(Interpolator.EASE_OUT);
            
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(600 + i * 100), ripple);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            
            fadeTransition.setOnFinished(e -> parent.getChildren().remove(ripple));
            
            ParallelTransition parallelTransition = new ParallelTransition();
            parallelTransition.getChildren().addAll(scaleTransition, fadeTransition);
            parallelTransition.setDelay(Duration.millis(i * 80));
            parallelTransition.play();
        }
    }
    
    /**
     * 미니게임 성공 시 화면 전체 애니메이션 효과 (강화된 버전)
     */
    public static void createMinigameSuccessEffect(javafx.scene.layout.Pane parent) {
        double centerX = parent.getWidth() / 2;
        double centerY = parent.getHeight() / 2;
        
        // 1. 화면 중앙에 큰 성공 원 (여러 개)
        for (int i = 0; i < 8; i++) {
            javafx.scene.shape.Circle successCircle = new javafx.scene.shape.Circle(0);
            successCircle.setFill(Color.TRANSPARENT);
            Color circleColor = i % 2 == 0 ? Color.LIME : Color.YELLOW;
            successCircle.setStroke(circleColor);
            successCircle.setStrokeWidth(6);
            
            // 강한 글로우 효과
            Glow successGlow = new Glow(2.0);
            successGlow.setInput(new DropShadow(40, circleColor));
            successCircle.setEffect(successGlow);
            
            successCircle.setLayoutX(centerX);
            successCircle.setLayoutY(centerY);
            
            parent.getChildren().add(successCircle);
            
            // 원이 커지면서 페이드 아웃
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1500 + i * 100), successCircle);
            scaleTransition.setFromX(0.0);
            scaleTransition.setFromY(0.0);
            scaleTransition.setToX(25.0);
            scaleTransition.setToY(25.0);
            scaleTransition.setInterpolator(Interpolator.EASE_OUT);
            
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(1500 + i * 100), successCircle);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            
            fadeTransition.setOnFinished(e -> parent.getChildren().remove(successCircle));
            
            ParallelTransition parallelTransition = new ParallelTransition();
            parallelTransition.getChildren().addAll(scaleTransition, fadeTransition);
            parallelTransition.setDelay(Duration.millis(i * 50));
            parallelTransition.play();
        }
        
        // 2. 대량의 파티클 효과 (화면 전체)
        for (int i = 0; i < 100; i++) {
            javafx.scene.shape.Circle particle = new javafx.scene.shape.Circle(4 + Math.random() * 6);
            Color particleColor = Math.random() > 0.5 ? Color.LIME : Color.YELLOW;
            particle.setFill(particleColor);
            
            Glow particleGlow = new Glow(1.5);
            particleGlow.setInput(new DropShadow(15, particleColor));
            particle.setEffect(particleGlow);
            
            double angle = (360.0 / 100) * i + Math.random() * 20;
            double radians = Math.toRadians(angle);
            double distance = 300 + Math.random() * 400; // 화면 전체로 퍼짐
            
            particle.setLayoutX(centerX);
            particle.setLayoutY(centerY);
            
            parent.getChildren().add(particle);
            
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(2000), particle);
            translateTransition.setByX(Math.cos(radians) * distance);
            translateTransition.setByY(Math.sin(radians) * distance);
            translateTransition.setInterpolator(Interpolator.EASE_OUT);
            
            ScaleTransition particleScale = new ScaleTransition(Duration.millis(2000), particle);
            particleScale.setFromX(1.0);
            particleScale.setFromY(1.0);
            particleScale.setToX(0.0);
            particleScale.setToY(0.0);
            
            FadeTransition particleFade = new FadeTransition(Duration.millis(2000), particle);
            particleFade.setFromValue(1.0);
            particleFade.setToValue(0.0);
            
            particleFade.setOnFinished(e -> parent.getChildren().remove(particle));
            
            ParallelTransition particleTransition = new ParallelTransition();
            particleTransition.getChildren().addAll(translateTransition, particleScale, particleFade);
            particleTransition.setDelay(Duration.millis(Math.random() * 300));
            particleTransition.play();
        }
        
        // 3. 별 모양 파티클 추가
        for (int i = 0; i < 20; i++) {
            javafx.scene.shape.Polygon star = createStar(8 + Math.random() * 8);
            star.setFill(Color.YELLOW);
            
            Glow starGlow = new Glow(1.8);
            starGlow.setInput(new DropShadow(20, Color.YELLOW));
            star.setEffect(starGlow);
            
            double angle = (360.0 / 20) * i;
            double radians = Math.toRadians(angle);
            double distance = 150 + Math.random() * 200;
            
            star.setLayoutX(centerX);
            star.setLayoutY(centerY);
            
            parent.getChildren().add(star);
            
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(1800), star);
            translateTransition.setByX(Math.cos(radians) * distance);
            translateTransition.setByY(Math.sin(radians) * distance);
            translateTransition.setInterpolator(Interpolator.EASE_OUT);
            
            RotateTransition rotateTransition = new RotateTransition(Duration.millis(1800), star);
            rotateTransition.setByAngle(360);
            
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(1800), star);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            
            fadeTransition.setOnFinished(e -> parent.getChildren().remove(star));
            
            ParallelTransition starTransition = new ParallelTransition();
            starTransition.getChildren().addAll(translateTransition, rotateTransition, fadeTransition);
            starTransition.setDelay(Duration.millis(Math.random() * 200));
            starTransition.play();
        }
    }
    
    /**
     * 별 모양 생성
     */
    private static javafx.scene.shape.Polygon createStar(double radius) {
        javafx.scene.shape.Polygon star = new javafx.scene.shape.Polygon();
        int points = 10;
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 * i / points - Math.PI / 2;
            double r = (i % 2 == 0) ? radius : radius * 0.5;
            star.getPoints().add(r * Math.cos(angle));
            star.getPoints().add(r * Math.sin(angle));
        }
        return star;
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

