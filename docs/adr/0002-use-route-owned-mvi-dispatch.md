# ADR 0002: Use route-owned MVI dispatch at Compose feature entry points

## Status

Accepted

## Date

2026-08-13

## Context

Compose features need a consistent boundary between stateless screen rendering, lifecycle-aware state collection,
navigation, one-off UI effects, and state mutation. Feature-specific callback surfaces make entry APIs inconsistent and
can move navigation concerns into reusable screen content.

## Decision

Feature entry screens expose `state: FeatureUiState` and `dispatch: FeatureDispatch`, rather than feature-level `onX`
callbacks. A feature normally owns `FeatureIntent`, `FeatureRoute`, `FeatureScreen`, and `FeatureViewModel`, plus
`FeatureEffect` when one-off effects are required.

Routes collect state and effects, render one-off UI effects, intercept navigation intents and map them to navigation
callbacks, and forward state-changing intents to the ViewModel. ViewModels expose `StateFlow` state, an optional
`SharedFlow` of effects, and a single public `dispatch(intent)` entry point; handlers remain behind dispatch. Feature
screens use shared app snackbar hosting unless an effect is genuinely local.

Tests should dispatch intents to verify state and effects and should verify that UI interaction emits the expected
intent. This is a design contract; no architecture guardrail test is assumed to enforce it.

## Consequences

Screens remain previewable and behavior-neutral, route ownership of navigation is explicit, and feature tests share a
stable vocabulary. Small leaf composables may still use focused callbacks; the constraint applies to feature entry
points.

## Alternatives

- Pass navigation callbacks through the entry screen: rejected because it couples rendering to routing.
- Let screens call ViewModels directly: rejected because it weakens previewability and explicit event flow.

## References

- `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/`
- `.agents/skills/design-compose-ui/SKILL.md`
