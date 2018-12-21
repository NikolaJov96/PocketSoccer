package colm.example.pocketsoccer.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(tableName = "score", primaryKeys = {"first_player_name", "second_player_name"})
public class Score {

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

    public Score(@NonNull String firstPlayerName, @NonNull String secondPlayerName, int firstPlayerScore, int secondPlayerScore) {
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
