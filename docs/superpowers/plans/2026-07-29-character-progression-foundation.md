# Character Progression Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or
> superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist a lossless, ordered level-one progression for every newly guided character while preserving existing
and manually created characters as unmanaged.

**Architecture:** Add a serializable progression domain model and a one-to-one Room entity related to
`CharacterEntity`. Guided creation will build a `CharacterCreationResult` containing both the playable sheet and managed
progression, then save both atomically. Character-sheet loading will expose managed/unmanaged status and a compact
read-only progression summary without adding level-up behavior.

**Tech Stack:** Kotlin 2.3, kotlinx.serialization, Room, Coroutines/Flow, Hilt, Jetpack Compose, JUnit4, Truth, MockK.

## Global Constraints

- Ruleset ID is exactly `srd-5e-2014-v1`.
- Total character level and class level remain limited to 1 through 20.
- Multiclass order is represented by an ordered `levels` list from the first persisted model.
- Existing character snapshots must remain byte-for-byte semantically unchanged by migration.
- Existing and manually created characters are `Unmanaged`; new guided characters are `Managed`.
- Stable IDs, not display names or parsed prose, own class, subclass, feature-choice, and spell decisions.
- Progression domain code must not depend on Android, Compose, or Room.
- Feature-entry UI follows `docs/mvi-dispatch-contract.md`.
- Use JDK 17, the user-approved Android SDK 36 baseline, JUnit4, Truth, and MockK.

---

## File Structure

- `domain/model/CharacterProgression.kt`: serializable progression state and ordered level record.
- `domain/model/CharacterCreationResult.kt`: atomic guided-creation output.
- `data/local/database/entity/CharacterProgressionEntity.kt`: one-to-one persisted progression JSON.
- `data/local/database/model/CharacterWithProgressionEntity.kt`: Room relation projection.
- `data/local/database/dao/CharacterDao.kt`: relation reads and transactional writes.
- `data/local/database/converter/CharacterProgressionConverter.kt`: JSON conversion.
- `data/mapper/CharacterProgressionMapper.kt`: persistence/domain mapping.
- `domain/repository/CharacterRepository.kt`: progression-aware observe/save contracts.
- `domain/usecase/SaveGuidedCharacterUseCase.kt`: guided atomic-save boundary.
- `domain/usecase/LoadCharacterWithProgressionUseCase.kt`: sheet-facing read boundary.
- `ui/feature/character/guided/internal/GuidedCharacterProgressionBuilder.kt`: level-one record construction.
- `ui/feature/character/sheet/model/ProgressionSummaryUiModel.kt`: read-only managed/unmanaged presentation.

### Task 1: Add the progression domain model

**Files:**
- Create: `app/src/main/kotlin/com/github/arhor/spellbindr/domain/model/CharacterProgression.kt`
- Create: `app/src/main/kotlin/com/github/arhor/spellbindr/domain/model/CharacterCreationResult.kt`
- Create: `app/src/test/kotlin/com/github/arhor/spellbindr/domain/model/CharacterProgressionTest.kt`

**Interfaces:**
- Produces: `ProgressionState`, `CharacterProgression`, `CharacterLevelRecord`, `HitPointGain`,
  `AbilityScoreDecision`, `SpellChanges`, `ClassSpellRef`, `SpellReplacement`, `CharacterOverrides`,
  `ValueOverride<T>`, and `CharacterCreationResult`.
- Consumes: existing `AbilityId`, `AbilityScores`, `CharacterSheet`, and `PactSlotState`.

- [ ] **Step 1: Write failing derived-property tests**

```kotlin
class CharacterProgressionTest {
    @Test
    fun `totalLevel and classLevels derive from ordered records`() {
        val progression = CharacterProgression(
            referenceDataVersion = "test-v1",
            origin = ProgressionOrigin.Guided,
            levels = listOf(
                level(characterLevel = 1, classId = "fighter", classLevel = 1),
                level(characterLevel = 2, classId = "wizard", classLevel = 1),
                level(characterLevel = 3, classId = "fighter", classLevel = 2),
            ),
        )

        assertThat(progression.totalLevel).isEqualTo(3)
        assertThat(progression.classLevels).containsExactly("fighter", 2, "wizard", 1)
        assertThat(progression.levels.map { it.classId })
            .containsExactly("fighter", "wizard", "fighter")
            .inOrder()
    }

    @Test
    fun `ruleset defaults to supported ruleset`() {
        val progression = CharacterProgression(
            referenceDataVersion = "test-v1",
            origin = ProgressionOrigin.Guided,
            levels = listOf(level(1, "fighter", 1)),
        )

        assertThat(progression.rulesetId).isEqualTo("srd-5e-2014-v1")
    }
}
```

