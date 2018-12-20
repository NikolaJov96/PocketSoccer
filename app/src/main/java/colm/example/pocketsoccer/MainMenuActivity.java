package colm.example.pocketsoccer;

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

        newGameButton = findViewById(R.id.new_game_button);

        statisticsButton = findViewById(R.id.player_statistics_button);

        settingsButton = findViewById(R.id.settings_button);

    }
}
