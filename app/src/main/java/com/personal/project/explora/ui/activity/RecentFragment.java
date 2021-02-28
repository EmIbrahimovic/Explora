package com.personal.project.explora.ui.activity;

import androidx.lifecycle.LiveData;

import com.personal.project.explora.db.Episode;
import com.personal.project.explora.ui.episode_list.EpisodeListFragment;
import com.personal.project.explora.utils.DateUtil;
import com.personal.project.explora.utils.PlayableEpisode;

import java.util.Collections;
import java.util.List;

public class RecentFragment extends EpisodeListFragment {

    public static RecentFragment newInstance() {
        return new RecentFragment();
    }

    @Override
    protected LiveData<List<Episode>> getEpisodes() {
        return super.mViewModel.getRecentEpisodes();
    }

    @Override
    protected void sortListToDisplay(List<PlayableEpisode> playableEpisodes) {

        Collections.sort(playableEpisodes, (o1, o2) ->
                DateUtil.compareStringDateTimes(o1.getRecent(), o2.getRecent()));

    }
}