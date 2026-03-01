# Spellbindr app overview

This document is a durable, code-evidenced map of the repository for engineers and agents.
It focuses on architecture, feature flows, and implementation boundaries that affect non-trivial changes.

## Product purpose and user value

Spellbindr is an Android app for D&D 5e workflows:

- Browse SRD reference content (spells, conditions, alignments, races).
- Create/manage characters (list, guided setup, manual editor, character sheet).
- Roll dice with check modes and amount/group roll breakdowns.
- Persist lightweight user preferences (theme mode) and favorites.

Evidence highlights:

- App summary and feature set in `README.md`.
- Navigation destinations in `app/src/main/kotlin/com/github/arhor/spellbindr/ui/navigation/AppDestinations.kt`.
- Screen routing in `app/src/main/kotlin/com/github/arhor/spellbindr/ui/navigation/AppNavGraph.kt`.

## High-level architecture

The repository is layered and modular:

- `:core:domain`: domain models, repository contracts, use cases.
- `:data:*`: concrete repository and storage implementations.
- `:feature:*`: Compose feature UI (`Route` / `Screen` / `ViewModel` / intents/effects).
- `:app`: application shell, DI wiring composition, root navigation, architecture tests.

Cross-module dependency guardrails are enforced in tests:

- Feature modules must not depend on `:data:*` or `:app`.
- Domain must not depend on feature/data/app.
- Data must not depend on UI.

Evidence highlights:

- `settings.gradle.kts` module list.
- `app/src/test/kotlin/com/github/arhor/spellbindr/architecture/ModuleDependencyRulesTest.kt`.
- `app/src/test/kotlin/com/github/arhor/spellbindr/architecture/ArchitectureRulesTest.kt`.

## Module structure and responsibilities

### App shell (`:app`)

- Entry points:
  - `SpellbindrApplication` (`@HiltAndroidApp`) starts asset bootstrap.
  - `MainActivity` installs splash + sets Compose content.
  - `SpellbindrApp` owns scaffold, top/bottom bars, nav host, app-level snackbar.
- Top-level destinations and graph wiring live in `AppDestinations.kt` / `AppNavGraph.kt`.

### Core modules

- `:core:common`: shared utility/logging helpers.
- `:core:domain`:
  - Repository interfaces (`domain/repository/*Repository.kt`).
  - Use cases (`domain/usecase/*UseCase.kt`) mediating UI ↔ repository interactions.
  - Domain models (`domain/model/*`).
- `:core:ui`: shared Compose theme and reusable UI components.
- `:core:ui-spells`: shared spell-specific UI components.
- `:core:testing`: shared test fakes/helpers used by module tests.

### Data modules

- `:data:compendium`:
  - Loads JSON assets (`src/main/assets/data/*.json`) through typed `AssetDataStoreBase` stores.
  - `DefaultAssetBootstrapper` initializes critical/deferred stores in app scope.
  - Repository implementations expose domain `Loadable` streams.
- `:data:character`:
  - Room DB (`SpellbindrDatabase`, DAOs/entities/converters).
  - `CharacterRepositoryImpl` maps between Room entities and domain character models/snapshots.
- `:data:favorites`:
  - DataStore-backed favorite IDs, keyed by `FavoriteType`.
- `:data:settings`:
  - DataStore-backed `AppSettings` (theme mode).

### Feature modules

- `:feature:character`: character list, editor, guided setup, sheet, spell picker.
- `:feature:compendium`: compendium home and sub-features for spells/details, conditions, alignments, races.
- `:feature:dice`: dice roller screen, intents, state, roll model components.
- `:feature:settings`: theme preference screen.

## Main feature areas, screens, and user flows

## 1) Character management flow

1. Start at `AppDestination.CharactersHome` (`CharactersListRoute`).
2. Select character → navigate to `CharacterSheet` with args.
3. Create character:
   - Guided setup (`GuidedCharacterSetupRoute`) then auto-navigate to `CharacterSheet`.
   - Manual entry (`CharacterEditorRoute`).
4. From sheet:
   - Open spell details.
   - Open spell picker and return selection via `SavedStateHandle` key.
   - Open full editor.

Key files:

- `app/.../AppNavGraph.kt`
- `feature/character/.../list/*`
- `feature/character/.../guided/*`
- `feature/character/.../sheet/*`

## 2) Compendium browsing flow

1. `CompendiumRoute` displays sections.
2. Section click maps to typed destination (`Spells`, `Conditions`, `Alignments`, `Races`).
3. Spells supports query, class filters, and favorite filtering.
4. Spell click navigates to `SpellDetails`.

Key files:

- `app/.../AppNavGraph.kt`
- `feature/compendium/.../CompendiumRoute.kt`
- `feature/compendium/.../spells/*`
- `feature/compendium/.../spelldetails/*`

## 3) Dice flow

- `DiceRollerRoute` + `DiceRollerViewModel` manage check mode (normal/adv/disadv) and configurable dice groups.
- Results include latest roll and optional details sheet models.

Key files:

- `feature/dice/.../DiceRollerRoute.kt`
- `feature/dice/.../DiceRollerViewModel.kt`
- `feature/dice/.../model/*`

## 4) Settings flow

- `SettingsRoute` observes `AppSettings` and dispatches `ThemeModeSelected` intents.
- Theme updates propagate to app shell via `SpellbindrAppViewModel.observeSettings` and `AppTheme`.

Key files:

- `feature/settings/.../SettingsRoute.kt`
- `feature/settings/.../SettingsViewModel.kt`
- `app/.../SpellbindrAppViewModel.kt`

