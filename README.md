# Spellbindr

Spellbindr is an Android app for Dungeons & Dragons 5e. It provides a Compose UI for browsing SRD reference data,
managing characters, and rolling dice.

## Overview

Spellbindr boots from `SpellbindrApplication` and `MainActivity`, then loads SRD JSON assets in
`app/src/main/assets/data` via the asset bootstrapper.

## Key Features

- Characters: list, guided setup, manual editor, and character sheet screens with spell selection.
- Compendium: spells (search, class filters, favorites, details), races, alignments, and conditions.
- Dice roller with advantage/disadvantage and roll breakdowns.
- Light/dark theme setting and favorites stored via DataStore Preferences.

## Tech Stack

- Kotlin 2.3, JVM 17 (`.java-version`), Android Gradle Plugin 8.13.2.
- Android minSdk 33, target/compile SDK 36 (`app/build.gradle.kts`).
- Jetpack Compose (Material3, Navigation), Hilt DI, KSP.
- Room for character persistence; DataStore Preferences for app settings and favorites.
- Kotlinx Serialization + Coroutines/Flow.

## Project Structure

```
app/
  src/main/kotlin/com/github/arhor/spellbindr/
    ui/                 # Compose screens, navigation, components
    domain/             # models, repositories, use cases
    data/               # Room, asset loaders, repository implementations
    di/                 # Hilt modules
  src/main/assets/data/ # D&D JSON assets
  src/androidTest/      # instrumentation tests
  src/test/             # unit + ArchUnit tests
```

## Architecture

- Layered packages (`ui`, `domain`, `data`) with repository interfaces in `domain/repository` and implementations in
  `data/repository`.
- Asset bootstrapper (`DefaultAssetBootstrapper`) loads JSON assets into `data/local/assets` stores during app startup.
- ArchUnit tests in `app/src/test/kotlin/com/github/arhor/spellbindr/architecture` enforce dependency boundaries.

## Getting Started

Prereqs:

- JDK 17.
- Android SDK 36 + build-tools 36.0.0.

## Build / Run / Test

- Build debug APK: `./gradlew assembleDebug` (output: `app/build/outputs/apk/debug/app-debug.apk`).
- Lint + unit tests (CI): `./gradlew lintDebug testDebugUnitTest`.
- Instrumentation tests: `./gradlew connectedDebugAndroidTest` (requires a device or emulator).

## Screenshot exports (Compose previews → PNG)

This project is set up for Android Studio / AGP Compose Screenshot Testing.

- Screenshot previews live in `app/src/screenshotTest/kotlin/...` and must be annotated with:
    - `@Preview...`
    - `@PreviewTest`
    - `@Composable`
- Reference images are generated under `app/src/screenshotTestDebug/reference/` (gitignored).

CLI export helper:

- Export screenshots for a single preview (or any pattern):
    - `run/export-preview-screenshot.sh --tests '*AppTopBar*'`
- Export the provided smoke templates:
    - `run/export-preview-screenshot.sh --tests '*SmokeScreenshotPreviews*'`
    - If you already ran Gradle separately:
      `run/export-preview-screenshot.sh --tests '*SmokeScreenshotPreviews*' --skip-gradle`

Exports are copied to `app/build/outputs/preview-screenshots/<timestamp>/`.

## CI / Quality

- `.github/workflows/android-ci.yml` runs `./gradlew lintDebug testDebugUnitTest` on push and PRs.
- PRs also assemble a debug APK and upload it as the `app-debug-apk` artifact.
