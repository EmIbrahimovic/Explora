package com.personal.project.explora.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.personal.project.explora.BuildConfig;
import com.personal.project.explora.R;
import com.squareup.picasso.Picasso;

public class HomeFragment extends Fragment {

    private static final String[] LINKS = {
            "https://en.astro.hr/",
            "https://www.facebook.com/VisnjanObservatory/",
            "https://radio.hrt.hr/radio-pula/",
            "http://www.exp.hr/",
            "https://www.youtube.com/results?search_query=Korado+Korlevi%C4%87"
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);


        ImageButton button1 = root.findViewById(R.id.visnjan_button);
        Picasso.get()
                .load(R.drawable.ic_visnjan)
                .centerInside()
                .fit()
                .into(button1);

        ImageButton button2 = root.findViewById(R.id.fb_button);
        Picasso.get()
                .load(R.drawable.ic_facebook)
                .centerInside()
                .fit()
                .into(button2);

        ImageButton button3 = root.findViewById(R.id.hrt_button);
        Picasso.get()
                .load(R.drawable.ic_radio_pula)
                .centerInside()
                .fit()
                .into(button3);

        ImageButton button4 = root.findViewById(R.id.explora_button);
        Picasso.get()
                .load(R.drawable.ic_explora_society)
                .centerInside()
                .fit()
                .into(button4);

        ImageButton button5 = root.findViewById(R.id.yt_button);
        Picasso.get()
                .load(R.drawable.ic_yt)
                .centerInside()
                .fit()
                .into(button5);

        button1.setOnClickListener(v -> openLink(0));
        button2.setOnClickListener(v -> openLink(1));
        button3.setOnClickListener(v -> openLink(2));
        button4.setOnClickListener(v -> openLink(3));
        button5.setOnClickListener(v -> openLink(4));

        TextView homeDescription = root.findViewById(R.id.home_description);
        homeDescription.setMovementMethod(LinkMovementMethod.getInstance());

        TextView versionCodeTV = root.findViewById(R.id.version_code);
        String tvText = getString(R.string.version_code) + ": " + BuildConfig.VERSION_NAME;
        versionCodeTV.setText(tvText);
        versionCodeTV.setOnClickListener(v -> openDialog());

        TextView authorTV = root.findViewById(R.id.author);
        authorTV.setMovementMethod(LinkMovementMethod.getInstance());

        return root;
    }

    private Intent createBrowserIntent(String uri) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openLink(int pos) {
        Intent intent = createBrowserIntent(LINKS[pos]);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void openDialog() {
        VersionNotesDialog versionNotesDialog = new VersionNotesDialog();
        versionNotesDialog.show(getParentFragmentManager(), null);
    }

}