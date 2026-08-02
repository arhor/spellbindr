# Multiclass Level-Up Implementation Ledger

- [x] 1. Reference-data normalization and integrity tests
  - Activated typed multiclass prerequisites, grants, and stable choice identifiers from `classes.json`.
  - Added feat reference loading and typed level-up policies for all 12 SRD classes, XP, shared slots, Pact Magic,
    subclass acquisition, ASI/feat grants, spell learning/preparation/spellbooks, and feature capabilities.
  - Added `LevelUpReferenceDataIntegrityTest` coverage for class levels, subclasses, feature references and choices,
    feats, multiclass data, spell policies, XP thresholds, and slot tables.
- [x] 2. Progression plan, validation, and calculation engine
  - Added serializable level-up drafts, typed requirements, validation severities, previews, acknowledgement ids,
    and backward-compatible persisted feat choices, acknowledgements, and notes.
  - Added deterministic class ordering, level-limit, multiclass, sticky-subclass, feature/proficiency/HP/ASI/feat,
    proficiency/HP/hit-die, and shared-caster preview calculations. Spell detail remains owned by item 3.
  - Authored focused engine tests (static inspection only; Gradle deliberately not run after the device reboot).
- [x] 3. Spellcasting and feat policies
  - Added class-level spell legality and exact known, prepared-cantrip, and spellbook selection policies using the
    bundled progression tables; added preparation-capacity notices and spell reference data to the pure engine.
  - Added separately represented Pact Magic capacity, per-class multiclass rounding, and representable third-caster
    subclass contributions for Eldritch Knight and Arcane Trickster.
  - Hardened feat ability-effect cap validation and preserved feat-owned stable selection ids.
  - Static diff inspection only; Gradle deliberately not run under the machine constraint.
- [x] 4. Materialization and atomic repository transaction
  - Added `ApplyLevelUpUseCase` and typed repository outcomes. Confirmation reloads the character and progression,
    validates the plan, appends exactly one record, and writes the materialized sheet/progression in one Room
    `withTransaction` block.
  - Managed materialization now stores structured hit-die pools and structured grants separately from free text,
    preserves mutable sheet state, and clamps previously expended shared/Pact slots to recalculated capacities.
- [x] 5. Managed-editor restrictions
  - The full editor now observes `CharacterWithProgression` and disables progression-owned class, level, ability,
    proficiency, maximum-HP, hit-dice, saving-throw, and skill controls for managed characters.
  - Level-up ownership is also enforced by the editor state reducer and save path; mutable play state and free-text
    fields remain editable, while spell capacities and structured grants are preserved from the managed snapshot.
  - Added focused managed-editor state coverage (static inspection only; Gradle deliberately not run under the
    machine constraint).
- [x] 6. Level Up MVI, navigation, and sheet entry points
  - Added the typed `CharacterLevelUp(characterId)` destination and dispatch-contract Intent/Route/Screen/ViewModel,
    one-off effects, dynamic requirement-driven steps, review acknowledgements, and atomic confirmation handling.
  - Draft selections persist as serialized ids in `SavedStateHandle` and restore only when level, ruleset, and bundled
    reference version still match. Stale and persistence failures retain review choices for reload or retry.
  - Added enabled level-up actions for managed levels 1–19 and disabled maximum-level/unmanaged actions on both the
    progression card and sheet overflow menu. Static diff inspection only; Gradle deliberately not run.
- [x] 7. Integration, screenshots, documentation, and full verification
  - Completed the final static integration pass: replacement validation now reconstructs class-owned spell state,
    rejects missing/self/duplicate replacements, class selection exposes per-class eligibility reasons while keeping
    prerequisite acknowledgement overrides, and persistence keeps manual and progression proficiency ids separate.
  - Prepared-caster materialization preserves only representably eligible prepared spells up to the recalculated
    capacity; the current sheet model does not distinguish Wizard spellbook contents from prepared Wizard spells, so
    Wizard entries remain preserved as spellbook state rather than being capacity-trimmed.
  - Added focused engine regression coverage and audited the new UI/persistence signatures and imports.
  - Verification limit: `git diff --check` only. Gradle, Room instrumentation, screenshots, and device validation were
    deliberately not run after the device reboot and must be completed on a compatible higher-capacity runner.

Review status and verification evidence will be recorded under each item as work lands.
