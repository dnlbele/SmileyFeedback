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
import com.belearn.smileyfeedback.model.Location;
import com.belearn.smileyfeedback.model.Question;
import com.belearn.smileyfeedback.utils.DbUtil;
import com.belearn.smileyfeedback.utils.Utils;

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

    private static final int WINDOW_HEIGHT = 560;
    private EditText etQuestionLocation;
    private Spinner spQuestions;
    private Spinner spLocations;
    private Button btnAssign;
    private Button btnArchiveQuestion;
    private Button btnArchiveLocation;
    private Button btnCreateQuestion;
    private Button btnCreateLocation;
    private Button btnClose;
    private ProgressBar pbDialog;

    private List<Question> questions;
    private Question selectedQuestion = null;

    private List<Location> locations;
    private Location selectedLocation = null;

    private MainActivity mainActivity;

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
        new FillQuestionsLocationsAsyncTask().execute();
        return dialogBuilder.create();
    }

    private void initWidgets(View view) {
        etQuestionLocation = view.findViewById(R.id.etQuestionLocation);
        spQuestions = view.findViewById(R.id.spQuestions);
        spLocations = view.findViewById(R.id.spLocations);
        btnAssign = view.findViewById(R.id.btnAssign);
        btnArchiveQuestion = view.findViewById(R.id.btnArchiveQuestion);
        btnArchiveLocation = view.findViewById(R.id.btnArchiveLocation);
        btnCreateQuestion = view.findViewById(R.id.btnCreateQuestion);
        btnCreateLocation = view.findViewById(R.id.btnCreateLocation);
        btnClose = view.findViewById(R.id.btnClose);
        pbDialog = view.findViewById(R.id.pbDialog);

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
        spLocations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                selectedLocation = locations.get(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (locations != null && locations.size() > 0) {
                    selectedLocation = locations.get(0);
                }
            }
        });

        btnAssign.setOnClickListener(view -> {
            if (assignFormIsValid()) {
                mainActivity.setQuestion(selectedQuestion);
                mainActivity.setLocation(selectedLocation);
                AdminDialogFragment.this.dismiss();
            }
        });
        btnArchiveQuestion.setOnClickListener(view -> {
            if (archiveQuestionFormIsValid()) {
                disableFormAndShowDialog();
                new ArchiveQuestionAsyncTask().execute();

            }
        });
        btnArchiveLocation.setOnClickListener(view -> {
            if (archiveLocationFormIsValid()) {
                disableFormAndShowDialog();
                new ArchiveLocationAsyncTask().execute();

            }
        });
        btnCreateQuestion.setOnClickListener(view -> {
            if (createFormIsValid()) {
                Utils.closeKeyboard(mainActivity, etQuestionLocation);
                disableFormAndShowDialog();
                new CreateQuestionAsyncTask().execute();
            }
        });
        btnCreateLocation.setOnClickListener(view -> {
            if (createFormIsValid()) {
                Utils.closeKeyboard(mainActivity, etQuestionLocation);
                disableFormAndShowDialog();
                new CreateLocationAsyncTask().execute();
            }
        });
        btnClose.setOnClickListener(view -> {
            AdminDialogFragment.this.dismiss();
        });
    }

    private void setupQuestionsAdapter() {
        List<String> listQuestions = new ArrayList<>();
        for (Question q : questions) {
            listQuestions.add(q.toString());
        }
        ArrayAdapter<String> adapterQuestions =
                new ArrayAdapter<>(mainActivity, R.layout.spinner_item, listQuestions.toArray(new String[listQuestions.size()]));
        spQuestions.setAdapter(adapterQuestions);

        if (mainActivity.getQuestion() != null && questions.indexOf(mainActivity.getQuestion()) != -1) {
            spQuestions.setSelection(questions.indexOf(mainActivity.getQuestion()));
        }
    }

    private void setupLocationAdapter() {
        List<String> listLocations = new ArrayList<>();
        for (Location l : locations) {
            listLocations.add(l.toString());
        }
        ArrayAdapter<String> adapterLocations =
                new ArrayAdapter<>(mainActivity, R.layout.spinner_item, listLocations.toArray(new String[listLocations.size()]));
        spLocations.setAdapter(adapterLocations);

        if (mainActivity.getLocation() != null && locations.indexOf(mainActivity.getLocation()) != -1) {
            spLocations.setSelection(locations.indexOf(mainActivity.getLocation()));
        }

    }

    private boolean assignFormIsValid() {
        if (spLocations.getSelectedItem() == null) {
            Toast.makeText(mainActivity, R.string.please_select_location, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spQuestions.getSelectedItem() == null) {
            Toast.makeText(mainActivity, R.string.please_select_question, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean archiveQuestionFormIsValid() {
        if (spQuestions.getSelectedItem() == null) {
            Toast.makeText(mainActivity, R.string.please_select_question, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean archiveLocationFormIsValid() {
        if (spLocations.getSelectedItem() == null) {
            Toast.makeText(mainActivity, R.string.please_select_location, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean createFormIsValid() {
        if (etQuestionLocation.getText().toString().trim().length() == 0){
            etQuestionLocation.setError(getString(R.string.please_insert_location_or_question));
            etQuestionLocation.requestFocus();
            return false;
        }
        return true;
    }

    private void disableFormAndShowDialog() {
        pbDialog.setVisibility(View.VISIBLE);
        btnArchiveQuestion.setEnabled(false);
        btnArchiveLocation.setEnabled(false);
        btnAssign.setEnabled(false);
        btnCreateQuestion.setEnabled(false);
        btnCreateLocation.setEnabled(false);
        btnClose.setEnabled(false);
    }

    private void enableFormAndDissmissDialog() {
        pbDialog.setVisibility(View.INVISIBLE);
        btnArchiveQuestion.setEnabled(true);
        btnArchiveLocation.setEnabled(true);
        btnAssign.setEnabled(true);
        btnCreateQuestion.setEnabled(true);
        btnCreateLocation.setEnabled(true);
        btnClose.setEnabled(true);
    }


    private class FillQuestionsLocationsAsyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            Callable<List<Question>> taskQuestions = () -> DbUtil.selectActiveQuestions();
            Callable<List<Location>> taskLocations = () -> DbUtil.selectActiveLocations();
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<List<Question>> futureQuestion = executor.submit(taskQuestions);
            Future<List<Location>> futureLocation = executor.submit(taskLocations);
            boolean exception = false;
            try {
                questions = futureQuestion.get(10, TimeUnit.SECONDS);
                locations = futureLocation.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                exception = true;
                Log.e(getClass().getName(), ""+e.getMessage());
            } catch (ExecutionException e) {
                exception = true;
                Log.e(getClass().getName(), ""+e.getMessage());
            } catch (TimeoutException e) {
                exception = true;
                Log.e(getClass().getName(), ""+e.getMessage());
            } finally {
                if (executor != null) {
                    try {
                        executor.shutdown();
                    } catch (Exception e) {
                        exception = true;
                        Log.e(getClass().getName(), ""+e.getMessage());
                    }
                }
            }
            return exception;
        }
        @Override
        protected void onPostExecute(Boolean exception) {
            super.onPostExecute(exception);
            if (exception) {
                Utils.showToastAtBottom(getContext(), R.string.unable_to_connect_to_server);
                dismiss();
            } else {
                setupQuestionsAdapter();
                setupLocationAdapter();
                enableFormAndDissmissDialog();
            }
        }
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
            } finally {
                if (executor != null) {
                    try {
                        executor.shutdown();
                    } catch (Exception e) {
                        exception = true;
                        Log.e(getClass().getName(), ""+e.getMessage());
                    }
                }
            }
            return exception;
        }
        @Override
        protected void onPostExecute(Boolean exception) {
            super.onPostExecute(exception);
            if (exception) {
                Utils.showToastAtBottom(getContext(), R.string.unable_to_connect_to_server);
                dismiss();
            } else {
                setupQuestionsAdapter();
                enableFormAndDissmissDialog();
                spQuestions.setSelection(questions.size() - 1);
            }
        }
    }

    private class FillLocationsAsyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            Callable<List<Location>> task = () -> DbUtil.selectActiveLocations();
            ExecutorService executor = Executors.newFixedThreadPool(1);
            Future<List<Location>> future = executor.submit(task);
            boolean exception = false;
            try {
                locations = future.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                exception = true;
                Log.e(getClass().getName(), ""+e.getMessage());
            } catch (ExecutionException e) {
                exception = true;
                Log.e(getClass().getName(), ""+e.getMessage());
            } catch (TimeoutException e) {
                exception = true;
                Log.e(getClass().getName(), ""+e.getMessage());
            } finally {
                if (executor != null) {
                    try {
                        executor.shutdown();
                    } catch (Exception e) {
                        exception = true;
                        Log.e(getClass().getName(), ""+e.getMessage());
                    }
                }
            }
            return exception;
        }
        @Override
        protected void onPostExecute(Boolean exception) {
            super.onPostExecute(exception);
            if (exception) {
                Utils.showToastAtBottom(getContext(), R.string.unable_to_connect_to_server);
                dismiss();
            } else {
                setupLocationAdapter();
                enableFormAndDissmissDialog();
                spLocations.setSelection(locations.size() - 1);
            }
        }
    }


    private class CreateQuestionAsyncTask extends AsyncTask<Void, Void, AsyncResult> {
        @Override
        protected AsyncResult doInBackground(Void... voids) {
            Callable<Integer>task = () -> DbUtil.createQuestion(etQuestionLocation.getText().toString());
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
            } finally {
                if (executor != null) {
                    try {
                        executor.shutdown();
                    } catch (Exception e) {
                        exception = true;
                        Log.e(getClass().getName(), ""+e.getMessage());
                    }
                }
            }
            return new AsyncResult(exception, result);
        }
        @Override
        protected void onPostExecute(AsyncResult asyncResult) {
            super.onPostExecute(asyncResult);
            if (asyncResult.isException()) {
                Utils.showToastAtBottom(getContext(), R.string.unable_to_connect_to_server);
                dismiss();
            } else if (asyncResult.getResult() == 0) {
                Toast.makeText(mainActivity, R.string.unable_to_create_question, Toast.LENGTH_SHORT).show();
                enableFormAndDissmissDialog();
            } else {
                Toast.makeText(mainActivity, R.string.question_created, Toast.LENGTH_SHORT).show();
                etQuestionLocation.setText("");
                new FillQuestionsAsyncTask().execute();
            }
        }
    }

    private class CreateLocationAsyncTask extends AsyncTask<Void, Void, AsyncResult> {
        @Override
        protected AsyncResult doInBackground(Void... voids) {
            Callable<Integer>task = () -> DbUtil.createLocation(etQuestionLocation.getText().toString());
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
            } finally {
                if (executor != null) {
                    try {
                        executor.shutdown();
                    } catch (Exception e) {
                        exception = true;
                        Log.e(getClass().getName(), ""+e.getMessage());
                    }
                }
            }
            return new AsyncResult(exception, result);
        }
        @Override
        protected void onPostExecute(AsyncResult asyncResult) {
            super.onPostExecute(asyncResult);
            if (asyncResult.isException()) {
                Utils.showToastAtBottom(getContext(), R.string.unable_to_connect_to_server);
                dismiss();
            } else if (asyncResult.getResult() == 0) {
                Toast.makeText(mainActivity, R.string.unable_to_create_location, Toast.LENGTH_SHORT).show();
                enableFormAndDissmissDialog();
            } else {
                Toast.makeText(mainActivity, R.string.location_created, Toast.LENGTH_SHORT).show();
                etQuestionLocation.setText("");
                new FillLocationsAsyncTask().execute();
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
            } finally {
                if (executor != null) {
                    try {
                        executor.shutdown();
                    } catch (Exception e) {
                        exception = true;
                        Log.e(getClass().getName(), ""+e.getMessage());
                    }
                }
            }
            return new AsyncResult(exception, result);
        }
        @Override
        protected void onPostExecute(AsyncResult asyncResult) {
            super.onPostExecute(asyncResult);
            if (asyncResult.isException()) {
                Utils.showToastAtBottom(getContext(), R.string.unable_to_connect_to_server);
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

    private class ArchiveLocationAsyncTask extends AsyncTask<Void, Void, AsyncResult> {
        @Override
        protected AsyncResult doInBackground(Void... voids) {
            Callable<Integer>task = () -> DbUtil.archiveLocation(selectedLocation.getIdLocation());
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
            } finally {
                if (executor != null) {
                    try {
                        executor.shutdown();
                    } catch (Exception e) {
                        exception = true;
                        Log.e(getClass().getName(), ""+e.getMessage());
                    }
                }
            }
            return new AsyncResult(exception, result);
        }

        @Override
        protected void onPostExecute(AsyncResult asyncResult) {
            super.onPostExecute(asyncResult);
            if (asyncResult.isException()) {
                Utils.showToastAtBottom(getContext(), R.string.unable_to_connect_to_server);
                dismiss();
            } else if (asyncResult.getResult() == 0) {
                Toast.makeText(mainActivity, R.string.unable_to_archive_location, Toast.LENGTH_SHORT).show();
                enableFormAndDissmissDialog();
            } else {
                Toast.makeText(mainActivity, R.string.location_archived, Toast.LENGTH_SHORT).show();
                new FillLocationsAsyncTask().execute();
            }
        }
    }


}