package colm.example.pocketsoccer;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import colm.example.pocketsoccer.game_model.AppPreferences;
import colm.example.pocketsoccer.game_model.GameViewModel;

public class SettingsActivity extends AppCompatActivity {

    private static final int NUMBER_OF_FIELDS = 3;

    private Button resetButton;
    private Button backButton;
    private ImageButton leftButton;
    private ImageView fieldView;
    private ImageButton rightButton;
    private RadioGroup endGameRadioGroup;
    private RadioButton timeoutRadio;
    private RadioButton scoreRadio;
    private SeekBar seekBar;

    private GameViewModel model;
    private AppPreferences ap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        model = ViewModelProviders.of(this).get(GameViewModel.class);
        ap = model.getAppPreferences();

        resetButton = findViewById(R.id.reset_settings_button);
        resetButton.setOnClickListener(v -> {
            model.getAppPreferences().resetPreferences();
            initGuiState();
        });

        backButton = findViewById(R.id.back_settings_button);
        backButton.setOnClickListener(v -> finish());

        leftButton = findViewById(R.id.left_field_button);
        leftButton.setOnClickListener(v -> {
            ap.setFieldId((ap.getFieldId() + NUMBER_OF_FIELDS - 1) % NUMBER_OF_FIELDS);
            updateFieldImage();
        });

        fieldView = findViewById(R.id.field_view);

        rightButton = findViewById(R.id.right_field_button);
        rightButton.setOnClickListener(v -> {
            ap.setFieldId((ap.getFieldId() + 1) % NUMBER_OF_FIELDS);
            updateFieldImage();
        });

        endGameRadioGroup = findViewById(R.id.end_game_radio_group);
        endGameRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int endGameCondition = 0;
            switch (checkedId) {
                case R.id.time_radio:
                    endGameCondition = 0;
                    break;
                case R.id.score_radio:
                    endGameCondition = 1;
                    break;
            }
            if (ap.getEndGameCondition() != endGameCondition) {
                ap.setEndGameCondition(endGameCondition);
            }
        });
        timeoutRadio = findViewById(R.id.time_radio);
        scoreRadio = findViewById(R.id.score_radio);

        seekBar = findViewById(R.id.game_speed_seek_bar);
        seekBar.setMin(0);
        seekBar.setMax(2);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AppPreferences ap = model.getAppPreferences();
                if (ap.getGameSpeed() != seekBar.getProgress()) {
                    ap.setGameSpeed(seekBar.getProgress());
                }
            }
        });

        initGuiState();
    }

    private void updateFieldImage() {
        fieldView.setImageBitmap(model.getGameAssetManager().getField(ap.getFieldId()));
    }

    private void initGuiState() {

        updateFieldImage();

        switch (ap.getEndGameCondition()) {
            case 0:
                timeoutRadio.setChecked(true);
                break;
            case 1:
                scoreRadio.setChecked(true);
                break;
        }

        seekBar.setProgress(ap.getGameSpeed());

    }

}
