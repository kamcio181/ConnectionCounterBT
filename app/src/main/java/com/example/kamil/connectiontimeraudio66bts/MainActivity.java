package com.example.kamil.connectiontimeraudio66bts;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static MainActivity context = null;
    private static final String PREFS_NAME = "PrefsName";
    private static final String PLAYING_TIME_KEY = "playing";
    private static final String STANDBY_TIME_KEY = "standby";
    private Button button, button2;
    private TextView textView;
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button.setOnClickListener(this);
        button2.setOnClickListener(this);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private Dialog setTimeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_set_time, null);
        final RadioButton setTimeRadioButton = (RadioButton) layout.findViewById(R.id.radioButton);
        final EditText minutesEditText = (EditText) layout.findViewById(R.id.editText);

        return builder.setTitle("Set playing time").setView(layout).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long value = minutesEditText.getText().toString().length() > 0? (Long.parseLong(minutesEditText.getText().toString()))*60 : 0;
                if(!setTimeRadioButton.isChecked()){
                    value = preferences.getLong(PLAYING_TIME_KEY, 0) + value;
                }
                preferences.edit().putLong(PLAYING_TIME_KEY, value).apply();
                Log.e("Main", "setTime " + value);
                postResult(value, preferences.getLong(STANDBY_TIME_KEY, 0));
            }
        }).setNegativeButton("Cancel", null).create();
    }

    @Override
    protected void onStart() {
        super.onStart();
        postResult(preferences.getLong(PLAYING_TIME_KEY, 0), preferences.getLong(STANDBY_TIME_KEY, 0));
    }

    @Override
    protected void onPause() {
        super.onPause();
        context = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        context = this;
    }

    public void postResult(long totalSecondsPlay, long totalSecondsStandby){
        long hours = TimeUnit.SECONDS.toHours(totalSecondsPlay);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSecondsPlay) - hours*60;
        long seconds =totalSecondsPlay - minutes*60 - hours*3600;
        long hours2 = TimeUnit.SECONDS.toHours(totalSecondsStandby);
        long minutes2 = TimeUnit.SECONDS.toMinutes(totalSecondsStandby) - hours2*60;
        long seconds2 = totalSecondsStandby - minutes2*60 - hours2*3600;
        long totalTime = totalSecondsPlay + totalSecondsStandby;
        long hours3 = TimeUnit.SECONDS.toHours(totalTime);
        long minutes3 = TimeUnit.SECONDS.toMinutes(totalTime) - hours3*60;
        long seconds3 = totalTime - minutes3*60 - hours3*3600;
        textView.setText("Total: " + String.format("%02d:%02d:%02d", hours3, minutes3, seconds3) +
                "\nPlaying: " + String.format("%02d:%02d:%02d", hours, minutes, seconds) +
                "\nStandby: " + String.format("%02d:%02d:%02d", hours2, minutes2, seconds2));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                preferences.edit().remove(PLAYING_TIME_KEY).remove(STANDBY_TIME_KEY).apply();
                postResult(0, 0);
                break;
            case R.id.button2:
                setTimeDialog().show();
                break;
        }
    }
}
