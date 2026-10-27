package com.afan.headunitserver;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class VoiceActionActivity extends Activity {

    public static final String ACTION_VOICE_START = "com.afan.headunitserver.ACTION_VOICE_START";
    public static final String ACTION_VOICE_STOP = "com.afan.headunitserver.ACTION_VOICE_STOP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        handleVoiceCommand(intent);
        finish();
    }

    private void handleVoiceCommand(Intent intent) {
        boolean isStart = false;
        boolean isStop = false;

        if (intent != null) {
            String action = intent.getAction();
            Uri data = intent.getData();
            String query = intent.getStringExtra("query");
            if (query == null) {
                query = intent.getStringExtra(Intent.EXTRA_TEXT);
            }

            if (ACTION_VOICE_START.equals(action)) {
                isStart = true;
            } else if (ACTION_VOICE_STOP.equals(action)) {
                isStop = true;
            } else if (data != null) {
                String host = data.getHost();
                String path = data.getPath();
                if ("start".equalsIgnoreCase(host) || (path != null && path.contains("start"))) {
                    isStart = true;
                } else if ("stop".equalsIgnoreCase(host) || (path != null && path.contains("stop"))) {
                    isStop = true;
                }
            }

            if (!isStart && !isStop && query != null) {
                String lower = query.toLowerCase();
                if (lower.contains("nyala") || lower.contains("start") || lower.contains("hidup") || lower.contains("aktif")) {
                    isStart = true;
                } else if (lower.contains("mati") || lower.contains("stop") || lower.contains("padam")) {
                    isStop = true;
                }
            }
        }

        // Pastikan aksesibilitas siap atau dapat diaktifkan
        if (!HeadUnitAccessibilityService.isRunning() && !AccessibilityHelper.isServiceEnabledInSettings(this)) {
            if (!AccessibilityHelper.enableService(this)) {
                Toast.makeText(this, "Aktifkan 'Android Auto Server' di Aksesibilitas", Toast.LENGTH_LONG).show();
                AccessibilityHelper.openAccessibilitySettings(this);
                return;
            }
        }

        if (isStop) {
            Toast.makeText(this, "⏹ Mematikan Head Unit Server Mobil...", Toast.LENGTH_SHORT).show();
            HeadUnitAccessibilityService.triggerStop(this);
            notifyWidget();
        } else {
            // Default: Nyalakan server
            Toast.makeText(this, "🚗 Menyalakan Head Unit Server Mobil...", Toast.LENGTH_SHORT).show();
            HeadUnitAccessibilityService.triggerStart(this);
            notifyWidget();
        }
    }

    private void notifyWidget() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            HeadUnitWidgetProvider.refreshAllWidgets(getApplicationContext());
        }, 1500);
    }
}
