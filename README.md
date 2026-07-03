# 🐉 DRAGONIC System

> **Advanced Mobile Security · Intruder Detection · Face Recognition**
> *by Pai Leonore — Leonore Tech Team*

---

## Overview

DRAGONIC System adalah aplikasi keamanan Android yang memantau siapa yang membuka HP kamu saat kamu tidak ada. Setiap kali layar dinyalakan, sistem otomatis mengambil foto menggunakan kamera depan, menganalisis wajah, dan mencatat siapa yang menyentuh HP kamu.

---

## Features

| Feature | Description |
|---|---|
| 🛡️ Guard Service | Foreground service aktif di background, terpicu saat layar menyala |
| 📸 Auto Capture | Foto otomatis via kamera depan tanpa suara, tanpa preview |
| 🧠 Face Recognition | ML Kit mendeteksi & membandingkan wajah dengan database owner |
| 🗂️ Capture Logs | Galeri semua hasil capture + timestamp + status (owner/intruder) |
| ⚠️ Intruder Alert | Notifikasi instan kalau wajah tidak dikenali |
| 🔁 Auto-start | Guard aktif kembali otomatis setelah HP restart |
| ⚙️ Sensitivity | Pilih level deteksi: LOW / MEDIUM / HIGH |

---

## Tech Stack

```
Language     : Kotlin
UI           : Jetpack Compose
DI           : Hilt
Database     : Room
ML           : Google ML Kit Face Detection
Camera       : CameraX + Camera2
Storage      : DataStore Preferences
Build        : GitHub Actions CI/CD
Min SDK      : 26 (Android 8.0)
Target SDK   : 35 (Android 15)
```

---

## Build via GitHub Actions

Push ke branch `main` → GitHub Actions otomatis build APK.

1. Fork / upload project ke repo baru
2. Tunggu workflow selesai di tab **Actions**
3. Download APK dari **Artifacts → DRAGONIC-System-Release**

Workflow file: `.github/workflows/build.yml`

---

## How It Works

```
Layar menyala
      │
      ▼
DragonicGuardService triggered
      │
      ▼
Front camera capture (silent)
      │
      ▼
ML Kit Face Detection
      │
      ├── No face → Log as "NO FACE"
      │
      └── Face detected
                │
                ├── Match owner → Log as "OWNER" (no alert)
                │
                └── Unknown → Log as "INTRUDER" + Push Notification ⚠️
```

---

## Face Enrollment

Buka app → **FACE ID** → tap **ENROLL FACE**

- Disarankan enroll **3–5 foto** dengan kondisi pencahayaan berbeda
- Makin banyak foto, makin akurat pengenalan wajah

---

## Project Structure

```
dragonic-system/
├── app/src/main/
│   ├── java/com/dragonic/system/
│   │   ├── MainActivity.kt
│   │   ├── DragonicApp.kt
│   │   ├── data/
│   │   │   ├── db/          — Room Database
│   │   │   ├── model/       — Entities & DAOs
│   │   │   └── repository/  — DragonicRepository
│   │   ├── ml/
│   │   │   └── FaceAnalyzer.kt
│   │   ├── service/
│   │   │   ├── DragonicGuardService.kt
│   │   │   └── BootReceiver.kt
│   │   └── ui/
│   │       ├── screens/     — Dashboard, Logs, FaceEnroll, Settings
│   │       ├── components/  — Shared Compose components
│   │       └── theme/       — Cyberpunk color palette
│   └── res/
│       └── drawable/        — Custom dragon eye logo (vector)
└── .github/workflows/
    └── build.yml
```

---

## Permissions Required

| Permission | Purpose |
|---|---|
| `CAMERA` | Capture intruder photos |
| `FOREGROUND_SERVICE` | Keep guard active in background |
| `FOREGROUND_SERVICE_CAMERA` | Camera access from background service |
| `RECEIVE_BOOT_COMPLETED` | Auto-start after reboot |
| `POST_NOTIFICATIONS` | Intruder alert notifications |

---

## Notes

- Sistem hanya aktif saat HP **standby/terkunci**, bukan saat HP benar-benar mati (power off)
- Face recognition berbasis pixel similarity — akurasi meningkat dengan lebih banyak foto enrolled
- Semua data tersimpan **lokal di HP**, tidak ada server/cloud

---

## Developer

```
Developer  : Pai Leonore
Studio     : Leonore Tech Team  
Brand      : Dragonic de Calonne Studio
Version    : 1.0.0
```

---

*DRAGONIC System — Your device, your fortress.*
