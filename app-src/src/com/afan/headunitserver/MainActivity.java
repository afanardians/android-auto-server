package com.afan.headunitserver;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private TextView tvStatus;
    private TextView tvStatusDesc;
    private TextView tvTargetBtLabel;
    private TextView tvLog;
    private Button btnToggle;
    private Button btnPinWidget;
    private CheckBox cbAutoBt;
    private Spinner spBluetooth;
    private SharedPreferences prefs;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsServerRunning = false;
    private boolean mIsPollingActive = false;

    private final Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            queryServerStatus();
            if (mIsPollingActive) {
                mHandler.postDelayed(this, 2500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);

        tvStatus = findViewById(R.id.tv_status);
        tvStatusDesc = findViewById(R.id.tv_status_desc);
        tvTargetBtLabel = findViewById(R.id.tv_target_bt_label);
        tvLog = findViewById(R.id.tv_log);
        btnToggle = findViewById(R.id.btn_toggle);
        btnPinWidget = findViewById(R.id.btn_pin_widget);
        cbAutoBt = findViewById(R.id.cb_auto_bt);
        spBluetooth = findViewById(R.id.sp_bluetooth);

        setupBluetoothSection();
        setupToggleButton();
        setupWidgetPinning();
    }

    private void setupBluetoothSection() {
        cbAutoBt.setChecked(prefs.getBoolean("auto_bt", true));
        cbAutoBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("auto_bt", isChecked).apply();
                addLog("Otomasi Bluetooth: " + (isChecked ? "AKTIF" : "NONAKTIF"));
            }
        });

        String savedTarget = prefs.getString("target_bt", "Rush");
        tvTargetBtLabel.setText("Target Terpilih: " + savedTarget);

        List<String> deviceList = new ArrayList<>();
        try {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter != null) {
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                if (pairedDevices != null && !pairedDevices.isEmpty()) {
                    for (BluetoothDevice device : pairedDevices) {
                        String name = device.getName();
                        if (name != null && !name.trim().isEmpty()) {
                            deviceList.add(name);
                        }
                    }
                }
            }
        } catch (SecurityException e) {
            addLog("Peringatan: Izin Bluetooth diperlukan untuk membaca daftar perangkat.");
        }

        if (!deviceList.contains("Rush")) {
            deviceList.add(0, "Rush");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                deviceList
        );
        spBluetooth.setAdapter(adapter);

        // Pre-select current target
        int selectedIndex = 0;
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).equalsIgnoreCase(savedTarget)) {
                selectedIndex = i;
                break;
            }
        }
        spBluetooth.setSelection(selectedIndex);

        spBluetooth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String chosen = deviceList.get(position);
                prefs.edit().putString("target_bt", chosen).apply();
                tvTargetBtLabel.setText("Target Terpilih: " + chosen);
                addLog("Target Bluetooth disimpan: " + chosen);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupToggleButton() {
        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkAccessibility()) return;

                if (mIsServerRunning) {
                    addLog("⏹ Mengirim perintah STOP ke Head Unit Server...");
                    Toast.makeText(MainActivity.this, "Menghentikan Server...", Toast.LENGTH_SHORT).show();
                    HeadUnitAccessibilityService.triggerStop(MainActivity.this);
                } else {
                    addLog("▶ Mengirim perintah START ke Head Unit Server...");
                    Toast.makeText(MainActivity.this, "Memulai Server...", Toast.LENGTH_SHORT).show();
                    HeadUnitAccessibilityService.triggerStart(MainActivity.this);
                }

                // Query status after brief delay to verify execution
                mHandler.postDelayed(() -> queryServerStatus(), 1200);
            }
        });
    }

    private void setupWidgetPinning() {
        btnPinWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AppWidgetManager manager = getSystemService(AppWidgetManager.class);
                    if (manager != null && manager.isRequestPinAppWidgetSupported()) {
                        ComponentName provider = new ComponentName(MainActivity.this, HeadUnitWidgetProvider.class);
                        manager.requestPinAppWidget(provider, null, null);
                        Toast.makeText(MainActivity.this, "Silakan konfirmasi penambahan widget di layar", Toast.LENGTH_SHORT).show();
                        addLog("Permintaan pin widget ke layar depan dikirim.");
                    } else {
                        Toast.makeText(MainActivity.this, "Tekan lama layar depan -> pilih Widget -> RUSH", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Tekan lama layar depan untuk menambahkan widget", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPollingActive = true;
        mHandler.post(mStatusChecker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPollingActive = false;
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void queryServerStatus() {
        new Thread(() -> {
            boolean running = false;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("127.0.0.1", 5277), 150);
                running = true;
            } catch (Exception ignored) {}

            final boolean isRunning = running;
            mHandler.post(() -> updateServerUI(isRunning));
        }).start();
    }

    private void updateServerUI(boolean isRunning) {
        mIsServerRunning = isRunning;
        if (isRunning) {
            tvStatus.setText("🟢 SEDANG BERJALAN (AKTIF)");
            tvStatus.setTextColor(0xFF10B981);
            tvStatusDesc.setText("Port 5277 terbuka. Head unit mobil siap terhubung via WiFi.");
            btnToggle.setText("⏹ MATIKAN HEAD UNIT (AKTIF)");
            btnToggle.setBackgroundResource(R.drawable.btn_toggle_on_bg);
        } else {
            tvStatus.setText("⚪ TIDAK BERJALAN (MATI)");
            tvStatus.setTextColor(0xFF94A3B8);
            tvStatusDesc.setText("Server tidak aktif. Tekan tombol toggle di bawah untuk menyalakan.");
            btnToggle.setText("▶ NYALAKAN HEAD UNIT (OFF)");
            btnToggle.setBackgroundResource(R.drawable.btn_toggle_off_bg);
        }
    }

    private boolean checkAccessibility() {
        if (!HeadUnitAccessibilityService.isRunning()) {
            Toast.makeText(this, "Aktifkan 'RUSH' di pengaturan Aksesibilitas", Toast.LENGTH_LONG).show();
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (Exception ignored) {}
            return false;
        }
        return true;
    }

    private void addLog(String message) {
        String current = tvLog.getText().toString();
        tvLog.setText(message + "\n" + current);
    }
}
