package org.example.service;

import javafx.scene.media.AudioClip;
import java.net.URL;

/**
 * 사운드 효과를 관리하는 클래스
 */
public class SoundService {
    private static SoundService instance;
    private AudioClip placeSound;
    private AudioClip flipSound;
    private AudioClip gameOverSound;
    private boolean soundEnabled = true;
    
    private SoundService() {
        loadSounds();
    }
    
    public static SoundService getInstance() {
        if (instance == null) {
            instance = new SoundService();
        }
        return instance;
    }
    
    /**
     * 사운드 파일 로드
     * 실제 사운드 파일이 없으면 무음으로 처리
     */
    private void loadSounds() {
        try {
            // 기본 사운드 생성 (실제 파일이 없으면 무음)
            placeSound = createPlaceSound();
            flipSound = createFlipSound();
            gameOverSound = createGameOverSound();
        } catch (Exception e) {
            System.err.println("사운드 로드 실패: " + e.getMessage());
        }
    }
    
    /**
     * 돌 놓기 사운드 생성 (프로그래밍 방식으로 생성)
     */
    private AudioClip createPlaceSound() {
        // 간단한 비프음 생성 (실제 파일이 있으면 그것을 사용)
        try {
            // 실제 사운드 파일이 있다면
            URL soundUrl = getClass().getResource("/sounds/place.wav");
            if (soundUrl != null) {
                return new AudioClip(soundUrl.toExternalForm());
            }
        } catch (Exception e) {
            // 파일이 없으면 무음
        }
        return null; // 사운드 파일이 없으면 null 반환
    }
    
    /**
     * 돌 뒤집기 사운드 생성
     */
    private AudioClip createFlipSound() {
        try {
            URL soundUrl = getClass().getResource("/sounds/flip.wav");
            if (soundUrl != null) {
                return new AudioClip(soundUrl.toExternalForm());
            }
        } catch (Exception e) {}
        return null;
    }
    
    /**
     * 게임 종료 사운드 생성
     */
    private AudioClip createGameOverSound() {
        try {
            URL soundUrl = getClass().getResource("/sounds/gameover.wav");
            if (soundUrl != null) {
                return new AudioClip(soundUrl.toExternalForm());
            }
        } catch (Exception e) {}
        return null;
    }
    
    /**
     * 돌 놓기 사운드 재생
     */
    public void playPlaceSound() {
        if (soundEnabled && placeSound != null) {
            placeSound.play();
        }
    }
    
    /**
     * 돌 뒤집기 사운드 재생
     */
    public void playFlipSound() {
        if (soundEnabled && flipSound != null) {
            flipSound.play();
        }
    }
    
    /**
     * 게임 종료 사운드 재생
     */
    public void playGameOverSound() {
        if (soundEnabled && gameOverSound != null) {
            gameOverSound.play();
        }
    }
    
    /**
     * 사운드 활성화/비활성화
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}

