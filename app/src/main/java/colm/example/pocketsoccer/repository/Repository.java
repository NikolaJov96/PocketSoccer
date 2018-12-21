package colm.example.pocketsoccer.repository;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import java.util.List;

import colm.example.pocketsoccer.database.PocketSoccerDatabase;
import colm.example.pocketsoccer.database.dao.ScoreDao;
import colm.example.pocketsoccer.database.entity.Score;

public class Repository {

    private ScoreDao scoreDao;

    private LiveData<List<Score>> allScores;

    public Repository(Context context) {
        PocketSoccerDatabase db = PocketSoccerDatabase.getDatabase(context);
        scoreDao = db.scoreDao();
        allScores = scoreDao.getAll();
    }

    public LiveData<List<Score>> getAllScores() {
        return allScores;
    }

    public void insertScore(final Score score) {
        new Thread(() -> scoreDao.insert(score)).start();
    }

    public void deleteAllScores() {
        new Thread(() -> scoreDao.deleteAll()).start();
    }
}
