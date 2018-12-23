package colm.example.pocketsoccer;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import colm.example.pocketsoccer.game_model.Game;
import colm.example.pocketsoccer.game_model.GameViewModel;

public class GameActivity extends Activity {

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
            Game.getGame().resumeGame();
        }).start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Game.getGame().pauseGame();
        Game.getGame().clearGameView();
    }
}
