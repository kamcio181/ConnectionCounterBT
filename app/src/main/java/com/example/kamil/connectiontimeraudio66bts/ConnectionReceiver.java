package com.example.kamil.connectiontimeraudio66bts;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class ConnectionReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectionReceiver";
    private static final String PREFS_NAME = "PrefsName";
    private static final String TIME_KEY = "TimeKey";
    private static Runnable runnable;
    private static long time;
    private static int notificationId = 1;
    private static final android.os.Handler handler = new android.os.Handler();
    public ConnectionReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e(TAG, "onReceive");
        if(intent != null && intent.getAction() !=null){
            String action = intent.getAction();
            switch (action){
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    Log.e(TAG, "Adapter connection state changed");
                    if(!((BluetoothDevice)(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))).getAddress().equals("00:1A:7D:E0:35:5F"))
                        break;
                    Log.e(TAG, "Audio 66 BTS");
                    int currentState2 = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0);
                    int previousState2 = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, 0);
                    if(currentState2 != previousState2) {
                        Log.e(TAG, "state changed");
                        final SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        final NotificationCompat.Builder builder= new NotificationCompat.Builder(context);
                        Intent startActivityIntent = new Intent(context, MainActivity.class);
                        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context,
                                0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.setContentTitle("Audio 66 BTS timer").setContentIntent(startActivityPendingIntent).
                                setSmallIcon(R.mipmap.ic_launcher).setOngoing(true);

                        if (currentState2 == BluetoothAdapter.STATE_CONNECTED) {
                            Log.e(TAG, "connected");
                            time = preferences.getLong(TIME_KEY, 0);
                            runnable = new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG, "saveTime " + time);
                                    time++;
                                    if((time/1000) % 60 == 0) {
                                        Log.e(TAG, "time/60 " + "notify");
                                        builder.setContentText(getContent(time));
                                        manager.notify(notificationId, builder.build());
                                    }

                                    if((time/1000) % 30 == 0) {
                                        Log.e(TAG, "time/30 " + "save to file");
                                        preferences.edit().putLong(TIME_KEY, time).apply();
                                    }

                                    if (MainActivity.context != null) {
                                        Log.e(TAG, "activity is available" + "post result");
                                        MainActivity.context.postResult(time);
                                    }
                                    handler.postDelayed(this, 1000);
                                }
                            };
                            handler.postDelayed(runnable, 1000);
                        } else if (currentState2 == BluetoothAdapter.STATE_DISCONNECTED) {
                            Log.e(TAG, "disconnected " + time);
                            preferences.edit().putLong(TIME_KEY, time).apply();
                            builder.setOngoing(false);
                            builder.setContentText(getContent(time));
                            manager.notify(notificationId, builder.build());
                            handler.removeCallbacks(runnable);
                        }
                    }
                    break;
            }
        }
    }

    private String getContent(long time){
        long hours = TimeUnit.SECONDS.toHours(time);
        long minutes = TimeUnit.SECONDS.toMinutes(time) - hours*60;
        return (String.format("%02d:%02d", hours, minutes));
    }
}
