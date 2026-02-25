# Repository Guidelines

## Project Structure & Module Organization

Spellbindr is a multi-module Android project. Primary modules:

- `:app` (application shell, nav wiring, cross-module architecture tests, integration android tests)
- `:core:common`, `:core:domain`, `:core:ui`, `:core:ui-spells`, `:core:testing`
- `:data:character`, `:data:compendium`, `:data:favorites`, `:data:settings`
- `:feature:character`, `:feature:compendium`, `:feature:dice`, `:feature:settings`

Code should be tested in the same module that owns it. `:app` tests are reserved for cross-module and app-wiring
coverage. Static SRD data remains in `app/src/main/assets/data`, icons in `app/src/main/assets/icons`.

## Build, Test, and Development Commands

Golden paths:

- `./gradlew assembleDebug`: build the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.
- `./gradlew lintDebug test testDebugUnitTest --stacktrace`: CI task set in `.github/workflows/android-ci.yml`.
- `./gradlew test`: JVM tests for Kotlin/JVM modules (`:core:common`, `:core:domain`, `:core:testing`).
- `./gradlew testDebugUnitTest`: JVM unit tests for Android modules.
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

Unit tests use JUnit4, Truth, MockK, and ArchUnit. Place unit/android tests in the owning module under
`src/test/kotlin` and `src/androidTest/kotlin`. Shared helper/fake test fixtures live in `:core:testing`.
App-only tests in `app/src/test/kotlin/com/github/arhor/spellbindr/architecture` and
`app/src/androidTest/kotlin` cover cross-module rules and app integration wiring.

## CI / Automation

`.github/workflows/android-ci.yml` runs on push/PR to `master` and `stable` and executes
`./gradlew lintDebug test testDebugUnitTest --stacktrace`. On PRs, it also assembles a debug APK via
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

### Architecture Reference for Agents

- MVI feature-entry contract: `docs/mvi-dispatch-contract.md`
  Use this as the source of truth for `Intent/Route/Screen/ViewModel/Effect` wiring, route-owned navigation
  interception, and dispatch-based screen APIs.

## Compose Screenshot Exports (Preview → PNG)

This repo is configured for Android’s Compose Screenshot Testing (AGP screenshot plugin) to generate PNGs from Compose
previews.

Workflow:

- Add screenshot previews under the owning module, e.g.:
    - `core/ui/src/screenshotTest/kotlin/...`
    - `feature/character/src/screenshotTest/kotlin/...`
- Wrap preview content with that module’s `ScreenshotHarness` to ensure consistent theme/background/padding.
- Each exported preview must be annotated with:
    - `@PreviewTest` (from `com.android.tools.screenshot.PreviewTest`)
    - a Compose `@Preview...` annotation (`@Preview`, `@PreviewLightDark`, etc.)
    - `@Composable`
- Generate/update reference PNGs for a specific preview (or file/class) via Gradle test filtering:
    - `./gradlew :core:ui:updateDebugScreenshotTest --tests '*AppTopBar_Screenshot*'`
- Reference images are written to:
    - `<module>/src/screenshotTestDebug/reference/` (gitignored in this repo)
- Export the generated PNGs to a clean timestamped folder:
    - `run/export-preview-screenshot.sh --module :core:ui --tests '*AppTopBar_Screenshot*'`
    - If Gradle can’t be invoked from the script (sandbox/permissions), run Gradle first and then:
      `run/export-preview-screenshot.sh --module :core:ui --tests '*AppTopBar_Screenshot*' --skip-gradle`

## Visual Comparison Workflow (User-provided image → “match this”)

When the user provides a reference image and asks to make a composable match it:

1. Identify the target composable + state needed to reproduce the UI (inputs, theme, screen size, font scale, etc.).
2. Create/adjust a dedicated `@PreviewTest` preview in that module’s `src/screenshotTest/kotlin/...` that renders
   that exact state (use explicit `@Preview(widthDp=..., heightDp=...)` when size matters).
3. Generate a screenshot PNG and export it (commands above).
4. Compare images in-chat:
    - Render the exported PNG using a local absolute path in Markdown: `![generated](/absolute/path/to.png)`
    - Compare visually against the user-provided image, list differences (layout, padding, typography, colors, icons),
      and iterate on the composable until it matches closely.
