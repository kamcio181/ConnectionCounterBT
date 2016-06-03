package com.example.kamil.audio66btstimer;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectionReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectionReceiver";
    public ConnectionReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e(TAG, "onReceive");
        if(intent != null && intent.getAction() !=null){
            String action = intent.getAction();

            Intent serviceIntent = new Intent(context, MyService.class);
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
                            context.startService(serviceIntent);

                        } else if (currentState2 == BluetoothAdapter.STATE_DISCONNECTED &&
                                previousState2 == BluetoothAdapter.STATE_CONNECTED) {
                            context.stopService(serviceIntent);
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF
                            && isServiceRunning(context)){
                        context.stopService(serviceIntent);
                    }
                    break;
            }
        }
    }

    private boolean isServiceRunning(Context context){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MyService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
