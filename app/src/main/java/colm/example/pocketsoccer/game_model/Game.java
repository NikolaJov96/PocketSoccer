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

        Pack(Vec2 pos) {
            this.pos = pos;
            this.vel = new Vec2(0.0f, 0.0f);
        }

        public Pack(Vec2 pos, Vec2 vel) {
            this.pos = pos;
            this.vel = vel;
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
                packs[i] = new Pack(new Vec2(x, y));
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

    private long accumulatedGameDuration;
    private long timeOfLastResume;

    private boolean running;
    private boolean finished;

    private Side winner;

    private Game(NewGameDialog.NewGameDialogData data) {
        players = new Player[2];
        players[0] = new Player(Side.LEFT, data.p1Name, data.p1Flag, (data.p1Cpu ? PlayerType.CPU : PlayerType.HUMAN));
        players[1] = new Player(Side.RIGHT, data.p2Name, data.p2Flag, (data.p2Cpu ? PlayerType.CPU : PlayerType.HUMAN));

        ball = new Pack(new Vec2(FIELD_WIDTH / 2.0f, FIELD_HEIGHT / 2.0f));

        accumulatedGameDuration = 0;
        running = false;
        finished = false;
        winner = null;

        gameView = null;

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
                // detect collisions and update speed vectors
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
