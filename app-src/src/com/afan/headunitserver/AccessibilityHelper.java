package com.afan.headunitserver;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import java.util.LinkedHashSet;
import java.util.Set;

public class AccessibilityHelper {

    private static final String TAG = "AccessibilityHelper";

    public static String getServiceComponent(Context context) {
        return context.getPackageName() + "/" + HeadUnitAccessibilityService.class.getName();
    }

    public static boolean isServiceEnabledInSettings(Context context) {
        try {
            ContentResolver cr = context.getContentResolver();
            String enabled = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (enabled == null) return false;
            String target = getServiceComponent(context);
            String shortTarget = context.getPackageName() + "/.HeadUnitAccessibilityService";
            return enabled.contains(target) || enabled.contains(shortTarget);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean enableService(Context context) {
        return updateServiceState(context, true);
    }

    public static boolean disableService(Context context) {
        return updateServiceState(context, false);
    }

    private static boolean updateServiceState(Context context, boolean enable) {
        try {
            ContentResolver cr = context.getContentResolver();
            String target = getServiceComponent(context);
            String shortTarget = context.getPackageName() + "/.HeadUnitAccessibilityService";

            String current = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (current == null) current = "";

            Set<String> services = new LinkedHashSet<>();
            for (String s : current.split(":")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    services.add(trimmed);
                }
            }

            if (enable) {
                services.add(target);
            } else {
                services.remove(target);
                services.remove(shortTarget);
            }

            StringBuilder sb = new StringBuilder();
            for (String s : services) {
                if (sb.length() > 0) sb.append(":");
                sb.append(s);
            }

            boolean success = Settings.Secure.putString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, sb.toString());
            if (enable && success) {
                Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, 1);
            }
            Log.i(TAG, "updateServiceState(" + enable + ") -> success: " + success);
            return success;
        } catch (SecurityException e) {
            Log.w(TAG, "SecurityException: WRITE_SECURE_SETTINGS not granted: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed updating accessibility settings", e);
            return false;
        }
    }

    public static void openAccessibilitySettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ignored) {}
    }
}
