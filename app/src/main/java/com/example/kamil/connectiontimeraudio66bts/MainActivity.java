package com.example.kamil.connectiontimeraudio66bts;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static MainActivity context = null;
    private static final String PREFS_NAME = "PrefsName";
    private static final String TIME_KEY = "TimeKey";
    private Button button;
    private TextView textView;
    private SharedPreferences preferences; //TODO set current value, check if music is playing, change interval time and save time on disconnect
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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
        }
    }
}
