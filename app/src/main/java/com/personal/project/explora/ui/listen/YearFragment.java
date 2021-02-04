package com.personal.project.explora.ui.listen;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.personal.project.explora.EpisodeRepository;
import com.personal.project.explora.databinding.FragmentYearBinding;
import com.personal.project.explora.db.Episode;
import com.personal.project.explora.R;

import java.util.List;

public class YearFragment extends Fragment {

    private static final String TAG = "YearFragment";

    private static final String ARG_YEAR = "ARGUMENT_YEAR";

    private YearViewModel mViewModel;
    private RecyclerView mRecyclerView;
    private FragmentYearBinding mBinding;

    private int mYear;

    public YearFragment() { }

    public static YearFragment newInstance(int year) {
        Log.d(TAG, "newInstance of YearFragment: " + year);

        YearFragment newYearFragment = new YearFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        newYearFragment.setArguments(args);

        return newYearFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mYear = savedInstanceState.getInt(ARG_YEAR);
        }
        else if (getArguments() != null) {
            mYear = getArguments().getInt(ARG_YEAR);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_year, container, false);

        mRecyclerView = mBinding.episodeRecyclerView; //root.findViewById(R.id.episode_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new EpisodeAdapter());

        mBinding.setIsLoading(true);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(YearViewModel.class);

        subscribeUi(mViewModel.getEpisodesFromYear(mYear));
        mViewModel.getNetworkOperationStatus().observe(getViewLifecycleOwner(), integer -> {
            if (integer.equals(EpisodeRepository.FAILURE)) {
                Toast.makeText(getActivity(), "Failed to refresh episodes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subscribeUi(@NonNull LiveData<List<Episode>> myEpisodes) {
        Log.d(TAG, "subscribeUi: subscribeUi on year " + mYear);
        myEpisodes.observe(getViewLifecycleOwner(), new Observer<List<Episode>>() {
            @Override
            public void onChanged(List<Episode> episodes) {

                if (episodes != null && !episodes.isEmpty()) {
                    mBinding.setIsLoading(false);
                    ((EpisodeAdapter) mRecyclerView.getAdapter()).setEpisodes(episodes);
                } else {
                    mBinding.setIsLoading(true);
                }

                mBinding.executePendingBindings();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_YEAR, mYear);
    }

    @Override
    public void onDestroyView() {
        mBinding = null;
        super.onDestroyView();
    }
}