# Repository Guidelines

## Project Structure & Module Organization

Spellbindr is a single-module Android app (`:app` in `settings.gradle.kts`). Kotlin sources live in
`app/src/main/kotlin/com/github/arhor/spellbindr` and are organized by layers and features:

- `ui` (Compose entry points, `ui/navigation`, screens under `ui/feature`, shared `components` and `theme`)
- `domain` (models, repository interfaces in `domain/repository`, use cases in `domain/usecase`)
- `data` (Room + asset loaders in `data/local`, mappers in `data/mapper`, serialization helpers in
  `data/serialization`, repository implementations in `data/repository`)
- `di` (Hilt modules) and `utils` for shared helpers.
  Static data lives in `app/src/main/assets/data`, icons in `app/src/main/assets/icons`, and Android resources in
  `app/src/main/res`. Tests are split between `app/src/test/kotlin` (unit + ArchUnit) and
  `app/src/androidTest/kotlin` (Compose UI + Hilt integration).

## Build, Test, and Development Commands

Golden paths:

- `./gradlew assembleDebug`: build the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.
- `./gradlew lintDebug testDebugUnitTest --stacktrace`: CI task set in `.github/workflows/android-ci.yml`.
- `./gradlew testDebugUnitTest`: JVM unit tests only.
- `./gradlew connectedDebugAndroidTest`: instrumentation/UI tests (device or emulator required).
- `run/setup.sh`: Linux-only SDK bootstrap (downloads cmdline tools, installs SDK 36 + build-tools 36.0.0, runs
  `git submodule update`).

Prereqs: JDK 17 (`.java-version`) and Android SDK 36 / build-tools 36.0.0 (`app/build.gradle.kts`).
Set the SDK path in `local.properties` or `ANDROID_HOME`.

## Coding Style & Naming Conventions

Formatting follows `.editorconfig` (4-space indent, LF, max line length 120, final newline; JSON/YAML use 2 spaces).
Follow existing naming patterns: `*Screen` composables under `ui/feature`, `*Route` navigation objects, `*ViewModel`
classes, `*UseCase` in `domain/usecase`, and `*Repository`/`*RepositoryImpl` for interfaces and data-layer
implementations.

## Testing Guidelines

Unit tests use JUnit4, Truth, MockK, and ArchUnit (see `app/src/test/kotlin/com/github/arhor/spellbindr/architecture`).
Android tests live in `app/src/androidTest/kotlin` and use `androidx.compose.ui.test.junit4` with the Hilt runner
(`com.github.arhor.spellbindr.HiltApplicationTestRunner`).

## CI / Automation

`.github/workflows/android-ci.yml` runs on push/PR to `master` and `stable` and executes
`./gradlew lintDebug testDebugUnitTest --stacktrace`. On PRs, it also assembles a debug APK via
`.github/actions/assemble-debug-apk` and uploads `app/build/outputs/apk/debug/app-debug.apk`.

### CI Gotchas

- If you change the module name or APK output location, update the artifact path in
  `.github/actions/assemble-debug-apk/action.yml` or the upload step will fail.
- Instrumentation tests are not part of CI; run `./gradlew connectedDebugAndroidTest` locally when touching UI/DI.

## Commit & Pull Request Guidelines

Recent commits mostly follow `type: summary` (for example, `docs:`, `refactor:`, `feat:`), but there are exceptions in
history; follow the pattern when possible. CI only runs for PRs targeting `master` or `stable`.

- TODO (maintainers): Add a PR template / CONTRIBUTING docs if specific review steps or required checks are intended.

## Configuration Notes

The instrumentation runner is `com.github.arhor.spellbindr.HiltApplicationTestRunner`; keep it when adding or updating
androidTest components. Avoid committing local SDK paths or secrets.
