package colm.example.pocketsoccer.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import colm.example.pocketsoccer.database.entity.Score;

@Dao
public interface ScoreDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Score score);

    @Query("SELECT * FROM score")
    LiveData<List<Score>> getAll();

    @Delete
    void delete(Score score);

    @Query("DELETE FROM score")
    void deleteAll();
}
