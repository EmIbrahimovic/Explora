package com.personal.project.explora.ui;

import android.view.View;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.project.explora.R;

public class BindingAdapters {

    @BindingAdapter(value = {"showHideLoading", "showHideEmpty"}, requireAll = true)
    public static void showHide(View view, boolean isLoading, boolean isEmpty) {
        view.setVisibility(!isEmpty && !isLoading ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter(value = {"isItLoading", "isItEmpty"}, requireAll = true)
    public static void loadingOrEmpty(View view, boolean isLoading, boolean isEmpty) {
        if (isLoading) {
            ((TextView)view).setText(R.string.loading);
            view.setVisibility(View.VISIBLE);
        }
        else if (isEmpty) {
            ((TextView)view).setText(R.string.empty);
            view.setVisibility(View.VISIBLE);
        }
        else {
            ((TextView)view).setText("");
            view.setVisibility(View.GONE);
        }
    }
}