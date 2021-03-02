package com.personal.project.explora.service;

import android.app.Application;
import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Objects;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID;

public class PlayerServiceConnection {

    private static final String TAG = "PlayerServiceConnection";
    public static final PlaybackStateCompat EMPTY_PLAYBACK_STATE = new PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
            .build();
    public static final MediaMetadataCompat NOTHING_PLAYING = new MediaMetadataCompat.Builder()
            .putString(METADATA_KEY_MEDIA_ID, "-1")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
            .build();

    private static PlayerServiceConnection instance;
    private final Application context;

    private final MutableLiveData<Boolean> isConnected;
    private final MutableLiveData<Boolean> networkFailure;

    private final MutableLiveData<PlaybackStateCompat> playbackState;
    private final MutableLiveData<MediaMetadataCompat> nowPlaying;

    private final MediaBrowserConnectionCallback mediaBrowserConnectionCallback;
    private final MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;

    public PlayerServiceConnection(Application context) {
        this.context = context;

        isConnected = new MutableLiveData<>();
        isConnected.postValue(false);
        networkFailure = new MutableLiveData<>();
        networkFailure.postValue(false);

        playbackState = new MutableLiveData<>();
        playbackState.postValue(EMPTY_PLAYBACK_STATE);
        nowPlaying = new MutableLiveData<>();
        nowPlaying.postValue(NOTHING_PLAYING);

        mediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mediaBrowser = new MediaBrowserCompat(
                context,
                new ComponentName(context, PlayerService.class),
                mediaBrowserConnectionCallback,
                null);
        mediaBrowser.connect();
    }

    public static PlayerServiceConnection getInstance(Application context) {

        if (instance == null) {
            synchronized (PlayerServiceConnection.class) {
                if (instance == null) {
                    instance = new PlayerServiceConnection(context);
                }
            }
        }
        return instance;
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        return mediaController.getTransportControls();
    }

    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    public LiveData<Boolean> getNetworkFailure() {
        return networkFailure;
    }

    public LiveData<PlaybackStateCompat> getPlaybackState() {
        return playbackState;
    }

    public LiveData<MediaMetadataCompat> getNowPlaying() {
        return nowPlaying;
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

        //might need var here for context but might not

        @Override
        public void onConnected() {

            mediaController = new MediaControllerCompat(context, mediaBrowser.getSessionToken());
            mediaController.registerCallback(new MediaControllerCallback());

            isConnected.postValue(true);
        }

        @Override
        public void onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
            isConnected.postValue(false);
            //Log.w(TAG, "onConnectionSuspended: The Service has crashed.");
        }

        @Override
        public void onConnectionFailed() {
            // The Service has refused our connection
            isConnected.postValue(false);
            //Log.w(TAG, "onConnectionFailed: The Service has refused our connection");
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            if (Objects.equals(event, PlayerService.NETWORK_FAILURE))
                networkFailure.postValue(true);
        }

        @Override
        public void onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            playbackState.postValue(state == null ? EMPTY_PLAYBACK_STATE : state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {

            if (metadata.getString(METADATA_KEY_MEDIA_ID) == null ||
                    Objects.equals(metadata.getString(METADATA_KEY_MEDIA_ID), "-1") ||
                    metadata.getDescription().getTitle() == null)
                return;

            nowPlaying.postValue(metadata);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }
    }

}
