# Galleram

**Galleram** (Gallery + Telegram) is a modern, privacy-focused Android gallery app that uses your actual Telegram account as a high-performance cloud storage backend.

Unlike simple bot-based uploaders, Galleram is a full-featured, local-first media manager that treats Telegram as an infinite, secure, and free extension of your phone's storage.

## 🚀 Key Architectural Pillars

### 1. TDLib Integration (User-Account Based)
Galleram uses **TDLib** (Telegram Database Library) to log into your real Telegram account. This provides:
*   **Large File Support:** Upload videos up to 2GB (or 4GB with Telegram Premium).
*   **Native Streaming:** Stream your cloud videos directly without waiting for full downloads.
*   **MTProto Security:** Benefit from Telegram's battle-tested end-to-end encryption.

### 2. The "Trinity of Identity"
To ensure a reliable 1:1 mapping between your phone and the cloud, Galleram tracks every file using three unique keys:
*   **Local ID:** The Android MediaStore identifier for instant local access.
*   **SHA-256 Hashsum:** Used for "Smart" de-duplication. If you move a file or copy it to another device, Galleram recognizes it instantly and avoids redundant uploads.
*   **Remote Unique ID:** TDLib's persistent global identifier that links a local file to its specific Telegram message.

### 3. Local-First MVI Architecture
Galleram is built using **Model-View-Intent (MVI)** to ensure the UI remains fluid and responsive:
*   **Instant Load:** Your local photos are displayed immediately from the MediaStore.
*   **Gesture-Based Resizing:** A fluid, "Google Photos" style pinch-to-zoom grid that lets you dynamically change column counts.
*   **Background Sync:** A non-disturbing sync engine handles uploads and metadata parsing while you browse.

### 4. Smart Metadata & Organization
*   **Hashtag Folders:** Local folders map to Telegram hashtags (e.g., `/DCIM/Camera` -> `#Camera`). You can filter your entire cloud timeline with a single tap.
*   **Hidden Metadata:** EXIF data, original filenames, and location info are stored within hidden `<blockquote>` tags in Telegram message captions, making the storage self-describing and independent of the local database.
*   **Backup Channel Strategy:** A dedicated private channel can store app database backups, ensuring your entire gallery structure can be restored on a brand-new device in seconds.

## 🛠 Tech Stack
*   **Language:** Kotlin
*   **UI:** Jetpack Compose (Material 3)
*   **Database:** Room (with hash-based tracking)
*   **Core:** TDLib (Native JNI)
*   **Background Tasks:** WorkManager
*   **Image Loading:** Coil

## 🔒 Privacy & Trust
Galleram communicates directly with Telegram servers. No third-party servers, no telemetry, and no tracking. Your media stays between your phone and your Telegram account.
