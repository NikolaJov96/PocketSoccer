package colm.example.pocketsoccer.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import colm.example.pocketsoccer.database.entity.Score;
import colm.example.pocketsoccer.database.entity.TwoUsersScore;

@Dao
public interface ScoreDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Score score);

    @Query("SELECT * FROM score WHERE first_player_name = :player1 AND second_player_name = :player2 ORDER BY id DESC")
    LiveData<List<Score>> getAll(String player1, String player2);

    @Query("SELECT COUNT(*) FROM score")
    int getNumberOfGames();

    @Query("SELECT first_player_name, second_player_name, SUM(w1) AS first_player_wins, SUM(w2) AS second_player_wins FROM" +
            " (SELECT first_player_name, second_player_name, " +
            "   CASE WHEN first_player_score > second_player_score THEN 1 ELSE 0 END AS w1," +
            "   CASE WHEN second_player_score > first_player_score THEN 1 ELSE 0 END AS w2" +
            "   FROM score)" +
            " GROUP BY first_player_name, second_player_name")
    LiveData<List<TwoUsersScore>> getTwoUserScores();

    @Query("DELETE FROM score")
    void deleteAll();

    @Query("DELETE FROM score WHERE first_player_name = :player1 AND second_player_name = :player2")
    void deleteTwoPlayers(String player1, String player2);
}
