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
        continueGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueGame(false);
            }
        });

        newGameButton = findViewById(R.id.new_game_button);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueGame(true);
            }
        });

        statisticsButton = findViewById(R.id.player_statistics_button);
        statisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PlayerStatisticsActivity.class);
                startActivity(intent);
            }
        });

        settingsButton = findViewById(R.id.settings_button);

    }

    private void continueGame(boolean forceNewGame) {
        if (/*no game || */forceNewGame) {
            // init new game inside model
        }
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

}
