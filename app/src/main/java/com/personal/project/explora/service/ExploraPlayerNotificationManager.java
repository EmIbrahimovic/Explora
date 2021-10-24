package com.personal.project.explora.service;

import android.app.Application;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.Gravity;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.personal.project.explora.AppExecutors;
import com.personal.project.explora.BasicApp;
import com.personal.project.explora.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;

public class ExploraPlayerNotificationManager {

    public static final String NOW_PLAYING_CHANNEL_ID = "com.personal.project.explora.service.NOW_PLAYING";
    public static final int NOW_PLAYING_NOTIFICATION_ID = 0xb339;
    private static final int NOTIFICATION_LARGE_ICON_SIZE = 144; // px

    private final AppExecutors mExecutors;

    private final PlayerNotificationManager notificationManager;

    public ExploraPlayerNotificationManager(Application context, MediaSessionCompat.Token sessionToken,
                                            PlayerNotificationManager.NotificationListener notificationListener) {

        MediaControllerCompat mediaController = new MediaControllerCompat(context, sessionToken);

        mExecutors = ((BasicApp)context).getAppExecutors();

        notificationManager = new PlayerNotificationManager.Builder(context,
                NOW_PLAYING_NOTIFICATION_ID,
                NOW_PLAYING_CHANNEL_ID)
                .setChannelNameResourceId(R.string.notification_channel)
                .setChannelDescriptionResourceId(R.string.notification_channel_description)
                .setMediaDescriptionAdapter(new DescriptionAdapter(mediaController, context))
                .setNotificationListener(notificationListener)
                .setSmallIconResourceId(R.drawable.ic_play_circle_filled)
                .build();

        notificationManager.setMediaSessionToken(sessionToken);
        notificationManager.setUseStopAction(true);
        notificationManager.setUseChronometer(true);
        notificationManager.setUsePreviousAction(false);
        notificationManager.setUseRewindAction(true);
        notificationManager.setUseFastForwardAction(true);
    }

    public void hideNotification() {
        notificationManager.setPlayer(null);
    }

    public void showNotificationForPlayer(Player player) {
        notificationManager.setPlayer(player);
    }


    private class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

        Integer currentIconRes = null;
        Bitmap currentBitmap = null;
        private final MediaControllerCompat controller;
        private final PlayerServiceConnection connection;

        public DescriptionAdapter(MediaControllerCompat controller, Application app) {
            this.controller = controller;
            connection = ((BasicApp)app).getPlayerServiceConnection();
        }

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            // TODO
            // My addition! and probably bad practice! The thing is that controller.getMetadata()
            // didn't work for me since the metadata gets updated twice when I start the service:
            // the first time by me and the second time by God knows what. I fixed this in the
            // connection by adding a check that the metadata is not null when updating nowPlaying,
            // and the only way I saw to fix the issue in the notification manager was to take the
            // data from the connection instead of the controller
            MediaMetadataCompat metadata = connection.getNowPlaying().getValue();
            if (metadata == null) {
                metadata = controller.getMetadata();
            }
            return metadata.getDescription().getTitle();
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            return controller.getSessionActivity();
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            MediaMetadataCompat metadata = connection.getNowPlaying().getValue();
            if (metadata == null) {
                metadata = controller.getMetadata();
            }
            return metadata.getDescription().getDescription();
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            MediaMetadataCompat metadata = connection.getNowPlaying().getValue();
            if (metadata == null) {
                metadata = controller.getMetadata();
            }

            Uri iconUri = metadata.getDescription().getIconUri();
            Integer iconRes = null;
            if (iconUri != null)
                iconRes = Integer.parseInt(iconUri.toString());

            if (Objects.equals(currentIconRes, iconRes) || currentBitmap == null) {
                currentIconRes = iconRes;
                Integer finalIconRes = iconRes;
                if (finalIconRes == null) return null;
                mExecutors.diskIO().execute(() -> {
                    try {
                        currentBitmap = resolveUriAsBitmap(finalIconRes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    callback.onBitmap(currentBitmap);
                });
            }

            return currentBitmap;
        }

        private Bitmap resolveUriAsBitmap(Integer res) throws IOException {
            return Picasso.get()
                    .load(res)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .resize(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
                    .centerCrop(Gravity.START)
                    .get();
        }
    }

}
