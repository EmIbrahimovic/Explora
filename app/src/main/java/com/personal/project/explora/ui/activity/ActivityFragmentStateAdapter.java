package com.personal.project.explora.ui.activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.personal.project.explora.ui.ErrorFragment;

public class ActivityFragmentStateAdapter extends FragmentStateAdapter {

    private static final String TAG = "ActivityFragmentStat...";

    public ActivityFragmentStateAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = RecentFragment.newInstance();
                break;
            case 1:
                fragment = DownloadsFragment.newInstance();
                break;
            default:
                fragment = ErrorFragment.newInstance();
                //Log.e(TAG, "createFragment: invalid position");
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
            default:
                //Log.e(TAG, "getItemTitle: invalid position");
        }

        return title;
    }
}