Add a private `level(characterLevel, classId, classLevel)` fixture returning
`CharacterLevelRecord(..., hitPointGain = HitPointGain.Fixed(6))`.

- [ ] **Step 2: Run the focused test and verify it fails**

Run:

```bash
./gradlew testDebugUnitTest --tests '*CharacterProgressionTest' --stacktrace
```

Expected: compilation fails because the progression types do not exist.

- [ ] **Step 3: Implement the serializable model**

Use the exact fields from the approved design. Add `SUPPORTED_RULESET_ID = "srd-5e-2014-v1"` beside
`CharacterProgression`, default `rulesetId` to it, derive `totalLevel` from `levels.size`, and derive `classLevels` with
`groupingBy { it.classId }.eachCount()`. `CharacterCreationResult` is:

```kotlin
data class CharacterCreationResult(
    val sheet: CharacterSheet,
    val progression: CharacterProgression,
)
```

Do not add validation or calculation logic in this task.

- [ ] **Step 4: Run the focused test**

Run the Step 2 command. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/com/github/arhor/spellbindr/domain/model/CharacterProgression.kt \
  app/src/main/kotlin/com/github/arhor/spellbindr/domain/model/CharacterCreationResult.kt \
  app/src/test/kotlin/com/github/arhor/spellbindr/domain/model/CharacterProgressionTest.kt
git commit -m "feat: add character progression model"
```

### Task 2: Persist progression with a Room relation

**Files:**
- Create: `app/src/main/kotlin/com/github/arhor/spellbindr/data/local/database/entity/CharacterProgressionEntity.kt`
- Create: `app/src/main/kotlin/com/github/arhor/spellbindr/data/local/database/model/CharacterWithProgressionEntity.kt`
- Create: `app/src/main/kotlin/com/github/arhor/spellbindr/data/local/database/CharacterProgressionJsonCodec.kt`
- Create: `app/src/main/kotlin/com/github/arhor/spellbindr/data/mapper/CharacterProgressionMapper.kt`
- Modify: `app/src/main/kotlin/com/github/arhor/spellbindr/data/local/database/dao/CharacterDao.kt`
- Modify: `app/src/main/kotlin/com/github/arhor/spellbindr/data/local/database/SpellbindrDatabase.kt`
- Test: `app/src/test/kotlin/com/github/arhor/spellbindr/data/model/CharacterProgressionSerializationTest.kt`

**Interfaces:**
- Consumes: Task 1 progression types and the injected application `Json`.
- Produces: `CharacterProgressionEntity(characterId, stateJson)`,
  `CharacterWithProgressionEntity(character, progression)`, DAO relation flow, and atomic DAO save.

- [ ] **Step 1: Write a failing serialization round-trip test**

Construct `ProgressionState.Managed` with Fighter 1/Wizard 1, a subclass, feature choice, rolled HP, an ASI, and a spell
replacement. Encode with the same `Json` configuration as `AppInfrastructureModule`, decode it, and assert equality.
Also round-trip `ProgressionState.Unmanaged`.

- [ ] **Step 2: Run the focused serialization test**

```bash
./gradlew testDebugUnitTest --tests '*CharacterProgressionSerializationTest' --stacktrace
```

Expected: FAIL because the converter/serialized integration does not exist.

- [ ] **Step 3: Add persistence types and mapping**

Create a foreign-key entity with `onDelete = CASCADE`, primary key `characterId`, and `stateJson: String`. The relation
projection uses:

```kotlin
data class CharacterWithProgressionEntity(
    @Embedded val character: CharacterEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "characterId",
    )
    val progression: CharacterProgressionEntity?,
)
```

Map a missing progression row to `ProgressionState.Unmanaged`. The `@Singleton` codec exposes
`encode(state: ProgressionState): String` and `decode(json: String): ProgressionState`.

- [ ] **Step 4: Add DAO operations**

Add:

```kotlin
@Transaction
@Query("SELECT * FROM characters WHERE id = :id")
fun observeCharacterWithProgression(id: String): Flow<CharacterWithProgressionEntity?>

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun saveProgression(progression: CharacterProgressionEntity)

