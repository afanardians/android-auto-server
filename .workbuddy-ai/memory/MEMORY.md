# Android Auto Server Project Memory

## Project Overview
Standalone native Android application (`com.afan.headunitserver`, named **Android Auto Server**, v1.2.0) to automate and control Android Auto Head Unit Server on Samsung Galaxy S26 Ultra (`SM-S948B`) and modern Android versions.

## Target Device & Environment
- **Device:** Samsung Galaxy S26 Ultra (Model: `SM-S948B`)
- **ADB Connection:** Wireless ADB on local LAN (`192.168.18.48` / `192.168.0.107`)
- **Head Unit Port:** TCP `5277` (Android Auto Head Unit Server)
- **Target Car Bluetooth:** `"Rush"` (stored in SharedPreferences `target_bt`)
- **GitHub Repository:** `https://github.com/afanardians/android-auto-server` (`git@github.com:afanardians/android-auto-server.git`)

## Build Toolchain
- **Location:** `~/.local/android-tools` (`aapt2`, `android.jar` API 34, `r8.jar`, `uber-apk-signer.jar`)
- **Build Script:** `./build.sh` produces release signed APK (`outputs/AndroidAutoServer-v1.2.0.apk` & `AndroidAutoServer.apk`)

## Key Components
- `MainActivity`: UI with circular tactile power button (red OFF / green ON), real-time loopback TCP status polling, Bluetooth spinner, and widget pin launcher.
- `HeadUnitAccessibilityService`: Background UI automation driver interacting with Android Auto developer settings.
- `BluetoothReceiver`: BroadcastReceiver listening for Bluetooth connection events.
- `HeadUnitWidgetProvider`: 1-tap homescreen App Widget with dynamic circular power icon.

## Git & Workflow Conventions
- **Push Policy:** Push otomatis ke GitHub dinonaktifkan. Semua perubahan, commit, dan build APK hanya disimpan di lokal. `git push` HANYA dieksekusi jika pengguna meminta secara eksplisit.
