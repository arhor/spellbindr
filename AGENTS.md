# Spellbindr - Agent Guide

## Purpose & scope

This document is the living operating manual for anyone (human or agent) touching the Spellbindr codebase. Follow it
when you work inside `:app` so that architecture, layering, and workflows stay predictable. Treat every change as
needing context from the sections below.

## Quick facts

- Single-module Android app (`:app`) written in Kotlin + Jetpack Compose with Material 3.
- Entry point: `SpellbindrAppActivity` (`ui/SpellbindrAppActivity.kt`) hosts the nav graph and app chrome.
- DI: Hilt drives the app module, domain bindings, static asset loaders, and test overrides.
- Navigation: Compose navigation with typed `AppDestination` sealed classes and a shared bottom bar.
- Data: local-only (Room + DataStore + preloaded JSON assets); no networking layer.
- Testing: JVM unit tests (JUnit4 + Truth + MockK + ArchUnit + `kotlinx.coroutines.test`) plus Hilt-powered Compose UI
  tests.

## Architecture overview

### Application shell

- `SpellbindrAppActivity` installs the splash screen, wires `SpellbindrAppViewModel`, and composes
  `SpellbindrAppNavGraph` inside a `Scaffold` with `AppTopBar`/`AppBottomBar`.
- `SpellbindrAppViewModel` (`ui/SpellbindrAppViewModel.kt`) tracks `StateFlow<State>` for initial delay, asset
  preloading (`InitializableStaticAssetDataStore` set) and the current theme mode.
- `AppTheme` (`ui/theme/AppTheme.kt`) switches between `AppLightColorScheme` and `AppDarkColorScheme`; top/bottom bars
  and other shared chrome are supplied by `ProvideTopBarState`/`LocalTopBarState` plus `AppBottomBar`.

### Navigation & screen structure

- Typed destinations live in `ui/navigation/AppDestinations.kt`. Each `composable<AppDestination.*>` in
  `ui/navigation/AppNavGraph.kt` resolves arguments via `toRoute()` and instantiates corresponding `*Route` composables.
- Top-level destinations are defined in `BottomNavItems` and drive the bottom navigation on `SpellbindrAppActivity`.
  Routes that require custom chrome (e.g., sheet editor, spell detail) call `ProvideTopBarState`.
- Feature entry points live under `ui/feature/<feature>`. Each feature typically exposes a `*Route` that wires a
  `@HiltViewModel`, UI state, and callbacks (e.g., `CharactersListRoute`, `CompendiumRoute`, `DiceRollerRoute`,
  `SettingsRoute`, `CharacterSheetRoute`, `CharacterEditorRoute`, `CharacterSpellPickerRoute`).

### Layering & data flow

- UI ➜ ViewModel: `StateFlow` is exposed directly, `stateIn` + `SharingStarted.WhileSubscribed` keeps flows active
  without manual collection. `MutableSharedFlow` is used for one-off events/effects (e.g., `CharacterEditorEvent`,
  `CharacterSheetEffect`).
- ViewModel ➜ Domain: ViewModels talk to use cases (`domain/usecase/*`). Lightweight wrappers expose repository data (
  e.g., `ObserveCharacterSheetsUseCase`), while more complex use cases (`SearchAndGroupSpellsUseCase`,
  `ComputeDerivedBonusesUseCase`, `ToggleSpellSlotUseCase`) encapsulate business rules.
- Domain ➜ Data: Domain repositories (`domain/repository/*`) are bound to concrete implementations in
  `data/repository/*` via Hilt modules (e.g., `CharactersDomainModule`, `SpellsDomainModule`, `StaticAssetsModule`).
  Mapping helpers live in `data/mapper`.
- Data ➜ Persistence: Room (`data/local/db`) stores characters and a favorites table, DataStore (`preferences`) stores
  theme/favorites for quick access, and static JSON assets (`assets/data/*.json`) are parsed through
  `StaticAssetDataStoreBase`.

