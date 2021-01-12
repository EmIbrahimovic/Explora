package com.personal.project.explora.ui.question;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.personal.project.explora.R;

import java.util.List;

public class QuestionFragment extends Fragment {

    private QuestionViewModel questionViewModel;
    Button buttonMail;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        questionViewModel =
                new ViewModelProvider(this).get(QuestionViewModel.class);
                //ViewModelProviders.of(this).get(QuestionViewModel.class);

        View root = inflater.inflate(R.layout.fragment_question, container, false);
        buttonMail = root.findViewById(R.id.button_mail);

        buttonMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mailIntent = createEmailIntent(
                        "emira.ibrahimovic@hotmail.com",
                        "Subject",
                        "Body...");

                startActivity(mailIntent);
            }
        });

        return root;
    }

    public Intent createEmailIntent(final String toEmail,
                                    final String subject,
                                    final String message)
    {
        Intent sendTo = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode(toEmail) +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(message);
        Uri uri = Uri.parse(uriText);
        sendTo.setData(uri);

        List<ResolveInfo> resolveInfos =
                getActivity().getPackageManager().queryIntentActivities(sendTo, 0);

        // Emulators may not like this check...
        if (!resolveInfos.isEmpty()) {
            return sendTo;
        }

        // Nothing resolves send to, so fallback to send...
        Intent send = new Intent(Intent.ACTION_SEND);

        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_EMAIL,
                new String[] { toEmail });
        send.putExtra(Intent.EXTRA_SUBJECT, subject);
        send.putExtra(Intent.EXTRA_TEXT, message);

        return Intent.createChooser(send, "Choose preferred email app: ");
    }

}