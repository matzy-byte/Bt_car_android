package com.matzy_byte.bt_car_android.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.matzy_byte.bt_car_android.activities.ActivityCallback;

public class GattCallbackHandler extends BluetoothGattCallback {
    private final BluetoothService bluetoothService;
    private final ActivityCallback activityCallback;

    private Boolean checkMissingPermission(String permission) {
        return ContextCompat.checkSelfPermission(bluetoothService, permission) != PackageManager.PERMISSION_GRANTED;
    }

    public GattCallbackHandler(BluetoothService service, ActivityCallback activityCallback) {
        bluetoothService = service;
        this.activityCallback = activityCallback;
    }
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (checkMissingPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.e("BLE", "no permission to connect");
            return;
        }

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
            activityCallback.onConnected();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            gatt.close();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (checkMissingPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Toast.makeText(bluetoothService, "no permission to connect", Toast.LENGTH_SHORT).show();
            return;
        }

        if (status == BluetoothGatt.GATT_SUCCESS) {
            for (BluetoothGattService service : gatt.getServices()) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getProperties() > 0 & BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                        gatt.readCharacteristic(characteristic);
                    }
                }
            }
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            byte[] data = characteristic.getValue();
            // Process the data
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            // Write successful
        }
    }
}
