package com.belearn.smileyfeedback;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.belearn.smileyfeedback.model.AsyncResult;
import com.belearn.smileyfeedback.model.Question;
import com.belearn.smileyfeedback.utils.AnimationUtils;
import com.belearn.smileyfeedback.utils.DbUtil;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MainActivity extends AppCompatActivity {

    private Question question = null;
    private String location = null;
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
                login();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void login() {
        AuthenticationDialogFragment adf = new AuthenticationDialogFragment();
        adf.setMainActivity(this);
        adf.show(getSupportFragmentManager(), AuthenticationDialogFragment.class.getName());
    }

    public void showAdminDialog() {
        AdminDialogFragment adf = new AdminDialogFragment();
        adf.setMainActivity(this);
        adf.setLocation(location);
        adf.show(getSupportFragmentManager(), AdminDialogFragment.class.getName());
    }

    public void createFeedback(View view) {

        String viewId = getResources().getResourceName(view.getId());
        final int grade = Integer.parseInt(viewId.substring(viewId.length() - 1));

        if (formIsValid()) {
            AnimationUtils.rotate(view);
            new CreateFeedbackAsyncTask().execute(grade);
            //new Thread(() -> DbUtil.createFeedback(question.getIdQuestion(), location, grade)).start();
            Toast toast = Toast.makeText(this, R.string.thank_you_for_your_opinion, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private boolean formIsValid() {
        if (location == null || location.trim().length() == 0 || question == null){
            Toast.makeText(this, R.string.please_insert_question, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public void setQuestion(Question question) {
        this.question = question;
        tvQuestion.setText(question.toString());
    }

    public void setLocation(String location) {
        this.location = location;
    }


    private class CreateFeedbackAsyncTask extends AsyncTask<Integer, Void, AsyncResult> {

        @Override
        protected AsyncResult doInBackground(Integer... params) {

            Callable<Integer>task = () -> DbUtil.createFeedback(question.getIdQuestion(), location, params[0]);
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
                Toast.makeText(MainActivity.this, R.string.unable_to_connect_to_server, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
