# Repository Guidelines

## Project Structure & Module Organization

Spellbindr currently ships as a single `:app` module. All Kotlin sources live in
`app/src/main/kotlin/com/github/arhor/spellbindr` (there is no `app/src/main/java` tree), grouped by feature under
`ui/feature/<feature>` (e.g., `compendium`, `characters`, `diceRoller`), with shared Compose building blocks in
`ui/components` and theming utilities in `ui/theme`. Data access, Room entities, and repositories reside under `data`,
while dependency wiring is isolated in `di` and helpers in `utils`. Android resources stay in `app/src/main/res`, and
any static assets in `app/src/main/assets`. JVM tests use `app/src/test/kotlin`;
instrumentation and Compose UI suites belong in `app/src/androidTest/kotlin`.

## Build, Test, and Development Commands

- `./gradlew assembleDebug` — compiles the debug APK with all Compose previews enabled.
- `./gradlew lint` — executes Android lint; resolve or justify every warning before opening a PR.
- `./gradlew testDebugUnitTest` — runs JUnit-based JVM tests found in `app/src/test`.
- `./gradlew connectedAndroidTest` — launches instrumentation and Compose UI tests on the currently selected
  emulator/device.  
  Run each command from the repo root so Gradle picks up the shared configuration cache; Android Studio run
  configurations should call the same tasks.

## Coding Style & Naming Conventions

`.editorconfig` enforces UTF-8, LF endings, 4-space indentation, and a 120-character limit. Kotlin uses the “official”
style: PascalCase types and composables, camelCase functions/properties, CAPITAL_SNAKE constants, and no star imports (
threshold set to 99). Keep feature ViewModels, state holders, and composables inside their `ui.feature` package; place
repositories, data stores, and Room DAO/entity code under `data`. Annotate Hilt entry points with the narrowest scope
and colocate previews with their composable in the same file when practical.

## Testing Guidelines

Default to JVM tests for pure logic (dice aggregation, repository transforms) using JUnit 4 plus
`kotlinx-coroutines-test`’s `runTest` for deterministic coroutines. UI flows, interaction tests, and asset loading
checks belong in `androidTest`, leveraging Espresso and `androidx.compose.ui.test`. Name suites `<ClassName>Test` for
JVM and `<ClassName>AndroidTest` for device tests, and ensure each PR adds at least one happy-path and one edge case
covering the change. Keep emulator snapshots handy so `connectedAndroidTest` can run headlessly in CI.

## Commit & Pull Request Guidelines

Follow the Conventional Commit prefixes already in history (`feat:`, `fix:`, `refactor:`, etc.) and keep commits scoped
to a single concern. Pull requests should include a concise summary, linked issues, manual test notes (which Gradle
tasks ran and device/emulator details), plus screenshots or screen recordings for user-facing changes. Always rerun
`./gradlew lint testDebugUnitTest` locally—and `connectedAndroidTest` whenever UI logic or navigation changes—before
requesting review.
