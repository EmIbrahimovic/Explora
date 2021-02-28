/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* ft. some small editions and additions by me */
package com.personal.project.explora.service.download;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationHelper;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.google.android.exoplayer2.util.Util;
import com.personal.project.explora.AppExecutors;
import com.personal.project.explora.BasicApp;
import com.personal.project.explora.EpisodeRepository;
import com.personal.project.explora.R;
import com.personal.project.explora.db.Episode;

import java.util.List;

import static com.personal.project.explora.service.download.DemoUtil.DOWNLOAD_NOTIFICATION_CHANNEL_ID;

/** A service for downloading media. */
public class DemoDownloadService extends DownloadService {

  private static final int JOB_ID = 1;
  private static final int FOREGROUND_NOTIFICATION_ID = 1;

  public DemoDownloadService() {
    super(
        FOREGROUND_NOTIFICATION_ID,
        DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
        DOWNLOAD_NOTIFICATION_CHANNEL_ID,
        R.string.exo_download_notification_channel_name,
        /* channelDescriptionResourceId= */ 0);
  }

  @Override
  public void onCreate() {

    super.onCreate();
  }

  @Override
  @NonNull
  protected DownloadManager getDownloadManager() {
    // This will only happen once, because getDownloadManager is guaranteed to be called only once
    // in the life cycle of the process.
    AppExecutors appExecutors = ((BasicApp) getApplication()).getAppExecutors();
    DownloadManager downloadManager = DemoUtil.getDownloadManager(
            /* context= */ getApplicationContext(), appExecutors.networkIO(), appExecutors.diskIO());

    DownloadNotificationHelper downloadNotificationHelper =
        DemoUtil.getDownloadNotificationHelper(/* context= */ this);

    downloadManager.addListener(
        new TerminalStateNotificationHelper(
            this, downloadNotificationHelper, FOREGROUND_NOTIFICATION_ID + 1));
    downloadManager.addListener(new DownloadStateListener(getApplication()));

    return downloadManager;
  }

  @Override
  protected PlatformScheduler getScheduler() {
    return new PlatformScheduler(this, JOB_ID);
  }

  @Override
  @NonNull
  protected Notification getForegroundNotification(@NonNull List<Download> downloads) {
    return DemoUtil.getDownloadNotificationHelper(/* context= */ this)
        .buildProgressNotification(
            /* context= */ this,
            R.drawable.ic_download,
            /* contentIntent= */ null,
            /* message= */ null,
            downloads);
  }

  private static final class DownloadStateListener implements DownloadManager.Listener {

    private final EpisodeRepository repo;

    public DownloadStateListener(Application app) {
      repo = ((BasicApp) app).getRepository();
    }

    @Override
    public void onDownloadChanged(DownloadManager downloadManager, Download download, @Nullable Exception finalException) {
      String downloadId = download.request.id;

      if (downloadId != null) {
        int id = Integer.parseInt(downloadId);
        if (download.state == Download.STATE_DOWNLOADING)
          repo.getFromIdAndUpdateDownloadId(id, Episode.DOWNLOADING);

        else if (download.state == Download.STATE_COMPLETED)
          repo.getFromIdAndUpdateDownloadId(id, Episode.DOWNLOADED);

        else if (download.state == Download.STATE_STOPPED)
          repo.getFromIdAndUpdateDownloadId(id, Episode.NOT_DOWNLOADED);
      }
      else {
        Log.w("DemoDownloadService", "onDownloadRemoved: mystery downloadId " + downloadId);
      }

    }

    @Override
    public void onDownloadRemoved(DownloadManager downloadManager, Download download) {
      String downloadId = download.request.id;

      if (downloadId != null) {
        int id = Integer.parseInt(downloadId);
        repo.getFromIdAndUpdateDownloadId(id, Episode.NOT_DOWNLOADED);
      }
      else {
        Log.w("DemoDownloadService", "onDownloadRemoved: mystery downloadId " + downloadId);
      }
    }

  }

  /**
   * Creates and displays notifications for downloads when they complete or fail.
   *
   * <p>This helper will outlive the lifespan of a single instance of {@link DemoDownloadService}.
   * It is static to avoid leaking the first {@link DemoDownloadService} instance.
   */
  private static final class TerminalStateNotificationHelper implements DownloadManager.Listener {

    private final Context context;
    private final DownloadNotificationHelper notificationHelper;

    private int nextNotificationId;

    public TerminalStateNotificationHelper(
            Context context, DownloadNotificationHelper notificationHelper, int firstNotificationId) {
      this.context = context.getApplicationContext();
      this.notificationHelper = notificationHelper;
      nextNotificationId = firstNotificationId;
    }

    @Override
    public void onDownloadChanged(
            DownloadManager downloadManager, Download download, @Nullable Exception finalException) {
      Notification notification;

      if (download.state == Download.STATE_COMPLETED) {
        notification =
            notificationHelper.buildDownloadCompletedNotification(
                context,
                R.drawable.ic_download_done,
                /* contentIntent= */ null,
                Util.fromUtf8Bytes(download.request.data));

      } else if (download.state == Download.STATE_FAILED) {
        notification =
            notificationHelper.buildDownloadFailedNotification(
                context,
                R.drawable.ic_download_done,
                /* contentIntent= */ null,
                Util.fromUtf8Bytes(download.request.data));

      } else {
        return;
      }
      NotificationUtil.setNotification(context, nextNotificationId++, notification);
    }
  }
}
