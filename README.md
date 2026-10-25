# RUSH - Android Auto Head Unit Server Controller

Aplikasi Android mandiri (*standalone, lightweight, zero third-party dependencies*) untuk mengontrol dan mengotomasi **Android Auto Head Unit Server** di Samsung Galaxy S26 Ultra (`SM-S948B`) dan perangkat Android modern lainnya.

---

## Fitur Utama

- **Master Toggle Button:** Kontrol satu ketukan untuk Start / Stop Head Unit Server dengan indikator warna dinamis.
- **Deteksi Real-Time Status:** Memantau status server secara aktif melalui pengecekan TCP loopback port `5277`.
- **Otomasi Bluetooth:** Otomatis menyalakan server saat HP terhubung ke Bluetooth mobil (default: `"Rush"`), dengan dropdown pemilih perangkat paired.
- **Homescreen Widget:** Widget ringkas di layar depan untuk kontrol 1-tap tanpa perlu membuka aplikasi.
- **Otomasi Tanpa Root:** Menggunakan Android `AccessibilityService` untuk membuka developer settings Android Auto di latar belakang, menekan overflow menu, dan memilih aksi.

---

## Arsitektur & Struktur Proyek

```text
├── AndroidManifest.xml       # Konfigurasi package, permission, services, & receivers
├── res/
│   ├── layout/               # UI activity_main.xml & widget_headunit.xml
│   ├── drawable/             # AMOLED dark theme cards & stateful toggle drawables
│   ├── mipmap-*/             # Multi-density launcher icons (mdpi - xxxhdpi)
│   ├── values/strings.xml    # String resources
│   └── xml/                  # Metadata accessibility service & app widget
├── src/com/afan/headunitserver/
│   ├── MainActivity.java     # UI controller, status poller, Bluetooth spinner & widget pinner
│   ├── HeadUnitAccessibilityService.java # Engine otomasi UI Android Auto developer settings
│   ├── BluetoothReceiver.java            # Broadcast receiver deteksi koneksi Bluetooth
│   └── HeadUnitWidgetProvider.java       # Provider & lifecycle handler homescreen widget
├── build.sh                  # Skrip otomasi kompilasi, dexing (D8), & signing
├── outputs/                  # Direktori rilis APK siap instal
└── CHANGELOG.md              # Riwayat perubahan versi
```

---

## Cara Kompilasi (Build)

Jalankan skrip build di terminal:

```bash
./build.sh
```

Skrip akan mengeksekusi tahapan:
1. `aapt2 compile` dan `aapt2 link` untuk resource dan manifest.
2. `javac` terhadap API level 34 (`android.jar`).
3. Konversi bytecode menggunakan D8 (`r8.jar`).
4. Packaging APK dan penandatanganan kriptografis v1/v2/v3 menggunakan `uber-apk-signer`.
5. Output APK tersimpan di `outputs/RUSH-v1.0.0.apk` dan `RUSH.apk`.

---

## Setup & Izin Android

Aplikasi memerlukan izin Accessibility Service agar dapat mengotomasi menu Android Auto:

```bash
# Buka pembatasan restricted settings (Android 13+)
adb shell appops set com.afan.headunitserver ACCESS_RESTRICTED_SETTINGS allow

# Aktifkan Accessibility Service
adb shell settings put secure enabled_accessibility_services com.afan.headunitserver/com.afan.headunitserver.HeadUnitAccessibilityService

# Matikan optimasi baterai (Doze mode)
adb shell dumpsys deviceidle whitelist +com.afan.headunitserver
```
