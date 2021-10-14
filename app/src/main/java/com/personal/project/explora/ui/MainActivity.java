package com.personal.project.explora.ui;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.personal.project.explora.R;
import com.personal.project.explora.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    Fragment playerFragmentCurrent;
    ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        BottomNavigationView navView = mBinding.navView;
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navController);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        MainActivityViewModel mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        mViewModel.getNavigateToFragment().observe(this, it -> {
            if (it == null) return;
            MainActivityViewModel.FragmentNavigationRequest
                    fragmentRequest = it.getContentIfNotHandled();
            if (fragmentRequest != null) {
                playerFragmentCurrent = fragmentRequest.fragment;
                FragmentTransaction transaction =
                        getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                        R.anim.enter_from_right, R.anim.exit_to_right,
                        R.anim.enter_from_right, R.anim.exit_to_right);
                transaction.add(
                        R.id.big_container, fragmentRequest.fragment, fragmentRequest.tag);

                if (fragmentRequest.backStack) transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        mViewModel.getBackPressed().observe(this, event -> {
            if (event == null) return;
            Boolean pressed = event.getContentIfNotHandled();
            if (pressed != null && pressed) {
                getSupportFragmentManager().beginTransaction().remove(playerFragmentCurrent).commit();
            }
        });

        mViewModel.getBottomNavigationVisibleEvent().observe(this, event -> {
            if (event == null) return;
            Boolean show = event.getContentIfNotHandled();
            if (show != null) {
                if (show) mBinding.navView.setVisibility(View.VISIBLE);
                else mBinding.navView.setVisibility(View.GONE);
            }
        });

    }

}