package org.example.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;

/**
 * 사운드 효과를 관리하는 클래스
 */
public class SoundService {
    private static SoundService instance;
    private AudioClip placeSound;
    private AudioClip flipSound;
    private AudioClip gameOverSound;
    private AudioClip minigameWinSound;
    private AudioClip minigameLoseSound;
    private MediaPlayer minigameLosePlayer; // MP3 파일용 MediaPlayer
    private MediaPlayer bgmPlayer;
    private boolean soundEnabled = true;
    private Timeline placeSoundTimer; // 돌 놓기 사운드 타이머
    
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
            minigameWinSound = createMinigameWinSound();
            minigameLoseSound = createMinigameLoseSound();
            minigameLosePlayer = createMinigameLosePlayer(); // MP3 파일용
            bgmPlayer = createBGMPlayer();
            
            // 로드 결과 확인
            System.out.println("[사운드 로드] 돌 놓기: " + (placeSound != null ? "성공" : "실패"));
            System.out.println("[사운드 로드] 게임 종료: " + (gameOverSound != null ? "성공" : "실패"));
            System.out.println("[사운드 로드] 미니게임 승리: " + (minigameWinSound != null ? "성공" : "실패"));
            System.out.println("[사운드 로드] 미니게임 패배 (AudioClip): " + (minigameLoseSound != null ? "성공" : "실패"));
            System.out.println("[사운드 로드] 미니게임 패배 (MediaPlayer): " + (minigameLosePlayer != null ? "성공" : "실패"));
            System.out.println("[사운드 로드] BGM: " + (bgmPlayer != null ? "성공" : "실패"));
        } catch (Exception e) {
            System.err.println("사운드 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 돌 놓기 사운드 생성 (프로그래밍 방식으로 생성)
     */
    private AudioClip createPlaceSound() {
        // 여러 파일 형식 시도
        String[] extensions = {".wav", ".mp3", ".m4a"};
        for (String ext : extensions) {
            try {
                URL soundUrl = getClass().getResource("/sounds/place" + ext);
                if (soundUrl != null) {
                    try {
                        return new AudioClip(soundUrl.toExternalForm());
                    } catch (Exception e) {
                        // AudioClip이 지원하지 않는 형식일 수 있음
                        continue;
                    }
                }
            } catch (Exception e) {
                // 파일이 없거나 접근 불가
            }
        }
        return null; // 사운드 파일이 없으면 null 반환
    }
    
    /**
     * 돌 뒤집기 사운드 생성
     */
    private AudioClip createFlipSound() {
        // 여러 파일 형식 시도
        String[] extensions = {".wav", ".mp3", ".m4a"};
        for (String ext : extensions) {
            try {
                URL soundUrl = getClass().getResource("/sounds/flip" + ext);
                if (soundUrl != null) {
                    try {
                        return new AudioClip(soundUrl.toExternalForm());
                    } catch (Exception e) {
                        // AudioClip이 지원하지 않는 형식일 수 있음
                        continue;
                    }
                }
            } catch (Exception e) {
                // 파일이 없거나 접근 불가
            }
        }
        return null;
    }
    
    /**
     * 게임 종료 사운드 생성
     */
    private AudioClip createGameOverSound() {
        // 여러 파일 형식 시도
        String[] extensions = {".wav", ".mp3", ".m4a"};
        for (String ext : extensions) {
            try {
                URL soundUrl = getClass().getResource("/sounds/gameover" + ext);
                if (soundUrl != null) {
                    try {
                        return new AudioClip(soundUrl.toExternalForm());
                    } catch (Exception e) {
                        // AudioClip이 지원하지 않는 형식일 수 있음
                        continue;
                    }
                }
            } catch (Exception e) {
                // 파일이 없거나 접근 불가
            }
        }
        return null;
    }
    
    /**
     * 미니게임 승리 사운드 생성
     */
    private AudioClip createMinigameWinSound() {
        // 여러 파일 형식 시도
        String[] extensions = {".wav", ".mp3", ".m4a"};
        for (String ext : extensions) {
            try {
                URL soundUrl = getClass().getResource("/sounds/minigame_win" + ext);
                if (soundUrl != null) {
                    try {
                        return new AudioClip(soundUrl.toExternalForm());
                    } catch (Exception e) {
                        // AudioClip이 지원하지 않는 형식일 수 있음
                        System.err.println("미니게임 승리 사운드 로드 실패 (" + ext + "): " + e.getMessage());
                        // 다음 형식 시도
                        continue;
                    }
                }
            } catch (Exception e) {
                // 파일이 없거나 접근 불가
            }
        }
        return null;
    }
    
    /**
     * 미니게임 패배 사운드 생성 (WAV 파일용 AudioClip)
     */
    private AudioClip createMinigameLoseSound() {
        // WAV 파일만 시도 (MP3는 MediaPlayer 사용)
        try {
            URL soundUrl = getClass().getResource("/sounds/minigame_lose.wav");
            if (soundUrl != null) {
                try {
                    AudioClip clip = new AudioClip(soundUrl.toExternalForm());
                    clip.setVolume(1.0); // 볼륨 최대로 설정
                    System.out.println("[사운드 로드] 미니게임 패배 사운드 (WAV) 로드 성공: " + soundUrl);
                    return clip;
                } catch (Exception e) {
                    System.err.println("미니게임 패배 사운드 (WAV) 로드 실패: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("미니게임 패배 사운드 (WAV) 파일 접근 오류: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 미니게임 패배 사운드 MediaPlayer 생성 (MP3 파일용)
     */
    private MediaPlayer createMinigameLosePlayer() {
        try {
            // MP3 파일 시도
            URL soundUrl = getClass().getResource("/sounds/minigame_lose.mp3");
            if (soundUrl == null) {
                // m4a 파일 시도
                soundUrl = getClass().getResource("/sounds/minigame_lose.m4a");
            }
            if (soundUrl != null) {
                Media media = new Media(soundUrl.toExternalForm());
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(1.0); // 볼륨 최대로 설정
                
                // 재생 완료 후 자동으로 처음으로 돌아가도록 설정
                player.setOnEndOfMedia(() -> {
                    player.stop();
                    player.seek(javafx.util.Duration.ZERO);
                });
                
                // 오류 처리 (조용히 처리)
                player.setOnError(() -> {
                    // 오류 발생 시 조용히 처리 (로그 출력 안 함)
                });
                
                System.out.println("[사운드 로드] 미니게임 패배 사운드 (MP3/M4A) 로드 성공: " + soundUrl);
                return player;
            }
        } catch (Exception e) {
            System.err.println("미니게임 패배 사운드 (MP3/M4A) 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * BGM 플레이어 생성
     */
    private MediaPlayer createBGMPlayer() {
        try {
            URL bgmUrl = getClass().getResource("/sounds/bgm.wav");
            if (bgmUrl == null) {
                // wav가 없으면 mp3 시도
                bgmUrl = getClass().getResource("/sounds/bgm.mp3");
            }
            if (bgmUrl != null) {
                Media media = new Media(bgmUrl.toExternalForm());
                MediaPlayer player = new MediaPlayer(media);
                player.setCycleCount(MediaPlayer.INDEFINITE); // 무한 반복
                player.setVolume(0.5); // 볼륨 50%
                return player;
            }
        } catch (Exception e) {
            System.err.println("BGM 로드 실패: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 돌 놓기 사운드 재생 (1-2초 후 자동 중지)
     */
    public void playPlaceSound() {
        if (soundEnabled && placeSound != null) {
            placeSound.stop(); // 이전 재생 중지
            
            // 이전 타이머가 있으면 중지
            if (placeSoundTimer != null) {
                placeSoundTimer.stop();
            }
            
            placeSound.setVolume(1.0); // 볼륨 최대로 설정
            placeSound.play();
            
            // 1.5초 후 자동 중지
            placeSoundTimer = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
                if (placeSound != null) {
                    placeSound.stop();
                }
            }));
            placeSoundTimer.setCycleCount(1);
            placeSoundTimer.play();
        }
    }
    
    /**
     * 돌 뒤집기 사운드 재생 (이전 재생 중지 후 새로 재생)
     */
    public void playFlipSound() {
        if (soundEnabled && flipSound != null) {
            flipSound.stop(); // 이전 재생 중지
            flipSound.play();
        }
    }
    
    /**
     * 게임 종료 사운드 재생 (이전 재생 중지 후 새로 재생)
     */
    public void playGameOverSound() {
        if (soundEnabled && gameOverSound != null) {
            try {
                gameOverSound.stop(); // 이전 재생 중지
                gameOverSound.setVolume(1.0); // 볼륨 최대로 설정
                gameOverSound.play();
            } catch (Exception e) {
                System.err.println("게임 종료 사운드 재생 오류: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (soundEnabled) {
            System.err.println("게임 종료 사운드가 로드되지 않았습니다.");
        }
    }
    
    /**
     * 미니게임 승리 사운드 재생 (이전 재생 중지 후 새로 재생)
     */
    public void playMinigameWinSound() {
        if (soundEnabled && minigameWinSound != null) {
            try {
                minigameWinSound.stop(); // 이전 재생 중지
                minigameWinSound.setVolume(1.0); // 볼륨 최대로 설정
                minigameWinSound.play();
            } catch (Exception e) {
                System.err.println("미니게임 승리 사운드 재생 오류: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (soundEnabled) {
            System.err.println("미니게임 승리 사운드가 로드되지 않았습니다.");
        }
    }
    
    /**
     * 미니게임 패배 사운드 재생 (이전 재생 중지 후 새로 재생)
     */
    public void playMinigameLoseSound() {
        if (!soundEnabled) {
            System.err.println("사운드가 비활성화되어 있습니다.");
            return;
        }
        
        // AudioClip (WAV) 우선 시도
        if (minigameLoseSound != null) {
            try {
                minigameLoseSound.stop(); // 이전 재생 중지
                minigameLoseSound.setVolume(1.0); // 볼륨 최대로 설정
                minigameLoseSound.play();
                System.out.println("[사운드 재생] 미니게임 패배 사운드 재생 중 (AudioClip, 볼륨: 1.0)");
                return;
            } catch (Exception e) {
                System.err.println("미니게임 패배 사운드 (AudioClip) 재생 오류: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // MediaPlayer (MP3/M4A) 시도
        if (minigameLosePlayer != null) {
            try {
                MediaPlayer.Status status = minigameLosePlayer.getStatus();
                System.out.println("[사운드 재생] MediaPlayer 상태: " + status);
                
                // 재생 중이면 중지
                if (status == MediaPlayer.Status.PLAYING) {
                    minigameLosePlayer.stop();
                    // stop 후 상태가 변경될 때까지 잠시 대기
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                // 볼륨 최대로 설정
                minigameLosePlayer.setVolume(1.0);
                
                // 처음으로 되돌리기
                minigameLosePlayer.seek(javafx.util.Duration.ZERO);
                
                // 상태 확인 후 재생
                javafx.application.Platform.runLater(() -> {
                    try {
                        MediaPlayer.Status currentStatus = minigameLosePlayer.getStatus();
                        if (currentStatus == MediaPlayer.Status.READY || 
                            currentStatus == MediaPlayer.Status.STOPPED || 
                            currentStatus == MediaPlayer.Status.PAUSED) {
                            minigameLosePlayer.play();
                            System.out.println("[사운드 재생] 미니게임 패배 사운드 재생 중 (MediaPlayer, 볼륨: 1.0, 상태: " + currentStatus + ")");
                        } else {
                            // UNKNOWN이나 다른 상태면 READY가 될 때까지 대기
                            minigameLosePlayer.setOnReady(() -> {
                                minigameLosePlayer.play();
                                System.out.println("[사운드 재생] 미니게임 패배 사운드 재생 중 (MediaPlayer, READY 후 재생)");
                            });
                        }
                    } catch (Exception e) {
                        System.err.println("미니게임 패배 사운드 (MediaPlayer) 재생 오류: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                return;
            } catch (Exception e) {
                System.err.println("미니게임 패배 사운드 (MediaPlayer) 재생 오류: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.err.println("미니게임 패배 사운드가 로드되지 않았습니다. (AudioClip: " + 
                          (minigameLoseSound != null ? "있음" : "없음") + 
                          ", MediaPlayer: " + 
                          (minigameLosePlayer != null ? "있음" : "없음") + ")");
    }
    
    /**
     * BGM 시작
     */
    public void playBGM() {
        if (soundEnabled && bgmPlayer != null) {
            bgmPlayer.play();
        }
    }
    
    /**
     * BGM 중지
     */
    public void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
        }
    }
    
    /**
     * BGM 일시정지
     */
    public void pauseBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.pause();
        }
    }
    
    /**
     * BGM 재생 중인지 확인
     */
    public boolean isBGMPlaying() {
        if (bgmPlayer != null) {
            MediaPlayer.Status status = bgmPlayer.getStatus();
            return status == MediaPlayer.Status.PLAYING;
        }
        return false;
    }
    
    /**
     * 사운드 활성화/비활성화
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            stopBGM();
        } else if (bgmPlayer != null && bgmPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
            playBGM();
        }
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    /**
     * 리소스 정리
     */
    public void dispose() {
        stopBGM();
        if (bgmPlayer != null) {
            bgmPlayer.dispose();
        }
    }
}

