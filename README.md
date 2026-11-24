# Victus Tune 🎵

A modern, minimalist Android music player and online streaming app inspired by BitChord and LastWave, built with **Kotlin** and **Jetpack Compose**. Engineered for fluid 60/120fps animations, edge-to-edge dark AMOLED aesthetics, rock-solid background playback, and seamless online discovery.

---

## ✨ Features

- **Unified Local & Online Audio:** Play local MediaStore audio files (MP3, FLAC, WAV, M4A, OGG) and stream YouTube Music online under a single unified domain model.
- **Online Music Discovery (InnerTube API):**
  - **Explore Feed:** Discover Trending music, Quick Picks, New Albums, Recommended Playlists, and Artists.
  - **Auto-Radio Queue:** Tapping a track automatically fetches and queues continuous related recommendations via InnerTube `/next`.
- **Stream Resolution & Caching (NewPipeExtractor):**
  - Instant stream extraction with audio quality preference (`Auto`, `High`, `Medium`, `Low`), prioritizing 160kbps Opus and 256kbps M4A.
  - Media3 `ResolvingDataSource` resolves `stream://<videoId>` URIs lazily on the fly with automatic 403/410 expiry retry.
  - Media3 `CacheDataSource` backed by `SimpleCache` with configurable disk limits (256MB, 512MB, 1GB) and instant cache-clearing.
- **Upgraded Multi-Filter Search:**
  - Search filter chips: `Local`, `Songs`, `Albums`, `Artists`, `Playlists`.
  - Recent search history persisted in Room with single-tap replay and deletion.
- **Remote Detail Views:** Dedicated full detail pages for online Albums, Artists, and Playlists with "Play All", "Shuffle All", and "Add to Queue".
- **Streaming Settings:** Audio stream quality selection and real-time cache disk usage display.
- **Edge-to-Edge AMOLED UI:** Deep blacks with subtle frosted glass containers and dynamic accent colors extracted in real-time from the current track's artwork via AndroidX `Palette`.
- **Persistent Mini-Player:** Docked floating bar with marquee-scrolling metadata, animated micro-progress bar, play/pause toggle, and swipe left/right gestures to skip tracks.
- **Full-Screen Now Playing:**
  - Vertical drag-to-dismiss gesture and smooth expanding animations.
  - Interactive smooth waveform scrubber with elapsed and remaining timestamps.
  - Complete transport controls: Shuffle, Previous, Play/Pause morph, Next, Repeat (Off / All / One), plus Favorite heart button.
- **Karaoke Synced Lyrics:** Real-time synchronized `.lrc` lyrics with auto-scroll and tap-to-seek functionality.
- **Queue Management:** Modal bottom sheet for "Up Next" with reordering and removal, directly synchronized with ExoPlayer's timeline.
- **5-Band Hardware Equalizer:** Attached to ExoPlayer's `audioSessionId` with vertical frequency gain sliders (-10dB to +10dB) and 7 built-in presets (*Flat, Bass Boost, Electronic, Rock, Pop, Vocal Boost, Treble Boost*).
- **Sleep Timer:** Automatic playback sleep countdown (15m, 30m, 45m, 60m, 90m) with live remaining time display.
- **Room Database:** High-performance persistence for user playlists, favorites, play history, and search history.

---

## 🛠 Tech Stack

- **UI & Animation:** Kotlin, Jetpack Compose Material 3, Compose Animation, Edge-to-Edge layout.
- **Audio Engine:** AndroidX Media3 (`media3-exoplayer`, `media3-session`, `media3-ui`, `media3-datasource`) with foreground `MediaSessionService`.
- **Online Streaming:** InnerTube REST client (typed search, browse, next) + NewPipeExtractor (0.26.5) audio stream extraction.
- **Core Library Desugaring:** `com.android.tools:desugar_jdk_libs_nio:2.1.5` for full `java.time` and NIO support below Android 13.
- **Architecture:** Clean Architecture + MVVM with unidirectional data flow (`StateFlow` / `SharedFlow`).
- **Persistence:** Room 2.6.1 (with KSP) & DataStore Preferences 1.1.1.
- **Image Loading:** Coil 2.7.0 Compose + AndroidX Palette.

---

## 📦 Building and Running

To compile the debug APK:
```bash
./gradlew assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk` (and `victustune-player.apk`).