## Repository map
```
app/src/main/kotlin/com/github/arhor/spellbindr
├─ SpellbindrApp.kt / SpellbindrAppActivity.kt / ui/SpellbindrAppViewModel.kt
├─ ui/
│  ├─ components/        # shared Compose building blocks (AppTopBar, AppBottomBar, TopBarState, etc.)
│  ├─ navigation/        # typed AppDestination, SpellbindrAppNavGraph, BottomNavItems
│  ├─ theme/             # AppTheme, color schemes, typography, shapes
│  └─ feature/
│     ├─ characters/     # character list, editor, sheet, spell picker, reducers, helpers
│     ├─ compendium/     # spells, races, traits, filters, spell detail
│     ├─ dice/           # Dice roller UI + intents/state models
│     └─ settings/        # theme settings UI
├─ domain/
│  ├─ model/             # domain models (Character, Spell, Trait, ThemeMode, etc.)
│  ├─ repository/        # interfaces (CharacterRepository, SpellsRepository, ThemeRepository, etc.)
│  └─ usecase/            # business logic (observe/save/load character sheets, search spells, theme toggles, validation, etc.)
├─ data/
│  ├─ local/
│  │  ├─ db/             # Room entities, DAOs, database/migrations, converters
│  │  └─ assets/         # Json loaders (StaticAssetDataStoreBase, SpellAssetDataStore, etc.)
│  ├─ mapper/            # domain ↔ data extension helpers
│  ├─ model/             # Kotlinx-serializable models mirroring JSON assets
│  └─ repository/        # repository implementations that bridge Domain ↔ persistence
├─ di/                   # Hilt modules for AppModule, database, feature-specific bindings, static assets
└─ utils/                # Logger + helper extensions (strings, maps, serializers)
```

Assets:
```
app/src/main/assets/data/    # SRD JSON (spells, races, traits, features, alignments, etc.)
app/src/main/assets/icons/   # raw icon assets (used in Compose upstream)
```

## Engineering conventions & patterns

- Naming: PascalCase for types/composables, camelCase for members, `SCREAMING_SNAKE_CASE` for constants. Keep Compose
  modifiers grouped (layout → draw → semantics).
- Compose state: prefer `@Immutable` data classes, `@Stable` ViewModels, and expose UI state via `StateFlow`. Side
  effects go through `MutableSharedFlow` when relaying one-off events/effects.
- ViewModels: annotate with `@HiltViewModel`, inject use cases/repositories, scope flows with `viewModelScope`, and call
  `stateIn` when exposing derived state to avoid manual collectors.
- Reducers: complex form interactions use dedicated reducers (e.g., `CharacterEditorReducer`, `SpellListStateReducer`)
  to keep state transitions reproducible.
- Top bar: dynamic configuration happens via `ProvideTopBarState` inside feature screens; `SpellbindrAppActivity` falls
  back to `defaultTopBarConfig`.
- Navigation: `SpellbindrAppNavGraph` uses `composable<AppDestination.*>` generics. Don’t mix string routes or manual
  argument parsing; use `toRoute()` helpers provided by Compose navigation.
- Logging: use `Logger.createLogger` helpers from `utils/Logger.kt` when tracing async work.
- Serialization: `Json` configured via `di/AppModule` with `ignoreUnknownKeys = true` and `classDiscriminator = "type"`.
  Data models under `data/model` mirror the SRD JSON and are mapped to domain models via `data/mapper`.
- Persistence: Room uses `CharacterEntity` + `Converters` for complex fields. Character sheets are stored as
  `CharacterSheetSnapshot` inside the entity.
- DataStore qualifiers: use `@AppSettingsDataStore` and `@FavoritesDataStore` when injecting `DataStore<Preferences>`.
  Theme preferences live in `ThemeRepositoryImpl`, favorites in `FavoritesRepositoryImpl`.

## Data & persistence checklist

1. Room (app/src/main/kotlin/com/github/arhor/spellbindr/data/local/db):
    - `CharacterEntity` retains metadata and full snapshot (`manualSheet`) via `CharacterSheetSnapshot`.
    - `SpellbindrDatabase` exposes DAOs plus migrations (1→2 adds `manualSheet`, 2→3 creates `favorites` table).
    - `CharacterDao` flow-based `getAllCharacters`/`getCharacterById`; `saveCharacter` uses
      `OnConflictStrategy.REPLACE`.
2. DataStore (preferences):
    - `AppModule` supplies two DataStore instances (`app_settings.preferences_pb`, `favorites.preferences_pb`) with
      corruption handlers.
    - `ThemeRepositoryImpl` maps between `ThemeMode` and serialized `AppThemeMode`.
    - `FavoritesRepositoryImpl` stores favorites as string sets per `FavoriteType` (currently spells);
      `SpellsRepositoryImpl` delegates favorite flipping to it.
