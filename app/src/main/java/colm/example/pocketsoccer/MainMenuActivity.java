package colm.example.pocketsoccer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends AppCompatActivity {

    private Button continueGameButton;
    private Button newGameButton;
    private Button statisticsButton;
    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

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
            // init new game inside model
        }
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

}
