package com.personal.project.explora.db;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Episode.class, version = 1)
public abstract class EpisodeDatabase extends RoomDatabase {

    private static EpisodeDatabase instance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "episode-db";

    public abstract EpisodeDao episodeDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static EpisodeDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (EpisodeDatabase.class) {
                if (instance == null) {
                    instance = buildDatabase(context.getApplicationContext());
                    instance.updateDataBaseCreated(context.getApplicationContext());
                }
            }
        }

        return instance;
    }

    private static EpisodeDatabase buildDatabase(Context appContext) {

        return Room.databaseBuilder(appContext,
                EpisodeDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .createFromAsset("database/explora.db")
                .build();

    }

    private void updateDataBaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

}
