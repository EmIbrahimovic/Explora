package com.personal.project.explora.ui.listen;

import android.os.Bundle;
import android.util.Log;
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

import java.util.List;

public class ListenFragment extends Fragment {

    private static final String TAG = "ListenFragment";

    FragmentListenBinding mBinding;

    EpisodeListViewModel mViewModel;

    YearsFragmentStateAdapter mViewPagerAdapter;
    ViewPager2 mViewPager;
    TabLayout tabs;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_listen, container, false);
        mBinding.setIsLoading(true);
        mBinding.setIsEmpty(false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mViewPagerAdapter = new YearsFragmentStateAdapter(this);
        mViewPager = mBinding.yearsViewPager;
        tabs = mBinding.yearTabs;

        mViewModel = new ViewModelProvider(requireActivity()).get(EpisodeListViewModel.class);

        subscribeUiToNetworkStatus(mViewModel.getNetworkOperationStatus());
        subscribeToYearListChanges(mViewModel.getYears());
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

    private void subscribeToYearListChanges(LiveData<List<Integer>> years)
    {
        years.observe(getViewLifecycleOwner(), yearsList -> {
            if (yearsList == null || yearsList.isEmpty()) {
                mBinding.setIsLoading(true);
                Log.d(TAG, "subscribeToYearListChanges: getYears().observe() yearsList is empty");

                return;
            }

            Log.d(TAG, "subscribeToYearListChanges: Received valid yearsList");

            mBinding.setIsLoading(false);

            mViewModel.updateAllEpisodeMap(yearsList);

            mViewPagerAdapter = new YearsFragmentStateAdapter(this, yearsList);
            mViewPager.setAdapter(mViewPagerAdapter);
            new TabLayoutMediator(tabs, mViewPager,
                    (tab, position) -> tab.setText(mViewPagerAdapter.getItemTitle(position))).attach();

        });
    }

    @Override
    public void onDestroyView() {
        mBinding = null;
        super.onDestroyView();
    }
}