3. Static assets (app/src/main/assets/data):
    - `StaticAssetDataStoreBase` (and derived stores) load JSON via `Json.decodeFromStream` and expose
      `StateFlow<List<T>?>`.
    - `StaticAssetsModule` binds each `InitializableStaticAssetDataStore` into a Hilt `Set` so `SpellbindrAppViewModel`
      can `awaitAll`.
    - Asset directories include spells, races, traits, features, equipment, languages, backgrounds, alignments,
      monsters, rules, etc.

## UI system

- Material 3 Compose: `AppTheme` applies `AppColorScheme`, `AppTypography`, and `AppShapes`. Previews live next to
  composables.
- Shared components: `ui/components` hosts reusable UI (AppTopBar/AppBottomBar, `SelectableGrid`, `GradientDivider`,
  `NavButtons`, character-related inputs).
- Compose previews should wrap `AppTheme` with a `isDarkTheme` variation.
- Screens:
    * Characters: List → `CharactersListRoute/ViewModel`, `CharacterSheetRoute/ViewModel`, `CharacterEditorRoute`,
      `CharacterSpellPickerRoute`.
    * Compendium: `CompendiumRoute/ViewModel` (with nested sections for spells, races, traits, alignments);
      `SpellDetailsRoute/ViewModel`.
    * Dice: `DiceRollerRoute/ViewModel` uses `DiceRollerState`, `DiceRollerIntent`, and randomness purely on the
      ViewModel’s `Random`.
    * Settings: `SettingsRoute/ViewModel` toggles `ThemeMode` via DataStore.
- Compose navigation ensures top-level tabs reset on reentry; detail routes are pushed via `controller.navigate`.
- Use `ProvideTopBarState` for screens requiring custom titles or actions; fallback config in
  `SpellbindrAppActivity.defaultTopBarConfig`.

## Testing & quality checklist

- JVM unit tests (`app/src/test`):
    * Depend on JUnit4, Truth, MockK, ArchUnit, `kotlinx.coroutines.test`.
    * `MainDispatcherRule` swaps `Dispatchers.Main`.
    * Place tests near the code they exercise (`domain/usecase`, `ui/feature`, `data/repository`, etc.).
- Instrumentation + Compose UI tests (`app/src/androidTest`):
    * Custom runner `HiltApplicationTestRunner` (registered in `app/build.gradle.kts`) ensures `HiltTestApplication`.
    * Tests uninstall `AppModule`/`DatabaseModule` and install `TestDataModule` for in-memory DB/DataStore.
    * Compose tests use `createAndroidComposeRule<SpellbindrAppActivity>` and `HiltAndroidRule`.
- Formatting & lint:
    * Formatting follows `.editorconfig` (4 spaces, 120 cols, Kotlin style). No detekt/ktlint enforced.
    * Keep imports explicit (editorconfig threshold 99).
- Recommended commands:
    1. `./gradlew testDebugUnitTest` (JVM unit tests).
    2. `./gradlew connectedAndroidTest` (Compose/instrumented UI tests).
    3. `./gradlew assembleDebug` (quick build smoke).
    4. `./gradlew lint` (Android lint).
    6. `./gradlew help` (after SDK/script setup) to confirm environment.

## Tooling & workflows

- SDK provisioning: run `./run/setup.sh` in clean environments (script installs CLI tools, platform 36, build-tools
  36.0.0, updates `ANDROID_HOME`, and initializes submodules if any).
- Gradle: `app/build.gradle.kts` uses BOM for Compose (`androidx.compose:compose-bom`) and submits dependencies via
  `gradle/libs.versions.toml`. Always let Gradle sync bring in versions before editing.
- Hilt: `di/` includes `AppModule` (Json + DataStore), `DatabaseModule`, feature domain modules, and
  `StaticAssetsModule`. When adding dependencies/registering new repos, bind them via Hilt modules so they can be
  injected in ViewModels or other modules.
- Scripts: there is no `./scripts/setup-android-sdk.sh`; the current helper is `./run/setup.sh`. Keep documentation (
  README/AGENTS) in sync if this changes.
- Releases: there is no CI configuration in the repo—assume manual `./gradlew assembleRelease` (if needed) and follow
  standard Play store/Gradle release flows externally.

## Agent operating rules

- Match existing architecture/patterns; avoid speculative refactors. Keep churn minimal and follow the existing
  layering.
