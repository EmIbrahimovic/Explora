package com.personal.project.explora.ui.listen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.project.explora.R;
import com.personal.project.explora.db.Episode;
import com.squareup.picasso.Picasso;

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
        holder.textViewLastTime.setText(currentEpisode.getLength());
        Picasso.get()
                .load(currentEpisode.getImage())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.image);
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

        public EpisodeHolder(@NonNull View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.episode_title);
            textViewDescription = itemView.findViewById(R.id.episode_description);
            textViewLastTime = itemView.findViewById(R.id.episode_last_time);
            image = itemView.findViewById(R.id.episode_image);
        }
    }
}
