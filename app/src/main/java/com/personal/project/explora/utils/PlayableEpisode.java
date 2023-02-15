package com.personal.project.explora.utils;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.personal.project.explora.R;
import com.personal.project.explora.db.Episode;

import java.util.Objects;

public class PlayableEpisode extends Episode {

    public static final int RES_PLAY = R.drawable.exo_controls_play;
    public static final int RES_PAUSE = R.drawable.exo_controls_pause;
    public static String PLAYBACK_RES_CHANGED = "PLAYBACK_RES_CHANGED";
    public static String LAST_POSITION_CHANGED = "LAST_POSITION_CHANGED";
    public static String DESCRIPTION_CHANGED = "DESCRIPTION_CHANGED";
    public static  DiffUtil.ItemCallback<PlayableEpisode> diffCallback =
            new DiffUtil.ItemCallback<PlayableEpisode> () {

                @Override
                public boolean areItemsTheSame(@NonNull PlayableEpisode oldItem, @NonNull PlayableEpisode newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull PlayableEpisode oldItem, @NonNull PlayableEpisode newItem) {
                    return (oldItem.getId() == newItem.getId() &&
                            Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
                            oldItem.getLastPosition() == newItem.getLastPosition() &&
                            oldItem.getDownloadState() == newItem.getDownloadState() &&
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

                    return payload;
                }
            };
    private int playbackRes;


    /*
     * DIFF UTIL
     */

    public PlayableEpisode(Episode episode) {
        super(episode);
        this.playbackRes = RES_PLAY;
    }

    public int getPlaybackRes() {
        return playbackRes;
    }

    public void setPlaybackRes(int playbackRes) {
        if (isValidPlaybackRes(playbackRes))
            this.playbackRes = playbackRes;
    }

    private boolean isValidPlaybackRes(int playbackRes) {
        return playbackRes == RES_PLAY || playbackRes == RES_PAUSE;
    }

}
