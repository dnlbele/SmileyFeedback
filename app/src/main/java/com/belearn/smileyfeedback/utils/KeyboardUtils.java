package com.belearn.smileyfeedback.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by dnlbe on 12/21/2017.
 */

public class KeyboardUtils {
    public static void closeKeyboard(Context context, EditText et) {
        InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(et.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
