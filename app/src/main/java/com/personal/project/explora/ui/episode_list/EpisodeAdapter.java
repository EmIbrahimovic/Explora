package com.personal.project.explora.ui.episode_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.project.explora.R;
import com.personal.project.explora.db.Episode;
import com.personal.project.explora.utils.PlayableEpisode;
import com.personal.project.explora.utils.StringUtils;
import com.personal.project.explora.utils.YearsData;
import com.squareup.picasso.Picasso;

public class EpisodeAdapter extends ListAdapter<PlayableEpisode, EpisodeAdapter.EpisodeHolder> {

    private final EpisodeClickedListener episodeClickedListener;

    protected EpisodeAdapter(EpisodeClickedListener episodeClickedListener) {
        super(PlayableEpisode.DIFF_CALLBACK);
        this.episodeClickedListener = episodeClickedListener;
    }

    @NonNull
    @Override
    public EpisodeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.episode_card, parent, false);

        return new EpisodeHolder(itemView);
    }

//    @Override
//    public void onBindViewHolder(@NonNull EpisodeHolder holder, int position, @NonNull List<Object> payloads) {
//
//        PlayableEpisode currentEpisode = getItem(position);
//        boolean fullRefresh = payloads.isEmpty();
//
//        if (!fullRefresh) {
//            for (int i = 0; i < payloads.size(); i++) {
//                Bundle payload = (Bundle)payloads.get(i);
//
//                if (payload.getInt(PlayableEpisode.DESCRIPTION_CHANGED, -1) != -1)
//                    holder.textViewDescription.setText(currentEpisode.getDescription());
//
//                if (payload.getInt(PlayableEpisode.LAST_POSITION_CHANGED, -1) != -1 ||
//                        payload.getInt(PlayableEpisode.PLAYBACK_RES_CHANGED, -1) != -1) {
//                    holder.setLastPositionText(currentEpisode.getPlaybackRes(),
//                            currentEpisode.isCompleted(), currentEpisode.getLastPosition());
//
//                    holder.buttonCompleted.setOnClickListener(v -> {
//                        Episode clone = new Episode(currentEpisode);
//                        episodeClickedListener.onCompleteEpisodeClicked(clone); });
//                    holder.setBackgroundColor(currentEpisode.isCompleted());
//                    holder.setCompletedButtonResource(currentEpisode.isCompleted());
//                }
//
//                if (payload.getInt(PlayableEpisode.PLAYBACK_RES_CHANGED, -1) != -1)
//                    holder.buttonPlay.setImageResource(currentEpisode.getPlaybackRes());
//
//                if (payload.getInt(PlayableEpisode.DOWNLOAD_STATE_CHANGED, -1) != -1) {
//                    holder.setDownloadSituation(currentEpisode.getDownloadState());
//                    holder.buttonDownload.setOnClickListener(v -> {
//                        Episode clone = new Episode(currentEpisode);
//                        episodeClickedListener.onDownloadEpisodeClicked(clone);
//                    });
//                }
//
//                if (payload.getInt(PlayableEpisode.NON_PLAYABLE_CHANGED, -1) != -1)
//                    holder.setPlayableSituation(currentEpisode.isNonPlayable(),
//                            currentEpisode.getDownloadState());
//            }
//        }
//
//        if (fullRefresh) {
//            onBindViewHolder(holder, position);
//        }
//
//    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeHolder holder, int position) {
        PlayableEpisode currentEpisode = getItem(position);

        holder.setBackgroundColor(currentEpisode.isCompleted());

        holder.textViewTitle.setText(currentEpisode.getTitle());

        holder.setDescription(currentEpisode.getDescription());

        Picasso.get()
                .load(YearsData.getYearImageRes())
                .into(holder.image);

        holder.buttonShare.setOnClickListener(v ->
                episodeClickedListener.onShareEpisodeClicked(currentEpisode));
        holder.buttonShare.setText(R.string.share_button_text);

        holder.buttonDownload.setOnClickListener(v -> {
            Episode clone = new Episode(currentEpisode);
            episodeClickedListener.onDownloadEpisodeClicked(clone);
        });
        holder.setDownloadSituation(currentEpisode.getDownloadState());

        holder.buttonPlay.setOnClickListener(v -> {
            Episode clone = new Episode(currentEpisode);
            episodeClickedListener.onPlayEpisodeClicked(clone);
        });
        holder.buttonPlay.setImageResource(currentEpisode.getPlaybackRes());
        holder.setLastPositionText(currentEpisode.getPlaybackRes(),
                currentEpisode.isCompleted(), currentEpisode.getLastPosition());

        holder.buttonCompleted.setOnClickListener(v -> {
                Episode clone = new Episode(currentEpisode);
                episodeClickedListener.onCompleteEpisodeClicked(clone); });
        holder.setCompletedButtonResource(currentEpisode.isCompleted());

        holder.setPlayableSituation(currentEpisode.isNonPlayable(),
                currentEpisode.getDownloadState());
    }

    public interface EpisodeClickedListener {

        void onPlayEpisodeClicked(Episode episode);

        void onDownloadEpisodeClicked(Episode episode);

        void onCompleteEpisodeClicked(Episode episode);

        void onShareEpisodeClicked(Episode episode);
    }

    static class EpisodeHolder extends RecyclerView.ViewHolder {

        private final CardView container;
        private final TextView textViewTitle;
        private final TextView textViewDescription;
        private final TextView textViewLastPosition;
        private final TextView nonPlayableText;
        private final ImageView image;
        private final Button buttonShare;
        private final Button buttonDownload;
        private final ImageButton buttonPlay;
        private final ImageButton buttonCompleted;

        public EpisodeHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.episode_card_container);
            textViewTitle = itemView.findViewById(R.id.episode_title);
            textViewDescription = itemView.findViewById(R.id.episode_description);
            textViewLastPosition = itemView.findViewById(R.id.episode_last_position);
            nonPlayableText = itemView.findViewById(R.id.non_playable_text);
            image = itemView.findViewById(R.id.episode_image);
            buttonShare = itemView.findViewById(R.id.episode_share_button);
            buttonDownload = itemView.findViewById(R.id.episode_download_button);
            buttonPlay = itemView.findViewById(R.id.episode_play_button);
            buttonCompleted = itemView.findViewById(R.id.episode_mark_completed_button);
        }

        public void setLastPositionText(int playbackRes, boolean isCompleted, long lastPosition) {
            if (playbackRes == PlayableEpisode.RES_PAUSE)
                textViewLastPosition.setText(R.string.playing);
            else if (isCompleted)
                textViewLastPosition.setText(R.string.completed);
            else if (playbackRes == PlayableEpisode.RES_PLAY)
                textViewLastPosition.setText(StringUtils.timestampToMSS(lastPosition));
        }

        public void setBackgroundColor(boolean isCompleted) {
            container.setCardBackgroundColor(isCompleted
                    ? container.getContext().getColor(R.color.completed_card_background)
                    : container.getContext().getColor(R.color.not_completed_card_background));
        }

        public void setDescription(String description) {
            if (description != null) description = description.trim();

            textViewDescription.setText(description);
            if (description == null || description.equals("null"))
                textViewDescription.setVisibility(View.GONE);
            else
                textViewDescription.setVisibility(View.VISIBLE);
        }

        public void setDownloadSituation(int downloadState) {
            int imgR, textR;
            switch (downloadState) {
                case Episode.DOWNLOADED:
                    imgR = R.drawable.ic_trash;
                    textR = R.string.remove_download;
                    break;
                case Episode.DOWNLOADING:
                    imgR = R.drawable.ic_stop_download;
                    textR = R.string.downloading;
                    break;
                default:
                    imgR = R.drawable.ic_download_vec;
                    textR = R.string.download;
            }

            buttonDownload.setText(textR);
            buttonDownload.setCompoundDrawablesRelativeWithIntrinsicBounds(imgR, 0, 0, 0);
        }

        public void setCompletedButtonResource(boolean completed) {
            if (completed)
                buttonCompleted.setImageResource(R.drawable.ic_completed);
            else
                buttonCompleted.setImageResource(R.drawable.ic_mark_completed);
        }

        // TODO make adjustments for "CURRENTLY PLAYING"
        public void setPlayableSituation(boolean nonPlayable, int downloadState) {
            int downloadVisibility, textViewVisibility, playButtonVisibility, positionVisibility;
            if (nonPlayable) {
                if (downloadState == Episode.DOWNLOADED) {
                    textViewVisibility = View.GONE;
                    downloadVisibility = playButtonVisibility = positionVisibility = View.VISIBLE;
                }
                else {
                    textViewVisibility = View.VISIBLE;
                    downloadVisibility = playButtonVisibility = positionVisibility = View.GONE;
                }
            }
            else {
                textViewVisibility = View.GONE;
                downloadVisibility = playButtonVisibility = positionVisibility = View.VISIBLE;
            }

            textViewLastPosition.setVisibility(positionVisibility);
            buttonDownload.setVisibility(downloadVisibility);
            nonPlayableText.setVisibility(textViewVisibility);
            buttonPlay.setVisibility(playButtonVisibility);
        }
    }
}
