package com.matzy_byte.bt_car_android.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.matzy_byte.bt_car_android.R;
import com.matzy_byte.bt_car_android.bluetooth.BluetoothBinder;
import com.matzy_byte.bt_car_android.bluetooth.BluetoothService;
import com.matzy_byte.bt_car_android.logic.VirtualCar;

public class PlayScreen extends AppCompatActivity implements ActivityCallback {
    private Intent intent;
    private BluetoothService bluetoothService;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothBinder binder = (BluetoothBinder) service;
            bluetoothService = binder.getService();
            bluetoothService.setActivityCallback(PlayScreen.this);
            virtualCar = new VirtualCar(bluetoothService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
        }
    };
    private Boolean connection = false;
    private VirtualCar virtualCar;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_layout);

        intent = new Intent(this, BluetoothService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        Button connectionButton = findViewById(R.id.btn_connect);
        connectionButton.setOnClickListener(v -> {
            if (connection) {
                virtualCar.stopVirtualCar();
                bluetoothService.disconnect();
                connectionButton.setText("Connect");
            } else {
                virtualCar.startVirtualCar();
                bluetoothService.connectToDevice();
                connectionButton.setText("Disconnect");
            }
        });

        Button gasButton = findViewById(R.id.btn_gas);
        gasButton.setOnTouchListener((View v, MotionEvent event) -> {
            if (connection) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        virtualCar.pressGas();
                        return true;
                    case MotionEvent.ACTION_UP:
                        virtualCar.brake();
                        return true;
                    default:
                        return false;
                }
            }
            return false;
        });

        TextView statusText = findViewById(R.id.txt_status);
        statusText.setText("Not_connected");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothService != null) {
            bluetoothService.setActivityCallback(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bluetoothService != null) {
            bluetoothService.disconnect();
        }
    }

    @Override
    public void onFound() {
        return;
    }

    @Override
    public void onConnected() {
        connection = true;
        virtualCar.setTransmitting(true);
        TextView statusText = findViewById(R.id.txt_status);
        statusText.setText("Connected");
    }

    @Override
    public void onDisconnected() {
        connection = false;
        TextView statusText = findViewById(R.id.txt_status);
        statusText.setText("Not_connected");
    }

    @Override
    public void requestPermission() {
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
        return;
    }
}
