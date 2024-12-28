package com.matzy_byte.bt_car_android.bluetooth;

import android.os.Binder;

public class BluetoothBinder extends Binder {
    private final BluetoothService bluetoothService;
    public BluetoothBinder(BluetoothService service) {
        bluetoothService = service;
    }

    public BluetoothService getService() {
        return bluetoothService;
    }
}
