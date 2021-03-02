package com.personal.project.explora.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.personal.project.explora.R;

public class ErrorFragment extends Fragment {

    public static ErrorFragment newInstance() {

        Bundle args = new Bundle();

        ErrorFragment fragment = new ErrorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_error, container, false);
    }

}