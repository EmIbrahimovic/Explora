package com.personal.project.explora.ui;

import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.personal.project.explora.BasicApp;
import com.personal.project.explora.R;
import com.personal.project.explora.databinding.ActivityMainBinding;
import com.personal.project.explora.ui.player.PlayerFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Fragment playerFragmentCurrent;
    ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        BottomNavigationView navView = mBinding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_listen,
                R.id.navigation_activity, R.id.navigation_question)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        MainActivityViewModel mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        //((BasicApp)getApplication()).getPlayerServiceConnection().onActivityCreated();

        mViewModel.getNavigateToFragment().observe(this, it -> {
            if (it == null) return;
            MainActivityViewModel.FragmentNavigationRequest
                    fragmentRequest = it.getContentIfNotHandled();
            if (fragmentRequest != null) {
                playerFragmentCurrent = fragmentRequest.fragment;
                FragmentTransaction transaction =
                        getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right,
                        R.anim.enter_from_right, R.anim.exit_to_right);
                transaction.add(
                        R.id.big_container, fragmentRequest.fragment, fragmentRequest.tag
                );

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

    @Override
    protected void onStart() {
        super.onStart();

        //((BasicApp)getApplication()).getPlayerServiceConnection().onActivityStarted();
    }
}