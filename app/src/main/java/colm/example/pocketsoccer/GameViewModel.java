package colm.example.pocketsoccer;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import colm.example.pocketsoccer.database.entity.Score;
import colm.example.pocketsoccer.repository.Repository;

public class GameViewModel extends AndroidViewModel {

    private Repository repository;

    private LiveData<List<Score>> allScores;

    public GameViewModel(Application application) {
        super(application);
        repository = new Repository(application);
        allScores = repository.getAllScores();
    }

    LiveData<List<Score>> getAllScores() {
        return allScores;
    }

    void insert(Score score) {
        repository.insertScore(score);
    }

    void deleteAllScores() {
        repository.deleteAllScores();
    }

}
