package com.afan.headunitserver;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class HeadUnitAccessibilityService extends AccessibilityService {

    private static final String TAG = "HeadUnitA11y";

    public static final int ACTION_NONE = 0;
    public static final int ACTION_START = 1;
    public static final int ACTION_STOP = 2;

    private static int sPendingAction = ACTION_NONE;
    private static HeadUnitAccessibilityService sInstance = null;
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        sInstance = this;
        Log.i(TAG, "HeadUnitAccessibilityService connected");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sInstance = null;
        Log.i(TAG, "HeadUnitAccessibilityService unbound");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstance = null;
        Log.i(TAG, "HeadUnitAccessibilityService destroyed");
    }

    public static boolean isRunning() {
        return sInstance != null;
    }

    public static HeadUnitAccessibilityService getInstance() {
        return sInstance;
    }

    public static void triggerStart(Context context) {
        sPendingAction = ACTION_START;
        Log.i(TAG, "triggerStart called: enabling accessibility service in settings...");
        AccessibilityHelper.enableService(context);

        long delay = isRunning() ? 100 : 400;
        sHandler.postDelayed(() -> {
            launchAndroidAutoSettings(context);
            scheduleChecks();
        }, delay);
    }

    public static void triggerStop(Context context) {
        sPendingAction = ACTION_STOP;
        Log.i(TAG, "triggerStop called: ensuring service enabled for clicking stop...");
        if (!isRunning()) {
            AccessibilityHelper.enableService(context);
        }

        long delay = isRunning() ? 100 : 400;
        sHandler.postDelayed(() -> {
            launchAndroidAutoSettings(context);
            scheduleChecks();
        }, delay);

        // Fallback: nonaktifkan layanan setelah 3.5 detik jika stop action belum selesai
        sHandler.postDelayed(() -> {
            if (sPendingAction == ACTION_STOP) {
                sPendingAction = ACTION_NONE;
                Log.i(TAG, "Fallback timer: deactivating accessibility service after stop command...");
                disableSelfAndSetting(context.getApplicationContext());
            }
        }, 3500);
    }

    public static void disableSelfAndSetting(Context context) {
        sHandler.post(() -> {
            AccessibilityHelper.disableService(context);
            if (sInstance != null) {
                try {
                    sInstance.disableSelf();
                    Log.i(TAG, "disableSelf() executed on HeadUnitAccessibilityService");
                } catch (Exception e) {
                    Log.w(TAG, "Error executing disableSelf(): " + e.getMessage());
                }
            }
        });
    }

    private static void scheduleChecks() {
        for (int delay : new int[]{350, 750, 1200, 1800}) {
            sHandler.postDelayed(() -> {
                if (sInstance != null && sPendingAction != ACTION_NONE) {
                    AccessibilityNodeInfo root = sInstance.getRootInActiveWindow();
                    if (root != null) {
                        if (!sInstance.handleMenuItem(root)) {
                            sInstance.clickOverflowMenu(root);
                        }
                    }
                }
            }, delay);
        }
    }

    private static void launchAndroidAutoSettings(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.google.android.projection.gearhead",
                    "com.google.android.projection.gearhead.companion.settings.DefaultSettingsActivity"
            ));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error launching Android Auto settings", e);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (sPendingAction == ACTION_NONE) {
            return;
        }

        CharSequence pkg = event.getPackageName();
        if (pkg == null || !pkg.toString().contains("gearhead")) {
            return;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }

        // 1. Check if the target menu item is visible
        boolean handled = handleMenuItem(root);

        // 2. If not found, try to open the 3-dots overflow menu
        if (!handled) {
            clickOverflowMenu(root);
        }
    }

    private boolean handleMenuItem(AccessibilityNodeInfo root) {
        String[] startKeywords = new String[]{"start head unit server", "mulai server unit utama", "start headunit server"};
        String[] stopKeywords = new String[]{"stop head unit server", "hentikan server unit utama", "stop headunit server"};

        String[] targetKeywords = (sPendingAction == ACTION_START) ? startKeywords : stopKeywords;
        boolean wasStop = (sPendingAction == ACTION_STOP);

        for (String kw : targetKeywords) {
            AccessibilityNodeInfo item = findNodeWithText(root, kw);
            if (item != null) {
                Log.i(TAG, "Found target item: " + kw + ", clicking...");
                performClick(item);
                sPendingAction = ACTION_NONE;

                // Return to home / previous screen after short delay
                sHandler.postDelayed(() -> performGlobalAction(GLOBAL_ACTION_HOME), 300);
                sHandler.postDelayed(() -> HeadUnitWidgetProvider.refreshAllWidgets(getApplicationContext()), 800);
                sHandler.postDelayed(() -> HeadUnitWidgetProvider.refreshAllWidgets(getApplicationContext()), 1800);

                if (wasStop) {
                    // Nonaktifkan android auto server di pengaturan aksesibilitas setelah perintah matikan server
                    sHandler.postDelayed(() -> {
                        disableSelfAndSetting(getApplicationContext());
                        Log.i(TAG, "Accessibility service disabled after stop command.");
                    }, 800);
                }

                return true;
            }
        }
        return false;
    }

    private void clickOverflowMenu(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo overflow = findOverflowButton(root);
        if (overflow != null) {
            Log.i(TAG, "Found overflow button, clicking...");
            performClick(overflow);
        }
    }

    private AccessibilityNodeInfo findOverflowButton(AccessibilityNodeInfo node) {
        if (node == null) return null;

        CharSequence desc = node.getContentDescription();
        if (desc != null) {
            String d = desc.toString().toLowerCase();
            if (d.contains("more options") || d.contains("opsi lainnya") || d.contains("more") || d.contains("lainnya")) {
                return node;
            }
        }

        CharSequence resId = node.getViewIdResourceName();
        if (resId != null) {
            String id = resId.toString().toLowerCase();
            if (id.contains("overflow") || id.contains("more") || id.contains("action_menu")) {
                return node;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo found = findOverflowButton(node.getChild(i));
            if (found != null) return found;
        }

        return null;
    }

    private AccessibilityNodeInfo findNodeWithText(AccessibilityNodeInfo node, String keyword) {
        if (node == null) return null;

        CharSequence text = node.getText();
        if (text != null && text.toString().toLowerCase().contains(keyword.toLowerCase())) {
            return node;
        }

        CharSequence desc = node.getContentDescription();
        if (desc != null && desc.toString().toLowerCase().contains(keyword.toLowerCase())) {
            return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo found = findNodeWithText(node.getChild(i), keyword);
            if (found != null) return found;
        }

        return null;
    }

    private void performClick(AccessibilityNodeInfo node) {
        if (node == null) return;
        if (node.isClickable()) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            AccessibilityNodeInfo parent = node.getParent();
            if (parent != null) {
                performClick(parent);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Service interrupted");
    }
}
