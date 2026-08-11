package com.github.arhor.spellbindr.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.arhor.spellbindr.data.local.database.CharacterProgressionJsonCodec
import com.github.arhor.spellbindr.data.local.database.SpellbindrDatabase
import com.github.arhor.spellbindr.data.local.database.converter.CharacterSheetConverter
import com.github.arhor.spellbindr.data.local.database.converter.EntityRefConverter
import com.github.arhor.spellbindr.data.local.database.dao.CharacterDao
import com.github.arhor.spellbindr.data.local.database.entity.CharacterEntity
import com.github.arhor.spellbindr.data.mapper.toDomain
import com.github.arhor.spellbindr.data.mapper.toEntity
import com.github.arhor.spellbindr.data.mapper.toSnapshot
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.ApplyLevelUpResult
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.CharacterSpellOwnership
import com.github.arhor.spellbindr.domain.model.CharacterSpellPreparation
import com.github.arhor.spellbindr.domain.model.ClassLevel
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.DeathSaveState
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.HitDicePoolState
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelSpellcasting
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.ManagedProgressionSheetState
import com.github.arhor.spellbindr.domain.model.ManagedSpellGrant
import com.github.arhor.spellbindr.domain.model.ManagedSpellGrantType
import com.github.arhor.spellbindr.domain.model.MultiClassing
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.ProgressionState
import com.github.arhor.spellbindr.domain.model.PactSlotState
import com.github.arhor.spellbindr.domain.model.SavingThrowEntry
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.SkillEntry
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.domain.model.SpellReplacement
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.github.arhor.spellbindr.domain.model.Weapon
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterLevelUpRepositoryIntegrationTest {

    private lateinit var database: SpellbindrDatabase
    private lateinit var dao: CharacterDao
    private lateinit var codec: CharacterProgressionJsonCodec
    private lateinit var repository: CharacterRepositoryImpl

    @Before
    fun setUp() {
        val json = Json {
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            SpellbindrDatabase::class.java,
        )
            .addTypeConverter(CharacterSheetConverter(json))
            .addTypeConverter(EntityRefConverter(json))
            .allowMainThreadQueries()
            .build()
        dao = database.characterDao()
        codec = CharacterProgressionJsonCodec(json)
        repository = CharacterRepositoryImpl(dao, codec, database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `applyLevelUp should commit sheet and progression atomically when plan is valid`() = runBlocking {
        // Given
        val progression = progression("fighter", hitDie = 10)
        val sheet = fighterSheet().copy(currentHitPoints = 99, temporaryHitPoints = -4)
        seed(sheet, progression)

        // When
        val result = repository.applyLevelUp(CHARACTER_ID, 1, fighterPlan(), fighterReferenceData())
        val stored = requireNotNull(dao.getCharacterWithProgression(CHARACTER_ID))

        // Then
        assertThat(result).isInstanceOf(ApplyLevelUpResult.Success::class.java)
        assertThat(stored.character.manualSheet?.level).isEqualTo(2)
        assertThat(stored.character.manualSheet?.currentHitPoints).isEqualTo(16)
        assertThat(stored.character.manualSheet?.temporaryHitPoints).isEqualTo(0)
        val storedProgression = stored.progression.toDomain(codec) as ProgressionState.Managed
        assertThat(storedProgression.progression.totalLevel).isEqualTo(2)
        assertThat(storedProgression.progression.levels.last().classLevel).isEqualTo(2)
    }

    @Test
    fun `applyLevelUp should reject without writes when expected level is stale`() = runBlocking {
        // Given
        val progression = progression("fighter", hitDie = 10)
        seed(fighterSheet(), progression)
        val before = dao.getCharacterWithProgression(CHARACTER_ID)

        // When
        val result = repository.applyLevelUp(
            characterId = CHARACTER_ID,
            expectedTotalLevel = 0,
            plan = fighterPlan(expectedTotalLevel = 0),
            referenceData = fighterReferenceData(),
        )
        val after = dao.getCharacterWithProgression(CHARACTER_ID)

        // Then
        assertThat(result).isEqualTo(ApplyLevelUpResult.StaleState)
        assertThat(after).isEqualTo(before)
    }

    @Test
    fun `applyLevelUp should roll back sheet update when progression write fails`() = runBlocking {
        // Given
        val progression = progression("fighter", hitDie = 10)
        seed(fighterSheet(), progression)
        val before = dao.getCharacterWithProgression(CHARACTER_ID)
        database.openHelper.writableDatabase.execSQL(
            """
            CREATE TRIGGER fail_level_up_progression
            BEFORE INSERT ON character_progressions
            BEGIN
                SELECT RAISE(ABORT, 'forced progression failure');
            END
            """.trimIndent(),
        )

        // When
        val result = repository.applyLevelUp(CHARACTER_ID, 1, fighterPlan(), fighterReferenceData())
        val after = dao.getCharacterWithProgression(CHARACTER_ID)

        // Then
        assertThat(result).isInstanceOf(ApplyLevelUpResult.PersistenceFailure::class.java)
        assertThat(after).isEqualTo(before)
    }

    @Test
    fun `applyLevelUp should apply exactly once when confirmation is duplicated`() = runBlocking {
        // Given
        val progression = progression("fighter", hitDie = 10)
        seed(fighterSheet(), progression)
        val plan = fighterPlan()
        val referenceData = fighterReferenceData()

        // When
        val first = repository.applyLevelUp(CHARACTER_ID, 1, plan, referenceData)
        val duplicate = repository.applyLevelUp(CHARACTER_ID, 1, plan, referenceData)
        val stored = requireNotNull(dao.getCharacterWithProgression(CHARACTER_ID))

        // Then
        assertThat(first).isInstanceOf(ApplyLevelUpResult.Success::class.java)
        assertThat(duplicate).isEqualTo(ApplyLevelUpResult.StaleState)
        val storedProgression = stored.progression.toDomain(codec) as ProgressionState.Managed
        assertThat(storedProgression.progression.totalLevel).isEqualTo(2)
        assertThat(stored.character.manualSheet?.maxHitPoints).isEqualTo(16)
    }

    @Test
    fun `applyLevelUp should rethrow cancellation when repository dependency is cancelled`() = runBlocking {
        // Given
        val cancellingDao = mockk<CharacterDao>()
        coEvery { cancellingDao.getCharacterWithProgression(CHARACTER_ID) } throws CancellationException("cancelled")
        val cancellingRepository = CharacterRepositoryImpl(cancellingDao, codec, database)

        // When
        val failure = runCatching {
            cancellingRepository.applyLevelUp(CHARACTER_ID, 1, fighterPlan(), fighterReferenceData())
        }.exceptionOrNull()

        // Then
        assertThat(failure).isInstanceOf(CancellationException::class.java)
    }

    @Test
    fun `applyLevelUp should preserve mutable play state and separate grants when derived capacity changes`() {
        runBlocking {
            // Given
            val progression = progression("fighter", hitDie = 10)
            val sheet = fighterSheet().copy(
                currentHitPoints = 7,
                temporaryHitPoints = 4,
                inspiration = true,
                deathSaves = DeathSaveState(successes = 2, failures = 1),
                concentrationSpellId = "bless",
                savingThrows = listOf(
                    SavingThrowEntry(AbilityIds.STR, bonus = 4, proficient = true),
                    SavingThrowEntry(AbilityIds.WIS, bonus = 2, proficient = true),
                ),
                skills = listOf(
                    SkillEntry(Skill.ATHLETICS, bonus = 4, proficient = true),
                    SkillEntry(Skill.ARCANA, bonus = 2, proficient = true),
                ),
                spellSlots = listOf(SpellSlotState(level = 1, total = 2, expended = 2)),
                languages = "Common, Goblin",
                proficiencies = "Flute",
                equipment = "Explorer's pack",
                personalityTraits = "Quiet",
                notes = "Keep this",
                characterSpells = listOf(CharacterSpell("bless", "Magic Initiate")),
                weapons = listOf(Weapon(id = "weapon-1", name = "Longsword")),
                manualProficiencyIds = setOf("tool-flute"),
                managedProgression = ManagedProgressionSheetState(
                    hitDicePools = listOf(HitDicePoolState(dieSize = 10, total = 1, expended = 1)),
                ),
            )
            seed(sheet, progression)

            // When
            val result = repository.applyLevelUp(CHARACTER_ID, 1, fighterPlan(), fighterReferenceData())
            val stored = requireNotNull(dao.getCharacterWithProgression(CHARACTER_ID))
                .character.manualSheet?.toDomain(CHARACTER_ID)

            // Then
            assertThat(result).isInstanceOf(ApplyLevelUpResult.Success::class.java)
            requireNotNull(stored)
            assertThat(stored.currentHitPoints).isEqualTo(7)
            assertThat(stored.temporaryHitPoints).isEqualTo(4)
            assertThat(stored.inspiration).isTrue()
            assertThat(stored.deathSaves).isEqualTo(sheet.deathSaves)
            assertThat(stored.concentrationSpellId).isEqualTo("bless")
            assertThat(stored.languages).isEqualTo("Common, Goblin")
            assertThat(stored.proficiencies).isEqualTo("Flute")
            assertThat(stored.equipment).isEqualTo("Explorer's pack")
            assertThat(stored.personalityTraits).isEqualTo("Quiet")
            assertThat(stored.notes).isEqualTo("Keep this")
            assertThat(stored.weapons).isEqualTo(sheet.weapons)
            assertThat(stored.manualProficiencyIds).containsExactly("tool-flute")
            assertThat(stored.managedProgression?.proficiencyIds).contains("skill-athletics")
            assertThat(stored.managedProgression?.savingThrowAbilityIds).containsExactly(
                AbilityIds.STR,
                AbilityIds.CON,
            )
            assertThat(stored.managedProgression?.hitDicePools).containsExactly(
                HitDicePoolState(dieSize = 10, total = 2, expended = 1),
            )
            assertThat(stored.savingThrows.single { it.abilityId == AbilityIds.STR }.proficient).isFalse()
            assertThat(stored.savingThrows.single { it.abilityId == AbilityIds.WIS }.proficient).isTrue()
            assertThat(stored.skills.single { it.skill == Skill.ATHLETICS }.proficient).isFalse()
            assertThat(stored.skills.single { it.skill == Skill.ARCANA }.proficient).isTrue()
            assertThat(stored.spellSlots.single { it.level == 1 }.expended).isEqualTo(0)
            assertThat(stored.characterSpells).containsExactly(CharacterSpell("bless", "Magic Initiate"))
        }
    }

    @Test
    fun `applyLevelUp should leave sheet and progression unchanged when validation fails`() = runBlocking {
        val progression = progression("fighter", hitDie = 10)
        val sheet = fighterSheet().copy(
            currentHitPoints = 6,
            temporaryHitPoints = 3,
            characterSpells = listOf(CharacterSpell("bless", "Magic Initiate")),
            manualProficiencyIds = setOf("tool-flute"),
            managedProgression = ManagedProgressionSheetState(
                hitDicePools = listOf(HitDicePoolState(dieSize = 10, total = 1, expended = 1)),
            ),
        )
        seed(sheet, progression)
        val before = requireNotNull(dao.getCharacterWithProgression(CHARACTER_ID))

        val result = repository.applyLevelUp(
            CHARACTER_ID,
            expectedTotalLevel = 1,
            plan = fighterPlan().copy(selections = LevelUpSelections(hitPointGain = HitPointGain.Fixed(1))),
            referenceData = fighterReferenceData(),
        )
        val after = requireNotNull(dao.getCharacterWithProgression(CHARACTER_ID))

        assertThat(result).isInstanceOf(ApplyLevelUpResult.ValidationFailure::class.java)
        assertThat(after).isEqualTo(before)
    }

    @Test
    fun `applyLevelUp should preserve expended pact slots within recalculated capacity`() = runBlocking {
        val progression = progression("warlock", hitDie = 8)
        seed(
            fighterSheet().copy(pactSlots = PactSlotState(slotLevel = 1, total = 1, expended = 1)),
            progression,
        )
        val plan = LevelUpPlan(
            expectedTotalLevel = 1,
            rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
            referenceDataVersion = REFERENCE_VERSION,
            selectedClassId = "warlock",
            selections = LevelUpSelections(hitPointGain = HitPointGain.Fixed(5)),
        )

        val result = repository.applyLevelUp(CHARACTER_ID, 1, plan, warlockReferenceData())
        val stored = requireNotNull(dao.getCharacterWithProgression(CHARACTER_ID))
            .character.manualSheet?.toDomain(CHARACTER_ID)

        assertThat(result).isInstanceOf(ApplyLevelUpResult.Success::class.java)
        assertThat(stored?.pactSlots).isEqualTo(PactSlotState(slotLevel = 1, total = 2, expended = 1))
    }

    @Test
    fun `applyLevelUp should replace only class owned spell when spell decision supersedes a grant`(): Unit = runBlocking {
        // Given
        val progression = progression("bard", hitDie = 8).copy(
            levels = listOf(
                progression("bard", hitDie = 8).levels.single().copy(
                    spellChanges = SpellChanges(
                        learned = setOf(ClassSpellRef("bard", "old-keep"), ClassSpellRef("bard", "old-remove")),
                        addedToSpellbook = setOf(ClassSpellRef("bard", "old-remove")),
                        featureLearned = mapOf(
                            "overlapping-feature" to setOf(ClassSpellRef("bard", "old-remove")),
                        ),
                    ),
                ),
            ),
        )
        val sheet = fighterSheet().copy(
            className = "Bard 1",
            maxHitPoints = 8,
            spellSlots = listOf(SpellSlotState(level = 1, total = 2, expended = 1)),
            concentrationSpellId = "old-remove",
            characterSpells = listOf(
                CharacterSpell("old-keep", "Bard"),
                CharacterSpell("old-remove", "Bard"),
                CharacterSpell("old-remove", "Bard"),
                CharacterSpell("old-remove", "Elf trait"),
                CharacterSpell("manual-spell", "Wizard"),
            ),
            managedProgression = ManagedProgressionSheetState(
                spellGrants = setOf(
                    ClassSpellRef("bard", "old-keep"),
                    ClassSpellRef("bard", "old-remove"),
                ),
                ownedSpellGrants = listOf(
                    ManagedSpellGrant(
                        "level:1:bard:learned:old-keep",
                        ManagedSpellGrantType.Learned,
                        ClassSpellRef("bard", "old-keep"),
                    ),
                    ManagedSpellGrant(
                        "level:1:bard:learned:old-remove",
                        ManagedSpellGrantType.Learned,
                        ClassSpellRef("bard", "old-remove"),
                    ),
                    ManagedSpellGrant(
                        "level:1:bard:spellbook:old-remove",
                        ManagedSpellGrantType.Spellbook,
                        ClassSpellRef("bard", "old-remove"),
                    ),
                    ManagedSpellGrant(
                        "level:1:bard:feature:overlapping-feature:old-remove",
                        ManagedSpellGrantType.Feature,
                        ClassSpellRef("bard", "old-remove"),
                    ),
                ),
            ),
        )
        seed(sheet, progression)
        val changes = SpellChanges(
            learned = setOf(ClassSpellRef("bard", "new-level")),
            replaced = setOf(SpellReplacement("bard", "old-remove", "replacement")),
        )

        // When
        val result = repository.applyLevelUp(
            characterId = CHARACTER_ID,
            expectedTotalLevel = 1,
            plan = LevelUpPlan(
                expectedTotalLevel = 1,
                rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
                referenceDataVersion = REFERENCE_VERSION,
                selectedClassId = "bard",
                selections = LevelUpSelections(hitPointGain = HitPointGain.Fixed(5), spellChanges = changes),
            ),
            referenceData = bardReferenceData(),
        )
        val stored = requireNotNull(dao.getCharacterWithProgression(CHARACTER_ID))
            .character.manualSheet?.toDomain(CHARACTER_ID)

        // Then
        assertThat(result).isInstanceOf(ApplyLevelUpResult.Success::class.java)
        requireNotNull(stored)
        assertThat(stored.characterSpells).containsAtLeast(
            CharacterSpell("old-keep", "bard"),
            CharacterSpell("new-level", "bard"),
            CharacterSpell("replacement", "bard"),
            CharacterSpell("old-remove", "Elf trait"),
            CharacterSpell("manual-spell", "Wizard"),
        )
        assertThat(stored.characterSpells.count {
            it.spellId == "old-remove" && it.sourceClass.equals("bard", ignoreCase = true)
        }).isEqualTo(2)
        assertThat(stored.concentrationSpellId).isEqualTo("old-remove")
        assertThat(stored.spellSlots.single { it.level == 1 }.expended).isEqualTo(1)
        assertThat(stored.managedProgression?.spellGrants).containsExactly(
            ClassSpellRef("bard", "old-keep"),
            ClassSpellRef("bard", "old-remove"),
            ClassSpellRef("bard", "new-level"),
            ClassSpellRef("bard", "replacement"),
        )
        assertThat(stored.managedProgression?.ownedSpellGrants?.count {
            it.spell == ClassSpellRef("bard", "old-remove")
        }).isEqualTo(2)
        assertThat(stored.managedProgression?.ownedSpellGrants.orEmpty().filter {
            it.spell == ClassSpellRef("bard", "old-remove")
        }.map { it.type }).containsExactly(
            ManagedSpellGrantType.Spellbook,
            ManagedSpellGrantType.Feature,
        )
    }

    @Test
    fun `applyLevelUp should persist Magic Initiate spells with feat ownership`(): Unit = runBlocking {
        val progression = CharacterProgression(
            referenceDataVersion = REFERENCE_VERSION,
            origin = ProgressionOrigin.Guided,
            levels = (1..3).map { level ->
                CharacterLevelRecord(
                    characterLevel = level,
                    classId = "fighter",
                    classLevel = level,
                    hitPointGain = HitPointGain.Fixed(if (level == 1) 10 else 6),
                )
            },
        )
        val sheet = fighterSheet().copy(level = 3, className = "Fighter 3", maxHitPoints = 22, currentHitPoints = 22)
        seed(sheet, progression)
        val plan = LevelUpPlan(
            expectedTotalLevel = 3,
            rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
            referenceDataVersion = REFERENCE_VERSION,
            selectedClassId = "fighter",
            selections = LevelUpSelections(
                hitPointGain = HitPointGain.Fixed(6),
                abilityScoreDecision = AbilityScoreDecision.Feat("magic-initiate"),
                featChoices = mapOf(
                    "magic-initiate:class-list" to setOf("wizard"),
                    "magic-initiate:cantrips" to setOf("fire-bolt", "mage-hand"),
                    "magic-initiate:first-level-spell" to setOf("magic-missile"),
                ),
            ),
        )
        fun magicSpell(id: String, level: Int) = Spell(
            id = id,
            name = id.replace('-', ' ').replaceFirstChar { it.uppercase() },
            desc = emptyList(),
            level = level,
            range = "Self",
            ritual = false,
            school = EntityRef("evocation"),
            duration = "Instantaneous",
            castingTime = "1 action",
            classes = listOf(EntityRef("wizard")),
            components = emptyList(),
            concentration = false,
            source = "test",
        )
        val data = LevelUpReferenceData(
            classes = listOf(characterClass("fighter", 10), characterClass("wizard", 6)),
            features = emptyList(),
            feats = listOf(Feat("magic-initiate", "Magic Initiate", emptyList())),
            spells = listOf(
                magicSpell("fire-bolt", 0),
                magicSpell("mage-hand", 0),
                magicSpell("magic-missile", 1),
                magicSpell("shield", 1),
            ),
            referenceDataVersion = REFERENCE_VERSION,
        )

        val result = repository.applyLevelUp(CHARACTER_ID, 3, plan, data)
        val stored = requireNotNull(dao.getCharacterWithProgression(CHARACTER_ID))
        val storedProgression = stored.progression.toDomain(codec) as ProgressionState.Managed
        val storedSheet = requireNotNull(stored.character.manualSheet?.toDomain(CHARACTER_ID))

        assertThat(result).isInstanceOf(ApplyLevelUpResult.Success::class.java)
        assertThat(storedProgression.progression.levels.last().featChoices).containsEntry(
            "magic-initiate:class-list", setOf("wizard"),
        )
        assertThat(storedSheet.characterSpells.map { it.spellId }).containsAtLeast(
            "fire-bolt", "mage-hand", "magic-missile",
        )
        assertThat(storedSheet.characterSpells.first { it.spellId == "fire-bolt" }.ownership)
            .isEqualTo(CharacterSpellOwnership.Known)
        assertThat(storedSheet.characterSpells.first { it.spellId == "fire-bolt" }.preparation)
            .isEqualTo(CharacterSpellPreparation.AlwaysPrepared)
        val featGrants = storedSheet.managedProgression?.ownedSpellGrants.orEmpty()
            .filter { ":feature:feat:magic-initiate:" in it.ownerKey }
        assertThat(featGrants.map { it.spell }).containsExactly(
            ClassSpellRef("wizard", "fire-bolt"),
            ClassSpellRef("wizard", "mage-hand"),
            ClassSpellRef("wizard", "magic-missile"),
        )
    }

    private suspend fun seed(sheet: CharacterSheet, progression: CharacterProgression) {
        val character = CharacterEntity(
            id = CHARACTER_ID,
            name = sheet.name,
            classes = mapOf(EntityRef(progression.levels.first().classId) to progression.totalLevel),
            proficiencies = sheet.allProficiencyIds.mapTo(linkedSetOf(), ::EntityRef),
            spells = sheet.characterSpells.mapTo(linkedSetOf()) { EntityRef(it.spellId) },
            manualSheet = sheet.toSnapshot(),
        )
        dao.saveCharacterWithProgression(
            character = character,
            progression = ProgressionState.Managed(progression).toEntity(CHARACTER_ID, codec),
        )
    }

    private fun fighterSheet(): CharacterSheet = CharacterSheet(
        id = CHARACTER_ID,
        name = "Aster",
        level = 1,
        className = "Fighter 1",
        maxHitPoints = 10,
        currentHitPoints = 10,
    )

    private fun progression(classId: String, hitDie: Int): CharacterProgression = CharacterProgression(
        referenceDataVersion = REFERENCE_VERSION,
        origin = ProgressionOrigin.Guided,
        levels = listOf(
            CharacterLevelRecord(
                characterLevel = 1,
                classId = classId,
                classLevel = 1,
                hitPointGain = HitPointGain.Fixed(hitDie),
            ),
        ),
    )

    private fun fighterPlan(expectedTotalLevel: Int = 1): LevelUpPlan = LevelUpPlan(
        expectedTotalLevel = expectedTotalLevel,
        rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
        referenceDataVersion = REFERENCE_VERSION,
        selectedClassId = "fighter",
        selections = LevelUpSelections(hitPointGain = HitPointGain.Fixed(6)),
    )

    private fun fighterReferenceData(): LevelUpReferenceData = LevelUpReferenceData(
        classes = listOf(
            characterClass(
                id = "fighter",
                hitDie = 10,
                proficiencies = listOf("skill-athletics"),
                savingThrows = listOf(AbilityIds.STR, AbilityIds.CON),
            ),
        ),
        features = emptyList(),
        referenceDataVersion = REFERENCE_VERSION,
    )

    private fun bardReferenceData(): LevelUpReferenceData {
        val bard = characterClass("bard", 8).copy(
            levels = (1..20).map { level ->
                ClassLevel(
                    id = "bard-$level",
                    level = level,
                    features = emptyList(),
                    spellcasting = LevelSpellcasting(
                        cantrips = 0,
                        spells = if (level == 1) 2 else 3,
                        spellSlots = mapOf("1" to if (level == 1) 2 else 3),
                    ),
                )
            },
        )
        return LevelUpReferenceData(
            classes = listOf(bard),
            features = emptyList(),
            spells = listOf("old-keep", "old-remove", "new-level", "replacement", "manual-spell").map { id ->
                Spell(
                    id = id,
                    name = id,
                    desc = emptyList(),
                    level = 1,
                    range = "Self",
                    ritual = false,
                    school = EntityRef("evocation"),
                    duration = "Instantaneous",
                    castingTime = "1 action",
                    classes = listOf(EntityRef("bard")),
                    components = emptyList(),
                    concentration = false,
                    source = "test",
                )
            },
            referenceDataVersion = REFERENCE_VERSION,
        )
    }

    private fun warlockReferenceData(): LevelUpReferenceData = LevelUpReferenceData(
        classes = listOf(characterClass("warlock", 8)),
        features = emptyList(),
        referenceDataVersion = REFERENCE_VERSION,
    )

    private fun characterClass(
        id: String,
        hitDie: Int,
        proficiencies: List<String> = emptyList(),
        savingThrows: List<String> = emptyList(),
    ): CharacterClass = CharacterClass(
        id = id,
        name = id.replaceFirstChar { it.uppercase() },
        multiClassing = MultiClassing(),
        hitDie = hitDie,
        proficiencies = proficiencies,
        proficiencyChoices = emptyList(),
        savingThrows = savingThrows,
        subclasses = emptyList(),
        levels = (1..20).map { level -> ClassLevel("$id-$level", level, emptyList()) },
    )

    private companion object {
        private const val CHARACTER_ID = "character-level-up"
        private const val REFERENCE_VERSION = "test-v1"
    }
}
