package com.example.kamil.connectiontimeraudio66bts;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class MyService extends Service {
    private static final String TAG = "Service";
    private static final String PREFS_NAME = "PrefsName";
    private static final String PLAYING_TIME_KEY = "playing";
    private static final String STANDBY_TIME_KEY = "standby";
    private static SharedPreferences preferences;
    private static Runnable runnable;
    private static AudioManager audioManager;
    private static NotificationManager notificationManager;
    private static NotificationCompat.Builder builder;
    private static long timePlaying, timeStandby;
    private static int notificationId = 1;
    private static final android.os.Handler handler = new android.os.Handler();
    private static boolean isScreenOn;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");

        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        timePlaying = preferences.getLong(PLAYING_TIME_KEY, 0);
        timeStandby = preferences.getLong(STANDBY_TIME_KEY, 0);
        Log.e(TAG, "playing " + timePlaying + " standby " + timeStandby);

        builder= new NotificationCompat.Builder(this);
        Intent startActivityIntent = new Intent(this, MainActivity.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(this,
                0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(startActivityPendingIntent).
                setSmallIcon(R.mipmap.ic_launcher).setOngoing(true);

        runnable = new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "saveTime " + timePlaying + " " + timeStandby);
                long time;
                if(audioManager.isMusicActive()){
                    Log.e(TAG, "is music active true");
                    timePlaying++;
                    time = timePlaying;
                } else {
                    Log.e(TAG, "is music active false");
                    timeStandby++;
                    time = timeStandby;
                }
                if(isScreenOn && time % 60 == 0) {
                    Log.e(TAG, "timePlaying/60 " + "notify");
//                                        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getContent(timePlaying, timeStandby, false)));
//                                        notificationManager.notify(notificationId, builder.build());
                    notificationManager.notify(notificationId, getContent(builder, timePlaying, timeStandby, false).build());
                }

//                if(time % 30 == 0) {
//                    Log.e(TAG, "timePlaying/30 " + "save to file");
//                    preferences.edit().putLong(PLAYING_TIME_KEY, timePlaying)
//                            .putLong(STANDBY_TIME_KEY, timeStandby).apply();
//                }

                if (MainActivity.context != null) { //TODO broadcast receiver in activity?
                    Log.e(TAG, "activity is available" + "post result");
                    MainActivity.context.postResult(timePlaying, timeStandby);
                }
                handler.postDelayed(this, 1000);
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");

        preferences.edit().putLong(PLAYING_TIME_KEY, timePlaying)
                .putLong(STANDBY_TIME_KEY, timeStandby).apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStart");

        if(intent.hasExtra(Constants.connected)){
            handler.postDelayed(runnable, 1000);
        } else if(intent.hasExtra(Constants.screenOn)){
            isScreenOn = intent.getBooleanExtra(Constants.screenOn, false);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private NotificationCompat.Builder getContent(NotificationCompat.Builder builder, long playingTime, long standbyTime, boolean addSeconds){
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
            content = "Playing: " + String.format("%02d:%02d:%02d", hours, minutes, seconds) +
                    "\nStandby: " + String.format("%02d:%02d:%02d", hours2, minutes2, seconds2);
            title = "Total: "  + String.format("%02d:%02d:%02d", hours3, minutes3, seconds3);

        } else {
            content = "Playing: " + String.format("%02d:%02d", hours, minutes) +
                    "\nStandby: " + String.format("%02d:%02d", hours2, minutes2);
            title =  "Total: "  + String.format("%02d:%02d", hours3, minutes3);
        }

        return builder.setStyle(new NotificationCompat.BigTextStyle().bigText(content)).setContentTitle(title);
    }
}
