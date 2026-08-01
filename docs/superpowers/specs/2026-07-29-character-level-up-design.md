# Character Level-Up Design

## Summary

Spellbindr will support guided character level-up directly from the character sheet. Multiclassing is a foundational
requirement, not a later extension. The feature will validate the bundled 2014 5e SRD rules by default while allowing
explicit, visible overrides for house rules.

The current character sheet remains the optimized snapshot used during play. A new structured progression record becomes
the source of truth for permanent build decisions: ordered class levels, subclasses, hit-point gains, ability score
improvements, feats, class-feature choices, and spell-learning decisions. Derived values are recalculated from that
record and materialized into the sheet through one atomic level-up transaction.

## Goals

- Level a managed character from total level 1 through total level 20.
- Support multiclassing from the first released level-up flow.
- Preserve the order in which class levels were acquired.
- Validate multiclass prerequisites and class-level progression.
- Guide users through every choice introduced by a class or subclass level.
- Calculate proficiency bonus, hit-point maximum, hit dice, derived bonuses, and spell-slot capacity.
- Support shared multiclass spell slots and Warlock Pact Magic as separate slot pools.
- Support ability score improvements, feats, known/prepared spell decisions, and spell replacement.
- Allow intentional rule overrides without losing the calculated value or hiding the override.
- Preserve existing characters and provide a safe path from manually managed to progression-managed state.
- Keep progression rules independent of Compose, Room, and Android.

## Non-goals

- Turning temporary play events such as damage, rests, expended spell slots, concentration, and inspiration into
  progression events.
- Making every descriptive class feature executable before level-up can ship.
- Automatically inferring a trustworthy progression history from free-text class or feature descriptions.
- Supporting a total character level above 20 in the initial ruleset.
- Supporting simultaneous level gains in one transaction. Each confirmed transaction adds exactly one character level.
- Replacing the existing manual character editor.

## Existing System Assessment

### Useful foundations

- `classes.json` contains all 12 classes with continuous levels 1 through 20.
- Class levels reference 261 unique features, and all of those references resolve in `features.json`.
- Class and subclass level progressions are present.
- Every applicable class level contains its spellcasting progression.
- The domain already models class levels, subclasses, feature choices, spellcasting classes, spells, feats, and
  prerequisites.
- `CharacterSheet` already models play-state concerns such as current HP, expended slots, Pact Magic slots,
  concentration, spells, skills, saving throws, and weapons.
- Guided character creation already resolves level-one class, subclass, feature, proficiency, and spell choices.
- Character sheet features already follow the repository's MVI dispatch contract and typed navigation.

### Blocking limitations

- `CharacterSheet` stores only a total level and a free-text class name.
- `CharacterEntity.classes` stores class totals but not acquisition order.
- Saving a sheet reconstructs `CharacterEntity.classes` from the free-text class name and total level.
- Guided creation flattens subclass and feature selections into `featuresAndTraits`.
- Choice ownership and origin metadata are discarded after creation.
- Proficiency bonus, maximum HP, hit dice, slots, and derived bonuses can be edited independently.
- Most `Feature` records are descriptive or choice-aware, not mechanically executable.
- The existing multiclass model is disconnected from `CharacterClass` data.
- There is no character-level XP threshold table or formal ruleset version.
- There is no transactional level-up proposal, validation result, or atomic application operation.

Changing the current `level` field therefore changes only a displayed/manual value. It does not constitute a valid
level-up.

## Design Principles

### Progression is ordered

`Fighter 1 → Wizard 1` and `Wizard 1 → Fighter 1` are distinct histories even though both have the same final class
totals. The persisted record must preserve that order because first-level proficiencies, saving throws, HP, and other
grants depend on it.

### Permanent decisions and play state are separate

Progression answers how the character was built. The sheet answers what condition the character is currently in.
Recalculating progression may change slot capacity, but must not turn an expended slot into an available slot without an
explicit rule.

### Overrides are explicit data

The app always retains the rules-calculated value. An override records a replacement value and an optional user note.
The UI identifies overridden fields and offers a reset-to-calculated action.

### A level-up is proposed before it is applied

No persistent state changes while the user moves through the wizard. The feature builds a proposal, validates it, shows
the complete delta, and commits progression plus its materialized sheet in one repository transaction.

### Descriptive features do not block delivery

Every acquired feature is stored by stable ID. Features have three supported capability levels:

1. `Descriptive`: display and persist the feature.
2. `ChoiceAware`: request and persist the feature's required choices.
3. `MechanicallyApplied`: also contribute structured effects to derived state.

