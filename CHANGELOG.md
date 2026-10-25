# Changelog - RUSH (Head Unit Server Controller)

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-10-26

### Added
- **Rebranding ke "RUSH":** Nama aplikasi dan identitas visual diperbarui menjadi `RUSH`.
- **Custom Launcher Icon:** Icon aplikasi multi-densitas (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) dibuat dari aset visual resmi RUSH.
- **Master Toggle Button:** Menggantikan tombol terpisah START dan STOP dengan satu tombol Toggle interaktif di halaman utama:
  - Hijau emerald (`btn_toggle_on_bg.xml`): Saat server berjalan.
  - Abu-abu slate (`btn_toggle_off_bg.xml`): Saat server mati.
- **Deteksi Real-Time Status:**
  - Pengecekan aktif port loopback `127.0.0.1:5277` dengan connection timeout 150ms.
  - Polling berkala (2.5 detik) di `MainActivity` saat aplikasi aktif di foreground.
  - Penyesuaian `StrictMode.ThreadPolicy` (`permitAll()`) di `HeadUnitWidgetProvider` untuk mencegah `NetworkOnMainThreadException` pada synchronous widget update.
- **Homescreen App Widget:**
  - `HeadUnitWidgetProvider` (`@layout/widget_headunit`, `@xml/headunit_widget_info`).
  - Menampilkan branding RUSH, status port 5277, dan tombol aksi cepat 1-tap TOGGLE langsung dari home screen launcher.
  - Fitur programatik "Pasang Widget ke Layar Depan" via `AppWidgetManager.requestPinAppWidget()`.
- **Bluetooth Device Picker (`Spinner`):**
  - Mengambil daftar perangkat Bluetooth yang telah ter-pairing (`BluetoothAdapter.getBondedDevices()`).
  - Menyimpan pilihan target perangkat secara persisten menggunakan `SharedPreferences`.
  - Default otomatis mendeteksi perangkat bernama `"Rush"`.
- **Automated Accessibility Service (`HeadUnitAccessibilityService`):**
  - Otomasi navigasi internal developer settings Android Auto via traversal `AccessibilityNodeInfo`.
  - Otomatis menekan menu overflow 3-titik dan memilih opsi `"Start head unit server"` / `"Stop head unit server"`.
  - Dilengkapi mekanisme `scheduleChecks()` dengan delay bertingkat (350ms, 750ms, 1200ms) untuk keandalan eksekusi.
- **Automated Build Toolchain Script (`build.sh`):**
  - Pipeline build mandiri menggunakan `aapt2`, `javac`, `d8` (via `r8.jar`), dan `uber-apk-signer.jar`.
  - Menghasilkan rilis APK yang telah di-zipalign dan ditandatangani secara kriptografis dengan skema v1, v2, dan v3.

### Fixed
- Error `INSTALL_FAILED_VERSION_DOWNGRADE` diatasi dengan menaikkan `android:versionCode` ke `3`.
- Android 13+ restricted settings diatasi via `appops set com.afan.headunitserver ACCESS_RESTRICTED_SETTINGS allow`.
- Event accessibility yang tidak terpanggil saat halaman settings Android Auto telah aktif di background diatasi dengan retry polling berbasis node root.
