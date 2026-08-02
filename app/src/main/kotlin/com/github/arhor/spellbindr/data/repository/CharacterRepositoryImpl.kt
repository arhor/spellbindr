package com.github.arhor.spellbindr.data.repository

import androidx.room.withTransaction
import com.github.arhor.spellbindr.data.local.database.CharacterProgressionJsonCodec
import com.github.arhor.spellbindr.data.local.database.SpellbindrDatabase
import com.github.arhor.spellbindr.data.local.database.dao.CharacterDao
import com.github.arhor.spellbindr.data.local.database.entity.CharacterEntity
import com.github.arhor.spellbindr.data.mapper.toDomain
import com.github.arhor.spellbindr.data.mapper.toEntity
import com.github.arhor.spellbindr.data.mapper.toSnapshot
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.ApplyLevelUpResult
import com.github.arhor.spellbindr.domain.model.Character
import com.github.arhor.spellbindr.domain.model.CharacterCreationResult
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterWithProgression
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.HitDicePoolState
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceRules
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
import com.github.arhor.spellbindr.domain.model.ManagedProgressionSheetState
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.ProgressionState
import com.github.arhor.spellbindr.domain.model.SpellLearningPolicy
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import com.github.arhor.spellbindr.domain.usecase.LevelUpProgressionEngine
import com.github.arhor.spellbindr.utils.asLoadableFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [CharacterRepository] backed by a Room database.
 *
 * This repository manages the bidirectional mapping between:
 * - [CharacterSheet] (User inputs from the editor)
 * - [CharacterEntity] (Persistence model)
 * - [Character] (Domain model for gameplay logic)
 */
