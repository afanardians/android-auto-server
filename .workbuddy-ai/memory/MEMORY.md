# RUSH Project Memory

## Project Overview
Standalone native Android application (`com.afan.headunitserver`, named **RUSH**) to automate and control Android Auto Head Unit Server on Samsung Galaxy S26 Ultra (`SM-S948B`) and modern Android versions.

## Target Device & Environment
- **Device:** Samsung Galaxy S26 Ultra (Model: `SM-S948B`)
- **ADB Connection:** Wireless ADB on local LAN (`192.168.18.48`)
- **Head Unit Port:** TCP `5277` (Android Auto Head Unit Server)
- **Target Car Bluetooth:** `"Rush"` (stored in SharedPreferences `target_bt_name`)

## Build Toolchain
- **Location:** `~/.local/android-tools` (`aapt2`, `android.jar` API 34, `r8.jar`, `uber-apk-signer.jar`)
- **Build Script:** `./build.sh` produces release signed APK (`outputs/RUSH-v1.0.0.apk` & `RUSH.apk`)

## Key Components
- `MainActivity`: UI, real-time loopback TCP status polling, Bluetooth spinner, and widget pin launcher.
- `HeadUnitAccessibilityService`: Background UI automation driver interacting with Android Auto developer settings.
- `BluetoothReceiver`: BroadcastReceiver listening for Bluetooth connection events.
- `HeadUnitWidgetProvider`: 1-tap homescreen App Widget with dynamic status display.
