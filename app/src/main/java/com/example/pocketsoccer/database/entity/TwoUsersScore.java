package com.example.pocketsoccer.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.support.annotation.NonNull;

public class TwoUsersScore {

    @NonNull
    @ColumnInfo(name = "first_player_name")
    private String firstPlayerName;

    @NonNull
    @ColumnInfo(name = "second_player_name")
    private String secondPlayerName;

    @ColumnInfo(name = "first_player_wins")
    private int firstPlayerScore;

    @ColumnInfo(name = "second_player_wins")
    private int secondPlayerScore;

    public TwoUsersScore(@NonNull String firstPlayerName, @NonNull String secondPlayerName, int firstPlayerScore, int secondPlayerScore) {
        this.firstPlayerName = firstPlayerName;
        this.secondPlayerName = secondPlayerName;
        this.firstPlayerScore = firstPlayerScore;
        this.secondPlayerScore = secondPlayerScore;
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
}
