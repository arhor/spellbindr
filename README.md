# Spellbindr

Spellbindr is an Android app for Dungeons & Dragons 5e. It provides a Compose UI for browsing bundled, multi-source
reference data, managing characters, and rolling dice.

## Overview

Spellbindr boots from `SpellbindrApplication` and `MainActivity`, then loads the JSON assets in
`app/src/main/assets/data` via the asset bootstrapper. The bundled data is drawn from multiple 5e data sets, not only
the SRD, and includes source metadata where provided.

## Key Features

- Characters: list, guided setup, manual editor, character sheet, spell selection, and a one-level,
  multiclass-capable Level Up flow for managed characters.
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
  src/main/kotlin/      # application and package-organized implementation code
  src/test/kotlin/      # JVM tests and shared test helpers
  src/androidTest/      # instrumentation tests
```

## Architecture

- Repository interfaces, data implementations, domain logic, feature UI, and application wiring all live in `:app`.

### Character progression and level-up

Guided character creation atomically stores the mutable character-sheet snapshot and a separate, structured
`Managed` progression record. Progression keeps permanent build decisions as ordered level records, including stable
class/subclass references, hit-point choices, feature choices, ability-score decisions, and spell changes. Repeated
class entries support multiclass progression, and the progression history is the source of truth for permanent build
decisions.

Characters created through the manual flow and characters migrated from older databases have no synthesized
progression history and resolve as `Unmanaged`. Database migration 3→4 preserves their existing character and sheet
data while adding an empty one-to-one `character_progressions` table; deleting a character cascades to its progression
row. Do not infer progression history for these characters from their sheet snapshots.

Managed characters at levels 1–19 can start a one-level Level Up flow from the progression card or character-sheet
overflow menu. The flow validates bundled class and multiclass rules, collects applicable class/subclass, hit-point,
feature, proficiency, ability-score/feat, and spell decisions, then appends one progression record and materializes the
updated sheet in a single Room transaction. Stale state and validation failures are reported without partially saving.
Level-20 and `Unmanaged` characters expose disabled level-up actions with explanatory text.

For managed characters, the full editor keeps progression-owned class, level, ability-score, proficiency, maximum-HP,
hit-dice, saving-throw, and skill fields read-only while mutable play state and free-text fields remain editable. See
`docs/level-up-progress.md` for the implementation ledger and current limitations.

## Getting Started

Prereqs:

- JDK 17.
- Android SDK 37.
- An SDK path configured in `local.properties` or `ANDROID_HOME`.

API 37 is the compile/target baseline. AndroidX Core 1.19.0, Lifecycle 2.11.0, and Hilt Navigation Compose 1.4.0
are the compatible current direct dependency versions.

## Build / Run / Test

- Build debug APK: `./gradlew assembleDebug` (output: `app/build/outputs/apk/debug/app-debug.apk`).
- CI-equivalent checks: `./gradlew lintDebug test testDebugUnitTest assembleRelease --stacktrace`.
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

- `.github/workflows/android-ci.yml` runs `./gradlew lintDebug test testDebugUnitTest assembleRelease --stacktrace`
  on pushes and pull requests targeting `master` or `stable`.
- Pull requests also assemble a debug APK, upload it as the `app-debug-apk` artifact, and post its download link.
