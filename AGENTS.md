# Spellbindr – Agent Guide

Comprehensive instructions for AI coding agents contributing to the Spellbindr D&D 5e companion app. Follow these
guidelines to stay aligned with the maintainers’ expectations.

## Project Overview

- Single-module Android app (`:app`) written entirely in Kotlin with Jetpack Compose and Material 3 - no XML layouts.
- Domain: D&D 5e companion offering spell search, compendium browsing, dice rolling, and character/setting management.
- Architecture: Single-Activity (`MainActivity`) hosting the `SpellbindrApp` composable, MVVM ViewModels with
  unidirectional data flow, repositories over Room/DataStore, Kotlin Coroutines + Flow, and dependency injection via
  Hilt.
- Key libraries: Compose BOM, Navigation Compose with typed routes, Room, DataStore Preferences, Kotlinx Serialization,
  Coroutine Test utilities, Espresso/Compose UI testing, and Hilt navigation testing.

## Essential Rules

- **Compose best practices**: Stay aligned with Material Design 3, keep the clean architecture layering (domain, data,
  presentation), power async work with coroutines/Flow, rely on Hilt for DI, and uphold unidirectional data flow with
  proper state hoisting plus Compose Navigation for screen management.
- **UI authoring**: Use `remember`/`derivedStateOf` to memoize expensive calculations, keep modifiers ordered
  (layout → draw → semantics), follow composable naming conventions, provide meaningful previews, expose explicit error
  and loading states, lean on `MaterialTheme` for theming, honor accessibility requirements, and follow the app’s
  animation patterns.
- **Performance**: Minimize recomposition by supplying stable state and proper keys, prefer lazy containers
  (`LazyColumn`/`LazyRow`) for lists, ensure efficient image loading, guard against unnecessary state updates, respect
  lifecycle boundaries, and move heavy work to appropriate background dispatchers.
- **Testing**: Write unit tests for ViewModels/use cases with fake repositories and coroutine test dispatchers, cover UI
  flows with Compose testing APIs, and maintain meaningful coverage across new code.

## Repository Layout

- `app/src/main/kotlin/com/github/arhor/spellbindr`
    - `ui/feature/<feature>` – Screens, state holders, and feature-specific ViewModels (e.g., `characters`,
      `compendium`, `dice`, `settings`).
    - `ui/components` & `ui/theme` – Shared composables, typography/color definitions, AppTheme, and top-bar helpers (
      `AppTopBarControllerProvider`).
    - `navigation` – Route definitions (`AppDestination`) and bottom-nav configuration (`BottomNavItems`).
    - `data` – Entities (`CharacterEntity`), Room access (`data/local/db`), JSON asset loaders (`data/local/assets`
      implementing `InitializableStaticAssetDataStore`), repositories, and domain models.
    - `di` – Hilt modules wiring Room, DataStore, repositories, and static asset loaders.
    - `utils` – Cross-cutting helpers (logger, serializers, extensions).
- Resources: `app/src/main/res` (Compose previews rely on these), assets in `app/src/main/assets/{data,icons}` for SRD
  content.
- Tests: JVM tests in `app/src/test/kotlin`, instrumentation + Compose UI suites in `app/src/androidTest/kotlin` using
  `HiltApplicationTestRunner`.

## Build & Run

1. Requirements: Android Studio Koala Feature Drop (or newer) with JDK 17, Android SDK 36 platforms; ensure
   `local.properties` points to your SDK.
2. Sync the project (`File > Sync Project with Gradle Files`) so the Compose BOM and Hilt plugins resolve.
3. Common Gradle tasks (run from repo root):
    - `./gradlew assembleDebug` – build debug APK with Compose tooling.
    - `./gradlew installDebug` – deploy to the connected emulator/device.
    - `./gradlew lint` – Android lint (resolve/wjustify every warning).
    - `./gradlew testDebugUnitTest` – JVM tests.
    - `./gradlew connectedAndroidTest` – Instrumented UI tests (requires an API 33+ emulator with animations disabled
      for stability).
4. Android Studio run configurations should map to the same Gradle tasks; enable “Use Gradle JDK” (17) and “Optimize
   Gradle for smaller projects” for faster sync.
5. No external API keys are required.

## Testing

- **Unit tests (`app/src/test`)**: Use JUnit4 + `kotlinx-coroutines-test`’s `runTest`. Favor pure Kotlin tests for
  repositories, mappers, and utility logic.
- **Instrumented / Compose UI tests (`app/src/androidTest`)**: Run via `connectedAndroidTest`. Compose UI uses
  `androidx.compose.ui.test.junit4`. Espresso is available for hybrid flows. Hilt injection is enabled through
  `HiltAndroidRule` and `HiltApplicationTestRunner`.
- **Fakes & dispatchers**: Mock external dependencies with fake repositories or in-memory DAOs, and rely on coroutine
  test dispatchers/`runTest` to control timing deterministically.
- **Test data**: Prefer real SRD assets when possible; otherwise, use fixture builders under `data/model` or `utils`.
- **Expectations**: Every change should include at least one happy-path and one edge-case test. Run
  `./gradlew lint testDebugUnitTest` locally (and `connectedAndroidTest` whenever navigation/UI state changes) before
  opening a PR.

## Architecture & State Management

- **Entry point**: `MainActivity` installs the splash screen and hosts `SpellbindrApp`. `SpellbindrAppViewModel`
  preloads static assets via injected `InitializableStaticAssetDataStore`s before dismissing the splash.
