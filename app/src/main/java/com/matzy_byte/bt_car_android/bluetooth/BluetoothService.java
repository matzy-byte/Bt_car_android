package com.matzy_byte.bt_car_android.bluetooth;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.matzy_byte.bt_car_android.activities.ActivityCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothService extends Service {
    private String serviceUUID = "";
    private String powerCharacteristicsUUID = "";
    private ActivityCallback activityCallback;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;
    private final Handler handler = new Handler();
    private final Runnable scanTimeout = new Runnable() {
        @Override
        public void run() {
            if (checkMissingPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                Log.e("BLE", "cannot connect. no permission");
                return;
            }

            bluetoothLeScanner.stopScan(scanCallback);
            activityCallback.searchFailed("cannot connect. no permission");
            Log.d("BLE", "Scan stopped after 10 seconds.");
        }
    };
    protected final BluetoothBinder bluetoothBinder = new BluetoothBinder(this);

    private Boolean checkMissingPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED;
    }

    public void startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e("BLE", "bluetooth not enabled or not available");
            activityCallback.searchFailed("bluetooth not enabled or not available");
            return;
        }

        if (checkMissingPermission(Manifest.permission.BLUETOOTH_CONNECT) ||
                checkMissingPermission(Manifest.permission.BLUETOOTH_SCAN) ||
                checkMissingPermission(Manifest.permission.BLUETOOTH_SCAN)) {

            activityCallback.requestPermission();
            return;
        }

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (checkMissingPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    Log.e("BLE", "cannot find devices. no permission");
                    activityCallback.searchFailed("cannot find devices. no permission");
                    return;
                }

                Log.d("BLE", "Found device: " + result.getDevice().getName() + ", Address: " + result.getDevice().getAddress());
                if ("BluetoothCar".equals(result.getDevice().getName())) {
                    device = result.getDevice();
                    bluetoothLeScanner.stopScan(scanCallback);
                    handler.removeCallbacks(scanTimeout);
                    activityCallback.onFound();
                }
            }
            @Override
            public void onScanFailed(int errorCode) {
                Log.e("BLE", "Scan failed with error code: " + errorCode);
            }
        };

        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        List<ScanFilter> filters = new ArrayList<>();

        bluetoothLeScanner.startScan(filters, settings, scanCallback);
        handler.postDelayed(scanTimeout, 10000);
    }

    public void connectToDevice() {
        if (checkMissingPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.e("BLE", "cannot connect. no permission");
            return;
        }

        bluetoothGatt = device.connectGatt(this, false, new GattCallbackHandler(this, activityCallback));
    }

    public void disconnect() {
        if (checkMissingPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.e("BLE", "cannot disconnect. no permission");
            return;
        }

        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
        activityCallback.onDisconnected();
    }

    public void writeCharacteristic(byte[] value) {
        if (checkMissingPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.e("BLE", "no permission to connect");
            return;
        }

        BluetoothGattService carService = bluetoothGatt.getService(UUID.fromString(serviceUUID));
        if (carService != null) {
            BluetoothGattCharacteristic powerCharacteristic = carService.getCharacteristic(UUID.fromString(powerCharacteristicsUUID));
            if (powerCharacteristic != null) {
                powerCharacteristic.setValue(value);

                boolean success = bluetoothGatt.writeCharacteristic(powerCharacteristic);
                if (success) {
                    Log.d("BLE", "Successfully wrote to characteristic!");
                } else {
                    Log.e("BLE", "Failed to write to characteristic.");
                }
            } else {
                Log.e("ERROR", "NO CHARACTERISTIC");
            }
        } else {
            Log.e("ERROR", "NO SERVICE");
        }
    }

    public void setActivityCallback(ActivityCallback activityCallback) {
        this.activityCallback = activityCallback;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (checkMissingPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.e("BLE", "cannot disconnect. no permission");
        }

        device = null;
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
        bluetoothGatt = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return bluetoothBinder;
    }
}
