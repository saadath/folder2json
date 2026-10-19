# 📁 Folder2JSON

**Folder2JSON** is a modern, powerful, and privacy-focused Android application built with **Kotlin** and **Jetpack Compose**. It allows users to select any folder on their device using Android's **Storage Access Framework (SAF)**, scan its hierarchical structure and file metadata, convert it into a well-structured **JSON** format, and export or share it instantly.

---

## 🌟 Key Features

- **📂 Storage Access Framework (SAF) Integration:** Securely browse and select any directory on device storage without requesting broad storage permissions.
- **🌳 Interactive File Tree Viewer:** Navigate through nested folders and files in a clean, expandable tree structure with icons and metadata.
- **📄 Comprehensive JSON Viewer:** Instantly view generated JSON output (`data.json`), with options to copy to clipboard, share with other apps, or save to storage.
- **⚙️ Advanced Filtering & Configuration:**
  - **Filter Modes:** Whitelist ("Only Specified") or Blacklist ("Exclude Specified") file extensions.
  - **Empty Folders:** Option to include or omit empty directories.
  - **Depth Limiting:** Control maximum traversal depth.
  - **Indentation & Minification:** Format JSON with 2 spaces, 4 spaces, tabs, or minify.
- **📊 Detailed Scan Statistics:** Real-time summary card displaying total files scanned, folders counted, total directory size, and scan duration.
- **🎨 Material Design 3 & RTL Support:** Crafted using M3 design principles with full Persian (RTL) language and typography support.

---

## 🛠️ Tech Stack & Architecture

- **Language:** 100% Kotlin
- **UI Framework:** Jetpack Compose & Material 3
- **Asynchronous Operations:** Kotlin Coroutines & Flows (`StateFlow`, `SharedFlow`)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Testing:** Robolectric & JUnit local unit tests
- **Build System:** Gradle (Kotlin DSL)

---

## 🚀 Getting Started & Building

1. Clone the repository:
   ```bash
   git clone https://github.com/saadath/folder2json.git
   ```
2. Open the project in **Android Studio** (Hedgehog or newer).
3. Sync project with Gradle files.
4. Run tests or build debug APK:
   ```bash
   gradle :app:testDebugUnitTest
   gradle :app:assembleDebug
   ```

---

## 📄 License

This project is open-source and available under the MIT License.
