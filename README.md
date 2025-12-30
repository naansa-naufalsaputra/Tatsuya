# Tatsuya Manga Reader 📱

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)

**Tatsuya Manga Reader** is a modern, offline-first Android application built for reading manga from multiple sources. It features a sleek Material 3 design, granular history tracking, and a robust background download system.

## ✨ Key Features

*   **Multi-Source Architecture**:
    *   **MangaDex**: Full integration via REST API (Search, Popular, Details).
    *   **KomikCast**: Scraped source using **Jsoup** with Cloudflare bypass logic (User-Agent handling).
    *   **Unified UI**: Seamlessly browse content from mixed sources with visual source indicators.
*   **Offline-First Experience**:
    *   **Background Downloads**: Download chapters using `WorkManager` for offline reading.
    *   **Smart Caching**: `Coil` image caching ensures smooth reading even with flaky internet.
*   **Granular History Tracking**:
    *   Tracks reading progress down to the specific page number.
    *   Recently read chapters appear on the home screen for quick resumption.
    *   "Read" indicators for completed chapters.
*   **Smart Library**:
    *   Save favorites to your local library.
    *   **Background Updates**: Periodically checks for new chapters of favorite manga and sends system notifications.
*   **Modern UI/UX**:
    *   **Material 3 Design**: Fully thematic with Dark/Light mode support (persisted preferences).
    *   **Genre Filtering**: Filter browse results by tags (e.g., Action, Romance, Isekai).
    *   **Search**: Unified search bar querying multiple sources simultaneously.

## 🛠️ Tech Stack

*   **Language**: Kotlin
*   **UI Toolkit**: Jetpack Compose (Material 3)
*   **Architecture**: Clean Architecture + MVVM
*   **Dependency Injection**: Dagger Hilt
*   **Network**: Retrofit & OkHttp
*   **Scraping**: Jsoup (for HTML parsing)
*   **Database**: Room (SQLite abstraction)
*   **Async Operations**: Coroutines & Flow
*   **Image Loading**: Coil
*   **Background Tasks**: WorkManager
*   **Preferences**: DataStore

## 📷 Screenshots

| Browse Screen | Detail Screen | Reader |
|:---:|:---:|:---:|
| *(Add screenshot here)* | *(Add screenshot here)* | *(Add screenshot here)* |

## ⚠️ Disclaimer

This application is created for **educational purposes only**. The web scraping module (KomikCastSource) demonstrates parsing techniques and is not intended to violate any terms of service or copyright laws. Please respect the content creators and original platforms.

---

Built with ❤️ using Android Jetpack.
