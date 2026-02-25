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

- Kotlin 2.3, JVM 17 (`.java-version`), Android Gradle Plugin 9.0.0.
- Android minSdk 33, target/compile SDK 36 (`app/build.gradle.kts`).
- Jetpack Compose (Material3, Navigation), Hilt DI, KSP.
- Room for character persistence; DataStore Preferences for app settings and favorites.
- Kotlinx Serialization + Coroutines/Flow.

## Project Structure

```
app/                    # app shell + nav wiring + integration tests
core/
  common/               # pure Kotlin shared utils
  domain/               # models, repository contracts, use cases
  testing/              # shared test helpers/fakes
  ui/                   # shared Compose theme/components
  ui-spells/            # shared spell UI components
data/
  character/            # Room persistence
  compendium/           # SRD asset stores + repositories
  preferences/          # DataStore-backed repositories
feature/
  character/
  compendium/
  dice/
  settings/
```

## Architecture

- Repository interfaces live in `:core:domain`, while data implementations live in `:data:*`.
- Feature UI and state live in `:feature:*`, consumed by the `:app` shell.
- ArchUnit and end-to-end integration tests remain in `:app` as cross-module quality gates.

## Getting Started

Prereqs:

- JDK 17.
- Android SDK 36 + build-tools 36.0.0.

## Build / Run / Test

- Build debug APK: `./gradlew assembleDebug` (output: `app/build/outputs/apk/debug/app-debug.apk`).
- Lint + unit tests (CI): `./gradlew lintDebug test testDebugUnitTest`.
- Instrumentation tests: `./gradlew connectedDebugAndroidTest` (requires a device or emulator).

## Screenshot exports (Compose previews → PNG)

This project uses AGP Compose Screenshot Testing in module-local screenshot source sets.

- Screenshot previews currently live in:
    - `core/ui/src/screenshotTest/kotlin/...`
    - `feature/character/src/screenshotTest/kotlin/...`
- Each preview must be annotated with:
    - `@Preview...`
    - `@PreviewTest`
    - `@Composable`
- Reference images are generated under `<module>/src/screenshotTestDebug/reference/` (gitignored).

CLI export helper:

- Export screenshots for a module + preview filter:
    - `run/export-preview-screenshot.sh --module :core:ui --tests '*AppTopBar*'`
    - `run/export-preview-screenshot.sh --module :feature:character --tests '*SpellsTab_Screenshot*'`
    - If you already ran Gradle separately:
      `run/export-preview-screenshot.sh --module :core:ui --tests '*AppTopBar*' --skip-gradle`

Exports are copied to `<module>/build/outputs/preview-screenshots/<timestamp>/`.

## CI / Quality

- `.github/workflows/android-ci.yml` runs `./gradlew lintDebug test testDebugUnitTest assembleRelease` on push and PRs.
- PRs also assemble a debug APK and upload it as the `app-debug-apk` artifact.
