package colm.example.pocketsoccer;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import colm.example.pocketsoccer.game_model.GameViewModel;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;

    private GameViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        model = ViewModelProviders.of(this).get(GameViewModel.class);

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
            model.getGame().setGameView(gameView);
            model.getGame().resumeGame();
        }).start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        model.getGame().pauseGame();
        model.getGame().clearGameView();
    }
}
