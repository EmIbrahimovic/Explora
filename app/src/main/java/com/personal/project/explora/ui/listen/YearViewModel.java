package com.personal.project.explora.ui.listen;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.personal.project.explora.BasicApp;
import com.personal.project.explora.EpisodeRepository;
import com.personal.project.explora.db.Episode;

import java.util.List;
import java.util.Map;

public class YearViewModel extends AndroidViewModel {

    private Map<Integer, LiveData<List<Episode>>> allEpisodes;
    private EpisodeRepository mRepository;

    private LiveData<Integer> networkOperationStatus;

    public YearViewModel(@NonNull Application application) {
        super(application);

        mRepository = ((BasicApp)application).getRepository();
        allEpisodes = mRepository.getAllEpisodes();
        networkOperationStatus = mRepository.getNetworkOperationStatus();
    }

    public LiveData<List<Episode>> getEpisodesFromYear(int year) {
        return allEpisodes.get(year);
    }

    public LiveData<Integer> getNetworkOperationStatus() {
        return networkOperationStatus;
    }
}