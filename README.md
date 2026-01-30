# Media3 Service Demo Project

This is a **small demo Android project** that demonstrates how to use **Android Media3** together with a **foreground service** (`MediaSessionService`) to play local video files using a **custom UI**.

The project focuses on showing how the different Media3 components work together:
- `ExoPlayer`
- `MediaSession`
- `MediaSessionService`
- Media notifications
- Playback from a foreground service

---

##  Purpose of the Project

The goal of this project is to provide a **simple and practical example** of:
- Playing media using **Media3**
- Running playback inside a **foreground service**
- Exposing playback through a **MediaSession**
- Handling playback outside the app (notification / background)
- Wiring everything together without relying on the default Media3 UI

This is **not a full production app**, but a **learning & reference project**.

---

##  How It Works

1. The user selects **any MP4 video file** from their device storage.
2. The video is played using **ExoPlayer**.
3. Playback is handled inside a **MediaSessionService**.
4. The service runs as a **foreground service**, allowing background playback.
5. Media controls are exposed via the **Media notification**.
6. A **custom UI** controls playback instead of the default Media3 PlayerView.

---

##  How to Use the App

1. Clone the repository
2. Open the project in **Android Studio**
3. Run the app on a real device or emulator
4. Tap **Select Video**
5. Pick **any MP4 file** from your device
6. Playback starts immediately and continues in the background

---

##  Tech Stack

- Kotlin
- Android Media3
- ExoPlayer
- MediaSession
- MediaSessionService
- Foreground Service
- Custom playback UI

---

##  Notes

- This project focuses on **local media playback** (MP4 files).
- It is meant for **educational purposes** and experimentation.
- Error handling and edge cases are intentionally kept simple.

---

##  Why Media3?

Media3 is the modern replacement for:
- ExoPlayer (legacy)
- MediaSession (framework version)
- MediaBrowserService

It provides a **cleaner API**, better background playback support, and easier integration with system components like notifications, Bluetooth devices, and Android Auto.

---

## License

This project is provided as-is for learning and demonstration purposes.
