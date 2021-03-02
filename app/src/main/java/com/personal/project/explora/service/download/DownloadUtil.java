package com.personal.project.explora.service.download;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadCursor;
import com.google.android.exoplayer2.offline.DownloadIndex;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;
import com.personal.project.explora.AppExecutors;
import com.personal.project.explora.BasicApp;
import com.personal.project.explora.EpisodeRepository;
import com.personal.project.explora.db.Episode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownloadUtil {

    private static final String TAG = "DownloadUtil";

    public static void setRealDownloadsState(Application app) {
        AppExecutors appExecutors = ((BasicApp) app).getAppExecutors();
        DownloadManager downloadManager = DemoUtil.getDownloadManager(
                app.getApplicationContext(),
                appExecutors.networkIO(),
                appExecutors.diskIO());

        DownloadIndex downloadIndex = downloadManager.getDownloadIndex();
        appExecutors.diskIO().execute(() -> {
            try {
                DownloadCursor cursor = downloadIndex.getDownloads(Download.STATE_COMPLETED);
                List<Integer> downloadedIds = new ArrayList<>();
                while (cursor.moveToNext()) {
                    Download download = cursor.getDownload();
                    String downloadId = download.request.id;
                    if (downloadId != null) {
                        int id = Integer.parseInt(downloadId);
                        downloadedIds.add(id);
                    }
                    else {
                        Log.w(TAG, "onDownloadRemoved: null downloadId");
                    }

                    EpisodeRepository repo = ((BasicApp) app).getRepository();
                    repo.updateDownloads(downloadedIds);

                }

            } catch (IOException e) {
                Log.e(TAG, "getDownloads threw IOException", e);
            }
        });
    }

    /**
     * This guy automatically updates episode state in the db
     */
    public static void addDownload(Episode episode, Context context) {

        String contentId = String.valueOf(episode.getId());
        Uri contentUri = episode.getUri();

        DownloadRequest downloadRequest =
                new DownloadRequest.Builder(contentId, contentUri).build();

        DownloadService.sendAddDownload(
                context,
                DemoDownloadService.class,
                downloadRequest,
                false
        );
    }

    /**
     * This guy automatically updates episode state in the db
     */
    public static void removeDownload(Episode episode, Context context) {

        String contentId = String.valueOf(episode.getId());

        DownloadService.sendRemoveDownload(
                context,
                DemoDownloadService.class,
                contentId,
                false
        );
    }

    /*public static void removeAllDownloads(Context context) {

        DownloadService.sendRemoveAllDownloads(
                context,
                DemoDownloadService.class,
                false
        );
    }*/

}
