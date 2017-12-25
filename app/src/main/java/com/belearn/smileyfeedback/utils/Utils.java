package com.belearn.smileyfeedback.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.belearn.smileyfeedback.MainActivity;

/**
 * Created by dnlbe on 12/21/2017.
 */

public class Utils {
    public static void closeKeyboard(Context context, EditText et) {
        InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(et.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showToastAtBottom(Context context, int resourceId) {
        Toast toast = Toast.makeText(context, resourceId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 30);
        toast.show();
    }
}
