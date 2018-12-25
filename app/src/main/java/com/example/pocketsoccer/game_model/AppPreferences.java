package com.example.pocketsoccer.game_model;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.pocketsoccer.MainActivity;

public class AppPreferences {

    public enum EndGameConditions { TIMEOUT, SCORE }

    private static AppPreferences singletonAP;

    private static final int DEFAULT_FIELD_ID = 0;
    private static final String FIELD_ID = "FIELD_ID";
    private static final int DEFAULT_END_GAME_CONDITION = 0;
    private static final String END_GAME_CONDITION = "END_GAME_CONDITION";
    private static final int DEFAULT_GAME_SPEED = 1;
    private static final String GAME_SPEED = "GAME_SPEED";

    private int fieldId;
    private int endGameCondition;
    private int gameSpeed;

    static {
        singletonAP = null;
    }

    private AppPreferences() {
        SharedPreferences sp = MainActivity.mainActivity.getPreferences(Context.MODE_PRIVATE);
        fieldId = sp.getInt(FIELD_ID, DEFAULT_FIELD_ID);
        endGameCondition = sp.getInt(END_GAME_CONDITION, DEFAULT_END_GAME_CONDITION);
        gameSpeed = sp.getInt(GAME_SPEED, DEFAULT_GAME_SPEED);
        storePreferences();
    }

    private void storePreferences() {
        SharedPreferences sp = MainActivity.mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(FIELD_ID, fieldId);
        editor.putInt(END_GAME_CONDITION, endGameCondition);
        editor.putInt(GAME_SPEED, gameSpeed);
        editor.apply();
    }

    public void resetPreferences() {
        fieldId = DEFAULT_FIELD_ID;
        endGameCondition = DEFAULT_END_GAME_CONDITION;
        gameSpeed = DEFAULT_GAME_SPEED;
        storePreferences();
    }

    public static AppPreferences getAppPreferences() {
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

    public EndGameConditions getEndGameCondition() {
        if (endGameCondition == 0) {
            return EndGameConditions.TIMEOUT;
        } else {
            return EndGameConditions.SCORE;
        }
    }

    public void setEndGameCondition(EndGameConditions endGameCondition) {
        if (endGameCondition.equals(EndGameConditions.TIMEOUT)) {
            this.endGameCondition = 0;
        } else {
            this.endGameCondition = 1;
        }
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