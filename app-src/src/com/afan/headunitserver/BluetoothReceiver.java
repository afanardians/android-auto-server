package com.afan.headunitserver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class BluetoothReceiver extends BroadcastReceiver {

    private static final String TAG = "HeadUnitBT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) ||
            BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null) return;

            String deviceName = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && device.getAlias() != null) {
                    deviceName = device.getAlias();
                }
                if (deviceName == null || deviceName.trim().isEmpty()) {
                    deviceName = device.getName();
                }
            } catch (SecurityException e) {
                Log.w(TAG, "Bluetooth permission not granted", e);
            }

            if (deviceName == null || deviceName.trim().isEmpty()) {
                deviceName = device.getAddress();
            }

            Log.i(TAG, "Bluetooth event (" + action + "): " + deviceName);

            SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

            // Simpan nama perangkat ke histori bluetooth tersambung
            if (deviceName != null && !deviceName.trim().isEmpty()) {
                String cached = prefs.getString("cached_bt_devices", "");
                Set<String> set = new LinkedHashSet<>();
                if (!cached.isEmpty()) {
                    set.addAll(Arrays.asList(cached.split(";;")));
                }
                set.add(deviceName.trim());

                StringBuilder sb = new StringBuilder();
                for (String s : set) {
                    if (sb.length() > 0) sb.append(";;");
                    sb.append(s);
                }
                prefs.edit().putString("cached_bt_devices", sb.toString()).apply();
            }

            // Pemicu otomatis start jika cocok dengan target Bluetooth
            boolean autoEnabled = prefs.getBoolean("auto_bt", true);
            String targetBt = prefs.getString("target_bt", "Rush").toLowerCase();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) && autoEnabled &&
                deviceName != null && deviceName.toLowerCase().contains(targetBt)) {
                Log.i(TAG, "Target Bluetooth matched! Triggering Head Unit Server start...");
                HeadUnitAccessibilityService.triggerStart(context);
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                        () -> HeadUnitWidgetProvider.refreshAllWidgets(context), 2000
                );
            }
        }
    }
}
