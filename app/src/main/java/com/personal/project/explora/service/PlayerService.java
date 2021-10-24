package com.personal.project.explora.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.personal.project.explora.AppExecutors;
import com.personal.project.explora.BasicApp;
import com.personal.project.explora.EpisodeRepository;
import com.personal.project.explora.R;
import com.personal.project.explora.db.Episode;
import com.personal.project.explora.service.download.DemoUtil;
import com.personal.project.explora.utils.YearsData;

import java.util.List;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID;

public class PlayerService extends MediaBrowserServiceCompat
        implements EpisodeRepository.EpisodeRetrievedListenerForPrepare {

    public static final String
            NETWORK_FAILURE = "com.personal.project.explora.service.NETWORK_FAILURE";
    private static final String TAG = "PlayerService";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private final AudioAttributes mAudioAttributes = new AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build();
    private ExploraPlayerNotificationManager notificationManager;
    private EpisodeRepository mRepository;
    private Episode currentlyPlaying;
    private SimpleExoPlayer mExoPlayer;
    private PlaybackStateListener mPlaybackStateListener;
    private MediaSessionCompat mMediaSession;
    private boolean isForegroundService = false;
    private AppExecutors mExecutors;

    private Handler mHandler;

    /*

    SERVICE LIFECYCLE CALLBACKS

     */

    @Override
    public void onCreate() {
        super.onCreate();
        mRepository = ((BasicApp)getApplication()).getRepository();
        mExecutors = ((BasicApp) getApplication()).getAppExecutors();

        Intent sessionIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent sessionActivityPendingIntent =
                PendingIntent.getActivity(this, 0, sessionIntent, 0);

        initializePlayer();

        mMediaSession = new MediaSessionCompat(this, TAG);
        mMediaSession.setSessionActivity(sessionActivityPendingIntent);
        mMediaSession.setActive(true);

        MediaSessionCompat.Token mSessionToken = mMediaSession.getSessionToken();

        setSessionToken(mSessionToken);

        notificationManager = new ExploraPlayerNotificationManager(
                getApplication(),
                mSessionToken,
                new PlayerNotificationListener()
        );

        MediaSessionConnector mMediaSessionConnector = new MediaSessionConnector(mMediaSession);
        mMediaSessionConnector.setPlaybackPreparer(new MyPlaybackPreparer());
        mMediaSessionConnector.setPlayer(mExoPlayer);

        mHandler = new Handler(Looper.myLooper());

        notificationManager.showNotificationForPlayer(mExoPlayer);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

/*
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        saveLastPosition();
        super.onTaskRemoved(rootIntent);

        mExoPlayer.pause();
        mExoPlayer.stop(true);
    }
*/

    @Override
    public void onDestroy() {
        super.onDestroy();

        mMediaSession.setActive(false);
        mMediaSession.release();
        releasePlayer();
    }

    /*

    OTHER METHODS

     */

    private void initializePlayer() {
        if (mExoPlayer == null) {
            mPlaybackStateListener = new PlaybackStateListener();

            mExoPlayer = new SimpleExoPlayer.Builder(this)
                    .setMediaSourceFactory(new DefaultMediaSourceFactory(
                            DemoUtil.getDataSourceFactory(this, mExecutors.networkIO())))
                    .setSeekBackIncrementMs(15000)
                    .setSeekForwardIncrementMs(15000)
                    .build();
            mExoPlayer.setAudioAttributes(mAudioAttributes, true);
            mExoPlayer.addListener(mPlaybackStateListener);
        }
    }

    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.removeListener(mPlaybackStateListener);
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    private void prepareItem(MediaMetadataCompat itemToPlay,
                             Boolean playWhenReady,
                             Long playbackStartPositionMs) {

        Uri uri = Uri.parse(itemToPlay.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(uri)
                .setMediaId(itemToPlay.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))
                .setMediaMetadata(new MediaMetadata.Builder()
                        .setTitle(itemToPlay.getString(MediaMetadataCompat.METADATA_KEY_TITLE)).build())
                .build();

        mExoPlayer.setPlayWhenReady(playWhenReady);
        mExoPlayer.stop();
        mExoPlayer.clearMediaItems();
        mExoPlayer.setMediaItem(mediaItem, playbackStartPositionMs);
        mExoPlayer.prepare();
        mMediaSession.setMetadata(itemToPlay);
    }

    @Override
    public void onEpisodeRetrieved(Episode episode, boolean playWhenReady, Bundle extras) {
        if (currentlyPlaying != null && mExoPlayer.getCurrentPosition() != 0) {
            saveLastPosition();
        }
        currentlyPlaying = episode;
        if (currentlyPlaying == null) {
            Toast.makeText(PlayerService.this, "An error occurred", Toast.LENGTH_LONG).show();
            //Log.e(TAG, "onPrepareFromMediaId - onEpisodeRetrieved: episode is null");
            return;
        }

        int yearRes = YearsData.getYearImageRes(currentlyPlaying.getYear());
        MediaMetadataCompat item = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(currentlyPlaying.getId()))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentlyPlaying.getTitle())
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, currentlyPlaying.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, String.valueOf(yearRes))
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, currentlyPlaying.getLink())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currentlyPlaying.getDuration())
                .build();

        long playbackStartPositionMs = currentlyPlaying.getLastPosition();
        if (playbackStartPositionMs + 1500 >= currentlyPlaying.getDuration()) {
            playbackStartPositionMs = 0L;
        }

        prepareItem(item, playWhenReady, playbackStartPositionMs);
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }

    private void saveLastPosition() {
        currentlyPlaying.setLastPosition(mExoPlayer.getCurrentPosition());
        mRepository.update(currentlyPlaying);
    }

    /*

    INNER CLASSES

     */

    private class PlayerNotificationListener implements PlayerNotificationManager.NotificationListener {
        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                        getApplicationContext(),
                        new Intent(getApplicationContext(), PlayerService.class)
                );
            }

            startForeground(notificationId, notification);
            isForegroundService = true;
        }

        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            stopForeground(true);
            isForegroundService = false;
            stopSelf();
        }
    }

    private class PlaybackStateListener implements Player.Listener {

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            saveLastPosition();

            if (!playWhenReady) {
                stopForeground(false);
            }
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {

            if (playbackState == ExoPlayer.STATE_BUFFERING ||
                    playbackState == ExoPlayer.STATE_READY) {

                notificationManager.showNotificationForPlayer(mExoPlayer);

                if (playbackState == ExoPlayer.STATE_READY) {
                    saveLastPosition();
                }
            } else {
                notificationManager.hideNotification();
            }
        }

        @Override
        public void onPlayerError(PlaybackException error) {

            Toast.makeText(PlayerService.this, R.string.an_error_occurred, Toast.LENGTH_LONG).show();
        }

        /*@Override
        public void onPlayerError(ExoPlaybackException error) {

            String message = "An unexpected error occurred.";

            switch (error.type) {

                case ExoPlaybackException.TYPE_SOURCE:
                    message = "Unable to locate selected media. No internet connection.";
                    //Log.e(TAG, "TYPE_SOURCE: " + error.getSourceException().getMessage());
                    break;
                case ExoPlaybackException.TYPE_RENDERER:
                    //Log.e(TAG, "TYPE_RENDERER: " + error.getRendererException().getMessage());
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    //Log.e(TAG, "TYPE_UNEXPECTED: " + error.getUnexpectedException().getMessage());
                    break;
                case ExoPlaybackException.TYPE_REMOTE:
                    //Log.e(TAG, "TYPE_REMOTE: " + error.getMessage());
                    break;
            }

            Toast.makeText(PlayerService.this, message, Toast.LENGTH_LONG).show();
        }*/
    }

    private class MyPlaybackPreparer implements MediaSessionConnector.PlaybackPreparer {

        @Override
        public long getSupportedPrepareActions() {
            return ACTION_PREPARE_FROM_MEDIA_ID | ACTION_PLAY_FROM_MEDIA_ID;
        }

        @Override
        public void onPrepare(boolean playWhenReady) { }

        @Override
        public void onPrepareFromMediaId(String mediaId, boolean playWhenReady, @Nullable Bundle extras) {
            mRepository.getEpisodeFromIdForPrepare(Integer.parseInt(mediaId), PlayerService.this,
                    playWhenReady, extras);
        }

        @Override
        public void onPrepareFromSearch(String query, boolean playWhenReady, @Nullable Bundle extras) { }

        @Override
        public void onPrepareFromUri(Uri uri, boolean playWhenReady, @Nullable Bundle extras) { }

        @Override
        public boolean onCommand(Player player, ControlDispatcher controlDispatcher, String command, @Nullable Bundle extras, @Nullable ResultReceiver cb) {
            return false;
        }

    }

}
