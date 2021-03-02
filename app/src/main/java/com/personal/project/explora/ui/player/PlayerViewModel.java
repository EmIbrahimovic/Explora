package com.personal.project.explora.ui.player;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.personal.project.explora.BasicApp;
import com.personal.project.explora.R;
import com.personal.project.explora.service.PlayerServiceConnection;
import com.personal.project.explora.utils.StringUtils;

import static com.personal.project.explora.service.PlayerServiceConnection.EMPTY_PLAYBACK_STATE;
import static com.personal.project.explora.service.PlayerServiceConnection.NOTHING_PLAYING;

public class PlayerViewModel extends AndroidViewModel {

    private static final long POSITION_UPDATE_INTERVAL_MILLIS = 100L;

    public static final int RES_PLAY_ARROW = R.drawable.exo_controls_play;
    public static final int RES_PAUSE_LINES = R.drawable.exo_controls_pause;
    public static final int RES_REPLAY = R.drawable.ic_replay_32;

    private final Handler handler;

    private final PlayerServiceConnection playerServiceConnection;
    private PlaybackStateCompat playbackState;

    private final MutableLiveData<NowPlayingMetadata> mediaMetadata;
    private final MutableLiveData<Long> mediaPosition;
    private final MutableLiveData<Integer> mediaButtonResource;

    private boolean updatePosition;

    private final Observer<PlaybackStateCompat> playbackStateObserver;
    private final Observer<MediaMetadataCompat> mediaMetadataObserver;

    public PlayerViewModel(@NonNull Application application) {
        super(application);

        playerServiceConnection = ((BasicApp)application).getPlayerServiceConnection();

        playbackState = EMPTY_PLAYBACK_STATE;
        mediaMetadata = new MutableLiveData<>();
        mediaPosition = new MutableLiveData<>();
        mediaPosition.postValue(0L);
        mediaButtonResource = new MutableLiveData<>();
        mediaButtonResource.postValue(R.drawable.circle_button);

        updatePosition = true;
        handler = new Handler(Looper.getMainLooper());

        playbackStateObserver = playbackStateCompat -> {
            playbackState = (playbackStateCompat == null) ?
                    EMPTY_PLAYBACK_STATE : playbackStateCompat;

            MediaMetadataCompat metadata = playerServiceConnection.getNowPlaying().getValue();
            if (metadata == null) metadata = NOTHING_PLAYING;

            updateState(playbackState, metadata);
        };
        playerServiceConnection.getPlaybackState().observeForever(playbackStateObserver);

        mediaMetadataObserver = mediaMetadataCompat -> updateState(playbackState, mediaMetadataCompat);
        playerServiceConnection.getNowPlaying().observeForever(mediaMetadataObserver);

        checkPlaybackPosition();
    }

    public MutableLiveData<NowPlayingMetadata> getMediaMetadata() {
        return mediaMetadata;
    }

    public LiveData<Long> getMediaPosition() {
        return mediaPosition;
    }

    public LiveData<Integer> getMediaButtonResource() {
        return mediaButtonResource;
    }

    private void checkPlaybackPosition() {
        handler.postDelayed(() -> {
            long currentPosition;
            if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                long timeDelta =
                        SystemClock.elapsedRealtime() - playbackState.getLastPositionUpdateTime();
                currentPosition = (long)
                        (playbackState.getPosition() + (timeDelta * playbackState.getPlaybackSpeed()));
            } else {
                currentPosition = playbackState.getPosition();
            }

            if (mediaPosition.getValue() != null && mediaPosition.getValue() != currentPosition)
                mediaPosition.postValue(currentPosition);

            if (updatePosition)
                checkPlaybackPosition();

        }, POSITION_UPDATE_INTERVAL_MILLIS);
    }

    public void updateState(PlaybackStateCompat playbackState, MediaMetadataCompat mediaMetadata) {

        if (mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) != 0L &&
                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null &&
                !mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).equals("-1")) {

            NowPlayingMetadata nowPlayingMetadata = new NowPlayingMetadata(
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID),
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI),
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                    mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            );

            this.mediaMetadata.postValue(nowPlayingMetadata);
        }

        mediaButtonResource.postValue(
                (playbackState.getState() == PlaybackStateCompat.STATE_BUFFERING ||
                        playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) ?
                        RES_PAUSE_LINES : RES_PLAY_ARROW);
        if (getMediaMetadata().getValue() != null && getMediaPosition().getValue() != null &&
                getMediaPosition().getValue() + 1500 >= getMediaMetadata().getValue().durationMs) {
            mediaButtonResource.postValue(RES_REPLAY);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        playerServiceConnection.getPlaybackState().removeObserver(playbackStateObserver);
        playerServiceConnection.getNowPlaying().removeObserver(mediaMetadataObserver);

        updatePosition = false;
    }

    static class NowPlayingMetadata {

        int id;
        int albumArt;
        String title;
        long durationMs;
        String duration;

        public NowPlayingMetadata(String mediaId, String albumArt, String title, long duration) {
            if (mediaId != null) this.id = Integer.parseInt(mediaId);
            if (albumArt != null) this.albumArt = Integer.parseInt(albumArt);
            this.title = title;
            durationMs = duration;
            this.duration = StringUtils.timestampToMSS(duration);
        }
    }
}