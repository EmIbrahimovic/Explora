package com.personal.project.explora.ui.question;

import androidx.lifecycle.ViewModel;

public class QuestionViewModel extends ViewModel {

    private CharSequence subjectText;
    private CharSequence questionText;

    public QuestionViewModel() {
        subjectText = null;
        questionText = null;
    }

    public CharSequence getSavedSubject() {
        return (subjectText == null) ? "" : subjectText;
    }

    public CharSequence getSavedQuestion() {
        return (questionText == null) ? "" : questionText;
    }

    public void saveSubject(CharSequence subjectText) {
        this.subjectText = subjectText;
    }

    public void saveQuestion(CharSequence questionText) {
        this.questionText = questionText;
    }

}