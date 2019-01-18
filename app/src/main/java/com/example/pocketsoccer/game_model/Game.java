package com.example.pocketsoccer.game_model;

import android.app.Activity;
import android.os.SystemClock;
import android.view.animation.AccelerateInterpolator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.example.pocketsoccer.GameView;
import com.example.pocketsoccer.MainActivity;
import com.example.pocketsoccer.NewGameDialog;

public class Game extends Thread implements Serializable {

    private static final float FIELD_WIDTH = 1.0f;
    private static final float FIELD_PROPORTION = 0.6F;
    private static final float FIELD_HEIGHT = FIELD_WIDTH * FIELD_PROPORTION;

    private static final float GOAL_WIDTH = FIELD_WIDTH * 0.1f;
    private static final float GOAL_HEIGHT = FIELD_HEIGHT * 0.4f;

    private static final float BALL_RADIUS = FIELD_WIDTH * 0.025f;
    private static final float PACK_RADIUS = FIELD_WIDTH * 0.05f;

    private static final int TURN_TIME = 1000 * 5;
    private static final int ITERATION_TIME = 1000 / 60;
    private static final long MAX_GAME_DURATION = 1000 * 60 * 3;
    private static final int GOALS_FOR_THE_WIN = 3;

    private static final float KICK_COEFFICIENT = 2.3f;
    private static final float SPIN_COEFFICIENT = 80.0f;
    private static final float PACK_PACK_SPIN_COEFFICIENT = SPIN_COEFFICIENT * 3.0f;
    private static final float SPEED_BLEED_COEFFICIENT = 0.992f;
    private static final float BOUNCE_BLEED_COEFFICIENT = 0.93f;
    private static final float GAME_SPEED_COEFFICIENT = 0.5f;

    private static final long SCORE_SLEEP_TIME = 2500;

    private static final String DUMP_FILE_NAME = "game_dump.obj";

    public enum Side { LEFT, RIGHT }

    public enum PlayerType { HUMAN, CPU }

    private static class Vec2 implements Serializable {

        float x;
        float y;

        Vec2(float x, float y) {
            this.x = x;
            this.y = y;
        }

    }

    private static class Pack implements Serializable {

        private Vec2 initPos;
        private Vec2 initVel;
        Vec2 pos;
        Vec2 vel;
        float rot;
        float rotVel;
        float radius;

        Pack(Vec2 pos, float radius) {
            this.initPos = new Vec2(pos.x, pos.y);
            this.initVel = new Vec2(0.0f, 0.0f);
            reinitPositions();
            this.radius = radius;
        }

        void reinitPositions() {
            this.pos = new Vec2(initPos.x, initPos.y);
            this.vel = new Vec2(initVel.x, initVel.y);
            this.rot = 0.0f;
            this.rotVel = 0.0f;
        }

    }

    private class Player implements Serializable {

        class GameBot {
            private static final long AVERAGE_THINK_TIME = 1000 * 2;

            private long move_start_time;
            private long time_to_wait;
            private boolean moveMade;
            private Side lastTurn;

            GameBot() {
                init();
            }

            void init() {
                move_start_time = SystemClock.elapsedRealtime();
                time_to_wait = AVERAGE_THINK_TIME;
                moveMade = false;
                if (side == Side.LEFT) {
                    lastTurn = Side.RIGHT;
                } else {
                    lastTurn = Side.LEFT;
                }
            }

