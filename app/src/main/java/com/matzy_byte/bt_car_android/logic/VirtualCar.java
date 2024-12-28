package com.matzy_byte.bt_car_android.logic;

import android.os.Handler;

import com.matzy_byte.bt_car_android.bluetooth.BluetoothService;

public class VirtualCar {
    private final BluetoothService bluetoothService;
    private Boolean transmitting;
    private Boolean isPowered;
    private double power;
    private final double acceleration = 500;
    private final int deltaTime = 100;
    private final Runnable virtualCarUpdate = new Runnable() {
        @Override
        public void run() {
            if (isPowered) {
                power = Math.min(0.5 * acceleration * Math.pow((double) (deltaTime) / 1000, 2) + power, 100);
            } else {
                power = Math.max(power - 0.5 * acceleration * Math.pow((double) (deltaTime) / 1000, 2), 0);
            }

            if (transmitting) {
                bluetoothService.writeCharacteristic(new byte[]{(byte) power});
            } else {
                bluetoothService.writeCharacteristic(new byte[]{(byte) 0});
            }
            handler.postDelayed(this, deltaTime);
        }
    };
    private final Handler handler;

    public VirtualCar(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
        transmitting = false;
        isPowered = false;
        power = 0;
        handler = new Handler();
    }

    public void startVirtualCar() {
        transmitting = true;
        handler.post(virtualCarUpdate);
    }

    public void stopVirtualCar() {
        handler.removeCallbacks(virtualCarUpdate);
        power = 0;
        setTransmitting(false);
        bluetoothService.writeCharacteristic(new byte[]{(byte) 0});
    }

    public void setTransmitting(Boolean bool) {
        transmitting = bool;
    }

    public void pressGas() {
        isPowered = true;
    }

    public void brake() {
        isPowered = false;
    }
}
