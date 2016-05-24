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
    private static Runnable runnable;
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
                    Log.e(TAG, "66 Audio BTS");
                    int currentState2 = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0);
                    int previousState2 = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, 0);
                    if(currentState2 != previousState2) {
                        Log.e(TAG, "state changed");
                        if (currentState2 == BluetoothAdapter.STATE_CONNECTED) {
                            Log.e(TAG, "connected");
                            runnable = new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG, "saveTime ");

                                    SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                    long time = preferences.getLong(TIME_KEY, 0) + 1;
                                    preferences.edit().putLong(TIME_KEY, time).apply();
                                    if (MainActivity.context != null)
                                        MainActivity.context.postResult(time);
                                    handler.postDelayed(this, 1000);

                                }
                            };
                            handler.post(runnable);
                        } else if (currentState2 == BluetoothAdapter.STATE_DISCONNECTED) {
                            Log.e(TAG, "disconnected");
                            handler.removeCallbacks(runnable);
                        }
                    }
                    break;
            }
        }
    }
}