@Transaction
suspend fun saveCharacterWithProgression(
    character: CharacterEntity,
    progression: CharacterProgressionEntity,
) {
    saveCharacter(character)
    saveProgression(progression)
}
```

- [ ] **Step 5: Register the entity**

Add `CharacterProgressionEntity` to `SpellbindrDatabase.entities`, increment the database version from 3 to 4, add the
entity's required index, and leave the existing `@TypeConverters` list unchanged. Inject
`CharacterProgressionJsonCodec` into `CharacterRepositoryImpl`; the entity stores an ordinary JSON string and therefore
does not need a Room type converter.

- [ ] **Step 6: Run serialization and compilation checks**

```bash
./gradlew testDebugUnitTest --tests '*CharacterProgressionSerializationTest' --stacktrace
./gradlew compileDebugKotlin --stacktrace
```

Expected: both PASS.

- [ ] **Step 7: Commit**

Stage only the files listed in this task and commit:

```bash
git commit -m "feat: persist character progression"
```

### Task 3: Add and verify the database migration

**Files:**
- Create: `app/src/main/kotlin/com/github/arhor/spellbindr/data/local/database/Migrations.kt`
- Modify: `app/src/main/kotlin/com/github/arhor/spellbindr/di/DatabaseModule.kt`
- Modify: `app/src/main/kotlin/com/github/arhor/spellbindr/data/local/database/SpellbindrDatabase.kt`
- Modify: `app/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Test: `app/src/androidTest/kotlin/com/github/arhor/spellbindr/data/local/database/Migration3To4Test.kt`
- Create: `app/schemas/com.github.arhor.spellbindr.data.local.database.SpellbindrDatabase/4.json`

**Interfaces:**
- Produces: `MIGRATION_3_4`.
- Consumes: Room database version 3 and the Task 2 entity schema.

- [ ] **Step 1: Add Room migration testing support**

Add `androidx.room:room-testing` at the existing Room version to `libs.versions.toml` and expose it as
`libs.androidx.room.testing`. Add `androidTestImplementation(libs.androidx.room.testing)`.

- [ ] **Step 2: Enable schema export and migration-test assets**

Set `exportSchema = true`, configure KSP with
`arg("room.schemaLocation", "$projectDir/schemas")`, and add `$projectDir/schemas` as an androidTest asset source
directory. Use the repository's existing KSP configuration; do not introduce kapt or another Gradle plugin.

- [ ] **Step 3: Write the failing migration test**

Use `MigrationTestHelper` to create version 3, insert one complete `characters` row, run `MIGRATION_3_4`, assert the
original row still exists, and assert `character_progressions` is empty. Compare all original sheet JSON text exactly.

- [ ] **Step 4: Run the migration test and verify failure**

```bash
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.github.arhor.spellbindr.data.local.database.Migration3To4Test
```

Expected: FAIL because `MIGRATION_3_4` is missing. If no device is available, record that limitation and continue with
schema-generation and JVM checks; do not report the instrumentation test as passed.

- [ ] **Step 5: Implement and register the migration**

Create:

