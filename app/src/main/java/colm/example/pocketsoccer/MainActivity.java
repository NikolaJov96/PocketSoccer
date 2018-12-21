package colm.example.pocketsoccer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

class AssetLoader extends AsyncTask<AssetLoader.LoaderHandler, Integer, Boolean> {

    public interface LoaderHandler {
        void onLoaderFinished(boolean success);
        void updateSeekBar(int percentage);
    }

    private LoaderHandler loaderHandler;

    @Override
    protected Boolean doInBackground(LoaderHandler... loaderHandlers) {
        if (loaderHandlers.length != 1) {
            return false;
        }
        loaderHandler = loaderHandlers[0];
        int percentage = 0;
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
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        loaderHandler.onLoaderFinished(success);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        loaderHandler.updateSeekBar(values[values.length - 1]);
    }
}

public class MainActivity extends AppCompatActivity implements AssetLoader.LoaderHandler {

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