            void stepMove() {
                if (Game.this.turn.equals(lastTurn) && lastTurn.equals(side)) {
                    // wait for time to make move and make move
                    if (SystemClock.elapsedRealtime() - move_start_time > time_to_wait && !moveMade) {
                        double rnd = Math.random();
                        // random
                        int packId = 0;
                        if (rnd < 0.333) {
                            packId = 1;
                        } else if (rnd < 0.666) {
                            packId = 2;
                        }
                        float dist1 = dst(packs[0].pos.x, packs[0].pos.y, Game.this.ball.pos.x, Game.this.ball.pos.y);
                        float dist2 = dst(packs[1].pos.x, packs[1].pos.y, Game.this.ball.pos.x, Game.this.ball.pos.y);
                        float dist3 = dst(packs[2].pos.x, packs[2].pos.y, Game.this.ball.pos.x, Game.this.ball.pos.y);
                        // closest
                        if (dist1 < dist2 && dist1 < dist3) {
                            packId = 0;
                        } else if (dist2 < dist1 && dist2 < dist3) {
                            packId = 1;
                        } else if (dist3 < dist1 && dist3 < dist2) {
                            packId = 2;
                        }
                        // closest to the left
                        if (packs[packId].pos.x > ball.pos.x && side == Side.LEFT ||
                                packs[packId].pos.x < ball.pos.x && side == Side.RIGHT) {
                            if (packs[0].pos.x < ball.pos.x && side == Side.LEFT ||
                                    packs[0].pos.x > ball.pos.x && side == Side.RIGHT
                            ) {
                                packId = 0;
                            } else if (packs[1].pos.x < ball.pos.x && side == Side.LEFT ||
                                    packs[1].pos.x > ball.pos.x && side == Side.RIGHT
                            ) {
                                packId = 1;
                            } else if (packs[2].pos.x < ball.pos.x && side == Side.LEFT ||
                                    packs[2].pos.x > ball.pos.x && side == Side.RIGHT
                            ) {
                                packId = 2;
                            }
                        }

                        boolean firstMove = false;
                        if (Math.abs(packs[packId].pos.y - ball.pos.y) < 0.02f) {
                            if (Math.random() < 0.5f) {
                                packId = 0;
                            } else {
                                packId = 2;
                            }
                            firstMove = true;
                        }

                        float force = dst(packs[packId].pos.x, packs[packId].pos.y, Game.this.ball.pos.x, Game.this.ball.pos.y );
                        float x = (packs[packId].pos.x * Game.this.gameView.effectiveWidth + Game.this.gameView.leftSpacing);
                        float y = (packs[packId].pos.y * Game.this.gameView.effectiveWidth + Game.this.gameView.topSpacing);
                        Game.this.startMove((int)x, (int)y, PlayerType.CPU);
                        float correction = PACK_RADIUS;
                        if (side == Side.LEFT) {
                            correction *= -1;
                        }
                        if (firstMove) {
                            correction *= -3;
                        }
                        float x2 = ((Game.this.ball.pos.x + correction) * Game.this.gameView.effectiveWidth + Game.this.gameView.leftSpacing);
                        float y2 = (Game.this.ball.pos.y * Game.this.gameView.effectiveWidth + Game.this.gameView.topSpacing);

                        x2 = x2 + (x2 - x) / (0.7f + 0.3f * force);
                        y2 = y2 + (y2 - y) / (0.7f + 0.3f * force);
                        Game.this.endMove((int)x2, (int)y2);
                        moveMade = true;
                    }
                } else if (Game.this.turn.equals(side) && !lastTurn.equals(side)) {
                    // detect move beginning
                    move_start_time = SystemClock.elapsedRealtime();
                    time_to_wait = (long)(AVERAGE_THINK_TIME * (0.75 + 0.25 * Math.random()) * (0.2 + 0.8 / (1.0 + Game.this.apGameSpeed)));
                    moveMade = false;
                }
                lastTurn = Game.this.turn;
            }
        }

        Pack packs[];
        int goals;
        String name;
        int flag;
        Side side;
        PlayerType playerType;
        private GameBot gameBot;

        Player(Side side, String name, int flag, PlayerType playerType) {
            packs = new Pack[3];
            for (int i = 0; i < 3; i++) {
                float x = FIELD_WIDTH * 0.2f;
                if (i == 1) x *= 2.0f;
                if (side.equals(Side.RIGHT)) x = FIELD_WIDTH - x;
                float y = 0.0f;
                switch (i) {
                    case 0:
                        y = FIELD_HEIGHT * 0.21f;
                        break;
                    case 1:
                        y = FIELD_HEIGHT * 0.5f;
                        break;
                    case 2:
                        y = FIELD_HEIGHT * 0.79f;
                        break;
                }
                packs[i] = new Pack(new Vec2(x, y), PACK_RADIUS);
            }
            goals = 0;
            this.name = name;
            this.flag = flag;
            this.side = side;
            this.playerType = playerType;
            if (playerType == PlayerType.CPU) {
                gameBot = new GameBot();
            } else {
                gameBot = null;
            }
        }

        void stepMove() {
            if (playerType == PlayerType.CPU) {
                gameBot.stepMove();
            }
        }
    }

    private static Game singletonGame;

    public interface GameEndListener {
        void gameFinished(String player1, String player2, int goals1, int goals2, int time);
        void goalScored();
        void ballKicked();
        void redraw();
    }
    private GameEndListener gameEndListener;

    private int apGameSpeed;
    private AppPreferences.EndGameConditions apEndGameCondition;

    private GameView gameView;
    private int leftSpacing;
    private int topSpacing;

    private Player players[];
    private Pack ball;
    private Pack allPacks[];

    private long accumulatedGameDuration;
    private long accumulatedTurnDuration;
    private long timeOfLastResume;
    private long timeOfTurnChange;
    private long lastUpdateTime;

    private boolean running;
    private boolean finished;

    private Side turn;

    private Pack clickedPack;
    private float clickedX;
    private float clickedY;
    private boolean goalScored;

    private Game(NewGameDialog.NewGameDialogData data) {
        gameEndListener = null;
        AppPreferences ap = AppPreferences.getAppPreferences();
        apGameSpeed = ap.getGameSpeed();
        apEndGameCondition = ap.getEndGameCondition();

        players = new Player[2];
        players[0] = new Player(Side.LEFT, data.p1Name, data.p1Flag, (data.p1Cpu ? PlayerType.CPU : PlayerType.HUMAN));
        players[1] = new Player(Side.RIGHT, data.p2Name, data.p2Flag, (data.p2Cpu ? PlayerType.CPU : PlayerType.HUMAN));

        ball = new Pack(new Vec2(FIELD_WIDTH / 2.0f, FIELD_HEIGHT / 2.0f), BALL_RADIUS);

        allPacks = new Pack[7];
        for (int p = 0; p < 2; p++) {
            System.arraycopy(players[p].packs, 0, allPacks, 3 * p, 3);
        }
        allPacks[6] = ball;

        accumulatedGameDuration = 0;
        accumulatedTurnDuration = 0;
        lastUpdateTime = SystemClock.elapsedRealtime();
        running = false;
        finished = false;

        turn = Side.LEFT;

        gameView = null;

        clickedPack = null;
        goalScored = false;

        start();
    }

    public static Game getGame() {
        if (singletonGame != null) {
            return singletonGame;
        }
        ObjectInputStream objectIn = null;
        Object object = null;
        try {
            FileInputStream fileIn = MainActivity.mainActivity.getApplicationContext().openFileInput(DUMP_FILE_NAME);
            objectIn = new ObjectInputStream(fileIn);
            object = objectIn.readObject();
        }
        catch (Exception ignored) {}
        finally {
            if (objectIn != null) {
                try { objectIn.close(); }
                catch (IOException ignored) {}
            }
        }
        singletonGame = (Game)object;
        if (singletonGame != null) {
            singletonGame.start();
        }
        return singletonGame;
    }

    static void purgeGame() {
        if (singletonGame != null) {
            singletonGame.pauseGame();
            singletonGame.clearGameView();
        }
        singletonGame = null;
        try {
            MainActivity.mainActivity.getFileStreamPath(DUMP_FILE_NAME).delete();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
        try {
            while (!finished) {
                while (!running) synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                long iterationStartTime = SystemClock.elapsedRealtime();

                synchronized (this) {
                    // check timeouts
                    if (accumulatedGameDuration + SystemClock.elapsedRealtime() - timeOfLastResume > MAX_GAME_DURATION) {
                        finalizeGame();
                    }
                    if (accumulatedTurnDuration + SystemClock.elapsedRealtime() - timeOfTurnChange > TURN_TIME) {
                        changeTurn();
                    }

                    // step bots
                    players[0].stepMove();
                    players[1].stepMove();

                    // update pack and ball positions
                    float dt = (SystemClock.elapsedRealtime() - lastUpdateTime) * 0.001f * (1.0f + apGameSpeed * GAME_SPEED_COEFFICIENT);
                    for (int i = 0; i < 7; i++) {
                        allPacks[i].pos.x += allPacks[i].vel.x * dt;
                        allPacks[i].pos.y += allPacks[i].vel.y * dt;
                        allPacks[i].rot += allPacks[i].rotVel * dt;
                        allPacks[i].vel.x *= SPEED_BLEED_COEFFICIENT;
                        allPacks[i].vel.y *= SPEED_BLEED_COEFFICIENT;
                        allPacks[i].rotVel *= SPEED_BLEED_COEFFICIENT;
                    }
                    lastUpdateTime = SystemClock.elapsedRealtime();

                    // collision detection and resolving
                    // packs vs packs
                    for (int i = 0; i < 6; i++) {
                        for (int j = i + 1; j < 7; j++) {
                            float distSq = dstSq(allPacks[i].pos.x, allPacks[i].pos.y, allPacks[j].pos.x, allPacks[j].pos.y);
                            if (distSq < (allPacks[i].radius + allPacks[j].radius) * (allPacks[i].radius + allPacks[j].radius)) {
                                float x1 = allPacks[i].pos.x;
                                float y1 = allPacks[i].pos.y;
                                float x2 = allPacks[j].pos.x;
                                float y2 = allPacks[j].pos.y;

                                float intensity1 = dst(0, 0, allPacks[i].vel.x, allPacks[i].vel.y);
                                float angle1 = (float) Math.PI / 2.0f;
                                if (allPacks[i].vel.x != 0) {
                                    angle1 = (float) Math.atan(allPacks[i].vel.y / allPacks[i].vel.x);
                                } else if (allPacks[i].vel.y < 0) {
                                    angle1 += Math.PI / 2.0f;
                                }
                                if (allPacks[i].vel.x < 0) {
                                    angle1 += Math.PI;
                                    if (angle1 > Math.PI) angle1 -= 2 * Math.PI;
                                }
                                float intensity2 = dst(0, 0, allPacks[j].vel.x, allPacks[j].vel.y);
                                float angle2 = (float) Math.PI / 2.0f;
                                if (allPacks[j].vel.x != 0) {
                                    angle2 = (float) Math.atan(allPacks[j].vel.y / allPacks[j].vel.x);
                                } else if (allPacks[j].vel.y < 0) {
                                    angle2 += Math.PI / 2.0f;
                                }
                                if (allPacks[j].vel.x < 0) {
                                    angle2 += Math.PI;
                                    if (angle2 > Math.PI) angle2 -= 2 * Math.PI;
                                }

                                float centerVecX = x2 - x1;
                                float centerVecY = y2 - y1;
                                float centerVecAngle = (float) Math.PI / 2.0f;
                                if (centerVecX != 0) {
                                    centerVecAngle = (float) Math.atan(centerVecY / centerVecX);
                                } else if (centerVecY < 0) {
                                    centerVecAngle += Math.PI / 2.0f;
                                }
                                if (centerVecX < 0) {
                                    centerVecAngle += Math.PI;
                                    if (centerVecAngle > Math.PI) centerVecAngle -= 2 * Math.PI;
                                }

                                float diffAngle1 = angle1 - centerVecAngle;
                                float diffAngle2 = angle2 - centerVecAngle;
                                float norm1 = Math.abs((float) Math.cos(diffAngle1) * intensity1);
                                float norm2 = Math.abs((float) Math.cos(diffAngle2) * intensity2);
                                float par1 = (float) Math.sin(diffAngle1) * intensity1;
                                float par2 = (float) Math.sin(diffAngle2) * intensity2;

                                float mass1 = allPacks[i].radius * allPacks[i].radius;
                                float mass2 = allPacks[j].radius * allPacks[j].radius;
                                float accNorm = norm1 * mass1 + norm2 * mass2;

                                norm1 = accNorm * mass1 / (mass1 + mass2) / (allPacks[i].radius * allPacks[i].radius);
                                norm2 = accNorm * mass2 / (mass1 + mass2) / (allPacks[j].radius * allPacks[j].radius);

                                allPacks[i].vel.x = (float) (-Math.cos(centerVecAngle) * norm1 + Math.cos(centerVecAngle + Math.PI / 2.0f) * par1) * BOUNCE_BLEED_COEFFICIENT;
                                allPacks[i].vel.y = (float) (-Math.sin(centerVecAngle) * norm1 + Math.sin(centerVecAngle + Math.PI / 2.0f) * par1) * BOUNCE_BLEED_COEFFICIENT;
                                allPacks[i].rotVel += (par1 - par2) / 2.0 * PACK_PACK_SPIN_COEFFICIENT;
                                allPacks[j].vel.x = (float) (Math.cos(centerVecAngle) * norm2 + Math.cos(centerVecAngle + Math.PI / 2.0f) * par2) * BOUNCE_BLEED_COEFFICIENT;
                                allPacks[j].vel.y = (float) (Math.sin(centerVecAngle) * norm2 + Math.sin(centerVecAngle + Math.PI / 2.0f) * par2) * BOUNCE_BLEED_COEFFICIENT;
                                allPacks[j].rotVel -= (par1 - par2) / 2.0 * PACK_PACK_SPIN_COEFFICIENT;

                                if (j == 6) {
                                    gameEndListener.ballKicked();
                                }
                            }
                        }
                    }

                    float goalBottomY = (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f;
                    float goalTopY = FIELD_HEIGHT - goalBottomY;
                    for (int i = 0; i < 7; i++) {
                        // packs vs field edges
                        if (allPacks[i].pos.x - allPacks[i].radius < 0) {
                            allPacks[i].vel.x = Math.abs(allPacks[i].vel.x) * BOUNCE_BLEED_COEFFICIENT;
                            allPacks[i].rotVel += allPacks[i].vel.y * SPIN_COEFFICIENT;
                        }
                        if (allPacks[i].pos.x + allPacks[i].radius > FIELD_WIDTH) {
                            allPacks[i].vel.x = -Math.abs(allPacks[i].vel.x) * BOUNCE_BLEED_COEFFICIENT;
                            allPacks[i].rotVel -= allPacks[i].vel.y * SPIN_COEFFICIENT;
                        }
                        if (allPacks[i].pos.y - allPacks[i].radius < 0) {
                            allPacks[i].vel.y = Math.abs(allPacks[i].vel.y) * BOUNCE_BLEED_COEFFICIENT;
                            allPacks[i].rotVel -= allPacks[i].vel.x * SPIN_COEFFICIENT;
                        }
                        if (allPacks[i].pos.y + allPacks[i].radius > FIELD_HEIGHT) {
                            allPacks[i].vel.y = -Math.abs(allPacks[i].vel.y) * BOUNCE_BLEED_COEFFICIENT;
                            allPacks[i].rotVel += allPacks[i].vel.x * SPIN_COEFFICIENT;
                        }

                        // packs vs goal edges
                        if (allPacks[i].pos.x < GOAL_WIDTH && allPacks[i].pos.y > goalBottomY - allPacks[i].radius && allPacks[i].pos.y < goalBottomY + allPacks[i].radius) {
                            if (allPacks[i].pos.y < goalBottomY) {
                                allPacks[i].vel.y = -Math.abs(allPacks[i].vel.y);
                                allPacks[i].rotVel += allPacks[i].vel.x * SPIN_COEFFICIENT;
                            } else {
                                allPacks[i].vel.y = Math.abs(allPacks[i].vel.y);
                                allPacks[i].rotVel -= allPacks[i].vel.x * SPIN_COEFFICIENT;
                            }
                        }
                        if (allPacks[i].pos.x < GOAL_WIDTH && allPacks[i].pos.y > goalTopY - allPacks[i].radius && allPacks[i].pos.y < goalTopY + allPacks[i].radius) {
                            if (allPacks[i].pos.y < goalTopY) {
                                allPacks[i].vel.y = -Math.abs(allPacks[i].vel.y);
                                allPacks[i].rotVel += allPacks[i].vel.x * SPIN_COEFFICIENT;
                            } else {
                                allPacks[i].vel.y = Math.abs(allPacks[i].vel.y);
                                allPacks[i].rotVel -= allPacks[i].vel.x * SPIN_COEFFICIENT;
                            }
                        }
                        if (allPacks[i].pos.x > FIELD_WIDTH - GOAL_WIDTH && allPacks[i].pos.y > goalBottomY - allPacks[i].radius && allPacks[i].pos.y < goalBottomY + allPacks[i].radius) {
                            if (allPacks[i].pos.y < goalBottomY) {
                                allPacks[i].vel.y = -Math.abs(allPacks[i].vel.y);
                                allPacks[i].rotVel += allPacks[i].vel.x * SPIN_COEFFICIENT;
                            } else {
                                allPacks[i].vel.y = Math.abs(allPacks[i].vel.y);
                                allPacks[i].rotVel -= allPacks[i].vel.x * SPIN_COEFFICIENT;
                            }
                        }
                        if (allPacks[i].pos.x > FIELD_WIDTH - GOAL_WIDTH && allPacks[i].pos.y > goalTopY - allPacks[i].radius && allPacks[i].pos.y < goalTopY + allPacks[i].radius) {
                            if (allPacks[i].pos.y < goalTopY) {
                                allPacks[i].vel.y = -Math.abs(allPacks[i].vel.y);
                                allPacks[i].rotVel += allPacks[i].vel.x * SPIN_COEFFICIENT;
                            } else {
                                allPacks[i].vel.y = Math.abs(allPacks[i].vel.y);
                                allPacks[i].rotVel -= allPacks[i].vel.x * SPIN_COEFFICIENT;
                            }
                        }

                        // packs vs goal post
                        if (dstSq(allPacks[i].pos.x, allPacks[i].pos.y, GOAL_WIDTH, goalBottomY) < allPacks[i].radius * allPacks[i].radius) {
                            bounce(allPacks[i], GOAL_WIDTH, goalBottomY);
                        } else if (dstSq(allPacks[i].pos.x, allPacks[i].pos.y, GOAL_WIDTH, goalTopY) < allPacks[i].radius * allPacks[i].radius) {
                            bounce(allPacks[i], GOAL_WIDTH, goalTopY);
                        } else if (dstSq(allPacks[i].pos.x, allPacks[i].pos.y, FIELD_WIDTH - GOAL_WIDTH, goalBottomY) < allPacks[i].radius * allPacks[i].radius) {
                            bounce(allPacks[i], FIELD_WIDTH - GOAL_WIDTH, goalBottomY);
                        } else if (dstSq(allPacks[i].pos.x, allPacks[i].pos.y, FIELD_WIDTH - GOAL_WIDTH, goalTopY) < allPacks[i].radius * allPacks[i].radius) {
                            bounce(allPacks[i], FIELD_WIDTH - GOAL_WIDTH, goalTopY);
                        }
                    }
                }

                if (!goalScored) {
                    if (ball.pos.x > 0 && ball.pos.x < GOAL_WIDTH &&
                            ball.pos.y > (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f &&
                            ball.pos.y < (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f + GOAL_HEIGHT) {
                        players[1].goals++;
                        new Thread(() -> {
                            try { Thread.sleep(SCORE_SLEEP_TIME); }
                            catch (InterruptedException ignored) {}
                            reinitPositions(Side.LEFT);
                        }).start();
                        goalScored = true;
                        gameEndListener.goalScored();
                    }
                    if (ball.pos.x > FIELD_WIDTH - GOAL_WIDTH && ball.pos.x < FIELD_WIDTH &&
                            ball.pos.y > (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f &&
                            ball.pos.y < (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f + GOAL_HEIGHT) {
                        players[0].goals++;
                        new Thread(() -> {
                            try { Thread.sleep(SCORE_SLEEP_TIME); }
                            catch (InterruptedException ignored) {}
                            reinitPositions(Side.RIGHT);
                        }).start();
                        goalScored = true;
                        gameEndListener.goalScored();
                    }
                }

                if (gameView != null) {
                    for (int i = 0; i < 3; i++) {
                        gameView.packPosX[i] = (int) (leftSpacing + players[0].packs[i].pos.x * gameView.effectiveWidth);
                        gameView.packPosY[i] = (int) (topSpacing + players[0].packs[i].pos.y * gameView.effectiveWidth);
                        gameView.packRot[i] = (int)players[0].packs[i].rot;
                        gameView.packFlag[i] = players[0].flag;
                    }
                    for (int i = 0; i < 3; i++) {
                        gameView.packPosX[i + 3] = (int) (leftSpacing + players[1].packs[i].pos.x * gameView.effectiveWidth);
                        gameView.packPosY[i + 3] = (int) (topSpacing + players[1].packs[i].pos.y * gameView.effectiveWidth);
                        gameView.packRot[i + 3] = (int)players[1].packs[i].rot;
                        gameView.packFlag[i + 3] = players[1].flag;
                    }

                    gameView.ballPosX = (int)(leftSpacing + ball.pos.x * gameView.effectiveWidth);
                    gameView.ballPosY = (int)(topSpacing + ball.pos.y * gameView.effectiveWidth);
                    gameView.ballRot = (int)ball.rot;

                    if (apEndGameCondition == AppPreferences.EndGameConditions.TIMEOUT) {
                        gameView.timer = (int) ((MAX_GAME_DURATION - accumulatedGameDuration - SystemClock.elapsedRealtime() + timeOfLastResume) / 1000);
                    } else {
                        gameView.timer = (int) ((accumulatedGameDuration + SystemClock.elapsedRealtime() - timeOfLastResume) / 1000);
                    }
                    gameView.leftSocre = players[0].goals;
                    gameView.rightScore = players[1].goals;

                    gameView.initialized = true;
                    gameEndListener.redraw();
                }

                long iterationDuration = SystemClock.elapsedRealtime() - iterationStartTime;
                try {
                    long timeToSleep = ITERATION_TIME - iterationDuration;
                    if (timeToSleep < 0) timeToSleep = 0;
                    sleep(timeToSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {}
        finalizeGame();
    }

    private void changeTurn() {
        if (turn == Side.LEFT) {
            changeTurn(Side.RIGHT);
        } else {
            changeTurn(Side.LEFT);
        }
    }

    private void changeTurn(Side side) {
        clickedPack = null;
        turn = side;
        timeOfTurnChange = SystemClock.elapsedRealtime();
        accumulatedTurnDuration = 0;
        if (gameView != null) {
            gameView.turn = turn;
        }
    }

    private void finalizeGame() {
        accumulatedGameDuration += SystemClock.elapsedRealtime() - timeOfLastResume;
        if (players[0].name.compareTo(players[1].name) > 0) {
            Player temp = players[0];
            players[0] = players[1];
            players[1] = temp;
        }
        running = false;
        finished = true;
        interrupt();

        try {
            MainActivity.mainActivity.getFileStreamPath(DUMP_FILE_NAME).delete();
        } catch (Exception e) { e.printStackTrace(); }

        if (gameEndListener != null) {
            gameEndListener.gameFinished(players[0].name, players[1].name, players[0].goals, players[1].goals, (int)(accumulatedGameDuration / 1000));
        }
    }

    private void reinitPositions(Side side) {
        for (Pack pack : allPacks) {
            pack.reinitPositions();
        }
        if (apEndGameCondition.equals(AppPreferences.EndGameConditions.SCORE) &&
                (players[0].goals >= GOALS_FOR_THE_WIN || players[1].goals >= GOALS_FOR_THE_WIN)) {
            finalizeGame();
        } else {
            goalScored = false;
            changeTurn(side);
        }
    }

    private void bounce(Pack pack, float x, float y) {
        float intensity1 = dst(0, 0, pack.vel.x, pack.vel.y);
        float angle1 = (float)Math.PI / 2.0f;
        if (pack.vel.x != 0) { angle1 = (float)Math.atan(pack.vel.y / pack.vel.x); }
        else if (pack.vel.y < 0) { angle1 += Math.PI / 2.0f; }
        if (pack.vel.x < 0) {
            angle1 += Math.PI;
            if (angle1 > Math.PI) angle1 -= 2 * Math.PI;
        }

        float centerVecX = x - pack.pos.x;
        float centerVecY = y - pack.pos.y;
        float centerVecAngle = (float)Math.PI / 2.0f;
        if (centerVecX != 0) { centerVecAngle = (float)Math.atan(centerVecY / centerVecX); }
        else if (centerVecY < 0) { centerVecAngle += Math.PI / 2.0f; }
        if (centerVecX < 0) {
            centerVecAngle += Math.PI;
            if (centerVecAngle > Math.PI) centerVecAngle -= 2 * Math.PI;
        }

        float diffAngle1 = angle1 - centerVecAngle;
        float norm1 = Math.abs((float)Math.cos(diffAngle1) * intensity1);
        float par1 = (float)Math.sin(diffAngle1) * intensity1;

        pack.vel.x = (float)(-Math.cos(centerVecAngle) * norm1 + Math.cos(centerVecAngle + Math.PI / 2.0f) * par1) * BOUNCE_BLEED_COEFFICIENT;
        pack.vel.y = (float)(-Math.sin(centerVecAngle) * norm1 + Math.sin(centerVecAngle + Math.PI / 2.0f) * par1) * BOUNCE_BLEED_COEFFICIENT;
        pack.rotVel -= par1 / 2.0 * PACK_PACK_SPIN_COEFFICIENT;
    }

    public synchronized void resumeGame(GameEndListener gameEndListener) {
        this.gameEndListener = gameEndListener;
        timeOfLastResume = SystemClock.elapsedRealtime();
        timeOfTurnChange = SystemClock.elapsedRealtime();
        lastUpdateTime = SystemClock.elapsedRealtime();
        running = true;
        notifyAll();
    }

    public void pauseGame() {
        gameEndListener = null;
        accumulatedGameDuration += SystemClock.elapsedRealtime() - timeOfLastResume;
        accumulatedTurnDuration += SystemClock.elapsedRealtime() - timeOfTurnChange;
        running = false;
        dumpGame();
    }

    private synchronized void dumpGame() {
        gameView = null;
        ObjectOutputStream objectOut = null;
        try {
            FileOutputStream fileOut = MainActivity.mainActivity.openFileOutput(DUMP_FILE_NAME, Activity.MODE_PRIVATE);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(this);
            fileOut.getFD().sync();
        }
        catch (IOException ignored) {}
        finally {
            if (objectOut != null) {
                try { objectOut.close(); }
                catch (IOException ignored) {}
            }
        }
    }

    static void newGame(NewGameDialog.NewGameDialogData data) {
        if (singletonGame != null) {
            try {
                MainActivity.mainActivity.getFileStreamPath(DUMP_FILE_NAME).delete();
            } catch (Exception e) { e.printStackTrace(); }
        }
        singletonGame = new Game(data);
    }

    public synchronized void startMove(int x, int y, PlayerType playerType) {
        clickedX = ((float)x - leftSpacing) / gameView.effectiveWidth;
        clickedY = ((float)y - topSpacing) / gameView.effectiveWidth;
        clickedPack = null;
        int p = 0;
        if (turn == Side.RIGHT) { p = 1; }
        if (players[p].playerType.equals(playerType)) {for (int i = 0; i < 3; i++) {
            float distSq = dstSq(clickedX, clickedY, players[p].packs[i].pos.x, players[p].packs[i].pos.y);
            if (distSq < PACK_RADIUS * PACK_RADIUS) {
                clickedPack = players[p].packs[i];
                return;
            }
        }
        }
    }

    public synchronized void endMove(int x, int y) {
        if (clickedPack != null) {
            float relX = ((float)x - leftSpacing) / gameView.effectiveWidth;
            float relY = ((float)y - topSpacing) / gameView.effectiveWidth;
            clickedPack.vel.x += (relX - clickedX) * KICK_COEFFICIENT;
            clickedPack.vel.y += (relY - clickedY) * KICK_COEFFICIENT;
            changeTurn();
        }
    }

    private float dstSq(float x1, float y1, float x2, float y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    private float dst(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt(dstSq(x1, y1, x2, y2));
    }

    public void setGameView(GameView gameView) {
        this.gameView = gameView;
        float gameViewProportion = ((float)gameView.getWidth()) / gameView.getHeight();
        if (gameViewProportion > FIELD_PROPORTION) {
            topSpacing = 0;
            leftSpacing = (int)((gameView.getWidth() - gameView.getHeight() / FIELD_PROPORTION) / 2.0f);
        } else {
            topSpacing = (int)((gameView.getHeight() - gameView.getWidth() * FIELD_PROPORTION) / 2.0f);
            leftSpacing = 0;
        }
        gameView.leftSpacing = leftSpacing;
        gameView.effectiveWidth = gameView.getWidth() - 2 * leftSpacing;
        gameView.topSpacing = topSpacing;
        gameView.effectiveHeight = gameView.getHeight() - 2 * topSpacing;
        gameView.goalHeight = (int)(GOAL_HEIGHT * gameView.effectiveWidth);
        gameView.goalWidth = (int)(GOAL_WIDTH * gameView.effectiveWidth);
        gameView.packRadius = (int)(PACK_RADIUS * gameView.effectiveWidth);
        gameView.ballRadius = (int)(BALL_RADIUS * gameView.effectiveWidth);
        gameView.turn = turn;
    }

    public void clearGameView() {
        gameView = null;
    }
}
