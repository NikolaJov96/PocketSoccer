package colm.example.pocketsoccer.game_model;

import android.os.SystemClock;

import java.io.Serializable;

public class Game extends Thread implements Serializable {

    private static final float FIELD_WIDTH = 1.0f;
    private static final float FIELD_HEIGHT = FIELD_WIDTH * 0.6f;

    private static final float GOAL_WIDTH = FIELD_WIDTH * 0.1f;
    private static final float GOAL_HEIGHT = FIELD_HEIGHT * 0.5f;

    private static final float BALL_RADIUS = FIELD_WIDTH * 0.02f;
    private static final float PACK_RADIUS = FIELD_WIDTH * 0.01f;

    private static final int ITERATION_TIME = 1000 / 60;

    private enum Side { LEFT, RIGHT }

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

        public Player(Side side) {
            packs = new Pack[3];
            for (int i = 0; i < 3; i++) {
                float x = FIELD_WIDTH * 0.2f;
                if (i == 1) x *= 2.0f;
                if (side == Side.RIGHT) x = FIELD_WIDTH - x;
                float y = FIELD_HEIGHT * 0.25f * (i + 1);
                packs[i] = new Pack(new Vec2(x, y));
            }
            goals = 0;
        }
    }

    private static Game singletonGame;

    private Player players[];
    private Pack ball;

    private long accumulatedGameDuration;
    private long timeOfLastResume;

    private boolean running;
    private boolean finished;

    private Side winner;

    private Game() {
        players = new Player[2];
        players[0] = new Player(Side.LEFT);
        players[1] = new Player(Side.RIGHT);

        ball = new Pack(new Vec2(FIELD_WIDTH / 2.0f, FIELD_HEIGHT / 2.0f));

        accumulatedGameDuration = 0;
        running = false;
        finished = false;
        winner = null;
    }

    public static Game getGame() {
        if (singletonGame == null) {
            singletonGame = new Game();
        }
        return singletonGame;
    }

    @Override
    public void run() {
        while (!finished) {
            while (!running) synchronized (this) {
                try { wait(); }
                catch (InterruptedException e) { e.printStackTrace();
                }
            }
            long iterationStartTime = SystemClock.elapsedRealtime();

            // update pack and ball positions
            // detect collisions and update speed vectors

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

            long iterationDuration = SystemClock.elapsedRealtime() - iterationStartTime;
            try { sleep(ITERATION_TIME - iterationDuration); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }

        if (winner == null) {
            // tie
        } else if (winner.equals(Side.LEFT)) {
            // winner is left player
        } else {
            // winner is right player
        }

    }

    public void resumeGame() {
        timeOfLastResume = SystemClock.elapsedRealtime();
        running = true;
    }

    public void pauseGame() {
        accumulatedGameDuration += SystemClock.elapsedRealtime() - timeOfLastResume;
        running = false;
    }

    public static Game newGame() {
        singletonGame = new Game();
        return singletonGame;
    }
}
