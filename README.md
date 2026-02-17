# 🎵 MySound - Modern Music Streaming App

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=google-play&logoColor=white)
![Media3](https://img.shields.io/badge/Media3-ExoPlayer-red?style=for-the-badge)

**MySound** is a fully functional, native Android music streaming application built with **Kotlin** and **Jetpack Compose**. It implements modern Android architecture standards to provide a seamless audio experience, mimicking features found in industry-leading apps like Spotify.

This project demonstrates advanced concepts such as foreground services for media playback, JWT authentication, dynamic theming, and multi-language support.

---

## 🚀 Key Features

### 🎧 Core Experience
* **Media3 & ExoPlayer:** Robust audio playback with a foreground service that keeps music playing even when the app is closed.
* **Global MiniPlayer:** A persistent floating player accessible from any screen within the app.
* **Full-Screen Player:** Interactive UI with seek bar, play/pause, skip, and cover art animations.

### 🔐 User & Security
* **JWT Authentication:** Secure Login and Registration flow with token persistence.
* **Profile Management:** User statistics, "Musical Soulmate" matching algorithm, and avatar upload (Multipart).
* **Secure Networking:** `Retrofit` interceptors for automatic token injection and error handling.

### 📂 Content Management
* **Library:** Create custom playlists, view details, and manage collections.
* **Search:** Real-time song search with "Add to Playlist" functionality directly from results.
* **Dynamic UI:** Optimized images using `Coil` with caching and smart placeholders.

### ⚙️ Settings & Accessibility
* **Internationalization (i18n):** Full support for **English** and **Spanish**, switchable in-app without restart.
* **Theme Engine:** Dynamic **Dark/Light mode** switching compliant with Material Design 3.

---

## 🛠 Tech Stack

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
* **Asynchronous:** Coroutines & Flow
* **Network:** [Retrofit 2](https://square.github.io/retrofit/) + OkHttp 3 + Gson
* **Image Loading:** [Coil](https://coil-kt.github.io/coil/)
* **Audio:** [AndroidX Media3](https://developer.android.com/media/media3) (ExoPlayer)
* **Navigation:** Jetpack Compose Navigation
* **Lifecycle:** ViewModel & LiveData

---

## 🏗️ Architecture Overview

The app follows the recommended **App Architecture Guide**:
