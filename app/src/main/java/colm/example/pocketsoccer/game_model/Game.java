package colm.example.pocketsoccer.game_model;

import android.app.Activity;
import android.os.SystemClock;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import colm.example.pocketsoccer.GameView;
import colm.example.pocketsoccer.MainActivity;
import colm.example.pocketsoccer.MainMenuActivity;
import colm.example.pocketsoccer.NewGameDialog;

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
    private static final float SPEED_BLEED_COEFFICIENT = 0.992f;
    private static final float BOUNCE_BLEED_COEFFICIENT = 0.93f;
    private static final float GAME_SPEED_COEFFICIENT = 0.3f;

    private static final long SCORE_SLEEP_TIME = 2500;

    private static final String DUMP_FILE_NAME = "game_dump.obj";

    public enum Side { LEFT, RIGHT }

    private enum PlayerType { HUMAN, CPU }

    private static class Vec2 implements Serializable {

        float x;
        float y;

        public Vec2() {}

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
        float rotation;
        float radius;

        Pack(Vec2 pos, float radius) {
            this.initPos = new Vec2(pos.x, pos.y);
            this.initVel = new Vec2(0.0f, 0.0f);
            reinitPositions();
            this.radius = radius;
        }

        public Pack(Vec2 pos, Vec2 vel, float radius) {
            this.initPos = new Vec2(pos.x, pos.y);
            this.initVel = new Vec2(vel.x, vel.y);
            reinitPositions();
            this.radius = radius;
        }

        void reinitPositions() {
            this.pos = new Vec2(initPos.x, initPos.y);
            this.vel = new Vec2(initVel.x, initVel.y);
            this.rotation = 0.0f;
        }

    }

    private static class Player implements Serializable {

        Pack packs[];
        int goals;
        String name;
        int flag;
        PlayerType playerType;

        Player(Side side, String name, int flag, PlayerType playerType) {
            packs = new Pack[3];
            for (int i = 0; i < 3; i++) {
                float x = FIELD_WIDTH * 0.2f;
                if (i == 1) x *= 2.0f;
                if (side.equals(Side.RIGHT)) x = FIELD_WIDTH - x;
                float y = FIELD_HEIGHT * 0.25f * (i + 1);
                packs[i] = new Pack(new Vec2(x, y), PACK_RADIUS);
            }
            goals = 0;
            this.name = name;
            this.flag = flag;
            this.playerType = playerType;
        }
    }

    private static Game singletonGame;

    public interface GameEndListener {
        void gameFinished(String player1, String player2, int goals1, int goals2, int time);
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
            for (int i = 0; i < 3; i++) {
                allPacks[3 * p + i] = players[p].packs[i];
            }
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
        /*try {
            FileInputStream fileIn = MainActivity.mainActivity.getApplicationContext().openFileInput(DUMP_FILE_NAME);
            objectIn = new ObjectInputStream(fileIn);
            object = objectIn.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (objectIn != null) {
                try { objectIn.close(); }
                catch (IOException ignored) {}
            }
        }*/
        return (Game)object;
    }

    public static void purgeGame() {
        if (singletonGame != null) {
            singletonGame.pauseGame();
            singletonGame.clearGameView();
        }
        singletonGame = null;
    }

    @Override
    public void run() {
        while (!finished) {
            while (!running) synchronized (this) {
                try { wait(); }
                catch (InterruptedException e) { e.printStackTrace(); }
            }
            long iterationStartTime = SystemClock.elapsedRealtime();

            synchronized (this) {
                if (accumulatedGameDuration + SystemClock.elapsedRealtime() - timeOfLastResume > MAX_GAME_DURATION) {
                    finalizeGame();
                }
                if (accumulatedTurnDuration + SystemClock.elapsedRealtime() - timeOfTurnChange > TURN_TIME) {
                    changeTurn();
                }

                // update pack and ball positions
                float dt = (SystemClock.elapsedRealtime() - lastUpdateTime) * 0.001f * (1.0f + apGameSpeed * GAME_SPEED_COEFFICIENT);
                for (int i = 0; i < 7; i++) {
                    allPacks[i].pos.x += allPacks[i].vel.x * dt;
                    allPacks[i].pos.y += allPacks[i].vel.y * dt;
                    allPacks[i].vel.x *= SPEED_BLEED_COEFFICIENT;
                    allPacks[i].vel.y *= SPEED_BLEED_COEFFICIENT;
                }
                lastUpdateTime = SystemClock.elapsedRealtime();

                // collision detection and resolving
                for (int i = 0; i < 6; i++) {
                    for (int j = i + 1; j < 7; j++) {
                        float distSq = dstSq(allPacks[i].pos.x, allPacks[i].pos.y, allPacks[j].pos.x, allPacks[j].pos.y);
                        if (distSq < (allPacks[i].radius + allPacks[j].radius) * (allPacks[i].radius + allPacks[j].radius)) {
                            float x1 = allPacks[i].pos.x;
                            float y1 = allPacks[i].pos.y;
                            float x2 = allPacks[j].pos.x;
                            float y2 = allPacks[j].pos.y;

                            float inten1 = dst(0, 0, allPacks[i].vel.x, allPacks[i].vel.y);
                            float angle1 = (float)Math.PI / 2.0f;
                            if (allPacks[i].vel.x != 0) { angle1 = (float)Math.atan(allPacks[i].vel.y / allPacks[i].vel.x); }
                            else if (allPacks[i].vel.y < 0) { angle1 += Math.PI / 2.0f; }
                            if (allPacks[i].vel.x < 0) {
                                angle1 += Math.PI;
                                if (angle1 > Math.PI) angle1 -= 2 * Math.PI;
                            }
                            float inten2 = dst(0, 0, allPacks[j].vel.x, allPacks[j].vel.y);
                            float angle2 = (float)Math.PI / 2.0f;
                            if (allPacks[j].vel.x != 0) { angle2 = (float)Math.atan(allPacks[j].vel.y / allPacks[j].vel.x); }
                            else if (allPacks[j].vel.y < 0) { angle2 += Math.PI / 2.0f; }
                            if (allPacks[j].vel.x < 0) {
                                angle2 += Math.PI;
                                if (angle2 > Math.PI) angle2 -= 2 * Math.PI;
                            }

                            float centerVecX = x2 - x1;
                            float centerVecY = y2 - y1;
                            float centerVecAngl = (float)Math.PI / 2.0f;
                            if (centerVecX != 0) { centerVecAngl = (float)Math.atan(centerVecY / centerVecX); }
                            else if (centerVecY < 0) { centerVecAngl += Math.PI / 2.0f; }
                            if (centerVecX < 0) {
                                centerVecAngl += Math.PI;
                                if (centerVecAngl > Math.PI) centerVecAngl -= 2 * Math.PI;
                            }

                            float diffAngle1 = angle1 - centerVecAngl;
                            float diffAngle2 = angle2 - centerVecAngl;
                            float norm1 = Math.abs((float)Math.cos(diffAngle1) * inten1);
                            float norm2 = Math.abs((float)Math.cos(diffAngle2) * inten2);
                            float par1 = (float)Math.sin(diffAngle1) * inten1;
                            float par2 = (float)Math.sin(diffAngle2) * inten2;

                            float mass1 = allPacks[i].radius * allPacks[i].radius;
                            float mass2 = allPacks[j].radius * allPacks[j].radius;
                            float accNorm = norm1 * mass1 + norm2 * mass2;

                            norm1 = accNorm * mass1 / (mass1 + mass2) / (allPacks[i].radius * allPacks[i].radius);
                            norm2 = accNorm * mass2 / (mass1 + mass2) / (allPacks[j].radius * allPacks[j].radius);

                            allPacks[i].vel.x = (float)(-Math.cos(centerVecAngl) * norm1 + Math.cos(centerVecAngl + Math.PI / 2.0f) * par1) * BOUNCE_BLEED_COEFFICIENT;
                            allPacks[i].vel.y = (float)(-Math.sin(centerVecAngl) * norm1 + Math.sin(centerVecAngl + Math.PI / 2.0f) * par1) * BOUNCE_BLEED_COEFFICIENT;
                            allPacks[j].vel.x = (float)( Math.cos(centerVecAngl) * norm2 + Math.cos(centerVecAngl + Math.PI / 2.0f) * par2) * BOUNCE_BLEED_COEFFICIENT;
                            allPacks[j].vel.y = (float)( Math.sin(centerVecAngl) * norm2 + Math.sin(centerVecAngl + Math.PI / 2.0f) * par2) * BOUNCE_BLEED_COEFFICIENT;
                        }
                    }
                }

                float goalBottomY = (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f;
                float goalTopY = FIELD_HEIGHT - goalBottomY;
                for (int i = 0; i < 7; i++) {
                    if (allPacks[i].pos.x - allPacks[i].radius < 0) {
                        allPacks[i].vel.x = Math.abs(allPacks[i].vel.x) * BOUNCE_BLEED_COEFFICIENT;
                    }
                    if (allPacks[i].pos.x + allPacks[i].radius > FIELD_WIDTH) {
                        allPacks[i].vel.x = - Math.abs(allPacks[i].vel.x) * BOUNCE_BLEED_COEFFICIENT;
                    }
                    if (allPacks[i].pos.y - allPacks[i].radius < 0) {
                        allPacks[i].vel.y = Math.abs(allPacks[i].vel.y) * BOUNCE_BLEED_COEFFICIENT;
                    }
                    if (allPacks[i].pos.y + allPacks[i].radius > FIELD_HEIGHT) {
                        allPacks[i].vel.y = - Math.abs(allPacks[i].vel.y) * BOUNCE_BLEED_COEFFICIENT;
                    }

                    if (allPacks[i].pos.x < GOAL_WIDTH && allPacks[i].pos.y > goalBottomY - allPacks[i].radius && allPacks[i].pos.y < goalBottomY + allPacks[i].radius) {
                        if (allPacks[i].pos.y < goalBottomY) {
                            allPacks[i].vel.y = -Math.abs(allPacks[i].vel.y);
                        } else {
                            allPacks[i].vel.y = Math.abs(allPacks[i].vel.y);
                        }
                    }
                    if (allPacks[i].pos.x < GOAL_WIDTH && allPacks[i].pos.y > goalTopY - allPacks[i].radius && allPacks[i].pos.y < goalTopY + allPacks[i].radius) {
                        if (allPacks[i].pos.y < goalTopY) {
                            allPacks[i].vel.y = -Math.abs(allPacks[i].vel.y);
                        } else {
                            allPacks[i].vel.y = Math.abs(allPacks[i].vel.y);
                        }
                    }
                    if (allPacks[i].pos.x > FIELD_WIDTH - GOAL_WIDTH && allPacks[i].pos.y > goalBottomY - allPacks[i].radius && allPacks[i].pos.y < goalBottomY + allPacks[i].radius) {
                        if (allPacks[i].pos.y < goalBottomY) {
                            allPacks[i].vel.y = -Math.abs(allPacks[i].vel.y);
                        } else {
                            allPacks[i].vel.y = Math.abs(allPacks[i].vel.y);
                        }
                    }
                    if (allPacks[i].pos.x > FIELD_WIDTH - GOAL_WIDTH && allPacks[i].pos.y > goalTopY - allPacks[i].radius && allPacks[i].pos.y < goalTopY + allPacks[i].radius) {
                        if (allPacks[i].pos.y < goalTopY) {
                            allPacks[i].vel.y = -Math.abs(allPacks[i].vel.y);
                        } else {
                            allPacks[i].vel.y = Math.abs(allPacks[i].vel.y);
                        }
                    }

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
                        catch (InterruptedException e) {}
                        reinitPositions(Side.LEFT);
                    }).start();
                    goalScored = true;
                }
                if (ball.pos.x > FIELD_WIDTH - GOAL_WIDTH && ball.pos.x < FIELD_WIDTH &&
                        ball.pos.y > (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f &&
                        ball.pos.y < (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f + GOAL_HEIGHT) {
                    players[0].goals++;
                    new Thread(() -> {
                        try { Thread.sleep(SCORE_SLEEP_TIME); }
                        catch (InterruptedException e) {}
                        reinitPositions(Side.RIGHT);
                    }).start();
                    goalScored = true;
                }
            }

            if (gameView != null) {
                for (int i = 0; i < 3; i++) {
                    gameView.packPosX[i] = (int)(leftSpacing + players[0].packs[i].pos.x * gameView.effectiveWidth);
                    gameView.packPosY[i] = (int)(topSpacing + players[0].packs[i].pos.y * gameView.effectiveWidth);
                    gameView.packFlag[i] = players[0].flag;
                }
                for (int i = 0; i < 3; i++) {
                    gameView.packPosX[i + 3] = (int)(leftSpacing + players[1].packs[i].pos.x * gameView.effectiveWidth);
                    gameView.packPosY[i + 3] = (int)(topSpacing + players[1].packs[i].pos.y * gameView.effectiveWidth);
                    gameView.packFlag[i + 3] = players[1].flag;
                }

                gameView.ballPosX = (int)(leftSpacing + ball.pos.x * gameView.effectiveWidth);
                gameView.ballPosY = (int)(topSpacing + ball.pos.y * gameView.effectiveWidth);

                if (apEndGameCondition == AppPreferences.EndGameConditions.TIMEOUT) {
                    gameView.timer = (int)((MAX_GAME_DURATION - accumulatedGameDuration - SystemClock.elapsedRealtime() + timeOfLastResume) / 1000);
                } else {
                    gameView.timer = (int)((accumulatedGameDuration + SystemClock.elapsedRealtime() - timeOfLastResume) / 1000);
                }
                gameView.leftSocre = players[0].goals;
                gameView.rightScore = players[1].goals;

                gameView.initialized = true;
                gameView.invalidate();
            }

            long iterationDuration = SystemClock.elapsedRealtime() - iterationStartTime;
            try {
                long timeToSleep = ITERATION_TIME - iterationDuration;
                if (timeToSleep < 0) timeToSleep = 0;
                sleep(timeToSleep);
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
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
        gameView.turn = turn;
    }

    private void finalizeGame() {
        accumulatedGameDuration += SystemClock.elapsedRealtime() - timeOfLastResume;
        if (players[0].name.compareTo(players[1].name) > 0) {
            Player temp = players[0];
            players[0] = players[1];
            players[1] = temp;
        }
        finished = true;

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
        float inten1 = dst(0, 0, pack.vel.x, pack.vel.y);
        float angle1 = (float)Math.PI / 2.0f;
        if (pack.vel.x != 0) { angle1 = (float)Math.atan(pack.vel.y / pack.vel.x); }
        else if (pack.vel.y < 0) { angle1 += Math.PI / 2.0f; }
        if (pack.vel.x < 0) {
            angle1 += Math.PI;
            if (angle1 > Math.PI) angle1 -= 2 * Math.PI;
        }

        float centerVecX = x - pack.pos.x;
        float centerVecY = y - pack.pos.y;
        float centerVecAngl = (float)Math.PI / 2.0f;
        if (centerVecX != 0) { centerVecAngl = (float)Math.atan(centerVecY / centerVecX); }
        else if (centerVecY < 0) { centerVecAngl += Math.PI / 2.0f; }
        if (centerVecX < 0) {
            centerVecAngl += Math.PI;
            if (centerVecAngl > Math.PI) centerVecAngl -= 2 * Math.PI;
        }

        float diffAngle1 = angle1 - centerVecAngl;
        float norm1 = Math.abs((float)Math.cos(diffAngle1) * inten1);
        float par1 = (float)Math.sin(diffAngle1) * inten1;

        pack.vel.x = (float)(-Math.cos(centerVecAngl) * norm1 + Math.cos(centerVecAngl + Math.PI / 2.0f) * par1) * BOUNCE_BLEED_COEFFICIENT;
        pack.vel.y = (float)(-Math.sin(centerVecAngl) * norm1 + Math.sin(centerVecAngl + Math.PI / 2.0f) * par1) * BOUNCE_BLEED_COEFFICIENT;
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
        ObjectOutputStream objectOut = null;
        try {
            FileOutputStream fileOut = MainActivity.mainActivity.openFileOutput(DUMP_FILE_NAME, Activity.MODE_PRIVATE);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(this);
            fileOut.getFD().sync();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectOut != null) {
                try { objectOut.close(); }
                catch (IOException e) {}
            }
        }
    }

    public static Game newGame(NewGameDialog.NewGameDialogData data) {
        singletonGame = new Game(data);
        return singletonGame;
    }

    public synchronized void startMove(int x, int y) {
        clickedX = ((float)x - leftSpacing) / gameView.effectiveWidth;
        clickedY = ((float)y - topSpacing) / gameView.effectiveWidth;
        clickedPack = null;
        int p = 0;
        if (turn == Side.RIGHT) {
            p = 1;
        }
        for (int i = 0; i < 3; i++) {
            float distSq = dstSq(clickedX, clickedY, players[p].packs[i].pos.x, players[p].packs[i].pos.y);
            if (distSq < PACK_RADIUS * PACK_RADIUS) {
                clickedPack = players[p].packs[i];
                return;
            }
        }
    }

    public synchronized void endMove(int x, int y) {
        float relX = ((float)x - leftSpacing) / gameView.effectiveWidth;
        float relY = ((float)y - topSpacing) / gameView.effectiveWidth;
        if (clickedPack != null) {
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
