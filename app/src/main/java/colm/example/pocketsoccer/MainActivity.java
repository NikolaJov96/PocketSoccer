package colm.example.pocketsoccer;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

import colm.example.pocketsoccer.game_model.GameAssetManager;
import colm.example.pocketsoccer.game_model.GameViewModel;

public class MainActivity extends AppCompatActivity implements GameAssetManager.LoaderHandler {

    private static int MAIN_MENU_REQUEST_CODE = 1;

    public static MainActivity mainActivity;

    public SeekBar seekBar;

    private GameViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        seekBar = findViewById(R.id.game_loading_seek_bar);
        seekBar.setEnabled(false);

        model = ViewModelProviders.of(this).get(GameViewModel.class);


    }

    @Override
    public void updateSeekBar(int percentage) {
        seekBar.setProgress(percentage);
    }

    @Override
    public void onLoaderFinished(boolean success) {
        if (success) {
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivityForResult(intent, MAIN_MENU_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Error loading game assets!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAIN_MENU_REQUEST_CODE) {
            finish();
        }
    }
}
