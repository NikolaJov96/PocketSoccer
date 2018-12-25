package com.example.pocketsoccer.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "score")
public class Score {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "first_player_name")
    private String firstPlayerName;

    @NonNull
    @ColumnInfo(name = "second_player_name")
    private String secondPlayerName;

    @ColumnInfo(name = "first_player_score")
    private int firstPlayerScore;

    @ColumnInfo(name = "second_player_score")
    private int secondPlayerScore;

    @ColumnInfo(name = "game_duration")
    private int gameDuration;

    public Score(@NonNull String firstPlayerName, @NonNull String secondPlayerName, int firstPlayerScore, int secondPlayerScore, int gameDuration) {
        this.firstPlayerName = firstPlayerName;
        this.secondPlayerName = secondPlayerName;
        this.firstPlayerScore = firstPlayerScore;
        this.secondPlayerScore = secondPlayerScore;
        this.gameDuration = gameDuration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getFirstPlayerName() {
        return firstPlayerName;
    }

    public void setFirstPlayerName(@NonNull String firstPlayerName) {
        this.firstPlayerName = firstPlayerName;
    }

    @NonNull
    public String getSecondPlayerName() {
        return secondPlayerName;
    }

    public void setSecondPlayerName(@NonNull String secondPlayerName) {
        this.secondPlayerName = secondPlayerName;
    }

    public int getFirstPlayerScore() {
        return firstPlayerScore;
    }

    public void setFirstPlayerScore(int firstPlayerScore) {
        this.firstPlayerScore = firstPlayerScore;
    }

    public int getSecondPlayerScore() {
        return secondPlayerScore;
    }

    public void setSecondPlayerScore(int secondPlayerScore) {
        this.secondPlayerScore = secondPlayerScore;
    }

    public int getGameDuration() {
        return gameDuration;
    }

    public void setGameDuration(int gameDuration) {
        this.gameDuration = gameDuration;
    }
}
