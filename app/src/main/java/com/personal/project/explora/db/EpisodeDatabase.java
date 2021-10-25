package com.personal.project.explora.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.personal.project.explora.db.DatabaseConstants.EPISODE_DATE_PUBLISHED_COLUMN;
import static com.personal.project.explora.db.DatabaseConstants.EPISODE_DESCRIPTION_COLUMN;
import static com.personal.project.explora.db.DatabaseConstants.EPISODE_DURATION_COLUMN;
import static com.personal.project.explora.db.DatabaseConstants.EPISODE_LAST_POSITION_COLUMN;
import static com.personal.project.explora.db.DatabaseConstants.EPISODE_LINK_COLUMN;
import static com.personal.project.explora.db.DatabaseConstants.EPISODE_RECENT_COLUMN;
import static com.personal.project.explora.db.DatabaseConstants.EPISODE_TABLE_NAME;
import static com.personal.project.explora.db.DatabaseConstants.EPISODE_YEAR_COLUMN;
import static com.personal.project.explora.db.DatabaseConstants.V1_DB_FILENAME;
import static com.personal.project.explora.db.DatabaseConstants.V1_EPISODE_TITLE_COLUMN;

@Database(entities = Episode.class, version = DatabaseConstants.DB_VERSION)
public abstract class EpisodeDatabase extends RoomDatabase {

    private static final String TAG = "EpisodeDatabase";

    @VisibleForTesting
    public static final String DATABASE_NAME = EPISODE_TABLE_NAME;

    private static EpisodeDatabase instance;

