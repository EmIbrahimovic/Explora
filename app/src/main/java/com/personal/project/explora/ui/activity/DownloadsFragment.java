package com.personal.project.explora.ui.activity;

import androidx.lifecycle.LiveData;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.ui.episode_list.EpisodeListFragment;

import java.util.List;

public class DownloadsFragment extends EpisodeListFragment {

    public static DownloadsFragment newInstance() {
        return new DownloadsFragment();
    }

    @Override
    protected LiveData<List<Episode>> getEpisodes() {
        return super.mViewModel.getDownloadedEpisodes();
    }

    @Override
    public void onRefresh() {
        mBinding.swipeRefreshEpisodeList.setRefreshing(false);
    }
}