```sql
CREATE TABLE IF NOT EXISTS `character_progressions` (
  `characterId` TEXT NOT NULL,
  `stateJson` TEXT NOT NULL,
  PRIMARY KEY(`characterId`),
  FOREIGN KEY(`characterId`) REFERENCES `characters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
)
```

Create the required index if Room's generated schema declares one. Register only `MIGRATION_3_4` through
`.addMigrations(MIGRATION_3_4)`.

- [ ] **Step 6: Generate schema and run checks**

```bash
./gradlew kspDebugKotlin testDebugUnitTest --stacktrace
git diff --check
```

Expected: PASS and schema version 4 exists.

- [ ] **Step 7: Commit**

```bash
git commit -m "feat: migrate character progression storage"
```

### Task 4: Add progression-aware repository contracts

**Files:**
- Create: `app/src/main/kotlin/com/github/arhor/spellbindr/domain/model/CharacterWithProgression.kt`
- Create: `app/src/main/kotlin/com/github/arhor/spellbindr/domain/usecase/LoadCharacterWithProgressionUseCase.kt`
- Create: `app/src/main/kotlin/com/github/arhor/spellbindr/domain/usecase/SaveGuidedCharacterUseCase.kt`
- Modify: `app/src/main/kotlin/com/github/arhor/spellbindr/domain/repository/CharacterRepository.kt`
- Modify: `app/src/main/kotlin/com/github/arhor/spellbindr/data/repository/CharacterRepositoryImpl.kt`
- Test: `app/src/test/kotlin/com/github/arhor/spellbindr/data/repository/CharacterRepositoryImplTest.kt`

**Interfaces:**
- Produces: `CharacterWithProgression(sheet, progressionState)`,
  `observeCharacterWithProgression(id)`, and `saveGuidedCharacter(result)`.
- Consumes: Task 2 DAO transaction and mappings.

- [ ] **Step 1: Write failing repository tests**

Test that a missing progression row maps to `Unmanaged`, a present row maps to its exact managed progression, and
`saveGuidedCharacter` passes character plus managed progression to one DAO transaction.

- [ ] **Step 2: Run focused tests**

```bash
./gradlew testDebugUnitTest --tests '*CharacterRepositoryImplTest' --stacktrace
```

Expected: FAIL because progression-aware methods do not exist.

- [ ] **Step 3: Add domain and repository contracts**

```kotlin
data class CharacterWithProgression(
    val sheet: CharacterSheet,
    val progressionState: ProgressionState,
)
```

Add:

```kotlin
fun observeCharacterWithProgression(id: String): Flow<CharacterWithProgression?>
suspend fun saveGuidedCharacter(result: CharacterCreationResult)
```

Keep existing manual sheet APIs unchanged.

- [ ] **Step 4: Implement use cases and repository mapping**

`SaveGuidedCharacterUseCase(result)` delegates to the new repository method. `LoadCharacterWithProgressionUseCase(id)`
returns the new flow. Save `ProgressionState.Managed(result.progression)` with the sheet entity atomically.

- [ ] **Step 5: Run focused and existing repository tests**

```bash
./gradlew testDebugUnitTest \
  --tests '*CharacterRepositoryImplTest' \
  --tests '*CharacterSheetWeaponSerializationTest' \
  --stacktrace
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: expose character progression repository"
```

### Task 5: Make guided creation produce lossless progression

**Files:**
- Create:
  `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/internal/`
  `GuidedCharacterProgressionBuilder.kt`
- Modify:
  `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/internal/GuidedCharacterSheetBuilder.kt`
- Modify:
  `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/GuidedCharacterSetupViewModel.kt`
- Test:
  `app/src/test/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/GuidedCharacterSetupSheetBuilderTest.kt`
- Test:
  `app/src/test/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/GuidedCharacterSetupViewModelTest.kt`

**Interfaces:**
- Produces: `buildGuidedCharacterCreationResult(content): CharacterCreationResult`.
- Consumes: Task 1 model and Task 4 `SaveGuidedCharacterUseCase`.

- [ ] **Step 1: Add failing builder tests**

For a level-one Cleric with Life subclass, feature choices, cantrips, and spells, assert:

- progression is `Guided`, total level is 1, and ruleset ID is exact;
- record class ID is `cleric`, class level is 1, and subclass ID is `life`;
- fixed HP stores the class hit die before Constitution modifier;
- feature choices preserve the original feature-key-to-option-ID map;
- cantrip spell changes use stable spell IDs with class ID `cleric`, while mutable prepared spell selections do not
  enter progression;
- sheet content remains equal to the current expected sheet.

- [ ] **Step 2: Run the builder test and verify failure**

```bash
./gradlew testDebugUnitTest --tests '*GuidedCharacterSetupSheetBuilderTest' --stacktrace
```

Expected: FAIL because only a sheet is built.

- [ ] **Step 3: Implement the progression builder**

Build exactly one `CharacterLevelRecord`. Preserve selected starting-class proficiencies and level-one class/subclass
feature choices with stable source and option IDs. Apply the approved class-specific spell policy: class cantrips and
spells-known choices enter `SpellChanges.learned`, Wizard level-one spell choices enter `addedToSpellbook`, and mutable
Cleric/Druid prepared spell selections do not enter progression. Use the selected class hit die as
`HitPointGain.Fixed`; derived Constitution HP remains represented by the existing sheet.

- [ ] **Step 4: Replace guided save with atomic result save**

Inject `SaveGuidedCharacterUseCase`, replace `buildCharacterSheet` with
`buildCharacterCreationResult`, save the result once, and emit `result.sheet.id`. Update tests to verify exactly one
atomic save and no call to `SaveCharacterSheetUseCase`.

- [ ] **Step 5: Run guided tests**

```bash
./gradlew testDebugUnitTest \
  --tests '*GuidedCharacterSetupSheetBuilderTest' \
  --tests '*GuidedCharacterSetupViewModelTest' \
  --tests '*GuidedChoiceRequirementsTest' \
  --tests '*GuidedSetupValidationTest' \
  --stacktrace
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: preserve guided character progression"
```

### Task 6: Show read-only progression status on the character sheet

**Files:**
- Create:
  `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/sheet/model/ProgressionSummaryUiModel.kt`
- Create:
  `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/sheet/components/ProgressionSummaryCard.kt`
- Modify: `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/sheet/CharacterSheetUiState.kt`
- Modify: `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/sheet/CharacterSheetViewModel.kt`
- Modify: `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/sheet/CharacterSheetScreen.kt`
- Modify:
  `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/sheet/components/tabs/overview/OverviewTab.kt`
- Test: `app/src/test/kotlin/com/github/arhor/spellbindr/ui/feature/character/sheet/CharacterSheetViewModelTest.kt`
- Test:
  `app/src/screenshotTest/kotlin/com/github/arhor/spellbindr/ui/feature/character/sheet/`
  `ProgressionSummaryCardScreenshot.kt`

**Interfaces:**
- Produces: `ProgressionSummaryUiModel.Unmanaged` and
  `ProgressionSummaryUiModel.Managed(totalLevel, classes, levels)`.
- Consumes: Task 4 `LoadCharacterWithProgressionUseCase`.

- [ ] **Step 1: Write failing ViewModel mapping tests**

Assert unmanaged state maps to the message `"Set up level progression to enable guided level-up."`. Assert ordered
Fighter/Wizard/Fighter records map to class totals `"Fighter 2 / Wizard 1"` and history labels
`"1. Fighter 1"`, `"2. Wizard 1"`, `"3. Fighter 2"`.

- [ ] **Step 2: Run the focused ViewModel test**

```bash
./gradlew testDebugUnitTest --tests '*CharacterSheetViewModelTest' --stacktrace
```

Expected: FAIL because progression is not loaded or mapped.

- [ ] **Step 3: Load and map progression**

Replace the sheet-only load dependency with `LoadCharacterWithProgressionUseCase`. Keep `currentSheet` for existing play
operations and add progression summary to `CharacterSheetUiState.Content`. Resolve display class names from cached class
reference data; fall back to `EntityRef(id).prettyString()`.

- [ ] **Step 4: Add the compact read-only card**

Show status, class totals, and ordered level history on the Overview tab. Do not add a level-up button or navigation in
this increment. The card accepts a `ProgressionSummaryUiModel` and has no repository or ViewModel dependency.

- [ ] **Step 5: Add and export screenshot coverage**

Create managed and unmanaged `@PreviewTest` previews using the existing screenshot harness. Generate references with:

```bash
./gradlew :app:updateDebugScreenshotTest --tests '*ProgressionSummaryCardScreenshot*'
```

- [ ] **Step 6: Run feature verification**

```bash
./gradlew testDebugUnitTest --tests '*CharacterSheetViewModelTest' --stacktrace
./gradlew :app:validateDebugScreenshotTest --tests '*ProgressionSummaryCardScreenshot*'
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git commit -m "feat: show character progression history"
```

### Task 7: Run full verification and update documentation

**Files:**
- Modify: `README.md`
- Modify: `AGENTS.md`

**Interfaces:**
- Consumes: all prior tasks.
- Produces: documented progression persistence and verified foundation increment.

- [ ] **Step 1: Update architecture documentation**

Document that guided characters persist structured progression separately from mutable sheet state, manual/legacy
characters remain unmanaged, and the progression model is the future source of truth for permanent build decisions.
Keep the existing CI and screenshot commands unchanged.

- [ ] **Step 2: Run the full JVM/CI task set**

```bash
./gradlew lintDebug test testDebugUnitTest --stacktrace
```

Expected: PASS.

- [ ] **Step 3: Run instrumentation migration verification**

```bash
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.github.arhor.spellbindr.data.local.database.Migration3To4Test
```

Expected: PASS on a device/emulator. If no device exists, record the command as not run and do not claim migration
instrumentation passed.

- [ ] **Step 4: Review repository state**

```bash
git diff --check
git status --short
git log --oneline --decorate -8
```

Expected: no unstaged implementation changes and one commit per completed task.

- [ ] **Step 5: Commit documentation**

```bash
git add README.md AGENTS.md
git commit -m "docs: describe character progression storage"
```
