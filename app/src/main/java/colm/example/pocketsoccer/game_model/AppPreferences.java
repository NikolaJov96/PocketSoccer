package colm.example.pocketsoccer.game_model;

import android.content.Context;
import android.content.SharedPreferences;

import colm.example.pocketsoccer.MainActivity;

public class AppPreferences {

    private static AppPreferences singletonAP;

    private static final int DEFAULT_FIELD_ID = 0;
    private static final String FIELD_ID = "FIELD_ID";
    private static final int DEFAULT_END_GAME_CONTIDION = 0;
    private static final String END_GAME_CONTIDION = "END_GAME_CONTIDION";
    private static final int DEFAULT_GAME_SPEED = 1;
    private static final String GAME_SPEED = "GAME_SPEED";

    private int fieldId;
    private int endGameCondition;
    private int gameSpeed;

    {
        singletonAP = null;
    }

    private AppPreferences() {
        SharedPreferences sp = MainActivity.mainActivity.getPreferences(Context.MODE_PRIVATE);
        fieldId = sp.getInt(FIELD_ID, DEFAULT_FIELD_ID);
        endGameCondition = sp.getInt(END_GAME_CONTIDION, DEFAULT_END_GAME_CONTIDION);
        gameSpeed = sp.getInt(GAME_SPEED, DEFAULT_GAME_SPEED);
        storePreferences();
    }

    private void storePreferences() {
        SharedPreferences sp = MainActivity.mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(FIELD_ID, fieldId);
        editor.putInt(END_GAME_CONTIDION, endGameCondition);
        editor.putInt(GAME_SPEED, gameSpeed);
        editor.apply();
    }

    public void resetPreferences() {
        fieldId = DEFAULT_FIELD_ID;
        endGameCondition = DEFAULT_END_GAME_CONTIDION;
        gameSpeed = DEFAULT_GAME_SPEED;
        storePreferences();
    }

    static AppPreferences getAppPreferences() {
        if (singletonAP == null) {
            singletonAP = new AppPreferences();
        }
        return singletonAP;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
        storePreferences();
    }

    public int getEndGameCondition() {
        return endGameCondition;
    }

    public void setEndGameCondition(int endGameCondition) {
        this.endGameCondition = endGameCondition;
        storePreferences();
    }

    public int getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(int gameSpeed) {
        this.gameSpeed = gameSpeed;
        storePreferences();
    }
}