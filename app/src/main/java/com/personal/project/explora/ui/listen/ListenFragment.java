package com.personal.project.explora.ui.listen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.personal.project.explora.EpisodeRepository;
import com.personal.project.explora.R;
import com.personal.project.explora.databinding.FragmentListenBinding;
import com.personal.project.explora.ui.episode_list.EpisodeListViewModel;
import com.personal.project.explora.utils.Event;

public class ListenFragment extends Fragment {

    FragmentListenBinding mBinding;

    EpisodeListViewModel mViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_listen, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(getActivity()).get(EpisodeListViewModel.class);

        subscribeUiToNetworkStatus(mViewModel.getNetworkOperationStatus());
    }

    private void subscribeUiToNetworkStatus(LiveData<Event<Integer>> networkOperationStatus) {

        networkOperationStatus.observe(getViewLifecycleOwner(), event -> {
            Integer integer = event.getContentIfNotHandled();
            if (integer == null) return;

            if (integer.equals(EpisodeRepository.LOADING)) {
                Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
            } else if (integer.equals(EpisodeRepository.FAILURE)) {
                Toast.makeText(getActivity(), "Failed to refresh episodes", Toast.LENGTH_SHORT).show();
            } else if (integer.equals(EpisodeRepository.SUCCESS)) {
                Toast.makeText(getActivity(), "Refresh successful", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        YearsFragmentStateAdapter mViewPagerAdapter = new YearsFragmentStateAdapter(this);
        ViewPager2 mViewPager = mBinding.yearsViewPager;
        mViewPager.setAdapter(mViewPagerAdapter);

        TabLayout tabs = mBinding.yearTabs;
        new TabLayoutMediator(tabs, mViewPager,
                (tab, position) -> tab.setText(mViewPagerAdapter.getItemTitle(position))).attach();
    }

    @Override
    public void onDestroyView() {
        mBinding = null;
        super.onDestroyView();
    }
}