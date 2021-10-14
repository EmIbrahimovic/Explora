package com.personal.project.explora.ui.listen;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;


public class YearsFragmentStateAdapter extends FragmentStateAdapter {

    private final List<Integer> years;

    public YearsFragmentStateAdapter(@NonNull Fragment fragment) {
        super(fragment);
        years = new ArrayList<>();
    }

    public YearsFragmentStateAdapter(@NonNull Fragment fragment, List<Integer> yearList) {
        super(fragment);
        years = yearList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return YearFragment.newInstance(years.get(position));
    }

    @Override
    public int getItemCount() {
        return years.size();
    }

    public String getItemTitle(int position) {
        return String.valueOf(years.get(position));
    }

}
