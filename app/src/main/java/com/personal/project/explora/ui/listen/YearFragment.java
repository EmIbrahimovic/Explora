package com.personal.project.explora.ui.listen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.ui.episode_list.EpisodeListFragment;

import java.util.List;

public class YearFragment extends EpisodeListFragment {

    private static final String TAG = "YearFragment";

    private static final String ARG_YEAR = "ARGUMENT_YEAR";

    private int mYear;

    public YearFragment() {
    }

    public static YearFragment newInstance(int year) {

        YearFragment newYearFragment = new YearFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        newYearFragment.setArguments(args);

        return newYearFragment;
    }


    @Override
    protected LiveData<List<Episode>> getEpisodes() {
        return super.mViewModel.getEpisodesFromYear(mYear);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mYear = savedInstanceState.getInt(ARG_YEAR);
        } else if (getArguments() != null) {
            mYear = getArguments().getInt(ARG_YEAR);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mBinding.swipeRefreshEpisodeList.setOnRefreshListener(() -> {
            mViewModel.doRefresh();
            mBinding.swipeRefreshEpisodeList.setRefreshing(false);
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_YEAR, mYear);
    }

}