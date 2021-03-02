package com.personal.project.explora.ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.personal.project.explora.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VersionNotesDialog extends AppCompatDialogFragment {

    private static final String TAG = "VersionNotesDialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.release_notes);
        builder.setMessage(getMessage());
        builder.setPositiveButton("OK", (dialog, which) -> { });

        return builder.create();
    }

    private String getMessage() {
        StringBuilder stringBuilder = new StringBuilder();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(requireContext().getAssets().open("version_notes.txt")));

            String mLine;
            while ((mLine = reader.readLine()) != null) {
                stringBuilder.append(mLine).append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "getMessage", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "getMessage", e);
                }
            }
        }

        return stringBuilder.toString();
    }
}
