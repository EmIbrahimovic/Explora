package com.personal.project.explora.service;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.Gravity;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.util.NonNullApi;
import com.personal.project.explora.AppExecutors;
import com.personal.project.explora.BasicApp;
import com.personal.project.explora.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExploraPlayerNotificationManager {

    public static final String NOW_PLAYING_CHANNEL_ID = "com.example.musicplayerexample_firsttry.NOW_PLAYING";
    public static final int NOW_PLAYING_NOTIFICATION_ID = 0xb339;
    private static final int NOTIFICATION_LARGE_ICON_SIZE = 144; // px

    private AppExecutors mExecutors;

    private Context context;
    private MediaSessionCompat.Token sessionToken;
    private PlayerNotificationManager.NotificationListener notificationListener;

    private Player player = null;
    private PlayerNotificationManager notificationManager;

    public ExploraPlayerNotificationManager(Application context, MediaSessionCompat.Token sessionToken,
                                            PlayerNotificationManager.NotificationListener notificationListener) {
        this.context = context;
        this.sessionToken = sessionToken;
        this.notificationListener = notificationListener;

        MediaControllerCompat mediaController = new MediaControllerCompat(context, sessionToken);

        mExecutors = ((BasicApp)context).getAppExecutors();

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
                context,
                NOW_PLAYING_CHANNEL_ID,
                R.string.notification_channel,
                R.string.notification_channel_description,
                NOW_PLAYING_NOTIFICATION_ID,
                new DescriptionAdapter(mediaController, context),
                notificationListener);
        notificationManager.setSmallIcon(R.drawable.exo_styled_controls_vr);
        notificationManager.setMediaSessionToken(sessionToken);
        notificationManager.setUseStopAction(true);
        notificationManager.setUseChronometer(true);
        notificationManager.setUsePreviousAction(false);
        notificationManager.setControlDispatcher(new DefaultControlDispatcher(15000, 15000));
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
        private MediaControllerCompat controller;
        private PlayerServiceConnection connection;

        public DescriptionAdapter(MediaControllerCompat controller, Application app) {
            this.controller = controller;
            connection = ((BasicApp)app).getPlayerServiceConnection();
        }

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
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
