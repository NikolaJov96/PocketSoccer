package com.example.pocketsoccer.game_model;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import com.example.pocketsoccer.MainActivity;


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
            float percentageStep = 100.0f / (NUMBER_OF_FIELDS + 2 * NUMBER_OF_FLAGS + NUMBER_OF_BALLS + NUMBER_OF_SOUNDS);
            AssetManager am = MainActivity.mainActivity.getAssets();

            try {

                for (int i = 0; i < NUMBER_OF_FIELDS; i++) {
                    fields[i] = BitmapFactory.decodeStream(am.open("fields/f" + (i + 1) + ".jpg"));
                    percentage += percentageStep;
                    publishProgress((int) percentage);
                }

                for (int i = 0; i < NUMBER_OF_FLAGS; i++) {
                    flags[i] = BitmapFactory.decodeStream(am.open("flags/f" + (i + 1) + ".png"));
                    percentage += percentageStep;
                    publishProgress((int)percentage);
                }

                for (int i = 0; i < NUMBER_OF_FLAGS; i++) {
                    grayFlags[i] = BitmapFactory.decodeStream(am.open("flags/f" + (i + 1) + "g.png"));
                    percentage += percentageStep;
                    publishProgress((int)percentage);
                }

                ball = BitmapFactory.decodeStream(am.open("ball.png"));
                percentage += percentageStep;
                publishProgress((int)percentage);

                goalPlayer = new MediaPlayer();
                goalPlayer.setDataSource(am.openFd("sound/goal.wav"));
                goalPlayer.setVolume(0.4f, 0.4f);
                goalPlayer.prepare();
                percentage += percentageStep;
                publishProgress((int)percentage);

                for (int i = 0; i < NUMBER_OF_SOUNDS - 1; i++) {
                    kickPlayer[i] = new MediaPlayer();
                    kickPlayer[i].setDataSource(am.openFd("sound/kick.wav"));
                    kickPlayer[i].setVolume(3.0f, 3.0f);
                    kickPlayer[i].prepare();
                    percentage += percentageStep;
                    publishProgress((int)percentage);
                }

                loaded = true;

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

    public static final int NUMBER_OF_FIELDS = 3;
    public static final int NUMBER_OF_FLAGS = 5;
    public static final int NUMBER_OF_BALLS = 1;
    public static final int NUMBER_OF_SOUNDS = 7;

    private static GameAssetManager singletonGAM;

    private Bitmap fields[];
    private Bitmap flags[];
    private Bitmap grayFlags[];
    private Bitmap ball;
    private MediaPlayer goalPlayer;
    private MediaPlayer kickPlayer[];

    private boolean loaded;

    static {
        singletonGAM = null;
    }

    private GameAssetManager() {
        loaded = false;
        fields = new Bitmap[NUMBER_OF_FIELDS];
        flags = new Bitmap[NUMBER_OF_FLAGS];
        grayFlags = new Bitmap[NUMBER_OF_FLAGS];
        kickPlayer = new MediaPlayer[NUMBER_OF_SOUNDS - 1];
        new AssetLoader().execute(MainActivity.mainActivity);
    }

    public static GameAssetManager getGameAssetManager() {
        if (singletonGAM == null) {
            singletonGAM = new GameAssetManager();
        }
        return singletonGAM;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public Bitmap getField(int id) {
        return fields[id];
    }

    public Bitmap getFlag(int id) {
        return flags[id];
    }

    public Bitmap getGrayFlag(int id) {
        return grayFlags[id];
    }

    public Bitmap getBall() {
        return ball;
    }

    public MediaPlayer getGoalPlayer() {
        return goalPlayer;
    }

    public MediaPlayer getKickPlayer(int id) {
        return kickPlayer[id];
    }

}
