package com.personal.project.explora.ui.listen;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.personal.project.explora.Episode;

import java.util.List;

public class YearViewModel extends ViewModel {
    LiveData<List<Episode>> episodes;

    public LiveData<List<Episode>> getAllEpisodes() {
        return episodes;
    }
}