package com.afan.headunitserver;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private TextView tvStatus;
    private TextView tvStatusDesc;
    private TextView tvTargetBtLabel;
    private TextView tvLog;
    private ImageButton btnPowerToggle;
    private TextView tvToggleLabel;
    private Button btnPinWidget;
    private CheckBox cbAutoBt;
    private Spinner spBluetooth;
    private TextView btnRefreshBt;
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
        btnPowerToggle = findViewById(R.id.btn_power_toggle);
        tvToggleLabel = findViewById(R.id.tv_toggle_label);
        btnPinWidget = findViewById(R.id.btn_pin_widget);
        cbAutoBt = findViewById(R.id.cb_auto_bt);
        spBluetooth = findViewById(R.id.sp_bluetooth);
        btnRefreshBt = findViewById(R.id.btn_refresh_bt);

        setupBluetoothSection();
        setupToggleButton();
        setupWidgetPinning();
        setupDynamicShortcuts();
    }

    private void setupDynamicShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                android.content.pm.ShortcutManager sm = getSystemService(android.content.pm.ShortcutManager.class);
                if (sm != null) {
                    Intent startIntent = new Intent(VoiceActionActivity.ACTION_VOICE_START);
                    startIntent.setClass(this, VoiceActionActivity.class);

                    android.content.pm.ShortcutInfo startShortcut = new android.content.pm.ShortcutInfo.Builder(this, "start_server")
                            .setShortLabel("Nyalakan Server Mobil")
                            .setLongLabel("Nyalakan Server Mobil")
                            .setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_power_on))
                            .setIntent(startIntent)
                            .build();

                    Intent stopIntent = new Intent(VoiceActionActivity.ACTION_VOICE_STOP);
                    stopIntent.setClass(this, VoiceActionActivity.class);

                    android.content.pm.ShortcutInfo stopShortcut = new android.content.pm.ShortcutInfo.Builder(this, "stop_server")
                            .setShortLabel("Matikan Server Mobil")
                            .setLongLabel("Matikan Server Mobil")
                            .setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_power_off))
                            .setIntent(stopIntent)
                            .build();

                    sm.setDynamicShortcuts(Arrays.asList(startShortcut, stopShortcut));
                }
            } catch (Exception ignored) {}
        }
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

        if (btnRefreshBt != null) {
            btnRefreshBt.setOnClickListener(v -> {
                loadBondedBluetoothDevices();
                Toast.makeText(this, "Daftar Bluetooth diperbarui", Toast.LENGTH_SHORT).show();
            });
        }

        loadBondedBluetoothDevices();
    }

    private void loadBondedBluetoothDevices() {
        Set<String> deviceSet = new LinkedHashSet<>();

        // 1. Ambil dari riwayat cache SharedPreferences
        String cachedString = prefs.getString("cached_bt_devices", "");
        if (!cachedString.isEmpty()) {
            for (String dev : cachedString.split(";;")) {
                if (!dev.trim().isEmpty()) {
                    deviceSet.add(dev.trim());
                }
            }
        }

        // 2. Minta izin Bluetooth runtime jika belum diberikan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 101);
            }
        }

        // 3. Ambil semua perangkat yang pernah terhubung / dipasangkan (Bonded Devices)
        try {
            BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter btAdapter = (bm != null) ? bm.getAdapter() : BluetoothAdapter.getDefaultAdapter();
            if (btAdapter != null) {
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                if (pairedDevices != null && !pairedDevices.isEmpty()) {
                    for (BluetoothDevice device : pairedDevices) {
                        String name = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            try {
                                name = device.getAlias();
                            } catch (Exception ignored) {}
                        }
                        if (name == null || name.trim().isEmpty()) {
                            try {
                                name = device.getName();
                            } catch (Exception ignored) {}
                        }
                        if (name == null || name.trim().isEmpty()) {
                            name = device.getAddress();
                        }
                        if (name != null && !name.trim().isEmpty()) {
                            deviceSet.add(name.trim());
                        }
                    }
                }
            }
        } catch (SecurityException e) {
            addLog("Peringatan: Izin Bluetooth diperlukan untuk membaca perangkat.");
        } catch (Exception e) {
            addLog("Peringatan Bluetooth: " + e.getMessage());
        }

        // 4. Pastikan target default "Rush" selalu tersedia
        if (!deviceSet.contains("Rush")) {
            deviceSet.add("Rush");
        }

        // 5. Simpan daftar gabungan ke SharedPreferences agar selalu ada
        StringBuilder cacheSb = new StringBuilder();
        for (String dev : deviceSet) {
            if (cacheSb.length() > 0) cacheSb.append(";;");
            cacheSb.append(dev);
        }
        prefs.edit().putString("cached_bt_devices", cacheSb.toString()).apply();

        // 6. Update Spinner UI
        List<String> deviceList = new ArrayList<>(deviceSet);
        String savedTarget = prefs.getString("target_bt", "Rush");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                deviceList
        );
        spBluetooth.setAdapter(adapter);

        int selectedIndex = 0;
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).equalsIgnoreCase(savedTarget)) {
                selectedIndex = i;
                break;
            }
        }
        spBluetooth.setSelection(selectedIndex);
        tvTargetBtLabel.setText("Target Terpilih: " + savedTarget);

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadBondedBluetoothDevices();
            addLog("Izin Bluetooth diberikan. Daftar perangkat diperbarui.");
        }
    }

    private void setupToggleButton() {
        btnPowerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsServerRunning) {
                    addLog("⏹ Mengirim perintah STOP ke Head Unit Server...");
                    addLog("🔒 Layanan aksesibilitas akan dinonaktifkan otomatis.");
                    Toast.makeText(MainActivity.this, "Menghentikan Server...", Toast.LENGTH_SHORT).show();
                    HeadUnitAccessibilityService.triggerStop(MainActivity.this);
                } else {
                    if (!ensureAccessibilityReady()) return;
                    addLog("🔓 Mengaktifkan layanan aksesibilitas...");
                    addLog("▶ Mengirim perintah START ke Head Unit Server...");
                    Toast.makeText(MainActivity.this, "Memulai Server...", Toast.LENGTH_SHORT).show();
                    HeadUnitAccessibilityService.triggerStart(MainActivity.this);
                }

                // Query status after brief delay to verify execution & sync widget
                mHandler.postDelayed(() -> queryServerStatus(), 800);
                mHandler.postDelayed(() -> queryServerStatus(), 1600);
                mHandler.postDelayed(() -> queryServerStatus(), 2600);
            }
        });
    }

    private boolean ensureAccessibilityReady() {
        if (HeadUnitAccessibilityService.isRunning() || AccessibilityHelper.isServiceEnabledInSettings(this)) {
            return true;
        }
        if (AccessibilityHelper.enableService(this)) {
            addLog("🔓 Layanan aksesibilitas berhasil diaktifkan.");
            return true;
        }
        Toast.makeText(this, "Aktifkan 'Android Auto Server' di pengaturan Aksesibilitas", Toast.LENGTH_LONG).show();
        AccessibilityHelper.openAccessibilitySettings(this);
        return false;
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
                        Toast.makeText(MainActivity.this, "Tekan lama layar depan -> pilih Widget -> Android Auto Server", Toast.LENGTH_LONG).show();
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
        loadBondedBluetoothDevices();
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
            tvStatus.setTextColor(0xFF059669);
            tvStatusDesc.setText("Port 5277 terbuka. Head unit mobil siap terhubung via WiFi.");
            btnPowerToggle.setImageResource(R.drawable.ic_power_off);
            tvToggleLabel.setText("🟢 SERVER AKTIF — Ketuk untuk Mematikan");
            tvToggleLabel.setTextColor(0xFF059669);
        } else {
            tvStatus.setText("⚪ TIDAK BERJALAN (MATI)");
            tvStatus.setTextColor(0xFF64748B);
            tvStatusDesc.setText("Server tidak aktif. Ketuk tombol power di atas untuk menyalakan.");
            btnPowerToggle.setImageResource(R.drawable.ic_power_on);
            tvToggleLabel.setText("🔴 SERVER MATI — Ketuk untuk Menyalakan");
            tvToggleLabel.setTextColor(0xFFDC2626);
        }

        // Sinkronisasi status aplikasi ke homescreen widget secara langsung
        HeadUnitWidgetProvider.updateAllWidgetsDirect(this, isRunning);
    }

    private void addLog(String message) {
        String current = tvLog.getText().toString();
        tvLog.setText(message + "\n" + current);
    }
}
