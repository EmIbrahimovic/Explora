package com.personal.project.explora.utils;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.personal.project.explora.R;
import com.personal.project.explora.db.Episode;

import java.util.Objects;

public class PlayableEpisode extends Episode {

    public static final int RES_PLAY = R.drawable.ic_play;
    public static final int RES_PAUSE = R.drawable.ic_pause;
    public static String PLAYBACK_RES_CHANGED = "PLAYBACK_RES_CHANGED";
    public static String LAST_POSITION_CHANGED = "LAST_POSITION_CHANGED";
    public static String DESCRIPTION_CHANGED = "DESCRIPTION_CHANGED";
    public static String NON_PLAYABLE_CHANGED = "NON_PLAYABLE_CHANGED";
    public static String DOWNLOAD_STATE_CHANGED = "DOWNLOAD_STATE_CHANGED";

    public static  DiffUtil.ItemCallback<PlayableEpisode> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PlayableEpisode> () {

                @Override
                public boolean areItemsTheSame(@NonNull PlayableEpisode oldItem, @NonNull PlayableEpisode newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull PlayableEpisode oldItem, @NonNull PlayableEpisode newItem) {
                    return (Objects.equals(oldItem.getDatePublished(), newItem.getDatePublished()) &&
                            Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
                            oldItem.getLastPosition() == newItem.getLastPosition() &&
                            oldItem.getDownloadState() == newItem.getDownloadState() &&
                            oldItem.isNonPlayable() == newItem.isNonPlayable() &&
                            oldItem.playbackRes == newItem.playbackRes);
                }

                @Override
                public Object getChangePayload(@NonNull PlayableEpisode oldItem, @NonNull PlayableEpisode newItem) {
                    Bundle payload = new Bundle();
                    if (!Objects.equals(oldItem.getDescription(), newItem.getDescription())) {
                        payload.putInt(DESCRIPTION_CHANGED, 1);
                    }
                    if (oldItem.playbackRes != newItem.playbackRes) {
                        payload.putInt(PLAYBACK_RES_CHANGED, 1);
                    }
                    if (oldItem.getLastPosition() != newItem.getLastPosition()) {
                        payload.putInt(LAST_POSITION_CHANGED, 1);
                    }
                    if (oldItem.isNonPlayable() != newItem.isNonPlayable()) {
                        payload.putInt(NON_PLAYABLE_CHANGED, 1);
                    }
                    if (oldItem.getDownloadState() != newItem.getDownloadState()) {
                        payload.putInt(DOWNLOAD_STATE_CHANGED, 1);
                    }

                    return payload;
                }
            };

    private int playbackRes;
    private boolean nonPlayable;

    /*
     * DIFF UTIL
     */

    public PlayableEpisode(Episode episode) {
        super(episode);
        this.playbackRes = RES_PLAY;
        this.nonPlayable = false;
    }

    public int getPlaybackRes() {
        return playbackRes;
    }

    public void setPlaybackRes(int playbackRes) {
        if (isValidPlaybackRes(playbackRes))
            this.playbackRes = playbackRes;
    }

    public boolean isNonPlayable() {
        return nonPlayable;
    }

    public void setNonPlayable(boolean nonPlayable) {
            if (!(this.getDownloadState() == DOWNLOADED && nonPlayable))
            this.nonPlayable = nonPlayable;
    }

    private boolean isValidPlaybackRes(int playbackRes) {
        return playbackRes == RES_PLAY || playbackRes == RES_PAUSE;
    }

}
