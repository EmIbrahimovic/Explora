package com.personal.project.explora.ui;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.personal.project.explora.BasicApp;
import com.personal.project.explora.db.Episode;
import com.personal.project.explora.service.PlayerServiceConnection;
import com.personal.project.explora.ui.player.PlayerFragment;
import com.personal.project.explora.utils.Event;
import com.personal.project.explora.utils.PlayableEpisode;
import com.personal.project.explora.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class MainActivityViewModel extends AndroidViewModel {

    private static final String TAG = "MainActivityViewModel";

    public static final Boolean NETWORK_AVAILABLE = Boolean.TRUE;
    public static final Boolean NETWORK_UNAVAILABLE = Boolean.FALSE;

    private final PlayerServiceConnection playerServiceConnection;
    private final MutableLiveData<Boolean> networkAvailability;
    private final List<String> networks;

    private final MutableLiveData<Event<FragmentNavigationRequest>> navigateToFragment;
    private final MutableLiveData<Event<Boolean>> backPressedEvent;
    private final MutableLiveData<Event<Boolean>> bottomNavigationVisibleEvent;
    private boolean isServiceConnected;
    private Object thingToPlay;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        playerServiceConnection = ((BasicApp)application).getPlayerServiceConnection();

        navigateToFragment = new MutableLiveData<>();
        navigateToFragment.postValue(null);

        backPressedEvent = new MutableLiveData<>();
        bottomNavigationVisibleEvent = new MutableLiveData<>();

        networkAvailability = new MutableLiveData<>();
        networks = new ArrayList<>();

        isServiceConnected = false;
        thingToPlay = null;

        playerServiceConnection.getIsConnected().observeForever(isConnected -> {
            if (isConnected) {
                isServiceConnected = true;
                if (thingToPlay != null) {
                    if (thingToPlay instanceof Integer) playMediaId((Integer) thingToPlay);
                    if (thingToPlay instanceof PlayableEpisode) playMedia((PlayableEpisode) thingToPlay);
                }
            }
            else {
                isServiceConnected = false;
            }
        });

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        ConnectivityManager connectivityManager =
                application.getSystemService(ConnectivityManager.class);
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (!networks.contains(network.toString())) {
                    networks.add(network.toString());
                    notifyNetworkAvailable();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                networks.remove(network.toString());

                if (networks.isEmpty())
                    notifyNetworkUnavailable();
            }
        };
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    public LiveData<Boolean> getNetworkAvailability() {
        return networkAvailability;
    }

    public LiveData<Event<FragmentNavigationRequest>> getNavigateToFragment() {
        return navigateToFragment;
    }

    public boolean playableEpisodeClicked(Episode clickedEpisode) {
        boolean newRecent = playMedia(clickedEpisode);
        showFragment(PlayerFragment.newInstance());

        return newRecent;
    }

    private void showFragment(Fragment fragment) {
        FragmentNavigationRequest newFragmentNavigationRequest = new FragmentNavigationRequest(
                fragment, false, null);
        Event<FragmentNavigationRequest> newEvent = new Event<>(newFragmentNavigationRequest);
        navigateToFragment.postValue(newEvent);
    }

    private boolean playMedia(Episode episode) {

        MediaMetadataCompat nowPlaying = playerServiceConnection.getNowPlaying().getValue();
        PlaybackStateCompat playbackState = playerServiceConnection.getPlaybackState().getValue();

        if (!isServiceConnected) {
            thingToPlay = episode;
            return false;
        }

        boolean toRet = false;
        if (nowPlaying != null && playbackState != null) {
            String mediaId = nowPlaying.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            int id = -1;
            if (!StringUtils.isEmpty(mediaId)) id = Integer.parseInt(mediaId);

            int state = playbackState.getState();
            long actions = playbackState.getActions();
            boolean isPrepared = (state == PlaybackStateCompat.STATE_BUFFERING) ||
                    (state == PlaybackStateCompat.STATE_PLAYING) ||
                    (state == PlaybackStateCompat.STATE_PAUSED);

            MediaControllerCompat.TransportControls transportControls =
                    playerServiceConnection.getTransportControls();

            if (isPrepared && episode.getId() == id) {

                if ((state == PlaybackStateCompat.STATE_BUFFERING) ||
                        (state == PlaybackStateCompat.STATE_PLAYING)) // isPlaying
                {
                    transportControls.pause();
                }
                else if (((actions & PlaybackStateCompat.ACTION_PLAY) != 0L) ||
                        (((actions & PlaybackStateCompat.ACTION_PLAY_PAUSE) != 0L) && // is playEnabled
                                (state == PlaybackStateCompat.STATE_PAUSED)))
                {
                    transportControls.play();
                }
                else {
                    Log.w(TAG, "Playable item clicked but neither play nor pause are enabled! " + id);
                }

            } else {
                transportControls.playFromMediaId(String.valueOf(episode.getId()), null);
                toRet = true;
            }
        }

        return toRet;
    }

    public void playMediaId(int mediaId) {
        MediaMetadataCompat nowPlaying = playerServiceConnection.getNowPlaying().getValue();
        PlaybackStateCompat playbackState = playerServiceConnection.getPlaybackState().getValue();

        if (!isServiceConnected) {
            thingToPlay = mediaId;
            return;
        }

        if (nowPlaying != null && playbackState != null) {
            String mediaIdPlaying = nowPlaying.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            int idPlaying = -1;
            if (!StringUtils.isEmpty(mediaIdPlaying)) idPlaying = Integer.parseInt(mediaIdPlaying);

            int state = playbackState.getState();
            long actions = playbackState.getActions();
            boolean isPrepared = (state == PlaybackStateCompat.STATE_BUFFERING) ||
                    (state == PlaybackStateCompat.STATE_PLAYING) ||
                    (state == PlaybackStateCompat.STATE_PAUSED);

            MediaControllerCompat.TransportControls transportControls =
                    playerServiceConnection.getTransportControls();

            if (isPrepared && mediaId == idPlaying) {

                if ((state == PlaybackStateCompat.STATE_BUFFERING) ||
                        (state == PlaybackStateCompat.STATE_PLAYING))  // isPlaying
                {
                    transportControls.pause();
                }
                else if (((actions & PlaybackStateCompat.ACTION_PLAY) != 0L) ||
                        (((actions & PlaybackStateCompat.ACTION_PLAY_PAUSE) != 0L) &&
                                (state == PlaybackStateCompat.STATE_PAUSED))) // isPlayEnabled
                {
                    transportControls.play();
                }
                else {
                    Log.w(TAG, "Playable item clicked but neither play nor pause are enabled! " + idPlaying);
                }
            } else {
                transportControls.playFromMediaId(String.valueOf(mediaId), null);
            }
        }
    }

    public void seekTo(int position) {

        MediaMetadataCompat nowPlaying = playerServiceConnection.getNowPlaying().getValue();
        PlaybackStateCompat playbackState = playerServiceConnection.getPlaybackState().getValue();

        if (!isServiceConnected) {
            return;
        }

        if (nowPlaying != null && playbackState != null) {

            int state = playbackState.getState();
            boolean isPrepared = (state == PlaybackStateCompat.STATE_BUFFERING) ||
                    (state == PlaybackStateCompat.STATE_PLAYING) ||
                    (state == PlaybackStateCompat.STATE_PAUSED);

            MediaControllerCompat.TransportControls transportControls =
                    playerServiceConnection.getTransportControls();

            if (isPrepared) {
                transportControls.seekTo(position);
            }

        }

    }

    private void notifyNetworkAvailable() {
        networkAvailability.postValue(NETWORK_AVAILABLE);
    }

    private void notifyNetworkUnavailable() {
        networkAvailability.postValue(NETWORK_UNAVAILABLE);
    }

    public void onBackPressed() {
        backPressedEvent.postValue(new Event<>(Boolean.TRUE));
    }

    public LiveData<Event<Boolean>> getBackPressed() {
        return backPressedEvent;
    }

    public void hideBottomNavigation() {
        bottomNavigationVisibleEvent.postValue(new Event<>(Boolean.FALSE));
    }

    public void showBottomNavigation() {
        bottomNavigationVisibleEvent.postValue(new Event<>(Boolean.TRUE));
    }

    public LiveData<Event<Boolean>> getBottomNavigationVisibleEvent() {
        return bottomNavigationVisibleEvent;
    }

    static class FragmentNavigationRequest {
        Fragment fragment;
        Boolean backStack;
        String tag;

        public FragmentNavigationRequest(Fragment fragment, Boolean backStack, String tag) {
            this.fragment = fragment;
            this.backStack = backStack;
            this.tag = tag;
        }
    }
}
