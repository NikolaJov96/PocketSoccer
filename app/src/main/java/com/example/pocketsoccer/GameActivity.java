package com.example.pocketsoccer;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.example.pocketsoccer.game_model.Game;
import com.example.pocketsoccer.game_model.GameAssetManager;

public class GameActivity extends Activity implements Game.GameEndListener {

    public static final String PLAYER_1_EXTRA = "PLAYER_1_EXTRA";
    public static final String PLAYER_2_EXTRA = "PLAYER_2_EXTRA";
    public static final String PLAYER_1_GOALS = "PLAYER_1_GOALS";
    public static final String PLAYER_2_GOALS = "PLAYER_2_GOALS";
    public static final String TIME_EXTRA = "TIME_EXTRA";

    private GameView gameView;
    private GameAssetManager gam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.game_graphics_view);
        gam = GameAssetManager.getGameAssetManager();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(() -> {
            try { Thread.sleep(100); }
            catch (InterruptedException e) { e.printStackTrace(); }
            Game.getGame().setGameView(gameView);
            Game.getGame().resumeGame(this);
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Game.getGame().pauseGame();
        Game.getGame().clearGameView();
    }

    @Override
    public void gameFinished(String player1, String player2, int goals1, int goals2, int time) {

        Intent intent = new Intent();
        intent.putExtra(PLAYER_1_EXTRA, player1);
        intent.putExtra(PLAYER_2_EXTRA, player2);
        intent.putExtra(PLAYER_1_GOALS, goals1);
        intent.putExtra(PLAYER_2_GOALS, goals2);
        intent.putExtra(TIME_EXTRA, time);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void goalScored() {
        gam.getGoalPlayer().start();
    }

    @Override
    public void ballKicked() {
        for (int i = 0; i < GameAssetManager.NUMBER_OF_SOUNDS - 1; i++) {
            MediaPlayer mediaPlayer = gam.getKickPlayer(i);
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                break;
            }
        }
    }
}
