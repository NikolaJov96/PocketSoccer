package colm.example.pocketsoccer;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import colm.example.pocketsoccer.game_model.GameViewModel;

public class MainMenuActivity extends AppCompatActivity implements NewGameDialog.NewGameDialogListener {

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
        // continueGameButton.setVisibility(View.GONE);
        continueGameButton.setOnClickListener(v -> continueGame(false));

        newGameButton = findViewById(R.id.new_game_button);
        newGameButton.setOnClickListener(v -> continueGame(true));

        statisticsButton = findViewById(R.id.player_statistics_button);
        statisticsButton.setOnClickListener(v -> startActivity(new Intent(v.getContext(), PlayerStatisticsActivity.class)));

        settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> startActivity(new Intent(v.getContext(), SettingsActivity.class)));

    }

    private void continueGame(boolean forceNewGame) {
        if (/*no game || */forceNewGame) {
            FragmentManager fm = getSupportFragmentManager();
            NewGameDialog newGameDialog = new NewGameDialog();
            newGameDialog.show(fm, "new_game_dialog");
        }
    }

    @Override
    public void onFinishNewGameDialog(NewGameDialog.NewGameDialogData data) {
        model.newGame(data);
        startActivity(new Intent(this, GameActivity.class));
    }
}
