package com.personal.project.explora.db;

import static com.personal.project.explora.db.DatabaseConstants.EPISODE_TABLE_NAME;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Episode.class, version = DatabaseConstants.DB_VERSION)
public abstract class EpisodeDatabase extends RoomDatabase {

    private static final String TAG = "EpisodeDatabase";

    @VisibleForTesting
    public static final String DATABASE_NAME = EPISODE_TABLE_NAME;

    private static EpisodeDatabase instance;

    public abstract EpisodeDao episodeDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static EpisodeDatabase getInstance(Context context) {

        if (instance == null) {
            synchronized (EpisodeDatabase.class) {
                if (instance == null) {

                    instance = buildDatabase(context);

                    instance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }

        return instance;
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

