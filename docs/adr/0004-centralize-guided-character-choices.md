# ADR 0004: Centralize guided-character choice derivation and step ownership

## Status

Accepted

## Date

2026-08-13

## Context

Guided creation derives choices and grants from class, race, subrace, traits, and background. When planning,
navigation, validation, and materialization rediscover those requirements independently, they can disagree or retain
stale selections after an upstream choice changes.

## Decision

Use one pure, canonical derivation of fixed grants and choice requirements from a compact guided-choice context.
Requirements retain stable keys, resolved option IDs, category, and source identity/label. Feed the same derived inputs
to step planning, completion and review validation, reconciliation, UI rendering, and sheet materialization.

Keep selections source-aware and stable. When class, race, subrace, or background changes, reconcile by removing
inactive requirements and illegal or newly conflicting options while preserving still-valid selections. Duplicate
grants retain source information so the UI can explain conflicts deterministically.

The Race step owns race and required subrace identity only. Background owns background identity only. Ancestry owns
other racial choices. Proficiencies & Languages owns fixed and selectable skill, tool, armor, weapon, and language
choices. Equipment owns background equipment. Existing class-choice and spell steps retain their respective decisions.
Conditional steps are derived from active requirements and disappearing steps resolve to a nearby valid step.

## Consequences

Navigation, review errors, and persisted output agree on required choices; upstream changes cannot leave invisible stale
state. The canonical derivation becomes a shared internal contract and must remain independent of screen content state.

## Alternatives

- Let each step inspect reference data independently: rejected because it duplicates classification and completeness.
- Clear all downstream state after every upstream change: rejected because it discards valid user decisions.
- Move selections to a new persistence model immediately: rejected as unnecessary for centralizing ownership.

## References

- `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/internal/GuidedChoiceRequirements.kt`
- `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/internal/GuidedSetupValidation.kt`
- `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/internal/GuidedSetupStepPlanner.kt`
