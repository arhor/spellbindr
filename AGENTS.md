# Repository Guidelines

## Project Structure & Module Organization

Spellbindr is a single-module Android project:

- `:app` contains application wiring, domain models/use cases, data implementations, UI components, features, JVM
  tests, instrumentation tests, and screenshot tests.

Code should be tested in `:app`, close to the package that owns it. Static multi-source 5e reference data remains in
`app/src/main/assets/data`, icons in `app/src/main/assets/icons`.

## Build, Test, and Development Commands

Golden paths:

- `./gradlew assembleDebug`: build the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.
- `./gradlew lintDebug test testDebugUnitTest assembleRelease --stacktrace`: CI task set in
  `.github/workflows/android-ci.yml`.
- `./gradlew test`: JVM unit tests in `:app`.
- `./gradlew testDebugUnitTest`: debug-variant JVM unit tests.
- `./gradlew connectedDebugAndroidTest`: instrumentation/UI tests (device or emulator required).
- `run/setup.sh`: Linux-only SDK bootstrap (downloads cmdline tools, installs the required Android SDK, runs
  `git submodule update`).

Prereqs: JDK 17 (`.java-version`) and Android SDK 37 (`app/build.gradle.kts`).
Set the SDK path in `local.properties` or `ANDROID_HOME`.

## Coding Style & Naming Conventions

Formatting follows `.editorconfig` (4-space indent, LF, max line length 120, final newline; JSON/YAML use 2 spaces).
Follow existing naming patterns: `*Screen` composables under `ui/feature`, `*Route` navigation objects, `*ViewModel`
classes, `*UseCase` in `domain/usecase`, and `*Repository`/`*RepositoryImpl` for interfaces and data-layer
implementations.

## Testing Guidelines

Unit tests use JUnit4, Truth, and MockK. Place unit tests under `app/src/test/kotlin` and instrumentation tests under
`app/src/androidTest/kotlin`. Shared helper/fake test fixtures live under `app/src/test/kotlin`. Screenshot previews and
tests live under `app/src/screenshotTest/kotlin`.

## CI / Automation

`.github/workflows/android-ci.yml` runs on pushes and pull requests targeting `master` and `stable` and executes
`./gradlew lintDebug test testDebugUnitTest assembleRelease --stacktrace`. On pull requests, it also assembles a debug
APK via `.github/actions/assemble-debug-apk`, uploads `app/build/outputs/apk/debug/app-debug.apk`, and posts the artifact
link to the pull request.

### CI Gotchas

- If you change the module name or APK output location, update the artifact path in
  `.github/actions/assemble-debug-apk/action.yml` or the upload step will fail.
- Instrumentation and screenshot tests are not part of CI; run the relevant Gradle tasks locally when touching UI/DI.

## Commit & Pull Request Guidelines

Recent commits mostly follow `type: summary` (for example, `docs:`, `refactor:`, `feat:`), but there are exceptions in
history; follow the pattern when possible. CI only runs for pull requests targeting `master` or `stable`.

- TODO (maintainers): Add a PR template / CONTRIBUTING docs if specific review steps or required checks are intended.

## Configuration Notes

The instrumentation runner is `com.github.arhor.spellbindr.HiltApplicationTestRunner`; keep it when adding or updating
androidTest components. Avoid committing local SDK paths or secrets.

### Architecture References for Agents

- MVI feature-entry contract: `docs/mvi-dispatch-contract.md`
  Use this as the source of truth for `Intent/Route/Screen/ViewModel/Effect` wiring, route-owned navigation
  interception, and dispatch-based screen APIs.
- Multiclass level-up implementation ledger: `docs/level-up-progress.md`
  Use this to understand the implemented progression rules, persistence boundaries, managed-editor restrictions,
  known representation limits, and verification history.

### Managed Character Progression and Level-Up Boundary

- Newly created guided characters are `Managed`: the mutable sheet snapshot and structured progression are saved
  atomically. Progression is stored separately from mutable sheet/play state as ordered level records and is the
  source of truth for permanent build decisions.
- Characters created through the manual flow and legacy characters are `Unmanaged`. Migration 3→4 preserves existing
  character/sheet data exactly, creates the one-to-one `character_progressions` table empty, and relies on its
  character foreign key for cascading deletion. Do not infer or synthesize legacy progression from sheet snapshots.
- Managed characters at levels 1–19 can add exactly one level through `CharacterLevelUp`. The flow supports same-class
  and multiclass progression, applicable subclass, HP, feature, proficiency, ASI/feat, and spell decisions, then
  appends one progression record and materializes the updated sheet atomically. Level-20 and `Unmanaged` characters
  must remain ineligible.
- Keep level-up calculation and validation in `LevelUpProgressionEngine`, application orchestration in
  `ApplyLevelUpUseCase`, and the final sheet/progression write inside the repository Room transaction. Preserve typed
  stale-state, validation, unmanaged-character, missing-character, and persistence outcomes instead of converting
  them into partial writes.
- The managed editor must not mutate progression-owned class, level, ability-score, proficiency, maximum-HP,
  hit-dice, saving-throw, or skill fields. Mutable play state and free-text fields remain editable. Keep manual and
  progression-owned proficiency ids separate when materializing or saving.
- Keep compile/target SDK 37 as the stable baseline. Core KTX 1.19.0, Lifecycle 2.11.0, and Hilt Navigation Compose
  1.4.0 are the compatible current direct dependency versions.

## Compose Screenshot Exports (Preview → PNG)

This repo is configured for Android’s Compose Screenshot Testing (AGP screenshot plugin) to generate PNGs from Compose
previews.

Workflow:

- Add screenshot previews under the owning module, e.g.:
    - `app/src/screenshotTest/kotlin/...`
- Wrap preview content with that module’s `ScreenshotHarness` to ensure consistent theme/background/padding.
- Each exported preview must be annotated with:
    - `@PreviewTest` (from `com.android.tools.screenshot.PreviewTest`)
    - a Compose `@Preview...` annotation (`@Preview`, `@PreviewLightDark`, etc.)
    - `@Composable`
- Generate/update reference PNGs for a specific preview (or file/class) via Gradle test filtering:
    - `./gradlew :app:updateDebugScreenshotTest --tests '*AppTopBar_Screenshot*'`
- Reference images are written to:
    - `<module>/src/screenshotTestDebug/reference/` (gitignored in this repo)
- Export the generated PNGs to a clean timestamped folder:
    - `run/export-preview-screenshot.sh --module :app --tests '*AppTopBar_Screenshot*'`
    - If Gradle can’t be invoked from the script (sandbox/permissions), run Gradle first and then:
      `run/export-preview-screenshot.sh --module :app --tests '*AppTopBar_Screenshot*' --skip-gradle`

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
