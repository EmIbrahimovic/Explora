package com.personal.project.explora.ui.listen;

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
import com.personal.project.explora.databinding.FragmentListenBinding;

import java.util.ArrayList;

public class ListenFragment extends Fragment {

    FragmentListenBinding mBinding;

    ViewPager2 mViewPager;
    YearsFragmentStateAdapter mViewPagerAdapter;

    /*

    MAYBE PUT LAST VISITED TAB IN THE SAVED INSTANCE STATE

     */

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_listen, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mViewPagerAdapter = new YearsFragmentStateAdapter(this);
        mViewPager = mBinding.yearsViewPager;
        mViewPager.setAdapter(mViewPagerAdapter);

        TabLayout tabs = mBinding.yearTabs;
        new TabLayoutMediator(tabs, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(mViewPagerAdapter.getItemTitle(position));
            }
        }).attach();
    }

    @Override
    public void onDestroyView() {
        mBinding = null;
        super.onDestroyView();
    }
}