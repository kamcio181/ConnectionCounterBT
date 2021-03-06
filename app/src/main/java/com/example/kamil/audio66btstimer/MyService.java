package com.example.kamil.audio66btstimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MyService extends Service {
    private static final String TAG = "Service";
    private static SharedPreferences preferences;
    private static Runnable runnable;
    private static AudioManager audioManager;
    private static NotificationManager notificationManager;
    private static Notification.Builder builder;
    private static long timePlaying, timeStandby, startTime;
    private static final android.os.Handler handler = new android.os.Handler();
    private static boolean wasScreenOnPreviously;
    private static StringBuilder stringBuilder;
    private static boolean isScreenOn;
    private static boolean isRunning = false;
    private static boolean wasPlaying;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");

        preferences = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        timePlaying = preferences.getLong(Constants.PLAYING_TIME, 0);
        timeStandby = preferences.getLong(Constants.STANDBY_TIME, 0);
        stringBuilder = new StringBuilder();

        Log.e(TAG, stringBuilder.delete(0, stringBuilder.length()).append("playing ").
                append(timePlaying).append(" standby ").append(timeStandby).toString());

        builder= new Notification.Builder(this);
        Intent startActivityIntent = new Intent(this, MainActivity.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(this,
                0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(startActivityPendingIntent).
                setSmallIcon(R.mipmap.ic_launcher).setOngoing(true);

        startTime = Calendar.getInstance().getTimeInMillis();
        wasPlaying = audioManager.isMusicActive();

        runnable = new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, stringBuilder.delete(0, stringBuilder.length()).append("saveTime ").
                        append(timePlaying).append(" ").append(timeStandby).toString());

                isScreenOn = isScreenOn();
                Log.e(TAG, stringBuilder.delete(0, stringBuilder.length()).append("isScreenOn ").
                        append(isScreenOn).toString());
                Boolean isMusicActive = audioManager.isMusicActive();
                if(isMusicActive){
                    Log.e(TAG, "music IS active");
                    timePlaying++;
                    if(timePlaying % 60 == 0 && isScreenOn) {
                        Log.e(TAG, "timePlaying/60 notify");
                        notificationManager.notify(Constants.NOTIFICATION_ID,
                                getContent(builder, timePlaying, timeStandby, false).build());
                    }
                } else {
                    Log.e(TAG, "music is NOT active");
                    timeStandby++;
                    if(timeStandby % 60 == 0 && isScreenOn) {
                        Log.e(TAG, "timePlaying/60 notify");
                        notificationManager.notify(Constants.NOTIFICATION_ID, getContent(builder, timePlaying, timeStandby, false).build());
                    }
                }

                if(isMusicActive != wasPlaying){
                    saveToLog(wasPlaying, startTime, Calendar.getInstance().getTimeInMillis());
                    startTime = Calendar.getInstance().getTimeInMillis();
                }

                if (isScreenOn && !wasScreenOnPreviously){
                    Log.e(TAG, "screen was turned on");
                    notificationManager.notify(Constants.NOTIFICATION_ID, getContent(builder, timePlaying, timeStandby, false).build());
                }

                if(isScreenOn){
                    publishTime();
                }

                wasScreenOnPreviously = isScreenOn;
                wasPlaying = isMusicActive;
                handler.postDelayed(this, 1000);
            }
        };
    }

    private boolean isScreenOn(){
        DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = dm.getDisplays();
        for (Display display : displays) {
            if (display.getState() != Display.STATE_OFF) {
                return true;
            }
        }
        return false;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");

        handler.removeCallbacks(runnable);
        isRunning = false;

        saveToLog(wasPlaying, startTime, Calendar.getInstance().getTimeInMillis());

        builder.setOngoing(false);
        notificationManager.notify(Constants.NOTIFICATION_ID, getContent(builder, timePlaying, timeStandby, true).build());

        preferences.edit().putLong(Constants.PLAYING_TIME, timePlaying)
                .putLong(Constants.STANDBY_TIME, timeStandby).apply();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "onTaskRemoved");

        handler.removeCallbacks(runnable);
        isRunning = false;

        builder.setOngoing(false);
        notificationManager.notify(Constants.NOTIFICATION_ID, getContent(builder, timePlaying, timeStandby, true).build());

        preferences.edit().putLong(Constants.PLAYING_TIME, timePlaying)
                .putLong(Constants.STANDBY_TIME, timeStandby).apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStart");
        if(intent != null && intent.hasExtra(Constants.RESET_TIMER)){
            timePlaying = 0;
            timeStandby = 0;
        } else if(!isRunning) {
            isRunning = true;
            handler.postDelayed(runnable, 1000);
        }
        return super.onStartCommand(intent, Service.START_STICKY, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void publishTime() {
        Intent intent = new Intent(Constants.NOTIFICATION);
        intent.putExtra(Constants.PLAYING_TIME, timePlaying);
        intent.putExtra(Constants.STANDBY_TIME, timeStandby);
        sendBroadcast(intent);
    }

    private Notification.Builder getContent(Notification.Builder builder, long playingTime, long standbyTime, boolean addSeconds){
        long hours = TimeUnit.SECONDS.toHours(playingTime);
        long minutes = TimeUnit.SECONDS.toMinutes(playingTime) - hours*60;
        long hours2 = TimeUnit.SECONDS.toHours(standbyTime);
        long minutes2 = TimeUnit.SECONDS.toMinutes(standbyTime) - hours2*60;
        long totalTime = playingTime + standbyTime;
        long hours3 = TimeUnit.SECONDS.toHours(totalTime);
        long minutes3 = TimeUnit.SECONDS.toMinutes(totalTime) - hours3*60;

        String content;
        String title;

        if(addSeconds){
            long seconds = playingTime - minutes*60 - hours*3600;
            long seconds2 = standbyTime - minutes2*60 - hours2*3600;
            long seconds3 = totalTime - minutes3*60 - hours3*3600;

            content = stringBuilder.delete(0, stringBuilder.length())
                    .append(getString(R.string.playing)).append(": ")
                    .append(String.format("%02d:%02d:%02d", hours, minutes, seconds))
                    .append("\n").append(getString(R.string.standby)).append(": ")
                    .append(String.format("%02d:%02d:%02d", hours2, minutes2, seconds2)).toString();
            title = stringBuilder.delete(0, stringBuilder.length()).append(getString(R.string.total)).append(": ")
                    .append(String.format("%02d:%02d:%02d", hours3, minutes3, seconds3)).toString();

        } else {
            content = stringBuilder.delete(0, stringBuilder.length())
                    .append(getString(R.string.playing)).append(": ")
                    .append(String.format("%02d:%02d", hours, minutes))
                    .append("\n").append(getString(R.string.standby)).append(": ")
                    .append(String.format("%02d:%02d", hours2, minutes2)).toString();
            title = stringBuilder.delete(0, stringBuilder.length()).append(getString(R.string.total)).append(": ")
                    .append(String.format("%02d:%02d", hours3, minutes3)).toString();
        }

        return builder.setStyle(new Notification.BigTextStyle().bigText(content)).setContentTitle(title);
    }

    private void saveToLog(boolean isPlaying, long startTime, long stopTime){
        FileOutputStream outputStream;
        stringBuilder.delete(0, stringBuilder.length());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);

        stringBuilder.append(isPlaying? "Playing " : "Standby ")
                .append(String.format("%1$te/%1$tm/%1$tY %1$tT", calendar)).append(" - ");
        calendar.setTimeInMillis(stopTime);
        stringBuilder.append(String.format("%1$te/%1$tm/%1$tY %1$tT", calendar)).append("; ");
        stringBuilder.append(String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(stopTime-startTime)))
                .append(" min\n");

        try {
            outputStream = new FileOutputStream(new File(getFilesDir(), Constants.LOG_NAME), true);
            outputStream.write(stringBuilder.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