- Always keep domain models pure; add `data/mapper` helpers when introducing new serialization/model types.
- Update or add tests when behavior changes. Unit tests should live near the logic being tested and reuse
  `MainDispatcherRule` when coroutines are involved.
- Keep `ViewModel` state sealed/immutable and update `SpellbindrAppNavGraph` when new routes are introduced.
- New static assets should plug into `StaticAssetsModule` so `SpellbindrAppViewModel` can load them before the UI draws.
- When UI state requires asynchronous confirmation (e.g., saved states, dialogs), lean on `MutableSharedFlow`/
  `StateFlow` patterns already used in characters/compendium features.
- If you add new dependencies, register them with Hilt (or mark them in `TestDataModule` for instrumentation tests)
  before use.
- Document key decisions or assumptions you make (see Decision log below).

## Common tasks (playbooks)

1. **Add a new screen/feature**
    - Create the `*Route` composable under `ui/feature/<feature>` that provides the UI + wires `ProvideTopBarState`.
    - Add a `@HiltViewModel` in the same package exposing `StateFlow`/`SharedFlow` and inject necessary use
      cases/repositories.
    - Update `AppDestination`/`BottomNavItems` in `ui/navigation/AppDestinations.kt` (only add to `BottomNavItems` if it
      should appear in the bottom bar).
    - Wire the route in `SpellbindrAppNavGraph` with `composable<AppDestination.New> { ... }`, resolve arguments via
      `it.toRoute()` and pass navigation callbacks.
    - Ensure `SpellbindrAppActivity` top bar config defaults catch the new destination unless the screen pushes its own
      config via `ProvideTopBarState`.

2. **Add a new repository method / domain operation**
    - Define the interface in `domain/repository/*` and add any supporting domain models in `domain/model`.
    - Implement it inside `data/repository/*`, use existing assets/DAOs, and add mapping helpers inside `data/mapper` if
      needed.
    - Bind the implementation via the appropriate Hilt module under `di/` (e.g., `CharactersDomainModule`,
      `SpellsDomainModule`).
    - Add a use case in `domain/usecase/` if the operation is shared across features, and call that use case from
      ViewModels instead of raw repositories where reasonable.
    - Update tests (unit or instrumentation) covering the repository/use case behavior.

3. **Add a new static asset source**
    - Place the JSON under `app/src/main/assets/data`.
    - Create an `*AssetDataStore` in `data/local/assets` that extends `StaticAssetDataStoreBase<T>` and exposes
      `StateFlow<List<T>?>`.
    - Bind it to `InitializableStaticAssetDataStore` inside `di/StaticAssetsModule` (annotate with `@IntoSet`).
    - Consume the asset through a repository (or add use cases) so it is available to UI logic; the asset will
      automatically preload because `SpellbindrAppViewModel` awaits all registered loaders.

4. **Add a reusable UI component**
    - Implement the composable under `ui/components` (or a subfolder) with a `@Preview` that wraps `AppTheme`.
    - Keep Modifiers ordered (layout → draw → semantics), annotate the component/state as `@Immutable`/`@Stable` when
      needed.
    - If it needs to coordinate with the top bar (e.g., overlays, actions), use `LocalTopBarState`/`ProvideTopBarState`.
    - Add unit tests or Compose tests if the component contains nontrivial logic (e.g., dynamic animations, custom state
      handling).

5. **Add or adjust top/bottom bar behavior**
    - Update `ProvideTopBarState` usage in the relevant route to describe title/navigation/actions.
    - Modify `SpellbindrAppActivity.defaultTopBarConfig` only if the destination should display a default chrome, not
      for transient states (use `ProvideTopBarState` from the route instead).
    - For bottom navigation changes, update `BottomNavItems`, ensure `AppBottomBar` still composes exactly four tabs,
      and confirm `AppNavGraph` handles the splash screen/back stack correctly.

## Decision log

- **Single-Activity Compose shell**: `SpellbindrAppActivity` + `SpellbindrAppNavGraph` orchestrate navigation,
  top/bottom bars, and splash handling so feature modules stay UI-only.
- **Static asset preloading**: `InitializableStaticAssetDataStore` + `SpellbindrAppViewModel` preload every JSON asset
  before the splash screen dismisses, ensuring reads and favorites start with cached data.
- **Domain/usecase layering**: ViewModels rarely talk directly to repositories; use cases reside under
  `domain/usecase` (even simple wrappers) to keep feature code testable and decoupled.
