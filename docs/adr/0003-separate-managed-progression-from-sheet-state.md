# ADR 0003: Persist managed progression separately from mutable sheet state

## Status

Accepted

## Date

2026-08-13

## Context

A character sheet mixes permanent build decisions with mutable play state and free text. Guided creation and level-up
need structured, reproducible history, while manual and legacy characters lack enough evidence to reconstruct that
history safely.

## Decision

Guided characters are `Managed`: save their sheet snapshot and separate ordered progression atomically. Progression is
the source of truth for permanent class, subclass, HP, feature, proficiency, ability-score/feat, and spell decisions.
Manual and legacy characters are `Unmanaged`; migrations preserve their existing data, create no synthetic history,
and rely on the character foreign key for progression deletion.

Managed characters at levels 1–19 may append exactly one level through `CharacterLevelUp`; level-20 and unmanaged
characters are ineligible. Keep pure calculation and validation in `LevelUpProgressionEngine`, orchestration in
`ApplyLevelUpUseCase`, and the final sheet/progression write in one repository Room transaction. Preserve typed missing,
unmanaged, stale-state, validation, and persistence outcomes without partial writes.

The managed editor must preserve progression-owned class, level, ability-score, proficiency, maximum-HP, hit-dice,
saving-throw, skill, and structured spell/resource fields. Mutable play state and free-text fields remain editable.
Manual and progression-owned proficiency identifiers remain separate during save and materialization.

## Consequences

Permanent build history is auditable and level-up is atomic, while legacy data remains unchanged. Managed editor and
materialization paths carry additional ownership rules, and unmanaged characters cannot use guided level-up without a
separate explicit product decision.

## Alternatives

- Infer progression from current sheets: rejected because history and provenance are ambiguous.
- Store only progression and derive all play state: rejected because mutable sheet state has different lifecycle needs.
- Update sheet and progression separately: rejected because partial persistence would corrupt their relationship.

## References

- `app/src/main/kotlin/com/github/arhor/spellbindr/domain/usecase/LevelUpProgressionEngine.kt`
- `app/src/main/kotlin/com/github/arhor/spellbindr/domain/usecase/ApplyLevelUpUseCase.kt`
- `app/src/main/kotlin/com/github/arhor/spellbindr/data/repository/CharacterRepositoryImpl.kt`
