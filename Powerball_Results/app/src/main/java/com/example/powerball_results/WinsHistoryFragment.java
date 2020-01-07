package com.example.powerball_results;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class WinsHistoryFragment extends Fragment {

    // Array of winning combinations.
    // Half = Red ball, Full = Regular number.
    private double[] winningCombs = {0.5, 1.5, 2.5, 3, 3.5, 4, 4.5, 5, 5.5};

    private EditText[] winNum = new EditText[6];

    private Button btnCheckWin;
    private LinearLayout layout;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wins_history, container, false);

        layout = view.findViewById(R.id.results);
        progressBar = view.findViewById(R.id.progress_bar);

        winNum[0] = view.findViewById(R.id.num1);
        winNum[1] = view.findViewById(R.id.num2);
        winNum[2] = view.findViewById(R.id.num3);
        winNum[3] = view.findViewById(R.id.num4);
        winNum[4] = view.findViewById(R.id.num5);
        winNum[5] = view.findViewById(R.id.redBall);

        btnCheckWin = view.findViewById(R.id.btnCheckWin);
        btnCheckWin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckWinClick();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        onCheckWinClick();
    }

    private void onCheckWinClick(){
        // Check all inputs for data.
        if (checkInput()) {
            hideKeyboard();
            checkWin();
        } else
            Toast.makeText(getContext(), "All numbers required!", Toast.LENGTH_SHORT).show();
    }

    // Check all fields.
    private boolean checkInput() {
        for (int i = 0; i < winNum.length; i++) {
            if (winNum[i].getText().toString().length() == 0)
                return false;
        }

        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private void checkWin() {
        // Get the data from six inputs.
        String[] winNumbers = new String[6];
        for (int i = 0; i < winNumbers.length; i++) {
            winNumbers[i] = winNum[i].getText().toString();
        }

        new AsyncTask<String[], Void, JSONArray>() {
            @Override
            protected void onPreExecute() {
                btnCheckWin.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected JSONArray doInBackground(String[]... strings) {
                // Get all draws history.
                Object o = HttpConnection.connection("all", null);
                JSONArray originArray = null;

                if (o instanceof JSONArray)
                    originArray = (JSONArray) o;

                if (originArray != null) {
                    JSONArray newArray = new JSONArray();

                    for (int i = 0; i < originArray.length(); i++) {
                        try {
                            JSONObject obj = originArray.getJSONObject(i);
                            double winNumCount = isWinning(obj, strings[0]);

                            // Find draws where there is a win according to the win arr.
                            if (winNumCount != 0){
                                obj.put("winning_count", winNumCount);
                                newArray.put(obj);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    return newArray;
                }

                return originArray;
            }

            @Override
            protected void onPostExecute(JSONArray arr) {
                sortByDate(arr);

                // Show dates with their winning combination.
                for (int i = 0; i < arr.length(); i++) {
                    try {
                        // Add the new result to the screen.
                        layout.addView(createView(arr.getJSONObject(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                progressBar.setVisibility(View.GONE);
                btnCheckWin.setEnabled(true);
            }
        }.execute(winNumbers);
    }

    private View createView(JSONObject object) throws JSONException {
        // Create the containing layout.
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // create the TextView for the date.
        TextView resultDate = new TextView(getContext());
        ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resultDate.setLayoutParams(lparams);
        resultDate.setText(object.getString("draw_date").substring(0, 10) + ",  ");
        resultDate.setTextColor(getContext().getColor(R.color.black));
        resultDate.setTextSize(getResources().getDimension(R.dimen.date_size));
        layout.addView(resultDate);

        // Get the amount of regular numbers and red ball.
        double numAmount = object.getDouble("winning_count");

        String textResult = "won " + Math.round(Math.floor(numAmount)) + " Regular numbers";

        if(numAmount - Math.floor(numAmount) > 0){
            textResult += " + Red ball";
        }

        // Create the TextView for the result.
        TextView result = new TextView(getContext());
        lparams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        result.setLayoutParams(lparams);
        result.setText(textResult);
        result.setTextSize(getResources().getDimension(R.dimen.win_num_size));
        result.setTextColor(getContext().getColor(R.color.black));
        layout.addView(result);

        return layout;
    }

    private double isWinning(JSONObject object, String[] winNums) throws JSONException {
        String[] objWinNum = object.getString("winning_numbers").split(" ");
        double count = 0;

        // Check winning numbers.
        for (int i = 0; i < winNums.length - 1; i++) {
            for (int j = 0; j < winNums.length - 1; j++) {
                if (Integer.valueOf(objWinNum[i]) == Integer.valueOf(winNums[j])) {
                    count++;
                    break;
                }
            }
        }

        // Check red ball.
        if (Integer.valueOf(objWinNum[objWinNum.length - 1]) == Integer.valueOf(winNums[winNums.length - 1]))
            count += 0.5;

        // Check if current combination is a winning combination.
        for (int i = 0; i < winningCombs.length; i++) {
            if (count == winningCombs[i])
                return count;
        }

        return 0;
    }

    private void hideKeyboard() {
        View views = getActivity().getCurrentFocus();
        if (views != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(views.getWindowToken(), 0);
        }
    }

    private JSONArray sortByDate(JSONArray arr){
        List<JSONObject> jsonList = new ArrayList<JSONObject>();

        for (int i = 0; i < arr.length(); i++) {
            try {
                jsonList.add(arr.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(jsonList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                try {
                    String lid = lhs.getString("draw_date");
                    String rid = rhs.getString("draw_date");

                    // Here you could parse string id to integer and then compare.
                    return lid.compareTo(rid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return 0;
            }
        });
        return new JSONArray(jsonList);
    }
}
