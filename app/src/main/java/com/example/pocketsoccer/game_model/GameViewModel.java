package com.example.pocketsoccer.game_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import java.util.List;

import com.example.pocketsoccer.NewGameDialog;
import com.example.pocketsoccer.database.entity.Score;
import com.example.pocketsoccer.database.entity.TwoUsersScore;
import com.example.pocketsoccer.repository.Repository;

public class GameViewModel extends AndroidViewModel {

    public static class FilterStruct {
        public String p1;
        public String p2;

        public FilterStruct(String p1, String p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    private Repository repository;

    private LiveData<List<Score>> allScores;
    private LiveData<FilterStruct> filterLiveDataScores;
    private LiveData<List<TwoUsersScore>> allTwoPlayerScores;

    private AppPreferences appPreferences;

    private GameAssetManager gameAssetManager;

    public GameViewModel(Application application) {
        super(application);
        repository = new Repository(application);
        filterLiveDataScores = new MutableLiveData<>();
        ((MutableLiveData<FilterStruct>) filterLiveDataScores).setValue(new FilterStruct("", ""));
        allScores = Transformations.switchMap(filterLiveDataScores, input -> repository.getAllScores(input)); //repository.getAllScores();
        allTwoPlayerScores = repository.getAllTwoPlayerScores();
        appPreferences = AppPreferences.getAppPreferences();
        gameAssetManager = GameAssetManager.getGameAssetManager();
    }

    public LiveData<List<Score>> getAllScores() {
        return allScores;
    }

    public LiveData<List<TwoUsersScore>> getAllTwoPlayerScores() {
        return allTwoPlayerScores;
    }

    public void insert(Score score) {
        repository.insertScore(score);
    }

    public void deleteAllScores() {
        repository.deleteAllScores();
    }

    public void deleteTowPlayers(String player1, String player2) {
        repository.deleteTowPlayers(player1, player2);
    }

    public AppPreferences getAppPreferences() {
        return appPreferences;
    }

    public GameAssetManager getGameAssetManager() {
        return gameAssetManager;
    }

    public Game getGame() {
        return Game.getGame();
    }

    public void newGame(NewGameDialog.NewGameDialogData data) {
        Game.newGame(data);
    }

    public void purgeGame() {
        Game.purgeGame();
    }

    public void updateFilter(FilterStruct filterStruct) {
        ((MutableLiveData<FilterStruct>) filterLiveDataScores).setValue(filterStruct);
    }
}
