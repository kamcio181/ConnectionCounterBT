package com.example.kamil.audio66btstimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private TextView textView;
    private SharedPreferences preferences;
    private final StringBuilder builder = new StringBuilder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                postResult(bundle.getLong(Constants.PLAYING_TIME), bundle.getLong(Constants.STANDBY_TIME));
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        postResult(preferences.getLong(Constants.PLAYING_TIME, 0), preferences.getLong(Constants.STANDBY_TIME, 0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(Constants.NOTIFICATION));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
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
        builder.delete(0, builder.length()).append("<b>").append(getString(R.string.total)).append(": </b>")
                .append(String.format("%02d:%02d:%02d", hours3, minutes3, seconds3))
                .append("<br><b>").append(getString(R.string.playing)).append(": </b>")
                .append(String.format("%02d:%02d:%02d", hours, minutes, seconds))
                .append("<br><b>").append(getString(R.string.standby)).append(": </b>")
                .append(String.format("%02d:%02d:%02d", hours2, minutes2, seconds2));
        textView.setText(Html.fromHtml(builder.toString()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_reset:
                getConfirmationDialog("Do you want to reset timer?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, R.string.timer_cleared, Toast.LENGTH_SHORT).show();
                        preferences.edit().remove(Constants.PLAYING_TIME).remove(Constants.STANDBY_TIME).apply();
                        Intent intent = new Intent(MainActivity.this, MyService.class);
                        intent.putExtra(Constants.RESET_TIMER, true);
                        startService(intent);
                        postResult(0, 0);
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(Constants.NOTIFICATION_ID);
                        clearLog();
                    }
                }).show();
                break;
            case R.id.action_set:
                setTimeDialog().show();
                break;
            case R.id.action_log:
                Dialog dialog;
                if((dialog = getLogDialog()) != null)
                    dialog.show();
                else
                    Toast.makeText(this, "Log is empty", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Dialog getLogDialog() {
        BufferedReader input;
        File file;
        try {
            file = new File(getFilesDir(), Constants.LOG_NAME);
            if(file.exists()) {
                input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line;
                StringBuilder buffer = new StringBuilder();
                while ((line = input.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                return new AlertDialog.Builder(this).setTitle("Log").setMessage(buffer.toString().trim()).create();
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private Dialog getConfirmationDialog(String title, DialogInterface.OnClickListener listener) {
        return new AlertDialog.Builder(this).setTitle(title)
                .setPositiveButton(getString(R.string.confirm), listener)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
                    }
                }).create();
    }

    private Dialog setTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_set_time, null);
        final RadioButton setTimeRadioButton = (RadioButton) layout.findViewById(R.id.radioButton);
        final EditText minutesEditText = (EditText) layout.findViewById(R.id.editText);

        return builder.setTitle(getString(R.string.set_playing_time)).setView(layout).setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long value = minutesEditText.getText().toString().length() > 0 ? (Long.parseLong(minutesEditText.getText().toString())) * 60 : 0;
                if (!setTimeRadioButton.isChecked()) {
                    value = preferences.getLong(Constants.PLAYING_TIME, 0) + value;
                    saveToLog("Added " + minutesEditText.getText().toString() + " min");
                } else {
                    clearLog();
                    saveToLog("Set " + minutesEditText.getText().toString() + " min");
                }
                preferences.edit().putLong(Constants.PLAYING_TIME, value).apply();
                Toast.makeText(MainActivity.this, R.string.time_updated, Toast.LENGTH_SHORT).show();
                Log.e("Main", "setTime " + value);
                postResult(value, preferences.getLong(Constants.STANDBY_TIME, 0));
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
            }
        }).create();
    }

    private void clearLog() {
        File log = new File(getFilesDir(), Constants.LOG_NAME);
        if (log.exists())
            log.delete();
    }

    private void saveToLog(String message) {
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(new File(getFilesDir(), Constants.LOG_NAME), true);
            outputStream.write(message.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
