# Spellbindr

Spellbindr is an Android app for Dungeons & Dragons 5e. It provides a Compose UI for browsing bundled, multi-source
reference data, managing characters, and rolling dice.

## Overview

Spellbindr boots from `SpellbindrApplication` and `MainActivity`, then loads the JSON assets in
`app/src/main/assets/data` via the asset bootstrapper. The bundled data is drawn from multiple 5e data sets—not only
the SRD—and includes source metadata where provided.

## Key Features

- Characters: list, guided setup, manual editor, and character sheet screens with spell selection.
- Compendium: spells (search, class filters, favorites, details), races, alignments, and conditions.
- Dice roller with advantage/disadvantage and roll breakdowns.
- Light/dark theme setting and favorites stored via DataStore Preferences.

## Tech Stack

- Kotlin 2.4.10, JVM 17 (`.java-version`), Android Gradle Plugin 9.3.1.
- Android minSdk 33, target/compile SDK 37 (`app/build.gradle.kts`).
- Jetpack Compose (Material3, Navigation), Hilt DI, KSP.
- Room for character persistence; DataStore Preferences for app settings and favorites.
- Kotlinx Serialization + Coroutines/Flow.

## Project Structure

```
app/                    # all application, domain, data, feature, UI, and test code
  src/main/assets/data/ # bundled multi-source 5e reference data
  src/main/kotlin/     # application and package-organized implementation code
  src/test/kotlin/      # JVM tests and shared test helpers
  src/androidTest/     # instrumentation tests
```

## Architecture

- Repository interfaces, data implementations, domain logic, feature UI, and application wiring all live in `:app`.

## Getting Started

Prereqs:

- JDK 17.
- Android SDK 37.
- An SDK path configured in `local.properties` or `ANDROID_HOME`.

## Build / Run / Test

- Build debug APK: `./gradlew assembleDebug` (output: `app/build/outputs/apk/debug/app-debug.apk`).
- Lint + unit tests (CI): `./gradlew lintDebug test testDebugUnitTest`.
- Instrumentation tests: `./gradlew connectedDebugAndroidTest` (requires a device or emulator).
- Linux SDK bootstrap: `run/setup.sh`.

## Screenshot exports (Compose previews → PNG)

This project uses AGP Compose Screenshot Testing in module-local screenshot source sets.

- Screenshot previews currently live in:
    - `app/src/screenshotTest/kotlin/...`
- Each preview must be annotated with:
    - `@Preview...`
    - `@PreviewTest`
    - `@Composable`
- Reference images are generated under `<module>/src/screenshotTestDebug/reference/` (gitignored).

CLI export helper:

- Export screenshots for a module + preview filter:
    - `run/export-preview-screenshot.sh --module :app --tests '*AppTopBar*'`
    - `run/export-preview-screenshot.sh --module :app --tests '*SpellsTab_Screenshot*'`
    - If you already ran Gradle separately:
      `run/export-preview-screenshot.sh --module :app --tests '*AppTopBar*' --skip-gradle`

Exports are copied to `<module>/build/outputs/preview-screenshots/<timestamp>/`.

## CI / Quality

- `.github/workflows/android-ci.yml` runs `./gradlew lintDebug test testDebugUnitTest` on push and PRs.
- PRs also assemble a debug APK and upload it as the `app-debug-apk` artifact.