A feature can move to a higher capability without changing existing progression records.

## Ruleset

The initial ruleset ID is `srd-5e-2014-v1`. It represents the 2014 5e SRD-derived reference data bundled with the app.
Every managed progression stores this ID and the bundled reference-data version used for its latest confirmed level.

The ruleset defines:

- total character level range: 1 through 20;
- class level range: 1 through 20;
- proficiency bonus: `2 + floor((totalLevel - 1) / 4)`;
- multiclass eligibility requirements;
- first-class and multiclass proficiency grants;
- fixed HP gain and rolled/manual HP gain rules;
- ability score improvement and feat eligibility;
- effective caster-level contributions and rounding;
- shared multiclass spell-slot capacity;
- Pact Magic capacity;
- class-specific known, prepared, spellbook, and replacement rules;
- subclass acquisition levels and subclass spell grants;
- validation severity and override eligibility.

XP remains optional. A user may initiate level-up without using XP. If XP is present, the ruleset may show readiness
based on the standard threshold, but insufficient XP is a warning that can be overridden rather than a hard failure.

## Domain Model

### Character progression

```kotlin
@Serializable
data class CharacterProgression(
    val rulesetId: String,
    val referenceDataVersion: String,
    val origin: ProgressionOrigin,
    val levels: List<CharacterLevelRecord>,
    val overrides: CharacterOverrides = CharacterOverrides(),
) {
    val totalLevel: Int
        get() = levels.size

    val classLevels: Map<String, Int>
        get() = levels.groupingBy(CharacterLevelRecord::classId).eachCount()
}

@Serializable
enum class ProgressionOrigin {
    Guided,
    Reconciled,
}
```

`levels[index]` represents character level `index + 1`. A managed progression is invalid if these values diverge.

### Per-level record

```kotlin
@Serializable
data class CharacterLevelRecord(
    val characterLevel: Int,
    val classId: String,
    val classLevel: Int,
    val subclassId: String? = null,
    val hitPointGain: HitPointGain,
    val featureChoices: Map<String, Set<String>> = emptyMap(),
    val proficiencyChoices: List<ProficiencyChoiceSelection> = emptyList(),
    val abilityScoreDecision: AbilityScoreDecision? = null,
    val spellChanges: SpellChanges = SpellChanges(),
)

@Serializable
data class ProficiencyChoiceSelection(
    val choiceId: String,
    val selectedProficiencyIds: Set<String>,
)

@Serializable
sealed interface HitPointGain {
    val rolledValue: Int

    @Serializable
    data class Fixed(override val rolledValue: Int) : HitPointGain

    @Serializable
    data class Rolled(override val rolledValue: Int) : HitPointGain

    @Serializable
    data class Manual(override val rolledValue: Int) : HitPointGain
}
```

`rolledValue` is the die/fixed result before the Constitution modifier and other per-level effects. It is persisted so
recalculation never rerolls HP.

Starting-class proficiency selections use a stable, class-owned choice ID and stable proficiency option IDs. This
keeps multiple proficiency-choice grants distinct without persisting transient UI keys or display names.

### Ability score and feat decisions

```kotlin
@Serializable
sealed interface AbilityScoreDecision {
    @Serializable
    data class Increase(val increases: Map<AbilityId, Int>) : AbilityScoreDecision

    @Serializable
    data class Feat(val featId: String) : AbilityScoreDecision
}
```

Validation enforces the ruleset's point count, per-score maximum, and feat prerequisites. Overrides may bypass a
warning, but the chosen decision remains structured.

### Spell changes

```kotlin
@Serializable
data class SpellChanges(
    val learned: Set<ClassSpellRef> = emptySet(),
    val replaced: Set<SpellReplacement> = emptySet(),
    val addedToSpellbook: Set<ClassSpellRef> = emptySet(),
)

@Serializable
data class ClassSpellRef(
    val classId: String,
    val spellId: String,
)

@Serializable
data class SpellReplacement(
    val classId: String,
    val removedSpellId: String,
    val learnedSpellId: String,
)
```

Prepared spells are not permanent level-up choices for classes that may prepare a new list after a rest. Level-up
updates their preparation capacity and optionally offers a post-level-up preparation screen, but does not encode the
current prepared list as progression history.

### Overrides

