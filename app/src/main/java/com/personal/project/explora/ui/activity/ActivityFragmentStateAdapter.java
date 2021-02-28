package com.personal.project.explora.ui.activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ActivityFragmentStateAdapter extends FragmentStateAdapter {


    public ActivityFragmentStateAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = RecentFragment.newInstance();
                break;
            case 1:
                fragment = DownloadsFragment.newInstance();
                break;
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public String getItemTitle(int position) {
        String title = null;
        switch (position) {
            case 0:
                title = "Recent";
                break;
            case 1:
                title = "Downloads";
                break;
        }

        return title;
    }
}
