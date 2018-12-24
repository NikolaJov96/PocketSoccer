package colm.example.pocketsoccer.repository;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.system.StructUtsname;

import java.util.List;
import java.util.logging.Filter;

import colm.example.pocketsoccer.database.PocketSoccerDatabase;
import colm.example.pocketsoccer.database.dao.ScoreDao;
import colm.example.pocketsoccer.database.entity.Score;
import colm.example.pocketsoccer.database.entity.TwoUsersScore;
import colm.example.pocketsoccer.game_model.GameViewModel;

public class Repository {

    private ScoreDao scoreDao;

    private LiveData<List<Score>> allScores;
    private LiveData<List<TwoUsersScore>> allTwoPlayerScores;

    public Repository(Context context) {
        PocketSoccerDatabase db = PocketSoccerDatabase.getDatabase(context);
        scoreDao = db.scoreDao();
        allTwoPlayerScores = scoreDao.getTwoUserScores();
    }

    public LiveData<List<Score>> getAllScores(GameViewModel.FilterStruct filterStruct) {
        allScores = scoreDao.getAll(filterStruct.p1, filterStruct.p2);
        return allScores;
    }

    public LiveData<List<TwoUsersScore>> getAllTwoPlayerScores() {
        return allTwoPlayerScores;
    }

    public void insertScore(final Score score) {
        new Thread(() -> scoreDao.insert(score)).start();
    }

    public void deleteAllScores() {
        new Thread(() -> scoreDao.deleteAll()).start();
    }

    public void deleteTowPlayers(String player1, String player2) {
        new Thread(() -> scoreDao.deleteTwoPlayers(player1, player2)).start();
    }
}