```kotlin
@Serializable
data class CharacterOverrides(
    val proficiencyBonus: ValueOverride<Int>? = null,
    val maximumHitPoints: ValueOverride<Int>? = null,
    val abilityScores: ValueOverride<AbilityScores>? = null,
    val sharedSpellSlots: ValueOverride<Map<Int, Int>>? = null,
    val pactSlots: ValueOverride<PactSlotCapacity>? = null,
)

@Serializable
data class ValueOverride<T>(
    val value: T,
    val note: String? = null,
)
```

Fine-grained feature-specific overrides can be added when a mechanically applied feature requires them. Arbitrary
derived values are not added preemptively.

### Derived state

```kotlin
data class DerivedCharacterStats(
    val totalLevel: Int,
    val classLevels: Map<String, Int>,
    val subclasses: Map<String, String>,
    val proficiencyBonus: Int,
    val abilityScores: AbilityScores,
    val maximumHitPoints: Int,
    val hitDice: Map<Int, Int>,
    val proficiencies: Set<GrantedValue>,
    val features: List<GrantedFeature>,
    val knownSpells: Set<ClassSpellRef>,
    val sharedSpellSlots: Map<Int, Int>,
    val pactSlots: PactSlotCapacity?,
)

data class GrantedValue(
    val id: String,
    val source: GrantSource,
)

data class GrantedFeature(
    val featureId: String,
    val source: GrantSource,
    val selectedOptionIds: Set<String>,
)

sealed interface GrantSource {
    data class ClassLevel(val classId: String, val classLevel: Int) : GrantSource
    data class SubclassLevel(val classId: String, val subclassId: String, val classLevel: Int) : GrantSource
    data class Race(val raceId: String) : GrantSource
    data class Background(val backgroundId: String) : GrantSource
    data class Feat(val featId: String) : GrantSource
}
```

Origin metadata prevents duplicate grants and makes explanations, reconciliation, and future rebuilds possible.

## Managed and Legacy Characters

```kotlin
@Serializable
sealed interface ProgressionState {
    @Serializable
    data class Managed(val progression: CharacterProgression) : ProgressionState

    @Serializable
    data object Unmanaged : ProgressionState
}
```

New guided characters are always managed. New manual characters are unmanaged unless the user creates them through a
structured reconciliation flow.

Existing characters migrate to `Unmanaged`. Their existing snapshot is preserved exactly. The app must not parse
`className` or `featuresAndTraits` and silently claim a valid history.

Selecting **Level up** for an unmanaged character opens reconciliation:

1. Select the ruleset.
2. Select each class and its current level.
3. Order previously acquired levels or accept a clearly labeled generated ordering.
4. Select each class's subclass.
5. Resolve choices required for the resulting progression.
6. Enter historical HP gains or create a maximum-HP override.
7. Reconcile spells and feats.
8. Review differences between the existing sheet and the calculated result.
9. Preserve differences as explicit overrides.
10. Confirm once to create `ProgressionState.Managed`.

Cancellation leaves the original character unchanged.

## Persistence

Progression will be stored in a dedicated one-to-one Room entity rather than embedded inside the existing serialized
manual sheet:

```kotlin
@Entity(
    tableName = "character_progressions",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CharacterProgressionEntity(
    @PrimaryKey val characterId: String,
    val stateJson: String,
)
```

Reasons for a separate entity:

- progression has a different lifecycle and source-of-truth role from play state;
- existing sheet serialization stays backward compatible;
- migration can mark every existing character unmanaged without rewriting its snapshot;
- progression can evolve independently;
- an atomic Room transaction can update progression and sheet together.

The database migration creates the table and does not modify existing character rows. Room schema export must be enabled
before this migration ships so future migrations can be verified.

Repository operations:

```kotlin
fun observeCharacterWithProgression(id: String): Flow<CharacterWithProgression?>

suspend fun reconcileProgression(
    characterId: String,
    progression: CharacterProgression,
    sheet: CharacterSheet,
)

suspend fun applyLevelUp(
    characterId: String,
    expectedTotalLevel: Int,
    progression: CharacterProgression,
    sheet: CharacterSheet,
)
```

`applyLevelUp` runs in one Room transaction. It verifies that the persisted total level still equals
`expectedTotalLevel`, preventing a stale wizard from overwriting a newer change.

## Progression Engine

The progression engine is a pure Kotlin domain component. It receives progression, reference data, current play state,
and a proposal. It does not access Room or expose Compose types.

Primary components:

