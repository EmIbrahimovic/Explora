package com.personal.project.explora.ui.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.personal.project.explora.R;
import com.personal.project.explora.databinding.FragmentPlayerBinding;
import com.personal.project.explora.ui.MainActivityViewModel;
import com.personal.project.explora.utils.StringUtils;
import com.squareup.picasso.Picasso;

public class PlayerFragment extends Fragment {

    private static final int REWIND_VAL = 15000;
    private static final int FAST_FORWARD_VAL = 15000;

    private PlayerViewModel mPlayerViewModel;
    private MainActivityViewModel mMainActivityViewModel;

    private FragmentPlayerBinding mBinding;

    public PlayerFragment() {
    }

    public static PlayerFragment newInstance() {
        return new PlayerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_player, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPlayerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        mMainActivityViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        mBinding.mediaButton.setOnClickListener(v -> {
            if (mPlayerViewModel.getMediaMetadata().getValue() != null)
                mMainActivityViewModel.playMediaId(mPlayerViewModel.getMediaMetadata().getValue().id);
        });

        mBinding.rewindButton.setOnClickListener(v -> {
            if (mPlayerViewModel.getMediaPosition().getValue() != null) {
                int pos = Math.toIntExact(mPlayerViewModel.getMediaPosition().getValue()) - REWIND_VAL;
                if (pos < 0) pos = 0;
                mMainActivityViewModel.seekTo(pos);
            }
        });

        mBinding.fastForwardButton.setOnClickListener(v -> {
            if (mPlayerViewModel.getMediaPosition().getValue() != null) {
                int pos = Math.toIntExact(mPlayerViewModel.getMediaPosition().getValue()) + FAST_FORWARD_VAL;
                mMainActivityViewModel.seekTo(pos);
            }
        });

        mBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (fromUser)
//                    mMainActivityViewModel.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mPlayerViewModel.getMediaMetadata().getValue() != null) {
                    if (mPlayerViewModel.getMediaButtonResource().getValue() != null &&
                            mPlayerViewModel.getMediaButtonResource().getValue() == PlayerViewModel.RES_PAUSE_LINES)
                        mMainActivityViewModel.playMediaId(mPlayerViewModel.getMediaMetadata().getValue().id);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlayerViewModel.getMediaMetadata().getValue() != null) {
                    mMainActivityViewModel.seekTo(seekBar.getProgress());
                    mMainActivityViewModel.playMediaId(mPlayerViewModel.getMediaMetadata().getValue().id);
                }
            }
        });

        mBinding.playerFragmentToolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        mBinding.playerFragmentToolbar.setNavigationOnClickListener(
                v -> mMainActivityViewModel.onBackPressed());

        mBinding.playerFragmentToolbar.setTitle(R.string.loading);
        mBinding.duration.setText(StringUtils.timestampToMSS(0L));
        mBinding.position.setText(StringUtils.timestampToMSS(0L));


        mPlayerViewModel.getMediaMetadata().observe(getViewLifecycleOwner(),
                this::updateUI);
        mPlayerViewModel.getMediaButtonResource().observe(getViewLifecycleOwner(), res -> {
            mBinding.mediaButton.setImageResource(res);
            if (res == PlayerViewModel.RES_REPLAY) {
                mBinding.fastForwardButton.setVisibility(View.INVISIBLE);
                mBinding.rewindButton.setVisibility(View.INVISIBLE);
            } else {
                mBinding.fastForwardButton.setVisibility(View.VISIBLE);
                mBinding.rewindButton.setVisibility(View.VISIBLE);
            }
        });
        mPlayerViewModel.getMediaPosition().observe(getViewLifecycleOwner(), pos -> {
            mBinding.position.setText(StringUtils.timestampToMSS(pos));
            mBinding.seekBar.setProgress(Math.toIntExact(pos));
        });
    }

    private void updateUI(PlayerViewModel.NowPlayingMetadata metadata) {

        Picasso.get()
                .load(metadata.albumArt)
                .fit()
                .centerInside()
                .into(mBinding.albumArt);

        mBinding.playerFragmentToolbar.setTitle(metadata.title);
        mBinding.duration.setText(metadata.duration);
        mBinding.seekBar.setMax((int) metadata.durationMs);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMainActivityViewModel.hideBottomNavigation();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMainActivityViewModel.showBottomNavigation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mBinding = null;
    }
}