    public abstract EpisodeDao episodeDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) { }

    };

    public static EpisodeDatabase getInstance(Context context) {

        boolean v2DatabaseExist = doesDatabaseExist(context, DATABASE_NAME);
        boolean v1DatabaseExists = false;
        if (!v2DatabaseExist)
            v1DatabaseExists = doesDatabaseExist(context, V1_DB_FILENAME);

        if (instance == null) {
            synchronized (EpisodeDatabase.class) {
                if (instance == null) {

                    if (!v2DatabaseExist && !v1DatabaseExists)
                        instance = buildDatabaseFromAsset(context.getApplicationContext());
                    else
                    {
                        if (!v2DatabaseExist && v1DatabaseExists) {
                            upgradeDatabase(context);
                        }

                        instance = buildDatabase(context);
                    }

                    instance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }

        return instance;
    }

    private static EpisodeDatabase buildDatabaseFromAsset(Context appContext) {

        return Room.databaseBuilder(appContext,
                EpisodeDatabase.class, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
                .createFromAsset("databases/episodes_table.db")
                .build();

    }

    private static EpisodeDatabase buildDatabase(Context appContext) {

        return Room.databaseBuilder(appContext,
                EpisodeDatabase.class, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
                .build();

    }

    private static void upgradeDatabase(Context context)
    {
        Log.d(TAG, "upgradeDatabase: Upgrading database from version 1 to 2");
        copyFromAssets(context, V1_DB_FILENAME);
    }

    private static boolean doesDatabaseExist(Context context, String databaseFilename) {
        return context.getDatabasePath(databaseFilename).exists();
    }

    private static void copyFromAssets(Context context, String originalDBFilename) {

        File originalDBPath = context.getDatabasePath(originalDBFilename);
        // Open and close the original DB so as to checkpoint the WAL file
        SQLiteDatabase originalDB = SQLiteDatabase.openDatabase(
                originalDBPath.getPath(),null,SQLiteDatabase.OPEN_READWRITE);
        originalDB.close();

        //1. Rename original database
        String preservedDBName = "preserved_" + DATABASE_NAME;
        File preservedDBPath = new File (
                originalDBPath.getParentFile().getPath() + File.separator + preservedDBName);
        (context.getDatabasePath(originalDBFilename))
                .renameTo(preservedDBPath);

        //2. Copy the replacement database from the assets folder
        File copiedDBLocation = copyAssetFile(context);

        //3. Open the newly copied database
        SQLiteDatabase copiedDB = SQLiteDatabase.openDatabase(
                copiedDBLocation.getPath(),null,SQLiteDatabase.OPEN_READWRITE);
        SQLiteDatabase preservedDB = SQLiteDatabase.openDatabase(
                preservedDBPath.getPath(),null,SQLiteDatabase.OPEN_READONLY);

        //4. Apply preserved data to the newly copied data
        copiedDB.beginTransaction();
        preserveTableColumns(preservedDB, copiedDB, true);

        copiedDB.setVersion(DatabaseConstants.DB_VERSION);
        copiedDB.setTransactionSuccessful();
        copiedDB.endTransaction();

        //5. Cleanup
        copiedDB.close();
        preservedDB.close();
        preservedDBPath.delete();
    }

    private static File copyAssetFile(Context context) {
        int buffer_size = 8192;
        byte[] buffer = new byte[buffer_size];
        int bytes_read = 0;
        try {
            InputStream fis = context.getAssets().open("databases/episodes_table.db");
            File copiedDBLocation = new File(context.getDatabasePath(DATABASE_NAME).getPath());
            OutputStream os = new FileOutputStream(copiedDBLocation);
            while ((bytes_read = fis.read(buffer)) > 0) {
                os.write(buffer,0,bytes_read);
            }
            os.flush();
            os.close();
            fis.close();

            return  copiedDBLocation;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to copy from assets");
        }
    }

    /*private static int getDBVersion(Context context, String databaseName) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(
                context.getDatabasePath(databaseName).getPath(),null,SQLiteDatabase.OPEN_READONLY);
        int rv = db.getVersion();
        db.close();
        return rv;
    }

    private static void setDBVersion(Context context, String databaseName, int version) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(
                context.getDatabasePath(databaseName).getPath(),null,SQLiteDatabase.OPEN_READWRITE);
        db.setVersion(version);
        db.close();
    }*/

    private static boolean preserveTableColumns(
            SQLiteDatabase originalDatabase,
            SQLiteDatabase newDatabase,
            boolean failWithException) {

        String tableName = EPISODE_TABLE_NAME;

        // Checks for validity of tableName, originalDatabase, newDatabase
        StringBuilder sb = new StringBuilder();
        //Cursor csr = originalDatabase.query("sqlite_master",new String[]{"name"},"name=? AND type=?",new String[]{tableName,"table"},null,null,null);
        Cursor csr = originalDatabase.rawQuery("SELECT name FROM sqlite_master WHERE name = '" + tableName + "' AND type = 'table'", null);
        if (!csr.moveToFirst()) {
            sb.append("\n\tTable ").append(tableName).append(" not found in database ").append(originalDatabase.getPath());
        }
        //csr = newDatabase.query("sqlite_master",new String[]{"name"},"name=? AND type=?",new String[]{tableName,"table"},null,null,null);
        csr = newDatabase.rawQuery("SELECT name FROM sqlite_master WHERE name = '" + tableName + "' AND type = 'table'", null);
        if (!csr.moveToFirst()) {
            sb.append("\n\tTable ").append(tableName).append(" not found in database ").append(newDatabase.getPath());
        }
        if (sb.length() > 0) {
            if (failWithException) {
                throw new RuntimeException("Both databases are required to have a table named " + tableName + sb.toString());
            }
            return false;
        }

        String[] columnsToExtract = new String[] {
                V1_EPISODE_TITLE_COLUMN,
                EPISODE_LAST_POSITION_COLUMN,
                EPISODE_RECENT_COLUMN
        };
        String[] columnsToPreserve =  new String[] {
                EPISODE_LAST_POSITION_COLUMN,
                EPISODE_RECENT_COLUMN
        };
        String[] whereClauseColumns = new String[] {
                EPISODE_DATE_PUBLISHED_COLUMN
        };
        String[] columnsToCopyToNew = new String[] {
                EPISODE_YEAR_COLUMN,
                V1_EPISODE_TITLE_COLUMN,
                EPISODE_DESCRIPTION_COLUMN,
                EPISODE_LINK_COLUMN,
                EPISODE_LAST_POSITION_COLUMN,
                EPISODE_DURATION_COLUMN,
                EPISODE_RECENT_COLUMN
        };

        csr = originalDatabase.query(
                tableName,columnsToExtract,null,null,null,null,null);
        ContentValues cv = new ContentValues();
        while (csr.moveToNext()) {
            cv.clear();
            for (String pc: columnsToPreserve) {
                switch (csr.getType(csr.getColumnIndex(pc))) {
                    case Cursor.FIELD_TYPE_INTEGER:
                        cv.put(pc,csr.getLong(csr.getColumnIndex(pc)));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        cv.put(pc,csr.getString(csr.getColumnIndex(pc)));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        cv.put(pc,csr.getDouble(csr.getColumnIndex(pc)));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        cv.put(pc,csr.getBlob(csr.getColumnIndex(pc)));
                }
            }

            sb = new StringBuilder();
            sb.append(whereClauseColumns[0]).append("=? ");

            String[] whereargs = new String[whereClauseColumns.length];
            whereargs[0] = csr.getString(csr.getColumnIndex(V1_EPISODE_TITLE_COLUMN));
            whereargs[0] = Episode.titleToDatePublished(whereargs[0]);

            int updatedRows = newDatabase.update(tableName,cv,sb.toString(),whereargs);

            // If 0 row updated, the row with these whereargs does not exist in the new db
            // Then, I insert this row into the new database
            if (updatedRows == 0) {
                sb = new StringBuilder();
                sb.append(V1_EPISODE_TITLE_COLUMN).append("=? ");
                whereargs[0] = csr.getString(csr.getColumnIndex(V1_EPISODE_TITLE_COLUMN));

                Cursor newRow = originalDatabase.query(
                        tableName, columnsToCopyToNew, sb.toString(), whereargs, null, null, null);
                newRow.moveToFirst();
                ContentValues newRowContentValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(newRow, newRowContentValues);

                String title = newRowContentValues.getAsString(V1_EPISODE_TITLE_COLUMN);
                newRowContentValues.remove(V1_EPISODE_TITLE_COLUMN);
                newRowContentValues.put(EPISODE_DATE_PUBLISHED_COLUMN,
                        Episode.titleToDatePublished(title));

                newDatabase.insert(tableName, null, newRowContentValues);
            }
        }
        csr.close();
        return true;
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

