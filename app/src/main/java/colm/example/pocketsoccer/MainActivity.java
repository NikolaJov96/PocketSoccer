package colm.example.pocketsoccer;

import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

class AssetLoader extends AsyncTask<MainActivity, Integer, Boolean> {

    private int percentage;
    private MainActivity mainActivity;

    @Override
    protected Boolean doInBackground(MainActivity... mainActivities) {
        if (mainActivities.length != 1) {
            return false;
        }
        mainActivity = mainActivities[0];
        percentage = 0;
        for (int i = 0; i < 11; i++) {
            percentage += 10;
            publishProgress(percentage);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        mainActivity.onLoaderFinished(aBoolean);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mainActivity.updateSeekBar(values[values.length - 1]);
    }
}

public class MainActivity extends AppCompatActivity {

    private static int MAIN_MENU_REQUEST_CODE = 1;

    public SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.game_loading_seek_bar);
        seekBar.setEnabled(false);

        new AssetLoader().execute(this);
    }

    public void updateSeekBar(int percentage) {
        seekBar.setProgress(percentage);
    }

    public void onLoaderFinished(Boolean success) {
        if (success) {
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivityForResult(intent, MAIN_MENU_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Error loading game assets!", Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
