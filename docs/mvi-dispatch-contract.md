# MVI Dispatch Contract

This project uses a feature-entry MVI contract for Compose feature screens.

## Required files per feature

- `FeatureIntent.kt`
- `FeatureRoute.kt`
- `FeatureScreen.kt`
- `FeatureViewModel.kt`
- `FeatureEffect.kt` (only when one-off effects are needed)

## Feature entry contract

Feature entry screen composables must expose:

- `state: FeatureUiState`
- `dispatch: FeatureDispatch`

Feature-level `onX` callback parameters are not allowed on the entry screen API.
Feature entry screens should use the app-shared snackbar host; avoid per-screen `SnackbarHost` unless truly local-only
behavior is required.

## Route responsibilities

- Collect feature `uiState`.
- Collect feature `effects` and render one-off UI effects.
- Intercept navigation intents and map them to navigation callbacks.
- Forward state-changing intents to `vm.dispatch`.

## ViewModel responsibilities

- Expose `uiState: StateFlow<FeatureUiState>`.
- Expose `effects: SharedFlow<FeatureEffect>` when needed.
- Expose a single entry point: `fun dispatch(intent: FeatureIntent)`.
- Keep intent handlers private/internal behind `dispatch`.

## Tests checklist

- Unit tests dispatch intents and assert state/effect transitions.
- UI tests assert user interactions dispatch expected intents.
- Architecture guardrail tests enforce entry-screen dispatch signature.