- **Navigation**: Typed destinations defined in `AppDestination` (sealed `@Serializable` hierarchy). Use `NavHost` with
  `NavGraphBuilder.composable<AppDestination.*>` helpers. Extend `BottomNavItems` only when adding top-level
  destinations.
- **State flow**: ViewModels expose immutable state via `StateFlow`/`Flow`, collected in composables with
  `collectAsState()`. Keep mutation inside ViewModels; hoist UI state to maintain unidirectional data flow.
- **Feature structure**: Each feature package typically contains the composable `Route`, ViewModel, UI state models, and
  UI-specific helpers. Keep previews close to the composable definition.
- **Data layer**:
    - Room (`SpellbindrDatabase`, `CharacterDao`) holds player-created content.
    - Static SRD content is stored under `app/src/main/assets/data` and loaded through asset data stores; new loaders
      should implement `InitializableStaticAssetDataStore`.
    - Preferences and lightweight lists (e.g., favorites) use `FavoriteSpellsDataStore` backed by DataStore
      Preferences + Kotlinx Serialization.
    - Repositories (e.g., `CharacterRepository`, `SpellRepository`) expose suspend functions or Flows and handle mapping
      between entities (`CharacterEntity`) and domain models (`Character`, `CharacterSheet`).
- **DI**: Hilt provides all dependencies. Annotate Android entry points with `@AndroidEntryPoint` / `@HiltViewModel`,
  and register bindings in `di/*.kt`. Prefer constructor injection for testability.

## Coding Style & Conventions

- `.editorconfig` enforces UTF-8, LF, 4-space indent, 120-char width, and Kotlin official style (
  `kotlin.code.style=official`).
- Naming: PascalCase for types and composables, camelCase for members, `SCREAMING_SNAKE_CASE` for constants. Avoid star
  imports (threshold 99).
- Compose:
    - All new UI must be Compose; reuse building blocks in `ui/components` and follow Material 3 components.
    - Keep composables pure; side-effects belong in `LaunchedEffect`, `DisposableEffect`, or ViewModels.
    - Use `remember`/`derivedStateOf` for expensive derived data, and keep modifier order consistent (layout →
      graphics → semantics).
    - Model loading/error states explicitly and surface them through UI state.
    - Provide previews when practical and colocate them with the composable; ensure previews match `MaterialTheme`.
    - Keep accessibility in mind (content descriptions, semantics, focus order) and respect existing animation
      patterns.
- Organize new code under the existing package layout (`ui.feature`, `data.repository`, etc.) and keep tests in
  mirroring directories.
- Logging: prefer the structured logger from `utils/Logger` for new logging statements.
- Lint: run `./gradlew lint` and fix warnings or document rationale before submission.

## Key Libraries & Usage Patterns

- **Jetpack Compose BOM** – align versions across `material3`, icons, tooling.
- **Navigation Compose** – typed routes using `kotlinx.serialization` `@Serializable` destinations.
- **Material 3** – `AppTheme` adapts to system & stored theme preference; update `SettingsViewModel` when adding theme
  toggles.
- **Hilt** – `MainApplication` is annotated with `@HiltAndroidApp`; register modules for Room, DataStore, repositories,
  and asset loaders.
- **Room** – Entities live in `data/CharacterEntity.kt`; migrations should keep schema consistent with stored assets.
- **DataStore Preferences** – For lightweight persistence like favorites/theme; keep keys centralized and avoid large
  payloads.
- **Kotlinx Serialization** – Used heavily for SRD JSON; keep serializers in `utils` to share logic (e.g.,
  `ReferenceSerializer`, `CaseInsensitiveEnumSerializer`).
- **Coroutines & Flow** – Always leverage structured concurrency (`viewModelScope`) and `runTest` for deterministic unit
  tests.

## Security & Data Handling

- All SRD data is bundled locally; no network requests or secrets are currently required.
- Do **not** commit credentials or personalized data. If a new integration needs secrets, load them through
  `local.properties` or CI secrets and gate usage behind build configs.
- User-generated content (characters, favorites) remains on-device inside Room/DataStore. Follow Android privacy best
  practices if exporting/sharing data features are added.

## Git & Contribution Workflow

- Follow Conventional Commits (`feat:`, `fix:`, `refactor:`, `chore:`, etc.); keep each commit scoped to one concern.
- Before submitting a PR:
    1. `./gradlew lint testDebugUnitTest` (and `connectedAndroidTest` when UI/navigation changes).
    2. Update docs/tests relevant to your change.
    3. Capture manual test notes (device/emulator, Gradle tasks) and include screenshots/screen recordings for UI
       changes.
- PRs should link issues, describe the change, list verification steps, and highlight any known limitations.
- Never rewrite or delete unrelated local changes; if unexpected edits appear, coordinate with the maintainers/user
  before proceeding.

## Quick Reference Paths

- Entry: `app/src/main/kotlin/com/github/arhor/spellbindr/MainActivity.kt`
- Root composable: `ui/SpellbindrApp.kt` & `SpellbindrAppViewModel.kt`
- Navigation contracts: `navigation/AppDestinations.kt`
- Data: `data/local/db/SpellbindrDatabase.kt`, `data/repository/*.kt`, `data/local/assets/*.kt`
- DI modules: `di/*.kt`
- Custom instrumentation runner: `app/src/androidTest/kotlin/com/github/arhor/spellbindr/HiltApplicationTestRunner.kt`

Maintain this guide as project practices evolve so future agents can contribute confidently.
