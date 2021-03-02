package com.personal.project.explora.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.personal.project.explora.R;
import com.personal.project.explora.databinding.FragmentActivityBinding;

public class ActivityFragment extends Fragment {

    private FragmentActivityBinding mBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_activity, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        ActivityFragmentStateAdapter mViewPagerAdapter = new ActivityFragmentStateAdapter(this);
        ViewPager2 mViewPager = mBinding.activityViewPager;
        mViewPager.setAdapter(mViewPagerAdapter);

        TabLayout tabs = mBinding.activityTabs;
        new TabLayoutMediator(tabs, mViewPager,
                (tab, position) -> tab.setText(mViewPagerAdapter.getItemTitle(position))).attach();
    }

    @Override
    public void onDestroyView() {
        mBinding = null;
        super.onDestroyView();
    }
}