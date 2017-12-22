package com.belearn.smileyfeedback;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.belearn.smileyfeedback.model.AsyncResult;
import com.belearn.smileyfeedback.model.Question;
import com.belearn.smileyfeedback.utils.DbUtil;
import com.belearn.smileyfeedback.utils.KeyboardUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by dnlbe on 12/20/2017.
 */

public class AdminDialogFragment extends DialogFragment {

    private static final int WINDOW_HEIGHT = 500;
    private EditText etLocation;
    private EditText etQuestion;
    private Spinner spQuestions;
    private Button btnAssign;
    private Button btnArchive;
    private Button btnCreate;
    private Button btnClose;
    private ProgressBar pbDialog;

    private List<Question> questions;
    private Question selectedQuestion = null;

    private MainActivity mainActivity;
    private String location;

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        //params.height= ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = WINDOW_HEIGHT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mainActivity);

        LayoutInflater inflater = mainActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.admin_dialog, null);
        dialogBuilder.setView(dialogView);

        initWidgets(dialogView);
        setupListeners();

        disableFormAndShowDialog();
        new FillQuestionsAsyncTask().execute();

        return dialogBuilder.create();
    }

    private void initWidgets(View view) {
        etLocation = view.findViewById(R.id.etLocation);
        etQuestion= view.findViewById(R.id.etQuestion);
        spQuestions = view.findViewById(R.id.spQuestions);
        btnAssign = view.findViewById(R.id.btnAssign);
        btnArchive = view.findViewById(R.id.btnArchive);
        btnCreate = view.findViewById(R.id.btnCreate);
        btnClose = view.findViewById(R.id.btnClose);
        pbDialog = view.findViewById(R.id.pbDialog);
        if (location != null) {
            etLocation.setText(location);
        }
    }

    private void setupListeners() {
        spQuestions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                selectedQuestion = questions.get(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (questions != null && questions.size() > 0) {
                    selectedQuestion = questions.get(0);
                }
            }
        });
        btnAssign.setOnClickListener(view -> {
            if (assignFormIsValid()) {
                mainActivity.setQuestion(selectedQuestion);
                mainActivity.setLocation(etLocation.getText().toString().trim());
                AdminDialogFragment.this.dismiss();
            }
        });
        btnArchive.setOnClickListener(view -> {
            if (archiveFormIsValid()) {
                disableFormAndShowDialog();
                new ArchiveQuestionAsyncTask().execute();

            }
        });
        btnCreate.setOnClickListener(view -> {
            if (createFormIsValid()) {
                KeyboardUtils.closeKeyboard(mainActivity, etQuestion);
                disableFormAndShowDialog();
                new CreateQuestionAsyncTask().execute();
            }
        });
        btnClose.setOnClickListener(view -> {
            AdminDialogFragment.this.dismiss();
        });
    }

    private void setupAdapter() {
        List<String> list = new ArrayList<>();
        for (Question q : questions) {
            list.add(q.toString());
        }
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(mainActivity, R.layout.spinner_item, list.toArray(new String[list.size()]));
        spQuestions.setAdapter(adapter);
    }

    private boolean assignFormIsValid() {
        if (etLocation.getText().toString().trim().length() == 0){
            Toast.makeText(mainActivity, R.string.please_insert_location, Toast.LENGTH_SHORT).show();
            etLocation.requestFocus();
            return false;
        }
        if (spQuestions.getSelectedItem() == null) {
            Toast.makeText(mainActivity, R.string.please_select_question, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean archiveFormIsValid() {
        if (spQuestions.getSelectedItem() == null) {
            Toast.makeText(mainActivity, R.string.please_select_question, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean createFormIsValid() {
        if (etQuestion.getText().toString().trim().length() == 0){
            Toast.makeText(mainActivity, R.string.please_insert_question, Toast.LENGTH_SHORT).show();
            etQuestion.requestFocus();
            return false;
        }
        return true;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    private void disableFormAndShowDialog() {
        pbDialog.setVisibility(View.VISIBLE);
        btnArchive.setEnabled(false);
        btnAssign.setEnabled(false);
        btnCreate.setEnabled(false);
        btnClose.setEnabled(false);
    }

    private void enableFormAndDissmissDialog() {
        pbDialog.setVisibility(View.INVISIBLE);
        btnArchive.setEnabled(true);
        btnAssign.setEnabled(true);
        btnCreate.setEnabled(true);
        btnClose.setEnabled(true);
    }


    private class FillQuestionsAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            Callable<List<Question>> task = () -> DbUtil.selectActiveQuestions();
            ExecutorService executor = Executors.newFixedThreadPool(1);
            Future<List<Question>> future = executor.submit(task);
            boolean exception = false;
            try {
                questions = future.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                exception = true;
                Log.e(getClass().getName(), ""+e.getMessage());
            } catch (ExecutionException e) {
                exception = true;
                Log.e(getClass().getName(), ""+e.getMessage());
            } catch (TimeoutException e) {
                exception = true;
                Log.e(getClass().getName(), ""+e.getMessage());
            }
            return exception;
        }

        @Override
        protected void onPostExecute(Boolean exception) {
            super.onPostExecute(exception);
            if (exception) {
                Toast.makeText(mainActivity, R.string.unable_to_connect_to_server, Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                setupAdapter();
                enableFormAndDissmissDialog();
                spQuestions.setSelection(questions.size() - 1);
                etQuestion.setText("");
            }
        }
    }


    private class CreateQuestionAsyncTask extends AsyncTask<Void, Void, AsyncResult> {

        @Override
        protected AsyncResult doInBackground(Void... voids) {

            Callable<Integer>task = () -> DbUtil.createQuestion(etQuestion.getText().toString());
            int result = 0;
            ExecutorService executor = Executors.newFixedThreadPool(1);
            Future<Integer> future = executor.submit(task);
            boolean exception = false;
            try {
                result = future.get(10, TimeUnit.SECONDS);
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
                Toast.makeText(mainActivity, R.string.unable_to_connect_to_server, Toast.LENGTH_SHORT).show();
                dismiss();
            } else if (asyncResult.getResult() == 0) {
                Toast.makeText(mainActivity, R.string.unable_to_create_question, Toast.LENGTH_SHORT).show();
                enableFormAndDissmissDialog();
            } else {
                Toast.makeText(mainActivity, R.string.question_created, Toast.LENGTH_SHORT).show();
                new FillQuestionsAsyncTask().execute();
            }
        }
    }

    private class ArchiveQuestionAsyncTask extends AsyncTask<Void, Void, AsyncResult> {

        @Override
        protected AsyncResult doInBackground(Void... voids) {

            Callable<Integer>task = () -> DbUtil.archiveQuestion(selectedQuestion.getIdQuestion());
            int result = 0;
            ExecutorService executor = Executors.newFixedThreadPool(1);
            Future<Integer> future = executor.submit(task);
            boolean exception = false;
            try {
                result = future.get(10, TimeUnit.SECONDS);
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
                Toast.makeText(mainActivity, R.string.unable_to_connect_to_server, Toast.LENGTH_SHORT).show();
                dismiss();
            } else if (asyncResult.getResult() == 0) {
                Toast.makeText(mainActivity, R.string.unable_to_archive_question, Toast.LENGTH_SHORT).show();
                enableFormAndDissmissDialog();
            } else {
                Toast.makeText(mainActivity, R.string.question_archived, Toast.LENGTH_SHORT).show();
                new FillQuestionsAsyncTask().execute();
            }
        }
    }

}