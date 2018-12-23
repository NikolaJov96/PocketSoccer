package colm.example.pocketsoccer.game_model;

import android.os.SystemClock;

import java.io.Serializable;

import colm.example.pocketsoccer.GameView;
import colm.example.pocketsoccer.NewGameDialog;

public class Game extends Thread implements Serializable {

    private static final float FIELD_WIDTH = 1.0f;
    private static final float FIELD_PROPORTION = 0.6F;
    private static final float FIELD_HEIGHT = FIELD_WIDTH * FIELD_PROPORTION;

    private static final float GOAL_WIDTH = FIELD_WIDTH * 0.1f;
    private static final float GOAL_HEIGHT = FIELD_HEIGHT * 0.4f;

    private static final float BALL_RADIUS = FIELD_WIDTH * 0.025f;
    private static final float PACK_RADIUS = FIELD_WIDTH * 0.05f;

    private static final int ITERATION_TIME = 1000 / 60;

    private static final float KICK_COEFFICIENT = 2.9F;
    private static final float SPEED_BLEED_COEFFICIENT = 0.975f;

    private enum Side { LEFT, RIGHT }

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

        Vec2 pos;
        Vec2 vel;
        float rotation;
        float radius;

        Pack(Vec2 pos, float radius) {
            this.pos = pos;
            this.vel = new Vec2(0.0f, 0.0f);
            this.rotation = 0.0f;
            this.radius = radius;
        }

        public Pack(Vec2 pos, Vec2 vel, float radius) {
            this.pos = pos;
            this.vel = vel;
            this.rotation = 0.0f;
            this.radius = radius;
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

    private GameView gameView;
    private int leftSpacing;
    private int topSpacing;

    private Player players[];
    private Pack ball;
    private Pack allPacks[];

    private long accumulatedGameDuration;
    private long timeOfLastResume;
    private long lastUpdateTime;

    private boolean running;
    private boolean finished;

    private Side winner;

    private Pack clickedPack;
    private float clickedX;
    private float clickedY;

    private Game(NewGameDialog.NewGameDialogData data) {
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
        lastUpdateTime = SystemClock.elapsedRealtime();
        running = false;
        finished = false;
        winner = null;

        gameView = null;

        clickedPack = null;

        allPacks[1].pos.x = allPacks[5].pos.x + 10 * PACK_RADIUS;
        allPacks[1].pos.y = allPacks[5].pos.y - 1.95f * PACK_RADIUS;
        allPacks[1].vel.x = -0.7f;

        start();
    }

    public static Game getGame() {
        return singletonGame;
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
                // update pack and ball positions
                float dt = (SystemClock.elapsedRealtime() - lastUpdateTime) * 0.001f;
                for (int i = 0; i < 7; i++) {
                    allPacks[i].pos.x += allPacks[i].vel.x * dt;
                    allPacks[i].pos.y += allPacks[i].vel.y * dt;
                    allPacks[i].vel.x *= SPEED_BLEED_COEFFICIENT;
                    allPacks[i].vel.y *= SPEED_BLEED_COEFFICIENT;
                }
                lastUpdateTime = SystemClock.elapsedRealtime();

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

                            allPacks[i].vel.x = (float)(-Math.cos(centerVecAngl) * norm1 + Math.cos(centerVecAngl + Math.PI / 2.0f) * par1);
                            allPacks[i].vel.y = (float)(-Math.sin(centerVecAngl) * norm1 + Math.sin(centerVecAngl + Math.PI / 2.0f) * par1);
                            allPacks[j].vel.x = (float)( Math.cos(centerVecAngl) * norm2 + Math.cos(centerVecAngl + Math.PI / 2.0f) * par2);
                            allPacks[j].vel.y = (float)( Math.sin(centerVecAngl) * norm2 + Math.sin(centerVecAngl + Math.PI / 2.0f) * par2);

                        }
                    }
                }
            }

            boolean scored = false;
            if (ball.pos.x > 0 && ball.pos.x < GOAL_WIDTH &&
                    ball.pos.y > (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f &&
                    ball.pos.y < (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f + GOAL_HEIGHT) {
                scored = true;
                players[1].goals++;
            }
            if (ball.pos.x > FIELD_WIDTH - GOAL_WIDTH && ball.pos.x < FIELD_WIDTH &&
                    ball.pos.y > (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f &&
                    ball.pos.y < (FIELD_HEIGHT - GOAL_HEIGHT) / 2.0f + GOAL_HEIGHT) {
                scored = true;
                players[2].goals++;
            }
            if (scored) {
                // check end game condition
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

        if (winner == null) {
            // tie
        } else if (winner.equals(Side.LEFT)) {
            // winner is left player
        } else {
            // winner is right player
        }

    }

    public synchronized void resumeGame() {
        timeOfLastResume = SystemClock.elapsedRealtime();
        running = true;
        notifyAll();
    }

    public void pauseGame() {
        accumulatedGameDuration += SystemClock.elapsedRealtime() - timeOfLastResume;
        running = false;
    }

    public static Game newGame(NewGameDialog.NewGameDialogData data) {
        singletonGame = new Game(data);
        return singletonGame;
    }

    public synchronized void startMove(int x, int y) {
        clickedX = ((float)x - leftSpacing) / gameView.effectiveWidth;
        clickedY = ((float)y - topSpacing) / gameView.effectiveWidth;
        clickedPack = null;
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 3; i++) {
                float distSq = dstSq(clickedX, clickedY, players[p].packs[i].pos.x, players[p].packs[i].pos.y);
                if (distSq < PACK_RADIUS * PACK_RADIUS) {
                    clickedPack = players[p].packs[i];
                    return;
                }
            }
        }
    }

    public synchronized void endMove(int x, int y) {
        float relX = ((float)x - leftSpacing) / gameView.effectiveWidth;
        float relY = ((float)y - topSpacing) / gameView.effectiveWidth;
        if (clickedPack != null) {
            clickedPack.vel.x += (relX - clickedX) * KICK_COEFFICIENT;
            clickedPack.vel.y += (relY - clickedY) * KICK_COEFFICIENT;
            clickedPack = null;
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
        int asd = gameView.getWidth();
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
    }

    public void clearGameView() {
        gameView = null;
    }
}
