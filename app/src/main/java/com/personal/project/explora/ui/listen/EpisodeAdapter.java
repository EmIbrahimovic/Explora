package com.personal.project.explora.ui.listen;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.project.explora.Episode;
import com.personal.project.explora.EpisodePopupItemClickListener;
import com.personal.project.explora.R;

import java.util.ArrayList;
import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeHolder> {

    public List<Episode> episodes = new ArrayList<>();

    @NonNull
    @Override
    public EpisodeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.episode_card, parent, false);

        return new EpisodeHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeHolder holder, int position) {
        Episode currentEpisode = episodes.get(position);

        holder.textViewTitle.setText(currentEpisode.getTitle());
        holder.textViewDescription.setText(currentEpisode.getDescription());
        // TODO add episode holder things, figure out image etc.

        // TODO add onclick listeners for


    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
    }

    public Episode getEpisodeAt(int position) {
        return episodes.get(position);
    }

    class EpisodeHolder extends RecyclerView.ViewHolder {

        private TextView textViewTitle;
        private TextView textViewDescription;
        private TextView textViewLastTime;
        private ImageView image;
        private ImageButton buttonMore;
        private ImageButton buttonPlay;
        private ImageButton buttonShare;

        public EpisodeHolder(@NonNull View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.episode_title);
            textViewDescription = itemView.findViewById(R.id.episode_description);
            textViewLastTime = itemView.findViewById(R.id.episode_last_time);
            image = itemView.findViewById(R.id.episode_image);
            buttonMore = itemView.findViewById(R.id.episode_more_button);
            buttonPlay = itemView.findViewById(R.id.episode_play_button);
            buttonShare = itemView.findViewById(R.id.episode_share_button);

            buttonMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(buttonMore, getAdapterPosition());
                }
            });
        }
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popupMenu.getMenuInflater();

        inflater.inflate(R.menu.episode_popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new EpisodePopupItemClickListener(position));
        popupMenu.show();
    }
}
