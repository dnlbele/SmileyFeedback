package com.belearn.smileyfeedback;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.belearn.smileyfeedback.utils.Utils;

/**
 * Created by dnlbe on 12/20/2017.
 */

public class AuthenticationDialogFragment extends DialogFragment {

    private static final int WINDOW_WIDTH = 450;
    private static final int WINDOW_HEIGHT = 180;
    private static final String PASSWORD = "p@ssw0rd";

    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WINDOW_WIDTH;
        params.height = WINDOW_HEIGHT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        super.onResume();
    }

    private MainActivity mainActivity;

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mainActivity);

        LayoutInflater inflater = mainActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.authentication_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText etPassword = dialogView.findViewById(R.id.etPassword);
        final Button btnEnter = dialogView.findViewById(R.id.btnEnter);

        btnEnter.setOnClickListener(view -> {
            Utils.closeKeyboard(mainActivity, etPassword);
            if (PASSWORD.equals(etPassword.getText().toString())) {
                mainActivity.showAdminDialog();
            } else {
                Utils.showToastAtBottom(getContext(), R.string.wrong_password);
            }
            if (timeoutHandler != null && timeoutRunnable != null) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
            }
            AuthenticationDialogFragment.this.dismiss();
        });

        return dialogBuilder.create();
    }

    public void setTimeoutHandler(Handler handler, Runnable runnable) {
        timeoutHandler = handler;
        timeoutRunnable = runnable;
    }
}