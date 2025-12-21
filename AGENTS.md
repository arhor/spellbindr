# Spellbindr – Agent Guide

## Purpose & scope
This file is the operating manual for contributors and coding agents working in this repo. It documents the **current**
architecture, layout, and workflows. Follow it when making changes in any folder under the repo root.

## At-a-glance facts
- Single-module Android app (`:app`) written in Kotlin + Jetpack Compose (no XML layouts).
- Min SDK **33**, target/compile SDK **36** (`app/build.gradle.kts`).
- Single-Activity app (`MainActivity`) hosting the root `SpellbindrApp` composable.
- DI: Hilt. Navigation: Navigation Compose + typed routes.
- Local-only data: Room + DataStore + JSON assets. No networking layer.

## Repository map
```
app/src/main/kotlin/com/github/arhor/spellbindr
├─ MainActivity.kt / MainApplication.kt
├─ ui/
│  ├─ SpellbindrApp.kt / SpellbindrAppViewModel.kt
│  ├─ navigation/ (AppDestinations, AppNavGraph)
│  ├─ components/ (shared composables: top/bottom bars, etc.)
│  ├─ feature/ (feature screens + view models)
│  └─ theme/ (AppTheme, colors, typography)
├─ domain/
│  ├─ model/ (domain models)
│  ├─ repository/ (interfaces)
│  └─ usecase/ (application use cases)
├─ data/
│  ├─ local/
│  │  ├─ assets/ (JSON asset loaders)
│  │  └─ db/ (Room entities/DAO/database)
│  ├─ mapper/ (data ↔ domain mapping)
│  ├─ model/ (serialization models for assets)
│  └─ repository/ (implementations)
├─ di/ (Hilt modules)
└─ utils/ (Logger, serializers, extensions)

app/src/main/assets/
├─ data/ (SRD JSON)
└─ icons/
```

## Architecture & patterns
### UI + state management
- MVVM with unidirectional data flow.
- ViewModels expose immutable state via `StateFlow` (`stateIn`, `SharingStarted.WhileSubscribed`) and UI collects using
  `collectAsStateWithLifecycle`.
- UI state is usually modeled as sealed interfaces (`Loading` / `Content` / `Error`).
- One-off events/effects use `MutableSharedFlow` (e.g., `CharacterEditorEvent`, `CharacterSheetEffect`).
- Feature entry points are `*Route` composables in `ui/feature/**` that wire `ViewModel` + screen UI and provide top-bar
  configuration via `ProvideTopBarState`.

### Navigation
- Typed destinations are defined in `ui/navigation/AppDestinations.kt` as a `@Serializable` sealed class.
- `SpellbindrAppNavGraph` uses `composable<AppDestination.*>` with `toRoute()` for argument extraction.
- Bottom navigation items are defined in `BottomNavItems`; add to that list only for new top-level destinations.

### Top/bottom bars
- App chrome is centralized in `SpellbindrApp`.
- Top bar uses `LocalTopBarState` + `ProvideTopBarState` (not a controller provider).
- Bottom bar uses `AppBottomBar`, driven by `BottomNavItems`.

### Domain/data layering
- `domain/` defines interfaces and pure models; `data/` implements and maps.
- Mapping lives in `data/mapper` (extension functions like `toDomain`/`toEntity`).
- Use cases in `domain/usecase` encapsulate common operations; some ViewModels use use cases, others call repositories
  directly depending on complexity.

## Data & persistence
- **Room**: `SpellbindrDatabase` stores `CharacterEntity` and a `FavoriteEntity` table (currently favorites are not read
  from Room in repositories).
- **DataStore (Preferences)**:
  - App settings (theme) via `ThemeRepositoryImpl` and `@AppSettingsDataStore`.
  - Favorites via `FavoritesRepositoryImpl` and `@FavoritesDataStore`.
- **Static assets**: JSON in `app/src/main/assets/data` loaded through `StaticAssetDataStoreBase` implementations in
  `data/local/assets`. The app preloads all registered asset stores in `SpellbindrAppViewModel`.
- **Serialization**: Kotlinx Serialization; `Json` is provided in `di/AppModule` with `ignoreUnknownKeys = true`.

## Concurrency & error handling
- Coroutines run in `viewModelScope`; background IO uses `Dispatchers.IO` in repositories/asset loaders.
- Error surfaces are modeled in UI state; many ViewModels emit `Error` states/messages and continue with best-effort
  content when possible.

## UI system
- Material 3 Compose; theme is defined in `ui/theme` via `AppTheme`.
- Shared UI building blocks live in `ui/components`.
- Compose previews exist near components and use `AppTheme`.

## Testing & quality
- **Unit tests**: `app/src/test` using JUnit4 + Truth + MockK + `kotlinx-coroutines-test`.
  - `MainDispatcherRule` for swapping `Dispatchers.Main` in tests.
- **Instrumentation/Compose UI tests**: `app/src/androidTest` with `HiltApplicationTestRunner`.
- No detekt/ktlint configured. Formatting is driven by `.editorconfig` (4 spaces, 120 cols, Kotlin official style).

### Common Gradle tasks
- `./gradlew assembleDebug`
- `./gradlew testDebugUnitTest`
- `./gradlew connectedAndroidTest`
- `./gradlew lint`
- `./gradlew resolveAllDependencies` (warms caches; defined in root `build.gradle.kts`)

### SDK setup script (for CI/agents)
- `./scripts/setup-android-sdk.sh` installs SDK 36 + build tools 36.0.0 and writes `local.properties`.

## Engineering conventions
- Naming: PascalCase for types/composables, camelCase for members, `SCREAMING_SNAKE_CASE` for constants.
- Avoid star imports (`.editorconfig` sets threshold to 99).
- Compose: keep modifiers ordered (layout → draw → semantics); model loading/error states explicitly.
- Prefer `@Immutable`/`@Stable` for UI state models where appropriate (see existing ViewModels).

## Agent operating rules
- Match existing patterns and keep changes minimal.
- Update or add tests when behavior changes; prefer adding unit tests near `domain/usecase` or UI tests for composables.
- Avoid speculative refactors; focus on the requested change.
- When adding new data sources or models, add mapping extensions in `data/mapper` and keep domain models clean.

## Common tasks (playbooks)
### Add a new screen/feature
1. Create a `*Route` composable and screen UI in `ui/feature/<feature>`.
2. Add a `ViewModel` with `StateFlow` UI state (use sealed UI states if loading/error needed).
3. Add a new `AppDestination` entry and wire it in `SpellbindrAppNavGraph`.
4. If it’s top-level, add a `BottomNavItem`.
5. Provide top bar config via `ProvideTopBarState` in the `*Route`.

### Add a new repository method
1. Add or update the interface in `domain/repository`.
2. Implement in `data/repository` using existing mappers.
3. If a new dependency is needed, bind/provide it in `di/`.
4. Add/update use cases in `domain/usecase` if the operation is re-used across ViewModels.

### Add a new static asset source
1. Add JSON under `app/src/main/assets/data`.
2. Create a `*AssetDataStore` in `data/local/assets` extending `StaticAssetDataStoreBase`.
3. Bind it in `di/StaticAssetsModule` as an `InitializableStaticAssetDataStore` so the app preloads it.

### Add a reusable UI component
1. Add a composable in `ui/components` with previews in the same file.
2. Use `AppTheme` in previews and keep modifiers ordered.
3. If it needs app-wide state (top bar overlays, etc.), integrate with `LocalTopBarState`.
