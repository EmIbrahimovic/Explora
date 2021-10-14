package com.personal.project.explora.db;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Episode.class, version = 2)
public abstract class EpisodeDatabase extends RoomDatabase {

    private static EpisodeDatabase instance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "episodes-table";

    public abstract EpisodeDao episodeDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static EpisodeDatabase getInstanceFromAsset(Context context) {
        if (instance == null) {
            synchronized (EpisodeDatabase.class) {
                if (instance == null) {
                    instance = buildDatabaseFromAsset(context.getApplicationContext());
                    instance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }

        return instance;
    }

    public static EpisodeDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (EpisodeDatabase.class) {
                if (instance == null) {
                    instance = buildDatabase(context.getApplicationContext());
                    instance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }

        return instance;
    }

    private static EpisodeDatabase buildDatabaseFromAsset(Context appContext) {

        return Room.databaseBuilder(appContext,
                EpisodeDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .createFromAsset("databases/episodes_table.db")
                .build();

    }


    private static EpisodeDatabase buildDatabase(Context appContext) {

        return Room.databaseBuilder(appContext,
                EpisodeDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();

    }

    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

}
