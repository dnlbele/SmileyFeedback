package com.belearn.smileyfeedback.model;

/**
 * Created by dnlbe on 12/22/2017.
 */

public class AsyncResult {
    boolean exception;
    int result;
    public boolean isException() {
        return exception;
    }

    public int getResult() {
        return result;
    }


    public AsyncResult(boolean exception, int result) {
        this.exception = exception;
        this.result = result;
    }
}
