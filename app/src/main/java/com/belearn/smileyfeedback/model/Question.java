package com.belearn.smileyfeedback.model;

/**
 * Created by dnlbe on 12/20/2017.
 */

public class Question {
    private int idQuestion;
    private String text;
    private int active;

    public int getIdQuestion() {
        return idQuestion;
    }
    public Question(int idQuestion, String text, int active) {
        this.idQuestion = idQuestion;
        this.text = text;
        this.active = active;
    }

    @Override
    public String toString() {
        return text;
    }


}

