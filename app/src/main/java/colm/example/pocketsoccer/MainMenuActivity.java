package colm.example.pocketsoccer;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import colm.example.pocketsoccer.game_model.GameViewModel;

public class MainMenuActivity extends AppCompatActivity implements NewGameDialog.NewGameDialogListener {

    private static final int GAME_ACTIVITY_REQUEST_CODE = 1;

    private Button continueGameButton;
    private Button newGameButton;
    private Button statisticsButton;
    private Button settingsButton;

    private GameViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        model = ViewModelProviders.of(this).get(GameViewModel.class);

        continueGameButton = findViewById(R.id.continue_game_button);
        if (model.getGame() == null) {
            continueGameButton.setVisibility(View.GONE);
        }
        continueGameButton.setOnClickListener(v -> continueGame(false));

        newGameButton = findViewById(R.id.new_game_button);
        newGameButton.setOnClickListener(v -> continueGame(true));

        statisticsButton = findViewById(R.id.player_statistics_button);
        statisticsButton.setOnClickListener(v -> startActivity(new Intent(v.getContext(), PlayerStatisticsActivity.class)));

        settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> startActivity(new Intent(v.getContext(), SettingsActivity.class)));

    }

    private void continueGame(boolean forceNewGame) {
        if (model.getGame() == null || forceNewGame) {
            FragmentManager fm = getSupportFragmentManager();
            NewGameDialog newGameDialog = new NewGameDialog();
            newGameDialog.show(fm, "new_game_dialog");
        } else if (model.getGame() != null) {
            startActivityForResult(new Intent(this, GameActivity.class), GAME_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onFinishNewGameDialog(NewGameDialog.NewGameDialogData data) {
        model.newGame(data);
        startActivityForResult(new Intent(this, GameActivity.class), GAME_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (model.getGame() == null) {
            continueGameButton.setVisibility(View.GONE);
        } else {
            continueGameButton.setVisibility(View.VISIBLE);
        }
    }
}
