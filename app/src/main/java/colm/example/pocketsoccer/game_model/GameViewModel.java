package colm.example.pocketsoccer.game_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

import colm.example.pocketsoccer.MainActivity;
import colm.example.pocketsoccer.NewGameDialog;
import colm.example.pocketsoccer.database.entity.Score;
import colm.example.pocketsoccer.repository.Repository;

public class GameViewModel extends AndroidViewModel {

    private Repository repository;

    private LiveData<List<Score>> allScores;

    private AppPreferences appPreferences;

    private GameAssetManager gameAssetManager;

    private Game game;

    public GameViewModel(Application application) {
        super(application);
        repository = new Repository(application);
        allScores = repository.getAllScores();
        appPreferences = AppPreferences.getAppPreferences();
        gameAssetManager = GameAssetManager.getGameAssetManager();
        game = Game.getGame();
    }

    public LiveData<List<Score>> getAllScores() {
        return allScores;
    }

    void insert(Score score) {
        repository.insertScore(score);
    }

    public void deleteAllScores() {
        repository.deleteAllScores();
    }

    public AppPreferences getAppPreferences() {
        return appPreferences;
    }

    public GameAssetManager getGameAssetManager() {
        return gameAssetManager;
    }

    public Game getGame() {
        game = Game.getGame();
        return game;
    }

    public Game newGame(NewGameDialog.NewGameDialogData data) {
        game = Game.newGame(data);
        return game;
    }
}
