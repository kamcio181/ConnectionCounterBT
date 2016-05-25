package com.example.kamil.connectiontimeraudio66bts;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
    private static final String TIME_KEY = "TimeKey";
    private Button button, button2;
    private TextView textView;
    private SharedPreferences preferences; //TODO check if music is playing
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

        return builder.setTitle("Set time").setView(layout).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long value = minutesEditText.getText().toString().length() > 0? (Long.parseLong(minutesEditText.getText().toString()))*60000 : 0;
                if(!setTimeRadioButton.isChecked()){
                    value = preferences.getLong(TIME_KEY, 0) + value;
                }
                preferences.edit().putLong(TIME_KEY, value).apply();
                postResult(value);
            }
        }).setNegativeButton("Cancel", null).create();
    }

    @Override
    protected void onStart() {
        super.onStart();
        postResult(preferences.getLong(TIME_KEY, 0));
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

    public void postResult(long totalSeconds){
        long hours = TimeUnit.SECONDS.toHours(totalSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) - hours*60;
        long seconds = TimeUnit.SECONDS.toSeconds(totalSeconds) - minutes*60;
        textView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                preferences.edit().remove(TIME_KEY).apply();
                postResult(0);
                break;
            case R.id.button2:
                setTimeDialog().show();
                break;
        }
    }
}
