package com.example.kamil.connectioncounteraudio66bts;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.logging.Handler;

public class ConnectionReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectionReceiver";
    private static final String PREFS_NAME = "PrefsName";
    private static final String TIME_KEY = "TimeKey";
    private long startMillis;
    private boolean stop = false;
    private android.os.Handler handler = new android.os.Handler();
    public ConnectionReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e(TAG, "onReceive");
        if(intent != null && intent.getAction() !=null){
            String action = intent.getAction();
            switch (action){
                case BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED:
                    Log.e(TAG, "audio state changed");
                    if(!((BluetoothDevice)(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))).getAddress().equals("00:1A:7D:E0:35:5F"))
                        break;
                    Log.e(TAG, "66 Audio BTS");
                    int currentState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0);
                    int previousState = intent.getIntExtra(BluetoothHeadset.EXTRA_PREVIOUS_STATE, 0);
                    if(currentState != previousState) {
                        Log.e(TAG, "state changed");
                        if (currentState == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                            Log.e(TAG, "connected");
                            stop = false;
                            Calendar calendar = Calendar.getInstance();
                            startMillis = calendar.getTimeInMillis();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG, "saveTime");
                                    if(!stop){
                                        Calendar calendar2 = Calendar.getInstance();
                                        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                        preferences.edit().putLong(TIME_KEY, preferences.getLong(TIME_KEY, 0) + startMillis - calendar2.getTimeInMillis()).apply();
                                        handler.postDelayed(this, 1000);
                                    } else
                                        stop = false;
                                }
                            };
                            handler.postDelayed(runnable,1000);
                        } else if (currentState == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                            Log.e(TAG, "disconnected");
                            stop = true;
                            Calendar calendar2 = Calendar.getInstance();
                            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            preferences.edit().putLong(TIME_KEY, preferences.getLong(TIME_KEY, 0) + startMillis - calendar2.getTimeInMillis()).apply();
                        }
                    }
                    break;
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    Log.e(TAG, "Adapter connection state changed");
                    if(!((BluetoothDevice)(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))).getAddress().equals("00:1A:7D:E0:35:5F"))
                        break;
                    Log.e(TAG, "66 Audio BTS");
                    int currentState2 = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0);
                    int previousState2 = intent.getIntExtra(BluetoothHeadset.EXTRA_PREVIOUS_STATE, 0);
                    if(currentState2 != previousState2) {
                        Log.e(TAG, "state changed");
                        if (currentState2 == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                            Log.e(TAG, "connected");
                            stop = false;
                            Calendar calendar = Calendar.getInstance();
                            startMillis = calendar.getTimeInMillis();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG, "saveTime");
                                    if(!stop){
                                        Calendar calendar2 = Calendar.getInstance();
                                        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                        preferences.edit().putLong(TIME_KEY, preferences.getLong(TIME_KEY, 0) + startMillis - calendar2.getTimeInMillis()).apply();
                                        handler.postDelayed(this, 1000);
                                    } else
                                        stop = false;
                                }
                            };
                            handler.postDelayed(runnable,1000);
                        } else if (currentState2 == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                            Log.e(TAG, "disconnected");
                            stop = true;
                            Calendar calendar2 = Calendar.getInstance();
                            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            preferences.edit().putLong(TIME_KEY, preferences.getLong(TIME_KEY, 0) + startMillis - calendar2.getTimeInMillis()).apply();
                        }
                    }
                    break;
            }
        }
    }
}
