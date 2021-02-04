package com.personal.project.explora.ui.listen;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class YearsFragmentStateAdapter extends FragmentStateAdapter {

    private static final String TAG = "YearsFragmentStateAdapt";

    // TODO: FIND A WAY TO DYNAMICALLY ASSIGN YEARS
    public static final int[] mYears = { 2021, 2020, 2019, 2018, 2017 };

    public YearsFragmentStateAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(TAG, "createFragment: " + position + " " + mYears[position]);
        return YearFragment.newInstance(mYears[position]);
    }

    @Override
    public int getItemCount() {
        return mYears.length;
    }

    public String getItemTitle(int position) {
        return String.valueOf(mYears[position]);
    }
}
