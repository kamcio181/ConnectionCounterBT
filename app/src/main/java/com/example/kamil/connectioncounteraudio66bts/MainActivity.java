package com.example.kamil.connectioncounteraudio66bts;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String PREFS_NAME = "PrefsName";
    private static final String TIME_KEY = "TimeKey";
    private Button button, button2;
    private TextView textView;
    private SharedPreferences preferences;
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

    @Override
    protected void onStart() {
        super.onStart();
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis();
        long time = preferences.getLong(TIME_KEY, 0);
        int hours = (int)time/3600000;
        int minutes = (int) (time/600000-hours*60);
        textView.setText(hours + " : " + minutes);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                preferences.edit().remove(TIME_KEY).apply();
                break;
            case R.id.button2:
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                ArrayList<BluetoothDevice> list = new ArrayList<>();
                list.addAll(adapter.getBondedDevices());
                for(BluetoothDevice b : list){
                    Log.e("Bond", b.getAddress() + " name " + b.getName());
                }
                break;
        }
    }
}
