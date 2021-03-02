package com.personal.project.explora.ui.episode_list;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.personal.project.explora.R;
import com.personal.project.explora.databinding.FragmentEpisodeListBinding;
import com.personal.project.explora.db.Episode;
import com.personal.project.explora.ui.MainActivityViewModel;
import com.personal.project.explora.utils.PlayableEpisode;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.ACTION_SEND;

public abstract class EpisodeListFragment extends Fragment implements EpisodeAdapter.EpisodeClickedListener,
        SwipeRefreshLayout.OnRefreshListener {

    protected EpisodeListViewModel mViewModel;
    protected FragmentEpisodeListBinding mBinding;
    private MainActivityViewModel mMainActivityViewModel;
    private RecyclerView mRecyclerView;
    private EpisodeAdapter mAdapter;
    private LiveData<List<Episode>> mEpisodes;

    protected EpisodeListFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate
                (inflater, R.layout.fragment_episode_list, container, false);

        mRecyclerView = mBinding.episodeRecyclerView;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mBinding.swipeRefreshEpisodeList.setOnRefreshListener(this);

        mBinding.setIsLoading(true);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(requireActivity()).get(EpisodeListViewModel.class);
        mMainActivityViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
        mEpisodes = getEpisodes();

        mAdapter = new EpisodeAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        subscribeUi();
    }

    protected abstract LiveData<List<Episode>> getEpisodes();

    private void subscribeUi() {
        subscribeUiToEpisodeList(mEpisodes);
        subscribeUiToWhatIsPlaying(mViewModel.getNowPlayingId(), mViewModel.getIsPlaying());
    }

    private void subscribeUiToEpisodeList(LiveData<List<Episode>> myEpisodes) {
        myEpisodes.observe(getViewLifecycleOwner(), episodes -> {

            if (episodes != null) {

                mBinding.setIsLoading(false);

                if (episodes.isEmpty()) {
                    mBinding.setIsEmpty(true);
                    return;
                }

                mBinding.setIsEmpty(false);

                mAdapter.submitList(
                        makePlayableEpisodesList(
                                episodes,
                                mViewModel.getNowPlayingId().getValue(),
                                mViewModel.getIsPlaying().getValue()
                        )
                );

                mBinding.swipeRefreshEpisodeList.setRefreshing(false);

            } else {

                mBinding.setIsLoading(true);
                mBinding.swipeRefreshEpisodeList.setRefreshing(true);
            }

            mBinding.executePendingBindings();
        });


    }

    private void subscribeUiToWhatIsPlaying(LiveData<Integer> nowPlaying, LiveData<Boolean> isPlaying) {
        nowPlaying.observe(getViewLifecycleOwner(), id ->
                mAdapter.submitList(
                        makePlayableEpisodesList(
                                mEpisodes.getValue(),
                                id,
                                mViewModel.getIsPlaying().getValue()
                        )
                )
        );

        isPlaying.observe(getViewLifecycleOwner(),
                isItPlaying -> mAdapter.submitList(
                        makePlayableEpisodesList(
                                mEpisodes.getValue(),
                                mViewModel.getNowPlayingId().getValue(),
                                isItPlaying
                        )
                )
        );
    }

    private List<PlayableEpisode> makePlayableEpisodesList(@Nullable List<Episode> episodes,
                                                           Integer nowPlayingId,
                                                           Boolean isPlaying) {

        List<PlayableEpisode> playableEpisodes = new ArrayList<>();
        if (episodes != null) {
            for (Episode episode : episodes) {
                PlayableEpisode ep = new PlayableEpisode(episode);
                if (nowPlayingId != null && isPlaying != null &&
                        episode.getId() == nowPlayingId && isPlaying) {
                    ep.setPlaybackRes(R.drawable.exo_controls_pause);
                }

                playableEpisodes.add(ep);
            }
        }

        sortListToDisplay(playableEpisodes);

        return playableEpisodes;
    }

    protected void sortListToDisplay(List<PlayableEpisode> playableEpisodes) { }

    @Override
    public void onPlayEpisodeClicked(Episode episode) {

        boolean newRecent = mMainActivityViewModel.playableEpisodeClicked(episode);
        if (newRecent) {
            mViewModel.addNewRecent(episode);
        }
    }

    @Override
    public void onDownloadEpisodeClicked(Episode episode) {
        if (episode.getDownloadState() == Episode.NOT_DOWNLOADED) {
            mViewModel.download(episode);
        } else {
            mViewModel.removeDownload(episode);
        }
    }

    @Override
    public void onCompleteEpisodeClicked(Episode episode) {
        if (!episode.isCompleted()) {
            mViewModel.markAsCompleted(episode);
        } else {
            mViewModel.markAsNotCompleted(episode);
        }
    }

    @Override
    public void onShareEpisodeClicked(Episode episode) {
        Intent intent = makeShareIntent(episode);
        startActivity(intent);
    }

    private Intent makeShareIntent(Episode episode) {
        Intent sendIntent = new Intent(ACTION_SEND);
        String text = "Poslušajte emisiju Explore: " + episode.getTitle() + " " + episode.getLink();

        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        return Intent.createChooser(sendIntent, null);
    }

    @Override
    public void onDestroyView() {
        mBinding = null;
        super.onDestroyView();
    }

}