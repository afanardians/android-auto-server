package com.afan.headunitserver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.net.Socket;

public class HeadUnitWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_START = "com.afan.headunitserver.ACTION_START";
    public static final String ACTION_STOP = "com.afan.headunitserver.ACTION_STOP";
    public static final String ACTION_TOGGLE = "com.afan.headunitserver.ACTION_TOGGLE";

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    public static boolean isServerRunning() {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("127.0.0.1", 5277), 150);
                return true;
            } catch (Exception e) {
                return false;
            }
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        new Thread(() -> {
            boolean running = isServerRunning();
            sHandler.post(() -> {
                for (int id : appWidgetIds) {
                    updateWidgetState(context, appWidgetManager, id, running);
                }
            });
        }).start();
    }

    public static void updateWidgetState(Context context, AppWidgetManager manager, int widgetId, boolean isRunning) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_headunit);

        // Header click -> Open MainActivity
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent mainPending = PendingIntent.getActivity(
                context, 100, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_header, mainPending);

        // Toggle button click
        Intent toggleIntent = new Intent(context, HeadUnitWidgetProvider.class);
        toggleIntent.setAction(ACTION_TOGGLE);
        PendingIntent togglePending = PendingIntent.getBroadcast(
                context, 103, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_btn_toggle, togglePending);

        if (isRunning) {
            views.setTextViewText(R.id.widget_status, "STATUS: AKTIF (5277)");
            views.setTextColor(R.id.widget_status, 0xFF10B981);
            views.setImageViewResource(R.id.widget_btn_toggle, R.drawable.ic_power_off);
        } else {
            views.setTextViewText(R.id.widget_status, "STATUS: MATI");
            views.setTextColor(R.id.widget_status, 0xFF94A3B8);
            views.setImageViewResource(R.id.widget_btn_toggle, R.drawable.ic_power_on);
        }

        manager.updateAppWidget(widgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (ACTION_TOGGLE.equals(action)) {
            boolean running = isServerRunning();
            if (running) {
                Toast.makeText(context, "⏹ Menghentikan Head Unit Server...", Toast.LENGTH_SHORT).show();
                HeadUnitAccessibilityService.triggerStop(context);
            } else {
                Toast.makeText(context, "🚗 Memulai Head Unit Server...", Toast.LENGTH_SHORT).show();
                HeadUnitAccessibilityService.triggerStart(context);
            }
            sHandler.postDelayed(() -> refreshAllWidgets(context), 1500);
        } else if (ACTION_START.equals(action)) {
            Toast.makeText(context, "🚗 Memulai Head Unit Server...", Toast.LENGTH_SHORT).show();
            HeadUnitAccessibilityService.triggerStart(context);
            sHandler.postDelayed(() -> refreshAllWidgets(context), 1500);
        } else if (ACTION_STOP.equals(action)) {
            Toast.makeText(context, "⏹ Menghentikan Head Unit Server...", Toast.LENGTH_SHORT).show();
            HeadUnitAccessibilityService.triggerStop(context);
            sHandler.postDelayed(() -> refreshAllWidgets(context), 1500);
        }
    }

    public static void refreshAllWidgets(Context context) {
        new Thread(() -> {
            boolean running = isServerRunning();
            sHandler.post(() -> {
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                int[] ids = manager.getAppWidgetIds(new ComponentName(context, HeadUnitWidgetProvider.class));
                for (int id : ids) {
                    updateWidgetState(context, manager, id, running);
                }
            });
        }).start();
    }
}
