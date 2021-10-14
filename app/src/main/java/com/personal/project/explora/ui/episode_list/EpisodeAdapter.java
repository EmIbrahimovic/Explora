package com.personal.project.explora.ui.episode_list;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.project.explora.R;
import com.personal.project.explora.db.Episode;
import com.personal.project.explora.utils.PlayableEpisode;
import com.personal.project.explora.utils.StringUtils;
import com.personal.project.explora.utils.YearsData;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EpisodeAdapter extends ListAdapter<PlayableEpisode, EpisodeAdapter.EpisodeHolder> {

    private final EpisodeClickedListener episodeClickedListener;

    protected EpisodeAdapter(EpisodeClickedListener episodeClickedListener) {
        super(PlayableEpisode.diffCallback);
        this.episodeClickedListener = episodeClickedListener;
    }

    @NonNull
    @Override
    public EpisodeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.episode_card, parent, false);

        return new EpisodeHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeHolder holder, int position, @NonNull List<Object> payloads) {

        PlayableEpisode currentEpisode = getItem(position);
        boolean fullRefresh = payloads.isEmpty();

        if (!fullRefresh) {
            for (int i = 0; i < payloads.size(); i++) {
                Bundle payload = (Bundle)payloads.get(i);

                if (payload.getInt(PlayableEpisode.DESCRIPTION_CHANGED, -1) != -1)
                    holder.textViewDescription.setText(currentEpisode.getDescription());

                if (payload.getInt(PlayableEpisode.LAST_POSITION_CHANGED, -1) != -1 ||
                        payload.getInt(PlayableEpisode.PLAYBACK_RES_CHANGED, -1) != -1) {
                    holder.setLastPositionText(currentEpisode.getPlaybackRes(),
                            currentEpisode.isCompleted(), currentEpisode.getLastPosition());
                    holder.setBackgroundColor(currentEpisode.isCompleted());
                }

                if (payload.getInt(PlayableEpisode.PLAYBACK_RES_CHANGED, -1) != -1)
                    holder.buttonPlay.setImageResource(currentEpisode.getPlaybackRes());
            }
        }

        if (fullRefresh) {
            onBindViewHolder(holder, position);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeHolder holder, int position) {
        PlayableEpisode currentEpisode = getItem(position);

        holder.setBackgroundColor(currentEpisode.isCompleted());

        holder.textViewTitle.setText(currentEpisode.getTitle());

        holder.textViewDate.setText(currentEpisode.getDatePublished());

        holder.setDescription(currentEpisode.getDescription());

        Picasso.get()
                .load(YearsData.getYearImageRes(currentEpisode.getYear()))
                .into(holder.image);

        holder.buttonPlay.setOnClickListener(v ->
                episodeClickedListener.onPlayEpisodeClicked(currentEpisode));
        holder.buttonPlay.setImageResource(currentEpisode.getPlaybackRes());

        holder.setLastPositionText(currentEpisode.getPlaybackRes(),
                currentEpisode.isCompleted(), currentEpisode.getLastPosition());

        holder.buttonMenu.setOnClickListener(v -> showPopupMenu(holder.buttonMenu, position));

        holder.buttonShare.setOnClickListener(v ->
                episodeClickedListener.onShareEpisodeClicked(currentEpisode));
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        Menu menu = popupMenu.getMenu();

        inflater.inflate(R.menu.episode_card_options_menu, menu);
        popupMenu.setOnMenuItemClickListener(new MenuItemClickListener(position));

        if (getItem(position).getDownloadState() == Episode.DOWNLOADED) {
            menu.getItem(0).setTitle(R.string.remove_download);
        }

        if (getItem(position).isCompleted()) {
            menu.getItem(1).setTitle(R.string.mark_as_not_completed);
        }

        popupMenu.show();
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
        private final TextView textViewDate;
        private final TextView textViewDescription;
        private final TextView textViewLastPosition;
        private final ImageView image;
        private final ImageButton buttonPlay;
        private final ImageButton buttonMenu;
        private final ImageButton buttonShare;

        public EpisodeHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.episode_card_container);
            textViewTitle = itemView.findViewById(R.id.episode_title);
            textViewDate = itemView.findViewById(R.id.episode_date);
            textViewDescription = itemView.findViewById(R.id.episode_description);
            textViewLastPosition = itemView.findViewById(R.id.episode_last_position);
            image = itemView.findViewById(R.id.episode_image);
            buttonPlay = itemView.findViewById(R.id.episode_play_button);
            buttonMenu = itemView.findViewById(R.id.episode_menu_button);
            buttonShare = itemView.findViewById(R.id.episode_share_button);
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
                    ? container.getContext().getResources().getColor(R.color.completed_card_background)
                    : container.getContext().getResources().getColor(R.color.not_completed_card_background));
        }

        public void setDescription(String description) {
            textViewDescription.setText(description);
            if (description == null || description.equals("null"))
                textViewDescription.setVisibility(View.GONE);
            else
                textViewDescription.setVisibility(View.VISIBLE);
        }
    }

    class MenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private final int position;

        public MenuItemClickListener(int position) {
            this.position = position;
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.download_option:
                    episodeClickedListener.onDownloadEpisodeClicked(getItem(position));
                    return true;
                case R.id.complete_option:
                    Episode clone = new Episode(getItem(position));
                    episodeClickedListener.onCompleteEpisodeClicked(clone);
                    return true;
            }

            return false;
        }
    }
}
