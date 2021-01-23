package com.personal.project.explora.ui.listen;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.R;

import java.util.List;

public class YearFragment extends Fragment {

    private static final String ARG_YEAR = "YEAR_ARGUMENT_YEARFRAGMENT";
    private int mYear;

    private YearViewModel mViewModel;
    private RecyclerView mRecyclerView;

    public YearFragment() { }

    public static YearFragment newInstance(int year) {
        YearFragment newYearFragment = new YearFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        newYearFragment.setArguments(args);

        return newYearFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mYear = savedInstanceState.getInt(ARG_YEAR);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_year, container, false);

        mRecyclerView = root.findViewById(R.id.episode_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        mRecyclerView.setAdapter(new EpisodeAdapter());

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(YearViewModel.class);

        mViewModel.getAllEpisodes().observe(getViewLifecycleOwner(), new Observer<List<Episode>>() {
            @Override
            public void onChanged(List<Episode> episodes) {
                ((EpisodeAdapter) mRecyclerView.getAdapter()).setEpisodes(episodes);
            }
        });

        // TODO: Use the ViewModel
    }

}