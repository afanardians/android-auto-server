package com.afan.headunitserver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BluetoothReceiver extends BroadcastReceiver {

    private static final String TAG = "HeadUnitBT";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null) return;

            String deviceName = "";
            try {
                deviceName = device.getName();
            } catch (SecurityException e) {
                Log.w(TAG, "Bluetooth permission not granted", e);
            }

            Log.i(TAG, "Bluetooth connected: " + deviceName);

            SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
            boolean autoEnabled = prefs.getBoolean("auto_bt", true);
            String targetBt = prefs.getString("target_bt", "rush").toLowerCase();

            if (autoEnabled && deviceName != null && deviceName.toLowerCase().contains(targetBt)) {
                Log.i(TAG, "Target Bluetooth matched! Triggering Head Unit Server start...");
                HeadUnitAccessibilityService.triggerStart(context);
            }
        }
    }
}