## State management and data flow

The feature entry pattern follows the MVI dispatch contract documented in `docs/mvi-dispatch-contract.md`:

- `Screen` entry API accepts `state` + `dispatch` (no feature-level `onX` callback API).
- `Route` collects `uiState`, intercepts navigation intents, forwards state intents to `vm.dispatch`.
- `ViewModel` exposes `uiState` (`StateFlow`) and optional `effects` (`SharedFlow`).

Concrete example:

- `SpellsRoute` intercepts `SpellsIntent.SpellClicked` for navigation, forwards other intents to `SpellsViewModel`.

General data path:

1. UI dispatches `Intent`.
2. ViewModel updates UI state and/or invokes a domain use case.
3. Use case combines/filters repository flows.
4. Repository implementation reads/writes Room/DataStore/assets and emits domain `Loadable` states.

## Persistence, assets, repositories, networking, and external integrations

### Persistence

- Room DB for character data in `:data:character`:
  - `SpellbindrDatabase`, `CharacterDao`, entity/converter classes.
- DataStore for settings (`:data:settings`) and favorites (`:data:favorites`).

### Assets and bootstrap

- Compendium reference data is JSON under `data/compendium/src/main/assets/data`.
- Typed asset stores (`*AssetDataStore`) decode JSON via Kotlinx Serialization.
- `DefaultAssetBootstrapper` runs stores by `AssetLoadingPriority` (critical/deferred) and exposes readiness/error state.
- App startup:
  - `SpellbindrApplication.onCreate()` calls `assetBootstrapper.start()`.
  - `SpellbindrAppViewModel` observes bootstrap + settings and gates splash/app readiness.

### Repositories

- Contracts live in `core/domain/src/main/kotlin/com/github/arhor/spellbindr/domain/repository`.
- Implementations live in `data/*/src/main/kotlin/.../repository`.

### Networking / external services

- No explicit HTTP client stack (Retrofit/OkHttp/Ktor) appears in Gradle dependencies or repository implementations.
- Current external integration appears to be local Android platform storage APIs, JSON parsing, and Hilt/Compose/Navigation/Room/DataStore.

## Build, test, and developer workflow

### Build/test commands (project-level)

- `./gradlew assembleDebug`
- `./gradlew lintDebug test testDebugUnitTest --stacktrace`
- `./gradlew connectedDebugAndroidTest` (device/emulator)

### CI

- `.github/workflows/android-ci.yml` runs `lintDebug test testDebugUnitTest assembleRelease`.
- On PRs, reusable action `.github/actions/assemble-debug-apk/action.yml` assembles + uploads debug APK artifact.

### Environment/bootstrap

- JDK 17 and Android SDK 36 / build-tools 36.0.0.
- `run/setup.sh` automates Linux SDK setup + `git submodule update --init --recursive`.

### Screenshot workflow

- Compose screenshot testing enabled (notably in `:feature:character` and `:core:ui`).
- `run/export-preview-screenshot.sh` exports generated preview PNGs from module screenshot references.

## Key conventions, patterns, and constraints

- Naming patterns used broadly:
  - `*Screen`, `*Route`, `*ViewModel`, `*Intent`, `*Effect`, `*UseCase`, `*Repository`/`*RepositoryImpl`.
- Feature-entry dispatch contract (`docs/mvi-dispatch-contract.md`) is enforced by architecture tests in `:app`.
- Hilt DI modules provide app infrastructure (`Json`, app `CoroutineScope`), Room/DataStore instances, and repository bindings.
- `Loadable<T>` is the common state wrapper across data/use-case/viewmodel flows.

## Important entry points, core classes, and directories

### Entry points

- `app/src/main/kotlin/com/github/arhor/spellbindr/SpellbindrApplication.kt`
- `app/src/main/kotlin/com/github/arhor/spellbindr/ui/MainActivity.kt`
- `app/src/main/kotlin/com/github/arhor/spellbindr/ui/SpellbindrApp.kt`

### Navigation

- `app/src/main/kotlin/com/github/arhor/spellbindr/ui/navigation/AppDestinations.kt`
- `app/src/main/kotlin/com/github/arhor/spellbindr/ui/navigation/AppNavGraph.kt`

### Core architecture seams

- `core/domain/src/main/kotlin/com/github/arhor/spellbindr/domain/repository/`
- `core/domain/src/main/kotlin/com/github/arhor/spellbindr/domain/usecase/`
- `data/*/src/main/kotlin/.../repository/`
- `feature/*/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/`

### Storage/bootstrap core

- `data/compendium/.../local/assets/DefaultAssetBootstrapper.kt`
- `data/compendium/.../local/assets/AssetDataStoreBase.kt`
- `data/character/.../local/database/SpellbindrDatabase.kt`
- `data/settings/.../SettingsDataStoreModule.kt`
- `data/favorites/.../FavoritesDataStoreModule.kt`

## Known gaps, ambiguities, or areas needing confirmation

- `SpellbindrDatabase` still includes `FavoriteEntity` and `FavoritesDao`, but favorites behavior appears to be implemented via DataStore in `:data:favorites`. This may be legacy/unused DB schema surface and should be confirmed before removal.
- `README.md` references SRD assets under `app/src/main/assets/data`, while active asset data files are present under `data/compendium/src/main/assets/data` and loaded via merged Android assets at runtime. Clarify canonical docs location to avoid confusion.
- `docs/mvi-dispatch-contract.md` describes required files (`FeatureIntent/Route/Screen/ViewModel[/Effect]`); most features follow this, but strictness for all existing feature screens should be validated if broad refactors are planned.

