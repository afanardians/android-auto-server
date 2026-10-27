# Android Auto Server Project Memory

## Project Overview
Standalone native Android application (`com.afan.headunitserver`, named **Android Auto Server**, v1.6.0) to automate and control Android Auto Head Unit Server on Samsung Galaxy S26 Ultra (`SM-S948B`) and modern Android versions.

## Target Device & Environment
- **Device:** Samsung Galaxy S26 Ultra (Model: `SM-S948B`)
- **ADB Connection:** Wireless ADB on local LAN (`192.168.18.48` / `192.168.0.107`)
- **Head Unit Port:** TCP `5277` (Android Auto Head Unit Server)
- **Target Car Bluetooth:** `"Rush"` (stored in SharedPreferences `target_bt`)
- **GitHub Repository:** `https://github.com/afanardians/android-auto-server` (`git@github.com:afanardians/android-auto-server.git`)

## Build Toolchain
- **Location:** `~/.local/android-tools` (`aapt2`, `android.jar` API 34, `r8.jar`, `uber-apk-signer.jar`)
- **Build Script:** `./build.sh` produces release signed APK (`outputs/AndroidAutoServer-v1.6.0.apk` & `AndroidAutoServer.apk`)

## Key Components & UI
- `MainActivity`: Light mode theme UI, circular power button, synchronized label colors (red when stopped, green when active), paired Bluetooth list with refresh button, and real-time app-to-widget synchronization (`HeadUnitWidgetProvider.updateAllWidgetsDirect`).
- `AccessibilityHelper`: Programmatic lifecycle controller for Accessibility Service via `WRITE_SECURE_SETTINGS` (auto-enable on start, auto-disable on stop).
- `VoiceActionActivity`: Translucent/headless voice action receiver responding to Google Assistant, Gemini, and Samsung Bixby voice commands ("Nyalakan Server Mobil" and "Matikan Server Mobil").
- `HeadUnitAccessibilityService`: Background UI automation driver interacting with Android Auto developer settings.
- `BluetoothReceiver`: BroadcastReceiver listening for Bluetooth connection events.
- `HeadUnitWidgetProvider`: 1-tap homescreen App Widget in Dark Mode, real-time synchronized with application state.

## Git & Workflow Conventions
- **Push Policy:** Push otomatis ke GitHub dinonaktifkan. Semua perubahan, commit, dan build APK hanya disimpan di lokal. `git push` HANYA dieksekusi jika pengguna meminta secara eksplisit.
