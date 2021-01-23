package com.personal.project.explora.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EpisodeDao {

    @Insert
    void insert(Episode episode);

    @Update
    void update(Episode episode);

    @Delete
    void delete(Episode episode);

    @Query("select * from episodes_table where year = :requestedYear")
    LiveData<List<Episode>> getEpisodesFromYear(int requestedYear);

    @Query("select * from episodes_table where year = :requestedYear")
    List<Episode> getEpisodesFromYearSync(int requestedYear);

    @Query("select * from episodes_table where id = :episode_id")
    Episode getEpisode(int episode_id);
}
