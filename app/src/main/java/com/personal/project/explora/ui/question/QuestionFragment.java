package com.personal.project.explora.ui.question;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.personal.project.explora.R;

import java.util.List;

public class QuestionFragment extends Fragment {

    public static final String EXPLORA_EMAIL = "explora@hrt.hr";

    private QuestionViewModel mViewModel;

    private EditText subject;
    private EditText question;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_question, container, false);
        Button buttonMail = root.findViewById(R.id.button_mail);

        subject = root.findViewById(R.id.edit_text_subject);
        question = root.findViewById(R.id.edit_text_body);

        buttonMail.setOnClickListener(v -> sendMail());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(requireActivity()).get(QuestionViewModel.class);

        subject.setText(mViewModel.getSavedSubject());
        question.setText(mViewModel.getSavedQuestion());
    }

    private void sendMail() {
        Intent mailIntent = createEmailIntent(
                subject.getText().toString(),
                question.getText().toString());

        subject.setText(null);
        question.setText(null);
        startActivity(mailIntent);
    }

    private Intent createEmailIntent(final String subject,
                                     final String message) {

        Intent sendTo = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode(QuestionFragment.EXPLORA_EMAIL) +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(message);
        Uri uri = Uri.parse(uriText);
        sendTo.setData(uri);

        @SuppressLint("QueryPermissionsNeeded")
        List<ResolveInfo> resolveInfos =
                requireActivity().getPackageManager().queryIntentActivities(sendTo, 0);

        // Emulators may not like this check...
        if (!resolveInfos.isEmpty()) {
            return sendTo;
        }

        // Nothing resolves send to, so fallback to send...
        Intent send = new Intent(Intent.ACTION_SEND);

        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_EMAIL,
                new String[] {QuestionFragment.EXPLORA_EMAIL});
        send.putExtra(Intent.EXTRA_SUBJECT, subject);
        send.putExtra(Intent.EXTRA_TEXT, message);

        return Intent.createChooser(send, "Choose preferred email app: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.saveSubject(subject.getText());
        mViewModel.saveQuestion(question.getText());
    }

}