- `CharacterProgressionCalculator`: rebuild all derived character stats.
- `CreateLevelUpPlanUseCase`: determine the next class level and required decisions.
- `ValidateLevelUpPlanUseCase`: return blocking errors, warnings, and overrideable warnings.
- `ApplyLevelUpUseCase`: create the new level record, recalculate state, materialize the sheet, and persist atomically.
- `MulticlassEligibilityPolicy`: validate entry prerequisites and level limits.
- `HitPointProgressionCalculator`: calculate persisted gain plus Constitution and other effects.
- `FeatureGrantResolver`: resolve class/subclass feature grants and choices.
- `SpellcastingProgressionCalculator`: calculate shared slots, Pact Magic, spell eligibility, and learning requirements.
- `DerivedSheetMaterializer`: merge derived capacity with current play state and explicit overrides.

### Plan model

```kotlin
data class LevelUpPlan(
    val characterId: String,
    val expectedTotalLevel: Int,
    val selectedClassId: String?,
    val nextClassLevel: Int?,
    val requirements: List<LevelUpRequirement>,
    val selections: LevelUpSelections,
    val preview: LevelUpPreview?,
    val validation: LevelUpValidation,
)
```

Requirements are domain values such as:

- `ChooseClass`
- `ConfirmMulticlassWarning`
- `ChooseSubclass`
- `ChooseHitPointGain`
- `ChooseFeatureOptions`
- `ChooseAbilityScoreIncreaseOrFeat`
- `ChooseLearnedSpells`
- `ChooseSpellReplacement`
- `ReviewChanges`

The UI renders these requirements; domain rules never select a screen or navigation route.

### Validation

Validation results have three severities:

- `Blocking`: corrupt progression, total/class level above 20, missing reference data, missing required choice, or stale
  persisted state.
- `Warning`: unusual but legal consequence, such as no immediately usable slots for a learned multiclass spell.
- `Overrideable`: failed multiclass prerequisite, insufficient XP, nonstandard ability score, nonstandard HP result, or
  a user-selected house rule allowed by policy.

Every overrideable result requires an explicit acknowledgement stored with the plan application. Structural errors
cannot be overridden.

## Multiclass Rules

### Class selection

At each level-up, the user may:

- increase a class already present if that class is below level 20; or
- enter a new class if total level is below 20.

Entering a new class validates both the existing class prerequisites and the target class prerequisites as defined by
the 2014 multiclass rules. A failed prerequisite is overrideable and visibly recorded.

### Level order

The next `classLevel` is the count of prior records with the selected class ID plus one. The first record uses full
starting-class grants. A later class's first record uses multiclass proficiency grants.

### Subclasses

Subclass choice belongs to its class. A level-up requires subclass selection when the selected class reaches its
subclass acquisition level. Once chosen, that class's subclass cannot silently change. A future explicit rebuild flow
may change it and recalculate downstream levels.

### Proficiency bonus

Proficiency bonus uses total character level, not any class level.

### Hit points and hit dice

The first character level follows starting-class HP rules. Every later level, including the first level in another
class, uses that class's normal level-up HP rule.

Hit dice are represented as a pool by die size, for example `{ 10: 2, 6: 3 }`, rather than a free-text `"5d8"` field.
The sheet may format the pool for display.

When Constitution modifier changes, maximum HP is recalculated retroactively across all character levels. Current HP
does not automatically increase beyond the maximum; if maximum HP increases, current HP remains unchanged unless the
ruleset explicitly grants current HP.

## Spellcasting Rules

### Shared slots

Each class contributes to an effective caster level according to the 2014 multiclass rules. Contributions are summed
using the required per-class rounding behavior, then mapped through the ruleset's multiclass spell-slot table.

Shared spell slots determine casting capacity only. Spell learning and preparation remain per class and use that class's
actual level.

### Pact Magic

Warlock Pact Magic remains a separate pool calculated from Warlock class level. It does not add to effective caster
level. A character may cast an eligible spell using a suitable shared or Pact slot according to the existing casting
flow.

### Spell decisions

Class-specific policies determine:

- number and level of cantrips learned;
- spells learned at the new class level;
- whether one known spell may be replaced;
- spellbook additions;
- preparation capacity;
- subclass/domain spells;
- maximum spell level eligible for learning or preparation.

Each permanent learned/replaced/spellbook decision is recorded in the level record with its owning class ID. Prepared
spell selections remain mutable play state.

When slot capacity increases, existing expended counts are clamped to the new capacity but otherwise preserved. Level-up
does not perform a long rest.

## Sheet Materialization

The materializer updates derived fields while preserving mutable play state.

Recalculated:

- total level and class summary;
- proficiency bonus;
- ability scores after progression decisions;
- maximum HP;
- hit-dice capacity;
- saving throw and skill bonuses;
- granted proficiencies and features;
- spellcasting attack/DC values;
- known spell list derived from permanent progression choices;
- shared and Pact slot capacity.

Preserved:

- current and temporary HP, subject to valid bounds;
- expended slots, subject to capacity bounds;
- concentration;
- death saves;
- inspiration;
- equipment instances and inventory;
- notes and roleplay fields;
- current prepared spell selections when still eligible;
- manual free-text additions;
- explicit overrides.

The display model must distinguish structured grants from free-text additions so recalculation never duplicates or
deletes user-authored text.

## User Experience

### Entry point

The character sheet overflow menu gains **Level up**. Managed level-20 characters see the action disabled with a clear
explanation. Unmanaged characters see **Set up level progression**.

### Navigation and MVI contract

Add the typed destination:

```kotlin
@Serializable
data class CharacterLevelUp(val characterId: String) : AppDestination("Level up")
```

The feature follows `docs/mvi-dispatch-contract.md`:

- `CharacterLevelUpIntent.kt`
- `CharacterLevelUpRoute.kt`
- `CharacterLevelUpScreen.kt`
- `CharacterLevelUpViewModel.kt`
- `CharacterLevelUpEffect.kt`
- `CharacterLevelUpUiState.kt`

The screen receives only `state` and `dispatch`. The route intercepts navigation intents and one-off effects.

### Wizard flow

The dynamic requirement list produces this conceptual order:

1. **Choose class** — existing or new class, eligibility, and resulting class level.
2. **Class choices** — subclass and gained class/subclass feature choices.
3. **Hit points** — fixed value, injected dice roll, or manual override.
4. **ASI or feat** — only when granted at that class level.
5. **Spells** — learn, replace, add to spellbook, and review preparation capacity.
6. **Review** — complete before/after delta, warnings, and overrides.
7. **Confirm** — one atomic write followed by navigation back.

Steps with no requirements are omitted. Back navigation keeps selections. Cancel or process death leaves the character
unchanged.

### Review

The review groups changes by:

- class progression;
- HP and hit dice;
- ability scores and derived bonuses;
- proficiencies;
- features and selected options;
- spells and spell slots;
- overrides and acknowledged warnings.

Each changed value shows before and after. Overridden values also show the rules-calculated value.

### Errors

- Reference-data load failure offers retry and does not mutate the character.
- Stale progression at confirmation returns to review with an explanation and reload action.
- Persistence failure keeps the plan in memory and offers retry.
- Missing referenced rules data is blocking and identifies the missing stable ID.
- Unexpected calculation errors produce a feature-level failure state and are logged without exposing raw exceptions.

## Reference Data Changes

The existing data must be extended or normalized with:

- active `CharacterClass.multiclassing` definitions;
- full versus multiclass proficiency grants;
- stable subclass acquisition metadata;
- an explicit multiclass spell-slot table;
- per-class caster contribution and rounding policy;
- per-class spell-learning/preparation policy;
- ASI/feat grant metadata where it cannot be inferred safely from feature IDs;
- feature capability metadata: descriptive, choice-aware, or mechanically applied;
- structured effects for numerical features as automation expands;
- XP thresholds for levels 1 through 20;
- a stable reference-data version.

Data-integrity tests must reject:

- missing or duplicated class levels;
- unresolved class/subclass feature IDs;
- unresolved choice option IDs;
- invalid spell-slot levels or counts;
- a spellcasting class level without its expected progression entry;
- subclass levels outside the owning class range;
- invalid multiclass prerequisite references;
- unsupported feature capability declarations.

## Rollout

### Increment 1: Lossless progression foundation

- Add the progression domain model and serialization.
- Add the dedicated Room entity and migration.
- Make guided creation produce a level-one progression record.
- Preserve structured class, subclass, feature, and spell choices.
- Mark existing/manual characters unmanaged.
- Display read-only progression history.

This increment does not expose level-up but makes all new guided characters ready for it.

### Increment 2: Multiclass progression engine

- Activate and validate multiclass reference data.
- Implement ordered class-level calculation.
- Implement class eligibility, subclass acquisition, features, proficiency bonus, HP, and hit dice.
- Add pure domain test matrices for all class-order combinations relevant to first and subsequent class levels.

### Increment 3: Non-spellcasting level-up UI

