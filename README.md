# 📱 Dual Mode Call Manager

Dual Mode Call Manager is a native Android application designed to transform a single smartphone into a software-isolated **Work Phone** and **Personal Phone** through automated, policy-driven call screening.

---

## 🎯 Overview

Dual Mode Call Manager allows users to toggle between two operational profiles at any time:

* 🏢 **Work Mode**: Permits incoming calls from contacts categorized under **Work** or **Both**. Suppresses or silently rejects calls from Personal contacts and unapproved callers.
* 🏠 **Personal Mode**: Permits incoming calls from contacts categorized under **Personal** or **Both**. Suppresses or silently rejects calls from Work contacts and unapproved callers.

---

## ⚙️ Key Technical Features

* **Real-time Call Interception**: Integrates directly with Android's Telecom Framework (`CallScreeningService`) and `RoleManager` to intercept carrier calls prior to device ringing.
* **Notification Shade Quick Settings Tile**: Includes a native status bar tile (`TileService`) for instant profile toggling without opening the app UI.
* **Local Storage & Privacy**: Powered by **Room Database** and **PreferencesManager** with reactive `StateFlow` streams. All contact classifications and call logs remain strictly on-device.
* **Modern Compose Material 3 UI**: Clean, responsive interface featuring dynamic mode switching, contact categorization, call log analytics, and search functionality.

---

## 🏗️ Architecture & Tech Stack

* **Language**: Kotlin 2.0+
* **UI**: Jetpack Compose, Material 3, Navigation Compose
* **Architecture**: MVVM, Clean Architecture, Kotlin Coroutines & `StateFlow`
* **Database**: Room 2.7+ with KSP (Kotlin Symbol Processing) 2.0
* **Android Framework**: `CallScreeningService`, `RoleManager`, `TileService`

---

## 🚀 Getting Started & Building

1. Clone the repository:
   ```bash
   git clone https://github.com/Sidharth-786/dual-mode-call-manager.git
   ```
2. Open the project in **Android Studio (Ladybug or later)**.
3. Build & Run on an Android device or emulator running **Android 8.0+ (API 26+)**.
4. Upon launching the app, grant the **Default Call Screening App** permission when prompted.

---

## 📄 License

This project is licensed under the MIT License.
