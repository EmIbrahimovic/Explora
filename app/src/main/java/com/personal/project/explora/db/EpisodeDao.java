package com.personal.project.explora.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface EpisodeDao {

    @Insert
    void insert(Episode episode);

    @Update
    void update(Episode episode);

    @Delete
    void delete(Episode episode);

    @Query("select distinct year from episodes_table order by year desc")
    LiveData<List<Integer>> getYears();

    @Query("select * from episodes_table where year = :requestedYear order by id desc")
    LiveData<List<Episode>> getEpisodesFromYear(int requestedYear);

    @Query("select * from episodes_table where id = :episode_id")
    Episode getEpisodeSync(int episode_id);

    @Query("select * from episodes_table where title = :title")
    Episode getEpisodeByTitle(String title);

    @Query("select * from episodes_table where datePublished = :datePublished")
    Episode getEpisodeByDatePublished(String datePublished);

    @Query("select * from episodes_table where recent IS NOT NULL")
    LiveData<List<Episode>> getRecentEpisodes();

    @Query("select * from episodes_table where downloadId = 2 order by id DESC")
    LiveData<List<Episode>> getDownloadedEpisodes();

    @Query("select id from episodes_table where downloadId = 2 order by id DESC")
    List<Integer> getDownloadedEpisodesIdSync();
}
