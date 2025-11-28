package org.example.minigame.games.rooster;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Rooster 미니게임 전용 Stage를 생성/표시하는 뷰.
 * 메인 GameView의 Scene/레이아웃을 변경하지 않는다.
 */
public class RoosterGameView {

    private static final double WIDTH = 600;
    private static final double HEIGHT = 600;
    private static final double PLAYER_SPEED = 240;
    private static final double ENEMY_SPEED = 120;
    private static final double BULLET_SPEED = 420;
    private static final double GAME_TIME = 45; // seconds
    private static final int TARGET_SCORE = 20;

    private final RoosterGame game;
    private Stage stage;
    private Canvas canvas;
    private Label scoreLabel;
    private Label timeLabel;
    private Label livesLabel;

    private double playerX = WIDTH / 2;
    private double playerY = HEIGHT * 0.85;
    private int score = 0;
    private int lives = 3;
    private double timeLeft = GAME_TIME;

    private final List<Entity> enemies = new ArrayList<>();
    private final List<Entity> bullets = new ArrayList<>();
    private final Random random = new Random();
    private AnimationTimer timer;
    private long lastTime = 0;
    private boolean spectator = false;

    private Image enemyImg;
    private Image playerImg;

    public RoosterGameView(RoosterGame game) {
        this.game = game;
        loadImages();
    }

    private void loadImages() {
        enemyImg = new Image(getClass().getResource("/rooster/img/enemy.png").toExternalForm());
        playerImg = new Image(getClass().getResource("/rooster/img/player.png").toExternalForm());
    }

    public void show(Stage owner) {
        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.NONE);
        stage.setTitle("Rooster Minigame");

        canvas = new Canvas(WIDTH, HEIGHT);
        scoreLabel = new Label("Score 0");
        timeLabel = new Label("Time " + (int) timeLeft);
        livesLabel = new Label("Lives " + lives);

        HBox hud = new HBox(15, scoreLabel, timeLabel, livesLabel);
        hud.setAlignment(Pos.CENTER);
        hud.setPadding(new Insets(8));

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> {
            stopTimer();
            stage.close();
            game.finishGame(false, score, (long) (GAME_TIME - timeLeft));
        });

        VBox top = new VBox(8, hud, closeBtn);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(canvas);

        Scene scene = new Scene(root, WIDTH, HEIGHT + 60);
        scene.setOnKeyPressed(e -> {
            if (spectator) return;
            switch (e.getCode()) {
                case LEFT -> playerX -= PLAYER_SPEED * 0.016;
                case RIGHT -> playerX += PLAYER_SPEED * 0.016;
                case SPACE -> fireBullet();
                default -> { }
            }
        });

        stage.setScene(scene);
        stage.show();
    }

    public void startGame() {
        spectator = false;
        startTimer();
    }

    public void startSpectator() {
        spectator = true;
        startTimer();
    }

    private void startTimer() {
        lastTime = System.nanoTime();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                update(dt);
                render();
            }
        };
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) timer.stop();
    }

    private void update(double dt) {
        if (!spectator) {
            timeLeft -= dt;
            if (timeLeft <= 0) {
                finish(false);
                return;
            }
            spawnEnemies(dt);
            updateEnemies(dt);
            updateBullets(dt);
            checkCollisions();
            clampPlayer();
            if (score >= TARGET_SCORE) {
                finish(true);
            }
        }
        updateHud();
    }

    private void spawnEnemies(double dt) {
        if (random.nextDouble() < dt * 1.0) { // roughly 1 per second
            double x = 20 + random.nextDouble() * (WIDTH - 40);
            enemies.add(new Entity(x, -20, 26, 26, ENEMY_SPEED));
        }
    }

    private void updateEnemies(double dt) {
        Iterator<Entity> it = enemies.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            e.y += e.speed * dt;
            if (e.y > HEIGHT + 30) {
                it.remove();
                lives--;
                if (lives <= 0) {
                    finish(false);
                    return;
                }
            }
        }
    }

    private void updateBullets(double dt) {
        Iterator<Entity> it = bullets.iterator();
        while (it.hasNext()) {
            Entity b = it.next();
            b.y -= BULLET_SPEED * dt;
            if (b.y < -20) it.remove();
        }
    }

    private void fireBullet() {
        if (bullets.isEmpty() || (System.nanoTime() - bullets.get(bullets.size() - 1).spawn) > 200_000_000L) {
            bullets.add(new Entity(playerX, playerY - 30, 6, 14, 0));
        }
    }

    private void checkCollisions() {
        Iterator<Entity> bIt = bullets.iterator();
        while (bIt.hasNext()) {
            Entity b = bIt.next();
            Iterator<Entity> eIt = enemies.iterator();
            while (eIt.hasNext()) {
                Entity e = eIt.next();
                if (Math.abs(b.x - e.x) < (b.w + e.w) * 0.5 &&
                    Math.abs(b.y - e.y) < (b.h + e.h) * 0.5) {
                    bIt.remove();
                    eIt.remove();
                    score += 2;
                    break;
                }
            }
        }
    }

    private void clampPlayer() {
        playerX = Math.max(20, Math.min(WIDTH - 20, playerX));
    }

    private void updateHud() {
        scoreLabel.setText("Score " + score);
        timeLabel.setText("Time " + Math.max(0, (int) timeLeft));
        livesLabel.setText("Lives " + lives);
    }

    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#0b1a2d"));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // player
        gc.drawImage(playerImg, playerX - 16, playerY - 16, 32, 32);

        // enemies
        for (Entity e : enemies) {
            gc.drawImage(enemyImg, e.x - 13, e.y - 13, 26, 26);
        }

        // bullets
        gc.setFill(Color.web("#90caf9"));
        for (Entity b : bullets) {
            gc.fillRect(b.x - 3, b.y - 7, 6, 14);
        }
    }

    private void finish(boolean success) {
        stopTimer();
        game.finishGame(success, score, (long) (GAME_TIME - timeLeft));
        stage.close();
    }

    public void close() {
        stopTimer();
        if (stage != null) stage.close();
    }

    private static class Entity {
        double x, y, w, h, speed;
        long spawn = System.nanoTime();
        Entity(double x, double y, double w, double h, double speed) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.speed = speed;
        }
    }
}
