package colm.example.pocketsoccer.game_model;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import colm.example.pocketsoccer.MainActivity;


public class GameAssetManager {

    public interface LoaderHandler {
        void onLoaderFinished(boolean success);
        void updateSeekBar(int percentage);
    }

    public class AssetLoader extends AsyncTask<LoaderHandler, Integer, Boolean> {

        private LoaderHandler loaderHandler;

        @Override
        protected Boolean doInBackground(LoaderHandler... loaderHandlers) {
            if (loaderHandlers.length != 1) {
                return false;
            }
            loaderHandler = loaderHandlers[0];
            float percentage = 0.0f;
            float percentageStep = 100.0f / 3.0f;
            AssetManager am = MainActivity.mainActivity.getAssets();

            try {

                parquetBMP = BitmapFactory.decodeStream(am.open("fields/parquet.jpg"));
                percentage += percentageStep;
                publishProgress((int)percentage);

                concreteBMP = BitmapFactory.decodeStream(am.open("fields/concrete.jpg"));
                percentage += percentageStep;
                publishProgress((int)percentage);

                grassBMP = BitmapFactory.decodeStream(am.open("fields/grass.jpg"));
                percentage += percentageStep;
                publishProgress((int)percentage);

            } catch (Exception e) {
                return false;
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

    private static GameAssetManager singletonGAM;

    private Bitmap parquetBMP;
    private Bitmap concreteBMP;
    private Bitmap grassBMP;

    {
        singletonGAM = null;
    }

    private GameAssetManager() {
        new AssetLoader().execute(MainActivity.mainActivity);
    }

    public static GameAssetManager getGameAssetManager() {
        if (singletonGAM == null) {
            singletonGAM = new GameAssetManager();
        }
        return singletonGAM;
    }

    public Bitmap getParquetBMP() {
        return parquetBMP;
    }

    public Bitmap getConcreteBMP() {
        return concreteBMP;
    }

    public Bitmap getGrassBMP() {
        return grassBMP;
    }
}
