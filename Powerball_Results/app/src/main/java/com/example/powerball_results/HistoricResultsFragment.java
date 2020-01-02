package com.example.powerball_results;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class HistoricResultsFragment extends Fragment {

    private LinearLayout layout;
    private ProgressBar progressBar;

    private RadioGroup rgFilter;
    private RadioButton rbAll;
    private RadioButton rbLimit;
    private RadioButton rbDate;
    private EditText filterData;

    private OnFragmentInteractionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historic_results, container, false);

        layout = view.findViewById(R.id.results);
        progressBar = view.findViewById(R.id.progress_bar);

        rgFilter = view.findViewById(R.id.rgFilters);
        rbAll = view.findViewById(R.id.rbAll);
        rbLimit = view.findViewById(R.id.rbLimit);
        rbDate = view.findViewById(R.id.rbDate);
        filterData = view.findViewById(R.id.filterData);

        // Show/ hide the EditText with the right hint.
        rgFilter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // Empty the data.
                filterData.setText("");

                switch (checkedId) {
                    case R.id.rbAll:
                        // Disappear the input area.
                        showKeyboard(false, null);
                        filterData.setVisibility(View.GONE);
                        break;

                    case R.id.rbLimit:
                        // Show input area.
                        filterData.setVisibility(View.VISIBLE);
                        filterData.setInputType(InputType.TYPE_CLASS_NUMBER);
                        showKeyboard(true, filterData);

                        // change hint.
                        filterData.setHint("0-9999");
                        break;

                    case R.id.rbDate:
                        // Show input area.
                        filterData.setVisibility(View.VISIBLE);
                        filterData.setInputType(InputType.TYPE_CLASS_DATETIME);
                        showKeyboard(true, filterData);

                        // change hint.
                        filterData.setHint("2019-12-31");
                        break;
                }
            }
        });

        // Show all the results.
        getAndShowResults("all", "");

        return view;
    }

    public interface OnFragmentInteractionListener {

    }

    private void showKeyboard(boolean isShow, EditText editText) {
        // Pop the keyboard on the precise location.
        if (isShow) {
            editText.requestFocus();
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(editText, InputMethodManager.SHOW_FORCED);
        }

        // Hide the keyboard from the screen.
        else {
            View views = getActivity().getCurrentFocus();
            if (views != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(views.getWindowToken(), 0);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void getAndShowResults(final String action, final String filter) {
        // Show ProgressBar.
        progressBar.setVisibility(View.VISIBLE);

        // Get the requested results.
        new AsyncTask<Void, Void, JSONArray>() {

            @Override
            protected JSONArray doInBackground(Void... voids) {
                Object o = HttpConnection.connection(action, filter);
                JSONArray array = null;

                if (o instanceof JSONArray)
                    array = (JSONArray) o;

                return array;
            }

            @Override
            protected void onPostExecute(JSONArray arr) {
                // Delete the old results.
                layout.removeAllViews();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            // Add the new result to the screen.
                            layout.addView(createView(arr.getJSONObject(i)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Hide the progressBar.
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    // TODO: change the look of the layout. Maybe to use TableLayout & TableRow.
    // Creating the view with all the data of the result.
    private View createView(JSONObject object) throws JSONException {
        // Create the containing layout.
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // create the TextView for the date.
        TextView resultDate = new TextView(getContext());
        ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resultDate.setLayoutParams(lparams);
        resultDate.setText(object.getString("draw_date").substring(0, 10) + ":  ");
        resultDate.setTextColor(getContext().getColor(R.color.black));
        resultDate.setTextSize(getResources().getDimension(R.dimen.date_size));
        layout.addView(resultDate);

        // Create the TextView for the winning numbers.
        TextView resultWinNums = new TextView(getContext());
        lparams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resultWinNums.setLayoutParams(lparams);
        String winNums = object.getString("winning_numbers");
        String redBall = winNums.substring(winNums.length() - 2);
        winNums = winNums.substring(0, winNums.length() - 2);
        resultWinNums.setText(winNums);
        resultWinNums.setTextSize(getResources().getDimension(R.dimen.win_num_size));
        resultWinNums.setTextColor(getContext().getColor(R.color.win_num));
        layout.addView(resultWinNums);

        // Create the TextView for the red ball.
        TextView resultRedBall = new TextView(getContext());
        lparams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resultRedBall.setLayoutParams(lparams);
        resultRedBall.setText("   RB: " + redBall);
        resultRedBall.setTextSize(getResources().getDimension(R.dimen.win_num_size));
        resultRedBall.setTextColor(getContext().getColor(R.color.red_ball));
        layout.addView(resultRedBall);

        return layout;
    }

    // Submit the choice of the filter and it's data.
    public void chooseFilter(View view) {
        // Remove keyboard from screen.
        showKeyboard(false, null);

        // Check that the data is answering all the requirements.
        boolean dataOK = false;
        String data = null;

        switch (rgFilter.getCheckedRadioButtonId()) {
            case R.id.rbAll:
                dataOK = true;
                checkData(dataOK, "all", "");
                break;

            case R.id.rbLimit:
                // Check for a positive number. 0-9999
                data = filterData.getText().toString();

                dataOK = TextUtils.isDigitsOnly(data);
                checkData(dataOK, "limit", data);

                break;

            case R.id.rbDate:
                data = filterData.getText().toString();

                // Check for three numbers. 4,2,2, Year, Month, Day.
                String[] date = data.split("-");

                if (date.length != 3 || date[0].length() != 4 || date[1].length() != 2 || date[2].length() != 2){
                    dataOK = false;
                    checkData(dataOK, "date", data);
                    break;
                }

                // Check the numbers for relevant values.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                format.setLenient(false);

                try {
                    format.parse(data);
                    dataOK = true;

                } catch (ParseException e) {
                    dataOK = false;
                }

                checkData(dataOK, "date", data);
        }
    }

    private void checkData(boolean dataOK, String filter, String data) {
        // If true: continue to "getAndShowResults" with the data.
        if (dataOK) {
            getAndShowResults(filter, data);
        }

        // If false: pop message to the user.
        else {
            filterData.setText("");
            Toast.makeText(getContext(), "Please check again the numbers", Toast.LENGTH_SHORT).show();
        }
    }
}
