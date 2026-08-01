# Final Fix Report: Character Progression Foundation

## Status

DONE_WITH_CONCERNS. All requested final-review findings were addressed and committed in `ff6d5bb`. Direct Kotlin
compilation and focused JVM regression tests pass. The repository CI task set and Room instrumentation remain
unexecutable in this restored environment because `local.properties` points to the absent
`/tmp/spellbindr-android-sdk`; Gradle stops before compiling Android sources or running tasks.

## Fixes

- Replaced the character parent's `INSERT OR REPLACE` with Room `@Upsert`, so normal sheet updates no longer delete
  the parent row and cascade-delete managed progression. Added in-memory Room tests for exact relation reads, parent
  update preservation, transaction rollback, and intentional delete cascade.
- Applied class-specific level-one spell persistence: Cleric/Druid prepared selections are excluded, Wizard
  level-one selections enter `addedToSpellbook`, and cantrips plus spells-known selections retain class-owned IDs in
  `learned`.
- Replaced the emission counter with the stable bundled reference-data version `srd-5e-2014-data-v1` and covered
  repeated source emissions.
- Added lossless, class-owned starting-proficiency selections with stable choice/option IDs. Level-one choices owned by
  the selected subclass now participate in step planning, rendering, validation, reconciliation, sheet effects/text,
  and structured progression persistence. Bundled Draconic Sorcerer data and a representative builder case cover the
  path.
- Added explicit `@SerialName` values for every persisted polymorphic progression subtype. The codec translates the
  former fully-qualified discriminator values before decoding; golden output, legacy defaults, unknown fields, and
  legacy unmanaged state are covered.
- Corrected the all-class load error wording and added focused coverage for both that message and unknown-class label
  fallback.
- Corrected the approved design and implementation plan to use the API 36 baseline and the class-specific spell and
  structured-choice semantics.

## Verification

Tests were authored before their corresponding production changes. The focused Gradle RED attempts could not reach
source evaluation because the SDK path was already invalid.

Final direct Kotlin compiler overlay results:

- changed progression model, codec, spell/feature policies, reference coordinator, step planner, and DAO: **PASS**;
- `CharacterProgressionSerializationTest`, `GuidedCharacterProgressionPolicyTest`, and
  `GuidedSetupStepPlannerTest`: **PASS — OK (15 tests)**;
- `GuidedReferenceDataCoordinatorTest`: **PASS — OK (1 test)**. The first test formulation was corrected after it
  exposed a conflation-prone two-value `StateFlow` assertion; the final test asserts after each source emission.

Repository checks:

- `git diff --check`: **PASS**;
- no Room schema asset changed: **PASS** (the DAO conflict strategy does not alter the schema);
- static checks confirm `@Upsert` on the parent save and no remaining guided-data emission counter: **PASS**.

The required repository command was rerun with constrained settings:

```bash
./gradlew lintDebug test testDebugUnitTest --stacktrace --no-daemon --max-workers=1
```

Result: **BLOCKED before task execution** with `SDK location not found`; `sdk.dir` names a directory that does not
exist. The focused Room instrumentation suite is blocked by the same missing SDK and the lack of `adb`, so no device
test pass is claimed.

## Commit

- `ff6d5bb fix: preserve character progression semantics`

## Remaining concern

Restore an Android SDK containing API 36, then rerun the full CI task set and
`CharacterDaoIntegrationTest` on a device/emulator. Until then, the new Android/Room integration suite and the full
Android source graph have not received a fresh Gradle execution in this environment.
