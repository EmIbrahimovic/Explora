package com.personal.project.explora.ui.listen;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.personal.project.explora.Episode;
import com.personal.project.explora.R;

import java.util.List;

public class YearFragment extends Fragment {

    private YearViewModel mViewModel;
    private RecyclerView recyclerView;

    public static YearFragment newInstance() {
        return new YearFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_year, container, false);

        recyclerView = root.findViewById(R.id.episode_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(new EpisodeAdapter());

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(YearViewModel.class);
                //ViewModelProviders.of(this).get(YearViewModel.class);

        mViewModel.getAllEpisodes().observe(getViewLifecycleOwner(), new Observer<List<Episode>>() {
            @Override
            public void onChanged(List<Episode> episodes) {
                ((EpisodeAdapter)recyclerView.getAdapter()).setEpisodes(episodes);
            }
        });
        // TODO: Use the ViewModel
    }

}