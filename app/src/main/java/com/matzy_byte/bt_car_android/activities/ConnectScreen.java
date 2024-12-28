package com.matzy_byte.bt_car_android.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.matzy_byte.bt_car_android.R;
import com.matzy_byte.bt_car_android.bluetooth.BluetoothBinder;
import com.matzy_byte.bt_car_android.bluetooth.BluetoothService;

public class ConnectScreen extends AppCompatActivity implements ActivityCallback {
    private Boolean first = false;
    private Intent intent;
    private BluetoothService bluetoothService;
    private Boolean bound = false;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothBinder binder = (BluetoothBinder) service;
            bluetoothService = binder.getService();
            bluetoothService.setActivityCallback(ConnectScreen.this);
            bluetoothService.startScanning();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
            bound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_layout);

        Button connect = findViewById(R.id.btn_search);
        connect.setOnClickListener(v -> {
            connect.setText("Searching ...");
            connect.setClickable(false);
            if (!first) {
                requestPermission();
                first = true;
            }
            if (!bound) {
                intent = new Intent(this, BluetoothService.class);
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            } else {
                bluetoothService.startScanning();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothService != null) {
            bluetoothService.setActivityCallback(this);
        }
    }

    @Override
    public void onFound() {
        searchFailed("");
        Intent intent = new Intent(this, PlayScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onConnected() {
        return;
    }
    @Override
    public void onDisconnected() {
        return;
    }

    @Override
    public void requestPermission() {
        searchFailed("permissions required");
        ActivityCompat.requestPermissions(this,
            new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },
            100);
    }

    @Override
    public void searchFailed(String message) {
        TextView text = findViewById(R.id.txt_search_error);
        text.setText(message);
        Button connect = findViewById(R.id.btn_search);
        connect.setText("Search");
        connect.setClickable(true);
    }
}
