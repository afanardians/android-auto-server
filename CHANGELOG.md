# Changelog - Android Auto Server (Head Unit Server Controller)

All notable changes to this project will be documented in this file.

## [1.4.0] - 2026-10-26

### Changed
- **Penyelarasan Warna Label Status:**
  - Label `"🔴 SERVER MATI — Ketuk untuk Menyalakan"` diubah menjadi warna **MERAH** (`#DC2626`).
  - Label `"🟢 SERVER AKTIF — Ketuk untuk Mematikan"` diubah menjadi warna **HIJAU** (`#059669`).
- **Sinkronisasi Dua Arah Aplikasi ke Widget (Real-time App-to-Widget Sync):**
  - Menghubungkan langsung `MainActivity` ke `HeadUnitWidgetProvider` melalui method `updateAllWidgetsDirect()`.
  - Setiap kali aplikasi mendeteksi perubahan status (saat dibuka, polling berkala 2.5 detik, maupun setelah tombol power ditekan), status di homescreen widget secara otomatis ikut berubah secara instan (menyala saat aplikasi menyala, dan mati saat aplikasi mati).
  - Menambahkan callback update widget di `HeadUnitAccessibilityService` dan `BluetoothReceiver` saat otomasi berhasil dijalankan.
- **Kompilasi & Pemasangan:**
  - Build rilis baru `outputs/AndroidAutoServer-v1.4.0.apk` (`versionCode 7`, `versionName 1.4.0`).
  - Sukses dipasang langsung ke Samsung Galaxy S26 Ultra via wireless ADB.

## [1.3.0] - 2026-10-26

### Added
- **Switch Perilaku Tombol ON / OFF:**
  - Menukar status tampilan tombol aksi power:
    - Saat server **MATI (OFF)**: Tombol menampilkan warna **HIJAU** (`ic_power_on`) dengan label instruksi `"🟢 SERVER MATI — Ketuk untuk Menyalakan"`.
    - Saat server **AKTIF (ON)**: Tombol menampilkan warna **MERAH** (`ic_power_off`) dengan label instruksi `"🔴 SERVER AKTIF — Ketuk untuk Mematikan"`.
  - Menerapkan behavior tombol yang sama pada Homescreen Widget (`HeadUnitWidgetProvider`).
- **Tema Light Mode Aplikasi:**
  - Mendesain ulang seluruh antarmuka aplikasi utama (`activity_main.xml`) dengan tema **Light Mode** yang modern:
    - Background utama cerah (`#F8FAFC`).
    - Card container putih bersih (`#FFFFFF`, border `#E2E8F0`).
    - Tipografi dan kontras teks gelap berkelas (`#0F172A`, `#334155`, `#64748B`).
    - Tema sistem Android: `@android:style/Theme.DeviceDefault.Light.NoActionBar`.
    - Tombol aksi pasang widget dan log aktivitas disesuaikan dengan palet light theme.
- **Homescreen Widget Tetap Dark Mode:**
  - Sesuai permintaan pengguna, tema homescreen widget (`widget_headunit.xml`, `widget_bg.xml`) tetap dipertahankan dalam **Dark Mode** (`#1A1C20`, border `#363A42`, teks `#FFFFFF`).
- **Kompilasi & Deployment:**
  - Build rilis baru `outputs/AndroidAutoServer-v1.3.0.apk` (`versionCode 6`, `versionName 1.3.0`).
  - Berhasil dipasang langsung ke Samsung Galaxy S26 Ultra via wireless ADB.

## [1.2.0] - 2026-10-26

### Added
- **Icon Tombol Power Solid Red & Green Baru:**
  - Mengganti icon tombol power dengan desain solid circular button beraura cerah:
    - `ic_power_off.png`: Lingkaran solid merah menyala dengan simbol power putih bersih di tengah saat server OFF.
    - `ic_power_on.png`: Lingkaran solid hijau menyala dengan simbol power putih bersih di tengah saat server ON.
  - Penyesuaian `activity_main.xml` untuk header versi v1.2.0.
  - Kompilasi APK rilis `outputs/AndroidAutoServer-v1.2.0.apk` (`versionCode 5`, `versionName 1.2.0`).
  - Sukses dipasang dan diverifikasi langsung pada Samsung Galaxy S26 Ultra via wireless ADB.

## [1.1.0] - 2026-10-26

### Added
- **Rebranding ke "Android Auto Server":**
  - Mengubah nama aplikasi menjadi `Android Auto Server` di `strings.xml`, `AndroidManifest.xml`, header tampilan utama, dan homescreen widget.
- **Icon Baru Android Auto:**
  - Mengganti icon aplikasi dengan logo panah khas Android Auto multi-densitas (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`, dan drawable master 512x512).
- **Desain Tombol Power On/Off Baru:**
  - Mengganti tombol toggle lama dengan tombol power melingkar (tactile circular button):
    - `ic_power_off.png`: Tombol power gelap dengan simbol lingkaran merah menyala saat server OFF.
    - `ic_power_on.png`: Tombol power gelap dengan simbol lingkaran hijau neon menyala saat server ON.
  - Menambahkan indikator status teks dinamis di bawah tombol power (`tv_toggle_label`).
  - Mengintegrasikan icon tombol power yang sama ke homescreen widget (`ImageButton` di `widget_headunit.xml`).
- **Kompilasi & Pemasangan:**
  - Build rilis baru `outputs/AndroidAutoServer-v1.1.0.apk` (`versionCode 4`, `versionName 1.1.0`).
  - Sukses dipasang dan diverifikasi di Samsung Galaxy S26 Ultra via wireless ADB.

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
