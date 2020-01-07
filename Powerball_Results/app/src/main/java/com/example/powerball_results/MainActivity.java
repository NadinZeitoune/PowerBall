package com.example.powerball_results;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnHistoricResults;
    private Button btnWinsHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnHistoricResults = findViewById(R.id.btnHistoricResults);
        btnWinsHistory = findViewById(R.id.btnWinsHistory);

        HistoricResultsFragment historicResultsFragment = new HistoricResultsFragment();
        WinsHistoryFragment winsHistoryFragment = new WinsHistoryFragment();


        // Create click listeners.
        setClickListener(btnHistoricResults, historicResultsFragment, this);
        setClickListener(btnWinsHistory, winsHistoryFragment, this);

        // Show default fragment.
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentLayout, historicResultsFragment).commit();
    }

    private void setClickListener(Button button, final Fragment fragment, final Activity activity){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLayout, fragment).addToBackStack(null).commit();
                hideKeyboard(activity);
            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = activity.getCurrentFocus();

        if (view == null) {
            view = new View(activity);
        }

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
