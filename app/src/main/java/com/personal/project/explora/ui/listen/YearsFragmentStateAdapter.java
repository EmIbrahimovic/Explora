package com.personal.project.explora.ui.listen;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import static com.personal.project.explora.utils.YearsData.YEARS;

public class YearsFragmentStateAdapter extends FragmentStateAdapter {

    public YearsFragmentStateAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return YearFragment.newInstance(YEARS[position]);
    }

    @Override
    public int getItemCount() {
        return YEARS.length;
    }

    public String getItemTitle(int position) {
        return String.valueOf(YEARS[position]);
    }
}