@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val characterDao: CharacterDao,
    private val progressionJsonCodec: CharacterProgressionJsonCodec,
    private val database: SpellbindrDatabase,
) : CharacterRepository {

    override fun observeCharacterSheets(): Flow<Loadable<List<CharacterSheet>>> =
        characterDao.getAllCharacters()
            .map { it.mapNotNull(::entityToCharacterSheet) }
            .asLoadableFlow()

    override fun observeCharacterSheet(id: String): Flow<CharacterSheet?> =
        characterDao.getCharacterById(id).map {
            it?.manualSheet?.toDomain(it.id, it.proficiencies.mapTo(linkedSetOf()) { ref -> ref.id })
        }

    override fun observeCharacterWithProgression(id: String): Flow<CharacterWithProgression?> =
        characterDao.observeCharacterWithProgression(id).map { relation ->
            relation?.let {
                val sheet = it.character.manualSheet?.toDomain(
                    it.character.id,
                    it.character.proficiencies.mapTo(linkedSetOf()) { ref -> ref.id },
                ) ?: return@map null
                CharacterWithProgression(
                    sheet = sheet,
                    progressionState = it.progression.toDomain(progressionJsonCodec),
                )
            }
        }

    override fun observeCharacterSheetState(id: String): Flow<Loadable<CharacterSheet?>> =
        characterDao.getCharacterById(id)
            .map(::entityToCharacterSheet)
            .asLoadableFlow()

    /**
     * Saves a character sheet.
     * This method fetches the existing entity (if any) to preserve fields not present in the sheet
     * (though currently, the sheet drives most of the entity state via mapping).
     */
    override suspend fun upsertCharacterSheet(sheet: CharacterSheet) {
        val existing = characterDao.getCharacterById(sheet.id).firstOrNull()
        val base = existing ?: CharacterEntity(id = sheet.id)
        characterDao.saveCharacter(sheet.toCharacterEntity(base))
    }

    override suspend fun saveGuidedCharacter(result: CharacterCreationResult) {
        characterDao.saveCharacterWithProgression(
            character = result.sheet.toCharacterEntity(CharacterEntity(id = result.sheet.id)),
            progression = ProgressionState.Managed(result.progression).toEntity(
                characterId = result.sheet.id,
                codec = progressionJsonCodec,
            ),
        )
    }

    override suspend fun applyLevelUp(
        characterId: String,
        expectedTotalLevel: Int,
        plan: LevelUpPlan,
        referenceData: LevelUpReferenceData,
    ): ApplyLevelUpResult = try {
        database.withTransaction {
            val relation = characterDao.getCharacterWithProgression(characterId)
                ?: return@withTransaction ApplyLevelUpResult.MissingCharacter
            val sheet = relation.character.manualSheet?.toDomain(
                characterId,
                relation.character.proficiencies.mapTo(linkedSetOf()) { ref -> ref.id },
            )
                ?: return@withTransaction ApplyLevelUpResult.MissingCharacter
            val state = runCatching { relation.progression.toDomain(progressionJsonCodec) }.getOrElse {
                return@withTransaction ApplyLevelUpResult.ValidationFailure(
                    listOf(LevelUpValidationIssue(
                        code = LevelUpValidationCode.CorruptProgression,
                        message = "Stored progression data is corrupt and cannot be leveled up.",
                        severity = LevelUpValidationSeverity.Blocking,
                    ))
                )
            }
            val progression = when (state) {
                ProgressionState.Unmanaged -> return@withTransaction ApplyLevelUpResult.UnmanagedCharacter
                is ProgressionState.Managed -> state.progression
            }
            if (progression.totalLevel != expectedTotalLevel || plan.expectedTotalLevel != expectedTotalLevel) {
                return@withTransaction ApplyLevelUpResult.StaleState
            }

            val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData)
            if (!preview.canConfirm) return@withTransaction ApplyLevelUpResult.ValidationFailure(preview.validations)
            val clazz = plan.selectedClassId?.let(referenceData.classesById::get)
                ?: return@withTransaction ApplyLevelUpResult.ValidationFailure(preview.validations)
            val nextClassLevel = progression.classLevels.getOrDefault(clazz.id, 0) + 1
            val record = LevelUpProgressionEngine.recordFor(
                plan = plan,
                clazz = clazz,
                classLevel = nextClassLevel,
                progression = progression,
                referenceData = referenceData,
                validations = preview.validations,
            )
            val updatedProgression = progression.copy(levels = progression.levels + record)
            val updatedSheet = sheet.materializeManagedLevel(preview.after, record, referenceData)
            val updatedEntity = updatedSheet.toCharacterEntity(relation.character).copy(
                classes = preview.after.classLevels.mapKeys { EntityRef(it.key) },
                abilityScores = updatedSheet.toAbilityScoreMap(),
                // Structured entity fields can also contain user-authored values. Never replace them.
                proficiencies = updatedSheet.allProficiencyIds.mapTo(linkedSetOf(), ::EntityRef),
                spells = relation.character.spells + updatedSheet.characterSpells.map { EntityRef(it.spellId) },
            )
            characterDao.saveCharacterWithProgression(
                character = updatedEntity,
                progression = ProgressionState.Managed(updatedProgression).toEntity(characterId, progressionJsonCodec),
            )
            ApplyLevelUpResult.Success(updatedSheet, updatedProgression)
        }
    } catch (failure: Throwable) {
        ApplyLevelUpResult.PersistenceFailure(failure.message ?: "Unable to save level-up changes.")
    }

    override fun getCharacters(): Flow<List<Character>> {
        return characterDao.getAllCharacters().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCharacter(id: String): Flow<Character?> {
        return characterDao.getCharacterById(id).map { it?.toDomain() }
    }

    override suspend fun saveCharacter(character: Character) {
        characterDao.saveCharacter(character.toEntity())
    }

    override suspend fun deleteCharacter(id: String) {
        characterDao.deleteCharacter(id)
    }

    private fun entityToCharacterSheet(entity: CharacterEntity?): CharacterSheet? =
        entity?.manualSheet?.toDomain(entity.id, entity.proficiencies.mapTo(linkedSetOf()) { ref -> ref.id })

    private fun CharacterSheet.toCharacterEntity(base: CharacterEntity): CharacterEntity = base.copy(
        id = id,
        name = name,
        race = race.asEntityRef("race", id),
        background = background.asEntityRef("background", id),
        classes = toClassLevels(),
        abilityScores = toAbilityScoreMap(),
        proficiencies = allProficiencyIds.mapTo(linkedSetOf(), ::EntityRef),
        manualSheet = toSnapshot(),
    )

    private fun String.asEntityRef(prefix: String, id: String): EntityRef =
        EntityRef(this.takeIf { it.isNotBlank() } ?: "${prefix}_$id")

    private fun CharacterSheet.toClassLevels(): Map<EntityRef, Int> =
        if (className.isBlank()) emptyMap() else mapOf(
            EntityRef(className.trim()) to level.coerceAtLeast(1)
        )

    private fun CharacterSheet.toAbilityScoreMap(): Map<EntityRef, Int> = buildMap {
        put(EntityRef(AbilityIds.STR), abilityScores.strength)
        put(EntityRef(AbilityIds.DEX), abilityScores.dexterity)
        put(EntityRef(AbilityIds.CON), abilityScores.constitution)
        put(EntityRef(AbilityIds.INT), abilityScores.intelligence)
        put(EntityRef(AbilityIds.WIS), abilityScores.wisdom)
        put(EntityRef(AbilityIds.CHA), abilityScores.charisma)
    }

    private fun CharacterSheet.materializeManagedLevel(
        after: com.github.arhor.spellbindr.domain.model.LevelUpSnapshot,
        record: com.github.arhor.spellbindr.domain.model.CharacterLevelRecord,
        referenceData: LevelUpReferenceData,
    ): CharacterSheet {
        val existingPools = managedProgression?.hitDicePools.orEmpty().associateBy(HitDicePoolState::dieSize)
        val pools = after.hitDicePools.map { pool ->
            HitDicePoolState(
                dieSize = pool.dieSize,
                total = pool.total,
                expended = existingPools[pool.dieSize]?.expended.orZero().coerceIn(0, pool.total),
            )
        }
        val existingSlots = spellSlots.associateBy { it.level }
        val slots = (1..9).map { level ->
            val total = after.sharedSpellSlots[level].orZero()
            com.github.arhor.spellbindr.domain.model.SpellSlotState(
                level = level,
                total = total,
                expended = existingSlots[level]?.expended.orZero().coerceIn(0, total),
            )
        }
        val pact = after.pactMagic?.let { capacity ->
            com.github.arhor.spellbindr.domain.model.PactSlotState(
                slotLevel = capacity.slotLevel,
                total = capacity.slots,
                expended = pactSlots?.expended.orZero().coerceIn(0, capacity.slots),
            )
        }
        val selectedClass = referenceData.classesById[record.classId]
        val changedSpells = characterSpells
            .filterNot { spell -> record.spellChanges.replaced.any { replacement ->
                val sourceMatches = spell.sourceClass.equals(replacement.classId, ignoreCase = true) ||
                    spell.sourceClass.equals(selectedClass?.name, ignoreCase = true)
                sourceMatches && replacement.removedSpellId == spell.spellId
            } }
            .toMutableSet()
        (record.spellChanges.learned + record.spellChanges.addedToSpellbook +
            record.spellChanges.replaced.map { replacement ->
                com.github.arhor.spellbindr.domain.model.ClassSpellRef(
                    replacement.classId,
                    replacement.learnedSpellId,
                )
            }).forEach { spell ->
            changedSpells += com.github.arhor.spellbindr.domain.model.CharacterSpell(spell.spellId, spell.classId)
        }
        val spellPolicy = LevelUpReferenceRules.policyFor(record.classId)?.spells
        if (selectedClass != null && spellPolicy is SpellLearningPolicy.Prepared) {
            val maxSpellLevel = selectedClass.levels.firstOrNull { it.level == record.classLevel }
                ?.spellcasting?.spellSlots.orEmpty().keys.mapNotNull(String::toIntOrNull).maxOrNull().orZero()
            val capacity = (record.classLevel / spellPolicy.preparation.levelDivisor +
                after.abilityScores.modifierFor(spellPolicy.preparation.abilityId))
                .coerceAtLeast(spellPolicy.preparation.minimumPreparedSpells)
            var retainedPrepared = 0
            changedSpells.removeAll { stored ->
                val belongsToClass = stored.sourceClass.equals(selectedClass.id, ignoreCase = true) ||
                    stored.sourceClass.equals(selectedClass.name, ignoreCase = true)
                val spell = referenceData.spellsById[stored.spellId]
                when {
                    !belongsToClass -> false
                    spell == null -> true
                    spell.level == 0 -> false
                    else -> {
                        val eligible = selectedClass.id in spell.classes.map { it.id } && spell.level <= maxSpellLevel
                        val preserve = eligible && retainedPrepared < capacity
                        if (preserve) retainedPrepared++
                        !preserve
                    }
                }
            }
        }
        return copy(
            level = after.totalLevel,
            className = after.classDisplayName,
            abilityScores = after.abilityScores,
            proficiencyBonus = after.proficiencyBonus,
            maxHitPoints = after.maximumHitPoints,
            // Managed pools replace the old free-text hit-dice field; manual sheets remain untouched.
            hitDice = "",
            spellSlots = slots,
            pactSlots = pact,
            savingThrows = savingThrows.map { save ->
                save.copy(proficient = save.proficient || save.abilityId in after.savingThrowAbilityIds)
            },
            skills = skills.map { skill ->
                val skillId = "skill-${skill.skill.name.lowercase().replace('_', '-')}"
                skill.copy(proficient = skill.proficient || skillId in after.proficiencyIds)
            },
            characterSpells = changedSpells.sortedWith(compareBy({ it.sourceClass }, { it.spellId })),
            managedProgression = ManagedProgressionSheetState(
                hitDicePools = pools,
                proficiencyIds = after.proficiencyIds,
                savingThrowAbilityIds = after.savingThrowAbilityIds,
                featureIds = after.featureIds,
                languageIds = after.languageIds,
            ),
        )
    }

    private fun Int?.orZero(): Int = this ?: 0
}
