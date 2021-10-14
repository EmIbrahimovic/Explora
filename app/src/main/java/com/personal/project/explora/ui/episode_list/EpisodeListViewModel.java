package com.personal.project.explora.ui.episode_list;

import android.app.Application;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.personal.project.explora.BasicApp;
import com.personal.project.explora.EpisodeRepository;
import com.personal.project.explora.db.Episode;
import com.personal.project.explora.service.PlayerServiceConnection;
import com.personal.project.explora.service.download.DownloadUtil;
import com.personal.project.explora.utils.DateUtil;
import com.personal.project.explora.utils.Event;
import com.personal.project.explora.utils.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.personal.project.explora.service.PlayerServiceConnection.EMPTY_PLAYBACK_STATE;
import static com.personal.project.explora.service.PlayerServiceConnection.NOTHING_PLAYING;

public class EpisodeListViewModel extends AndroidViewModel {

    private final LiveData<List<Integer>> years;
    private Map<Integer, LiveData<List<Episode>>> episodes;
    private final LiveData<List<Episode>> recentEpisodes;
    private final LiveData<List<Episode>> downloadedEpisodes;
    private final EpisodeRepository mRepository;
    private final Observer<List<Episode>> recentEpisodesObserver;

    private final LiveData<Integer> networkOperationStatus;
    private MutableLiveData<Event<Integer>> networkOperationStatusEvent;

    private final PlayerServiceConnection playerServiceConnection;
    private final MutableLiveData<Integer> nowPlayingId;
    private final MutableLiveData<Boolean> isPlaying;
    //private final LiveData<Boolean> networkError;

    private final Observer<PlaybackStateCompat> playbackStateObserver;
    private final Observer<MediaMetadataCompat> mediaMetadataObserver;
    private final Observer<Integer> networkOperationStatusObserver;

    public EpisodeListViewModel(@NonNull Application application) {
        super(application);

        mRepository = ((BasicApp)application).getRepository();
        years = mRepository.getYears();
        episodes = new HashMap<>();
        recentEpisodes = mRepository.getRecentEpisodes();
        downloadedEpisodes = mRepository.getDownloadedEpisodes();
        recentEpisodesObserver = episodes -> {
            if (episodes.size() > 10) {
                Collections.sort(episodes, (o1, o2) ->
                        DateUtil.compareStringDateTimes(o1.getRecent(), o2.getRecent()));
                Episode last = episodes.get(episodes.size() - 1);
                last.setRecent(null);
                mRepository.update(last);
            }
        };
        recentEpisodes.observeForever(recentEpisodesObserver);

        nowPlayingId = new MutableLiveData<>();
        isPlaying = new MutableLiveData<>();

        networkOperationStatus = mRepository.getNetworkOperationStatus();
        networkOperationStatusObserver = status -> {
            if (status == null) mRepository.refreshNewEpisodes();
            networkOperationStatusEvent.postValue(new Event<>(status));
        };
        networkOperationStatusEvent = new MutableLiveData<>();
        networkOperationStatus.observeForever(networkOperationStatusObserver);

        playerServiceConnection = ((BasicApp)application).getPlayerServiceConnection();
        //networkError = playerServiceConnection.getNetworkFailure();

        playbackStateObserver = state -> {
            if (state == null) state = EMPTY_PLAYBACK_STATE;
            MediaMetadataCompat metadata =
                    (playerServiceConnection.getNowPlaying().getValue() == null) ?
                            NOTHING_PLAYING : playerServiceConnection.getNowPlaying().getValue();

            if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {
                updateState(state, metadata);
            }
        };
        mediaMetadataObserver = metadata -> {
            PlaybackStateCompat state =
                    (playerServiceConnection.getPlaybackState().getValue() == null) ?
                            EMPTY_PLAYBACK_STATE : playerServiceConnection.getPlaybackState().getValue();

            if (metadata == null) metadata = NOTHING_PLAYING;

            if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {
                updateState(state, metadata);
            }
        };

        playerServiceConnection.getPlaybackState().observeForever(playbackStateObserver);
        playerServiceConnection.getNowPlaying().observeForever(mediaMetadataObserver);
    }

    public void updateAllEpisodeMap(List<Integer> years)
    {
        episodes = new HashMap<>();
        for (Integer year : years) {
            episodes.put(year, mRepository.getEpisodesFromYear(year));
        }
    }

    public void doRefresh() {
        mRepository.refreshNewEpisodes();
    }

    public LiveData<List<Integer>> getYears() {
        return Transformations.distinctUntilChanged(years);
    }

    public LiveData<List<Episode>> getEpisodesFromYear(int year) {
        return episodes.get(year);
    }

    public LiveData<List<Episode>> getRecentEpisodes() {
        return recentEpisodes;
    }

    public void addNewRecent(Episode episode) {
        episode.setRecent(LocalDateTime.now().toString());
        mRepository.update(episode);
    }

    public LiveData<List<Episode>> getDownloadedEpisodes() {
        return downloadedEpisodes;
    }

    public LiveData<Event<Integer>> getNetworkOperationStatus() {
        return networkOperationStatusEvent;
    }

    /*public LiveData<Boolean> getNetworkError() {
        return networkError;
    }

    public LiveData<Boolean> getIsConnected() {
        return playerServiceConnection.getIsConnected();
    }*/

    public LiveData<Integer> getNowPlayingId() {
        return nowPlayingId;
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    private void updateState(PlaybackStateCompat playbackState, MediaMetadataCompat metadata) {
        if (playbackState == null || metadata == null) {
            return;
        }

        String mediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        int id = -1;
        if (!StringUtils.isEmpty(mediaId)) id = Integer.parseInt(mediaId);

        int state = playbackState.getState();
        boolean isPlaying = (state == PlaybackStateCompat.STATE_BUFFERING) ||
                (state == PlaybackStateCompat.STATE_PLAYING);

        nowPlayingId.postValue(id);
        this.isPlaying.postValue(isPlaying);
    }



    @Override
    protected void onCleared() {
        super.onCleared();

        recentEpisodes.removeObserver(recentEpisodesObserver);
        networkOperationStatus.removeObserver(networkOperationStatusObserver);
        playerServiceConnection.getPlaybackState().removeObserver(playbackStateObserver);
        playerServiceConnection.getNowPlaying().removeObserver(mediaMetadataObserver);
    }


    public void download(Episode episode) {

        if (((BasicApp)getApplication()).isOnline())
            DownloadUtil.addDownload(episode, getApplication().getApplicationContext());
        else {
            Toast.makeText(getApplication(), "Failed to start download", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeDownload(Episode episode) {
        DownloadUtil.removeDownload(episode, getApplication().getApplicationContext());
    }

    public void markAsCompleted(Episode episode) {
        episode.markAsCompleted();
        mRepository.update(episode);
    }

    public void markAsNotCompleted(Episode episode) {
        episode.resetProgress();
        mRepository.update(episode);
    }
}