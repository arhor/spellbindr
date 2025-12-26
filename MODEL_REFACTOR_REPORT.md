# Model consolidation report

## Duplicated model groups identified
- **Ability / Skill enums**: Previously defined in both `domain/model` and `data/model` solely to accommodate serialization.
- **Alignment / Race / Trait**: Parallel data-layer equivalents existed with identical fields to the domain representations.
- **Spell**: A data-layer copy mirrored the domain model; mapping existed only for asset decoding.
- **CharacterSheet graph**: The Room snapshot used a data-layer duplication of every sheet type (ability scores, skills, spells, weapons) even though the domain equivalents carried the same shape and defaults.

## Consolidation actions
- Promoted domain models to be `@Serializable` where asset or snapshot decoding is required (Ability, Skill, Alignment, Race, Trait, Spell, CharacterSheet and nested types).
- Removed redundant data-layer copies and the associated mapping helpers (`AbilitySkillMapper`, `ReferenceDataMapper`, `CharacterSheetMapper`, `SpellMappers`).
- Asset data stores now decode directly into domain models; repositories no longer perform trivial one-to-one mappings.
- Room snapshots use domain sheet types, keeping a lightweight `CharacterSheetSnapshot` wrapper (id-less) as the persistence shape.

## Models kept separate (and why)
- **CharacterSheetSnapshot**: Remains as a dedicated persistence container so Room continues to store an id-less snapshot alongside `CharacterEntity` while reusing domain sheet components internally.
- **Database entity / snapshot separation**: Room entities stay distinct to preserve schema requirements and converters.

## Canonical ID wrapper
- **EntityRef** remains the single typed wrapper for string identifiers across layers, serialized as a plain string via `EntityRefSerializer`. All usages continue to rely on this canonical type.