- Add the MVI feature and character-sheet entry point.
- Implement class, feature, HP, ASI/feat, review, and atomic confirmation.
- Allow spellcasting characters through levels that introduce no unresolved spell decisions only after their slot
  calculations are supported.

### Increment 4: Multiclass spellcasting

- Add effective caster-level and shared slot capacity.
- Add Pact Magic capacity.
- Add class-specific learning, replacement, spellbook, and preparation policies.
- Add spell decision UI and casting-state preservation.

### Increment 5: Reconciliation and override completion

- Add unmanaged-character reconciliation.
- Add explicit calculated-versus-overridden presentation.
- Add reset-to-calculated actions and override notes.
- Add migration, process-death, stale-plan, and persistence-failure tests.

### Increment 6: Mechanical feature expansion

- Convert high-value descriptive features to structured effects.
- Prioritize features that alter ability scores, HP, proficiencies, spell access, attacks, defenses, and resource
  capacity.
- Preserve compatibility with older descriptive progression records.

## Testing Strategy

### Domain unit tests

- Every class from level 1 through 20.
- Every subclass acquisition level.
- Every class/subclass feature choice.
- First-class versus later multiclass grants.
- Class-order permutations such as Fighter 1/Wizard 1 and Wizard 1/Fighter 1.
- Returning to an existing class after levels in another class.
- Total and class level limits.
- Multiclass prerequisites and explicit overrides.
- Proficiency bonus thresholds.
- Fixed, rolled, and manual HP gains.
- Constitution modifier changes across existing levels.
- ASIs, feats, score caps, and prerequisites.
- Effective caster-level rounding for full, half, and third casters.
- Shared slots plus Pact Magic.
- Known, prepared, replacement, spellbook, and subclass spells.
- Deterministic full recalculation from persisted progression.
- Materialization preserving mutable play state.

### Persistence tests

- Progression JSON round trip.
- Room relation loading.
- Atomic progression/sheet update.
- Stale expected-level rejection.
- Migration preserving every existing character snapshot.
- Character deletion cascading to progression.
- Unknown future JSON fields and backward-compatible defaults.

### ViewModel tests

- Dispatching each intent changes only expected wizard state.
- Dynamic requirements add and remove steps correctly.
- Back/cancel never persists.
- Confirmation invokes one atomic use case.
- Blocking validation prevents confirmation.
- Override acknowledgement enables confirmation where allowed.
- Stale and persistence errors surface retryable state/effects.

### UI and screenshot tests

- Managed and unmanaged character-sheet actions.
- Class choice and multiclass warnings.
- Subclass and feature choices.
- HP methods.
- ASI versus feat selection.
- Spell decisions.
- Before/after review and override indicators.
- Loading, blocking error, stale plan, and persistence error states.
- Light/dark and relevant screen-size previews.

## Acceptance Criteria

The complete feature is ready when:

- A guided character retains a lossless level-one progression record.
- A user can level a managed character one level at a time to total level 20.
- A user can enter and return to multiple classes from the first release.
- Class acquisition order produces the correct grants.
- Multiclass prerequisites are validated and may be explicitly overridden.
- Subclasses and all required feature choices are recorded by stable ID.
- HP, proficiency bonus, hit dice, ASIs, feats, and derived bonuses recalculate deterministically.
- Shared multiclass spell slots and Pact Magic are calculated separately and correctly.
- Permanent spell decisions are owned by a class and survive recalculation.
- Existing expended slots and other play state are preserved during level-up.
- Review shows every persistent change before confirmation.
- Confirmation performs one atomic write and detects stale state.
- Existing characters remain unchanged until explicitly reconciled.
- Calculated values and manual overrides remain separately inspectable.
- The progression engine has no Android, Compose, or Room dependency.
- Reference-data integrity and class-level test matrices pass in CI.

## Implementation Planning Boundaries

This design should be implemented through separate plans rather than one oversized plan:

1. **Progression foundation:** model, persistence, migration, guided creation, and read-only history.
2. **Multiclass rules engine:** prerequisites, ordered class levels, features, HP, ASIs, feats, derivation, and atomic
   application.
3. **Multiclass spellcasting:** caster-level calculation, shared slots, Pact Magic, and class spell policies.
4. **Level-up UI:** typed navigation, MVI wizard, review, confirmation, and character-sheet integration.
5. **Legacy reconciliation and overrides:** conversion wizard, difference review, override lifecycle, and resilience.

Each plan must leave the repository in a working, independently testable state and must use the final
multiclass-capable progression model from the beginning.
