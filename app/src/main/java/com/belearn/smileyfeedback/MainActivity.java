package com.belearn.smileyfeedback;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.belearn.smileyfeedback.model.AsyncResult;
import com.belearn.smileyfeedback.model.Question;
import com.belearn.smileyfeedback.utils.AnimationUtils;
import com.belearn.smileyfeedback.utils.DbUtil;
import com.belearn.smileyfeedback.utils.Utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MainActivity extends AppCompatActivity {

    private static final long TIMEOUT_DIALOG = 10000;
    private Question question = null;
    private TextView tvQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fixActionBar();
        initWidgets();
    }

    private void fixActionBar() {
        Drawable d=getResources().getDrawable(R.drawable.algebra_top);
        getSupportActionBar().setBackgroundDrawable(d);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    private void initWidgets() {
        tvQuestion = findViewById(R.id.tvQuestion);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miManage:
                showAutheticationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAutheticationDialog() {
        AuthenticationDialogFragment adf = new AuthenticationDialogFragment();
        adf.setMainActivity(this);
        adf.show(getSupportFragmentManager(), AuthenticationDialogFragment.class.getName());

        dissmissAuthenticationDialogDelayed(adf);
    }

    private void dissmissAuthenticationDialogDelayed(AuthenticationDialogFragment adf) {
        final Handler handler  = new Handler();
        final Runnable runnable = () -> {
            Fragment dialog = getSupportFragmentManager().findFragmentByTag(AuthenticationDialogFragment.class.getName());
            if (dialog != null) {
                DialogFragment df = (DialogFragment) dialog;
                df.dismiss();
            }
        };
        adf.setTimeoutHandler(handler, runnable);
        handler.postDelayed(runnable, TIMEOUT_DIALOG);
    }

    public void showAdminDialog() {
        AdminDialogFragment adf = new AdminDialogFragment();
        adf.setMainActivity(this);
        adf.show(getSupportFragmentManager(), AdminDialogFragment.class.getName());
    }

    public void createFeedback(View view) {

        String viewId = getResources().getResourceName(view.getId());
        final int grade = Integer.parseInt(viewId.substring(viewId.length() - 1));

        if (formIsValid()) {
            AnimationUtils.rotate(view);
            new CreateFeedbackAsyncTask().execute(grade);
            Utils.showToastAtBottom(this, R.string.thank_you_for_your_opinion);
        }
    }

    private boolean formIsValid() {
        if (Utils.getLocation(this) == null || question == null){
            Utils.showToastAtBottom(this, R.string.please_insert_question);
            return false;
        }
        return true;
    }


    public void setQuestion(Question question) {
        this.question = question;
        tvQuestion.setText(question.toString());
    }

    private class CreateFeedbackAsyncTask extends AsyncTask<Integer, Void, AsyncResult> {

        @Override
        protected AsyncResult doInBackground(Integer... params) {

            Callable<Integer>task = () -> DbUtil.createFeedback(question.getIdQuestion(), Utils.getLocation(MainActivity.this), params[0]);
            int result = 0;
            ExecutorService executor = Executors.newFixedThreadPool(1);
            Future<Integer> future = executor.submit(task);
            boolean exception = false;
            try {
                result = future.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.e(getClass().getName(), ""+e.getMessage());
                exception = true;
            } catch (ExecutionException e) {
                Log.e(getClass().getName(), ""+e.getMessage());
                exception = true;
            } catch (TimeoutException e) {
                Log.e(getClass().getName(), ""+e.getMessage());
                exception = true;
            }

            return new AsyncResult(exception, result);
        }

        @Override
        protected void onPostExecute(AsyncResult asyncResult) {
            super.onPostExecute(asyncResult);
            if (asyncResult.isException()) {
                Utils.showToastAtBottom(MainActivity.this, R.string.unable_to_connect_to_server);
            }
        }
    }

}
