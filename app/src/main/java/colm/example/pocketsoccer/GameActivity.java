package colm.example.pocketsoccer;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import colm.example.pocketsoccer.game_model.Game;
import colm.example.pocketsoccer.game_model.GameViewModel;

public class GameActivity extends Activity implements Game.GameEndListener {

    public static final String PLAYER_1_EXTRA = "PLAYER_1_EXTRA";
    public static final String PLAYER_2_EXTRA = "PLAYER_2_EXTRA";
    public static final String PLAYER_1_GOALS = "PLAYER_1_GOALS";
    public static final String PLAYER_2_GOALS = "PLAYER_2_GOALS";
    public static final String TIME_EXTRA = "TIME_EXTRA";

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.game_graphics_view);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Game.getGame().setGameView(gameView);
            Game.getGame().resumeGame(this);
        }).start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
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
}
