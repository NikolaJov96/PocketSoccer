package com.example.pocketsoccer.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.pocketsoccer.database.dao.ScoreDao;
import com.example.pocketsoccer.database.entity.Score;

@Database(entities = {Score.class}, version = 3, exportSchema = false)
public abstract class PocketSoccerDatabase extends RoomDatabase {

    private static PocketSoccerDatabase singletonDB;

    public abstract ScoreDao scoreDao();

    public static PocketSoccerDatabase getDatabase(final Context context) {
        if (singletonDB == null) {
            synchronized (PocketSoccerDatabase.class) {
                if (singletonDB == null) {
                    singletonDB = Room.databaseBuilder(
                            context.getApplicationContext(),
                            PocketSoccerDatabase.class,
                            "pocket_soccer_database"
                    )
                            .fallbackToDestructiveMigration()
                            .addCallback(initDatabase)
                            .build();
                }
            }
        }
        return singletonDB;
    }

    private static RoomDatabase.Callback initDatabase = new RoomDatabase.Callback() {

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsync(singletonDB).execute();
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(singletonDB).execute();
        }
    };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final ScoreDao scoreDao;

        PopulateDbAsync(PocketSoccerDatabase db) {
            scoreDao = db.scoreDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (scoreDao.getNumberOfGames() == 0) {
                scoreDao.insert(new Score("Pera", "Zika", 1, 3, 300));
                scoreDao.insert(new Score("Pera", "Zika", 1, 2, 231));
                scoreDao.insert(new Score("Pera", "Zika", 2, 1, 231));
                scoreDao.insert(new Score("Pera", "Zika", 2, 3, 231));

                scoreDao.insert(new Score("Mika", "Pera", 1, 3, 231));
                scoreDao.insert(new Score("Mika", "Pera", 2, 2, 231));
                scoreDao.insert(new Score("Mika", "Pera", 3, 0, 231));
                scoreDao.insert(new Score("Mika", "Pera", 2, 2, 231));

                scoreDao.insert(new Score("Mika", "Zika", 0, 2, 231));
                scoreDao.insert(new Score("Mika", "Zika", 3, 2, 231));
                scoreDao.insert(new Score("Mika", "Zika", 3, 3, 231));
                scoreDao.insert(new Score("Mika", "Zika", 1, 3, 231));
                scoreDao.insert(new Score("Mika", "Zika", 2, 0, 231));
            }
            return null;
        }
    }

}
