package com.example.kamil.connectiontimeraudio66bts;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class ConnectionReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectionReceiver";
    private static final String PREFS_NAME = "PrefsName";
    private static final String PLAYING_TIME_KEY = "playing";
    private static final String STANDBY_TIME_KEY = "standby";
    private static Runnable runnable;
    private static long timePlaying, timeStandby;
    private static int notificationId = 1;
    private static final android.os.Handler handler = new android.os.Handler();
    public ConnectionReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e(TAG, "onReceive");
        if(intent != null && intent.getAction() !=null){
            String action = intent.getAction();

            final SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final NotificationCompat.Builder builder= new NotificationCompat.Builder(context);
            Intent startActivityIntent = new Intent(context, MainActivity.class);
            PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context,
                    0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(startActivityPendingIntent).
                    setSmallIcon(R.mipmap.ic_launcher).setOngoing(true);

            switch (action){
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    Log.e(TAG, "Adapter connection state changed");
                    if(!((BluetoothDevice)(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))).getAddress().equals("00:1A:7D:E0:35:5F"))
                        break;
                    Log.e(TAG, "Audio 66 BTS");
                    int currentState2 = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                    int previousState2 = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1);

                    Log.e(TAG, "curr " + currentState2 + " pre " + previousState2);

                    if(currentState2 != previousState2) {
                        Log.e(TAG, "state changed");

                        if (currentState2 == BluetoothAdapter.STATE_CONNECTED) {
                            Log.e(TAG, "connected");
                            timePlaying = preferences.getLong(PLAYING_TIME_KEY, 0);
                            timeStandby = preferences.getLong(STANDBY_TIME_KEY, 0);
                            //builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getContent(timePlaying, timeStandby, false)));
                            //notificationManager.notify(notificationId, builder.build());
                            notificationManager.notify(notificationId, getContent(builder, timePlaying, timeStandby, false).build());
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
                                    if(time % 60 == 0) {
                                        Log.e(TAG, "timePlaying/60 " + "notify");
//                                        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getContent(timePlaying, timeStandby, false)));
//                                        notificationManager.notify(notificationId, builder.build());
                                        notificationManager.notify(notificationId, getContent(builder, timePlaying, timeStandby, false).build());
                                    }

                                    if(time % 30 == 0) {
                                        Log.e(TAG, "timePlaying/30 " + "save to file");
                                        preferences.edit().putLong(PLAYING_TIME_KEY, timePlaying)
                                                .putLong(STANDBY_TIME_KEY, timeStandby).apply();
                                    }

                                    if (MainActivity.context != null) {
                                        Log.e(TAG, "activity is available" + "post result");
                                        MainActivity.context.postResult(timePlaying, timeStandby);
                                    }
                                    handler.postDelayed(this, 1000);
                                }
                            };
                            handler.postDelayed(runnable, 1000);
                        } else if (currentState2 == BluetoothAdapter.STATE_DISCONNECTED &&
                                previousState2 == BluetoothAdapter.STATE_CONNECTED) {
                            Log.e(TAG, "disconnected " + timePlaying + " " + timeStandby);
                            preferences.edit().putLong(PLAYING_TIME_KEY, timePlaying)
                                    .putLong(STANDBY_TIME_KEY, timeStandby).apply();
                            builder.setOngoing(false);
//                            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getContent(timePlaying, timeStandby, true)));
//                            notificationManager.notify(notificationId, builder.build());
                            notificationManager.notify(notificationId, getContent(builder, timePlaying, timeStandby, true).build());
                            handler.removeCallbacks(runnable);
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF
                            && timePlaying > 0){
                        Log.e(TAG, "bluetooth off " + timePlaying + " " + timeStandby);
                        preferences.edit().putLong(PLAYING_TIME_KEY, timePlaying)
                                .putLong(STANDBY_TIME_KEY, timeStandby).apply();
                        builder.setOngoing(false);
//                        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getContent(timePlaying, timeStandby, true)));
//                        notificationManager.notify(notificationId, builder.build());
                        notificationManager.notify(notificationId, getContent(builder, timePlaying, timeStandby, true).build());
                        handler.removeCallbacks(runnable);
                    }
                    break;
            }
        }
    }

    private String getContent(long playingTime, long standbyTime, boolean addSeconds){
        long hours = TimeUnit.SECONDS.toHours(playingTime);
        long minutes = TimeUnit.SECONDS.toMinutes(playingTime) - hours*60;
        long hours2 = TimeUnit.SECONDS.toHours(standbyTime);
        long minutes2 = TimeUnit.SECONDS.toMinutes(standbyTime) - hours2*60;
        if(addSeconds){
            long seconds = playingTime - minutes*60;
            long seconds2 = standbyTime - minutes2*60;
            return ("Playing: " + String.format("%02d:%02d:%02d", hours, minutes, seconds) +
                    "\nStandby: " + String.format("%02d:%02d:%02d", hours2, minutes2, seconds2));
        } else
            return ("Playing: " + String.format("%02d:%02d", hours, minutes) +
                    "\nStandby: " + String.format("%02d:%02d", hours2, minutes2));
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
