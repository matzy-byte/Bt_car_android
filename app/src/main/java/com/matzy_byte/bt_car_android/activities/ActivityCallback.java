package com.matzy_byte.bt_car_android.activities;

import android.app.Activity;

public interface ActivityCallback {
    void onFound();
    void onConnected();
    void onDisconnected();
    void searchFailed(String message);
    void requestPermission();
}
