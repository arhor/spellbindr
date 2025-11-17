package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.AbilityScores
import com.github.arhor.spellbindr.data.model.CharacterSheet
import com.github.arhor.spellbindr.data.model.CharacterSpell
import com.github.arhor.spellbindr.data.model.DeathSaveState
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.model.SpellSlotState
import com.github.arhor.spellbindr.data.model.defaultSpellSlots
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.Skill
import com.github.arhor.spellbindr.data.repository.CharacterRepository
import com.github.arhor.spellbindr.data.repository.SpellRepository
import com.github.arhor.spellbindr.ui.feature.characters.CharacterSpellAssignment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterSheetViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val spellRepository: SpellRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val characterId: String? = savedStateHandle.get<String>("characterId")
    private val _selectedTab = MutableStateFlow(CharacterSheetTab.Overview)
    private val _editMode = MutableStateFlow(SheetEditMode.View)
    private val _editingState = MutableStateFlow<CharacterSheetEditingState?>(null)
    private val _hasLoaded = MutableStateFlow(characterId == null)
    private val _errors = MutableStateFlow(if (characterId == null) "Missing character id" else null)

    private var latestSheet: CharacterSheet? = null

    private val characterStream: Flow<CharacterSheet?> =
        characterId?.let { id ->
            characterRepository.observeCharacterSheet(id)
                .onEach { sheet ->
                    latestSheet = sheet
                    _hasLoaded.value = true
                    if (sheet == null) {
                        cancelEditMode()
                    }
                }
                .catch { throwable ->
                    _errors.value = throwable.message ?: "Unable to load character"
                    emit(null)
                }
        } ?: flowOf(null)

    private val uiInputs: Flow<CharacterSheetUiInputs> =
        characterStream
            .combine(_hasLoaded) { sheet, loaded ->
                CharacterSheetUiInputs(sheet = sheet, loaded = loaded)
            }
            .combine(_selectedTab) { inputs, tab ->
                inputs.copy(tab = tab)
            }
            .combine(_editMode) { inputs, mode ->
                inputs.copy(mode = mode)
            }
            .combine(_editingState) { inputs, editing ->
                inputs.copy(editing = editing)
            }
            .combine(spellRepository.allSpells) { inputs, spells ->
                inputs.copy(spells = spells)
            }

    val uiState: StateFlow<CharacterSheetUiState> =
        uiInputs
            .combine(_errors) { inputs, error ->
                when {
                    !inputs.loaded -> CharacterSheetUiState(
                        isLoading = true,
                        characterId = characterId,
                        selectedTab = inputs.tab,
                        editMode = inputs.mode,
                    )

                    inputs.sheet == null -> CharacterSheetUiState(
                        isLoading = false,
                        characterId = characterId,
                        selectedTab = inputs.tab,
                        editMode = SheetEditMode.View,
                        errorMessage = error ?: "Character not found",
                    )

                    else -> CharacterSheetUiState(
                        isLoading = false,
                        characterId = inputs.sheet.id,
                        selectedTab = inputs.tab,
                        editMode = inputs.mode,
                        header = inputs.sheet.toHeaderState(),
                        overview = inputs.sheet.toOverviewState(),
                        skills = inputs.sheet.toSkillsState(),
                        spells = inputs.sheet.toSpellsState(inputs.spells),
                        editingState = inputs.editing.takeIf { inputs.mode == SheetEditMode.Editing },
                        errorMessage = error,
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CharacterSheetUiState(isLoading = true, characterId = characterId),
            )

    fun onTabSelected(tab: CharacterSheetTab) {
        _selectedTab.value = tab
    }

    fun enterEditMode() {
        val sheet = latestSheet ?: return
        _editingState.value = CharacterSheetEditingState.fromSheet(sheet)
        _editMode.value = SheetEditMode.Editing
        _errors.value = null
    }

    fun cancelEditMode() {
        _editingState.value = null
        _editMode.value = SheetEditMode.View
    }

    fun saveInlineEdits() {
        val edits = _editingState.value ?: return
        updateSheet { sheet ->
            sheet.applyInlineEdits(edits)
        }
        cancelEditMode()
    }

    fun onMaxHpEdited(value: String) = updateEditingState { it.copy(maxHp = value.filterDigits()) }
    fun onCurrentHpEdited(value: String) = updateEditingState { it.copy(currentHp = value.filterDigits()) }
    fun onTemporaryHpEdited(value: String) = updateEditingState { it.copy(tempHp = value.filterDigits()) }
    fun onSpeedEdited(value: String) = updateEditingState { it.copy(speed = value) }
    fun onHitDiceEdited(value: String) = updateEditingState { it.copy(hitDice = value) }
    fun onSensesEdited(value: String) = updateEditingState { it.copy(senses = value) }
    fun onLanguagesEdited(value: String) = updateEditingState { it.copy(languages = value) }
    fun onProficienciesEdited(value: String) = updateEditingState { it.copy(proficiencies = value) }
    fun onEquipmentEdited(value: String) = updateEditingState { it.copy(equipment = value) }

    fun adjustCurrentHp(delta: Int) {
        updateSheet { sheet ->
            val maxHp = sheet.maxHitPoints.coerceAtLeast(0)
            val next = (sheet.currentHitPoints + delta).coerceIn(0, maxHp)
            sheet.copy(currentHitPoints = next)
        }
    }

    fun setCurrentHp(value: Int) {
        updateSheet { sheet ->
            val maxHp = sheet.maxHitPoints.coerceAtLeast(0)
            sheet.copy(currentHitPoints = value.coerceIn(0, maxHp))
        }
    }

    fun setTemporaryHp(value: Int) {
        updateSheet { sheet ->
            sheet.copy(temporaryHitPoints = value.coerceAtLeast(0))
        }
    }

    fun setDeathSaveSuccesses(count: Int) {
        updateSheet { sheet ->
            sheet.copy(
                deathSaves = sheet.deathSaves.copy(successes = count.coerceIn(0, 3)),
            )
        }
    }

    fun setDeathSaveFailures(count: Int) {
        updateSheet { sheet ->
            sheet.copy(
                deathSaves = sheet.deathSaves.copy(failures = count.coerceIn(0, 3)),
            )
        }
    }

    fun toggleSpellSlot(level: Int, slotIndex: Int) {
        updateSheet { sheet ->
            sheet.updateSpellSlot(level) { slot ->
                val totalSlots = slot.total.coerceAtLeast(0)
                if (totalSlots == 0) return@updateSpellSlot slot
                val normalizedIndex = slotIndex.coerceIn(0, totalSlots - 1)
                val newExpended =
                    if (normalizedIndex < slot.expended) normalizedIndex
                    else (normalizedIndex + 1).coerceAtMost(totalSlots)
                slot.copy(expended = newExpended)
            }
        }
    }

    fun setSpellSlotTotal(level: Int, total: Int) {
        updateSheet { sheet ->
            sheet.updateSpellSlot(level) { slot ->
                val safeTotal = total.coerceAtLeast(0)
                slot.copy(
                    total = safeTotal,
                    expended = slot.expended.coerceIn(0, safeTotal),
                )
            }
        }
    }

    fun removeSpell(spellId: String, sourceClass: String) {
        updateSheet { sheet ->
            val updated = sheet.characterSpells.filterNot {
                it.spellId == spellId && it.sourceClass.equals(sourceClass, ignoreCase = true)
            }
            sheet.copy(characterSpells = updated)
        }
    }

    fun addSpells(assignments: List<CharacterSpellAssignment>) {
        if (assignments.isEmpty()) return
        updateSheet { sheet ->
            val existing = sheet.characterSpells.toMutableList()
            assignments.forEach { assignment ->
                val normalizedClass = assignment.sourceClass.trim()
                val newSpell = CharacterSpell(
                    spellId = assignment.spellId,
                    sourceClass = normalizedClass,
                )
                val alreadyPresent = existing.any {
                    it.spellId == newSpell.spellId && it.sourceClass.equals(newSpell.sourceClass, ignoreCase = true)
                }
                if (!alreadyPresent) {
                    existing += newSpell
                }
            }
            sheet.copy(characterSpells = existing)
        }
    }

    private fun updateEditingState(transform: (CharacterSheetEditingState) -> CharacterSheetEditingState) {
        _editingState.update { current ->
            current?.let(transform)
        }
    }

    private fun updateSheet(transform: (CharacterSheet) -> CharacterSheet) {
        val sheet = latestSheet ?: return
        val transformed = transform(sheet)
        val updated = transformed.clearDeathSavesIfConscious()
        persist(updated)
    }

    private fun persist(updated: CharacterSheet) {
        if (characterId == null) return
        latestSheet = updated
        viewModelScope.launch {
            _errors.value = null
            runCatching { characterRepository.upsertCharacterSheet(updated) }
                .onFailure { throwable ->
                    _errors.value = throwable.message ?: "Unable to save changes"
                }
        }
    }
}

@Immutable
data class CharacterSheetUiState(
    val isLoading: Boolean = false,
    val characterId: String? = null,
    val selectedTab: CharacterSheetTab = CharacterSheetTab.Overview,
    val editMode: SheetEditMode = SheetEditMode.View,
    val header: CharacterHeaderUiState? = null,
    val overview: OverviewTabState? = null,
    val skills: SkillsTabState? = null,
    val spells: SpellsTabState? = null,
    val editingState: CharacterSheetEditingState? = null,
    val errorMessage: String? = null,
)

@Immutable
data class CharacterHeaderUiState(
    val name: String,
    val subtitle: String,
    val hitPoints: HitPointSummary,
    val armorClass: Int,
    val initiative: Int,
    val speed: String,
    val proficiencyBonus: Int,
    val inspiration: Boolean,
)

@Immutable
data class HitPointSummary(
    val max: Int,
    val current: Int,
    val temporary: Int,
)

@Immutable
data class OverviewTabState(
    val abilities: List<AbilityUiModel>,
    val hitDice: String,
    val senses: String,
    val languages: String,
    val proficiencies: String,
    val equipment: String,
    val background: String,
    val race: String,
    val alignment: String,
    val deathSaves: DeathSaveUiState,
)

@Immutable
data class AbilityUiModel(
    val ability: Ability,
    val label: String,
    val score: Int,
    val modifier: Int,
    val savingThrowBonus: Int,
    val savingThrowProficient: Boolean,
)

@Immutable
data class DeathSaveUiState(
    val successes: Int,
    val failures: Int,
)

@Immutable
data class SkillsTabState(
    val skills: List<SkillUiModel>,
)

@Immutable
data class SkillUiModel(
    val id: Skill,
    val name: String,
    val abilityAbbreviation: String,
    val totalBonus: Int,
    val proficient: Boolean,
    val expertise: Boolean,
)

@Immutable
data class SpellsTabState(
    val spellcastingGroups: List<SpellcastingGroupUiModel>,
    val spellSlots: List<SpellSlotUiModel>,
    val canAddSpells: Boolean,
)

@Immutable
data class SpellcastingGroupUiModel(
    val sourceKey: String,
    val sourceLabel: String,
    val spells: List<CharacterSpellUiModel>,
)

@Immutable
data class CharacterSpellUiModel(
    val spellId: String,
    val name: String,
    val level: Int,
    val school: String,
    val castingTime: String,
)

@Immutable
data class SpellSlotUiModel(
    val level: Int,
    val total: Int,
    val expended: Int,
)

@Immutable
data class CharacterSheetEditingState(
    val maxHp: String,
    val currentHp: String,
    val tempHp: String,
    val speed: String,
    val hitDice: String,
    val senses: String,
    val languages: String,
    val proficiencies: String,
    val equipment: String,
) {
    companion object {
        fun fromSheet(sheet: CharacterSheet): CharacterSheetEditingState = CharacterSheetEditingState(
            maxHp = sheet.maxHitPoints.toString(),
            currentHp = sheet.currentHitPoints.toString(),
            tempHp = sheet.temporaryHitPoints.toString(),
            speed = sheet.speed,
            hitDice = sheet.hitDice,
            senses = sheet.senses,
            languages = sheet.languages,
            proficiencies = sheet.proficiencies,
            equipment = sheet.equipment,
        )
    }
}

private fun CharacterSheet.toHeaderState(): CharacterHeaderUiState {
    val subtitleParts = buildList {
        val levelLabel =
            if (className.isBlank()) "Level $level" else "Level $level ${className.trim()}"
        add(levelLabel)
        race.takeIf { it.isNotBlank() }?.let { add(it.trim()) }
        background.takeIf { it.isNotBlank() }?.let { add(it.trim()) }
    }
    return CharacterHeaderUiState(
        name = name.ifBlank { "Unnamed hero" },
        subtitle = subtitleParts.joinToString(" • "),
        hitPoints = HitPointSummary(
            max = maxHitPoints,
            current = currentHitPoints,
            temporary = temporaryHitPoints,
        ),
        armorClass = armorClass,
        initiative = initiative,
        speed = speed.ifBlank { "—" },
        proficiencyBonus = proficiencyBonus,
        inspiration = inspiration,
    )
}

private fun CharacterSheet.toOverviewState(): OverviewTabState {
    val savingThrowLookup = savingThrows.associateBy { it.ability }
    val abilityModels = Ability.entries.map { ability ->
        val modifier = abilityScores.modifierFor(ability)
        val entry = savingThrowLookup[ability]
        val proficiencyBonusValue = if (entry?.proficient == true) proficiencyBonus else 0
        AbilityUiModel(
            ability = ability,
            label = ability.name,
            score = abilityScores.scoreFor(ability),
            modifier = modifier,
            savingThrowBonus = modifier + (entry?.bonus ?: 0) + proficiencyBonusValue,
            savingThrowProficient = entry?.proficient ?: false,
        )
    }

    return OverviewTabState(
        abilities = abilityModels,
        hitDice = hitDice.ifBlank { "—" },
        senses = senses,
        languages = languages,
        proficiencies = proficiencies,
        equipment = equipment,
        background = background,
        race = race,
        alignment = alignment,
        deathSaves = DeathSaveUiState(
            successes = deathSaves.successes.coerceIn(0, 3),
            failures = deathSaves.failures.coerceIn(0, 3),
        ),
    )
}

private fun CharacterSheet.toSkillsState(): SkillsTabState {
    val entries = skills.associateBy { it.skill }
    val models = Skill.entries.map { skill ->
        val entry = entries[skill]
        val proficiencyBonus = when {
            entry?.expertise == true -> this.proficiencyBonus * 2
            entry?.proficient == true -> this.proficiencyBonus
            else -> 0
        }
        SkillUiModel(
            id = skill,
            name = skill.displayName,
            abilityAbbreviation = skill.ability.name,
            totalBonus = abilityScores.modifierFor(skill.ability) + proficiencyBonus + (entry?.bonus ?: 0),
            proficient = entry?.proficient ?: false,
            expertise = entry?.expertise ?: false,
        )
    }
    return SkillsTabState(models)
}

private fun CharacterSheet.toSpellsState(allSpells: List<Spell>): SpellsTabState {
    val spellLookup = allSpells.associateBy { it.id }
    val spellGroups = characterSpells
        .groupBy { it.sourceClass }
        .map { (sourceKey, spells) ->
            val displayLabel = sourceKey.takeIf { it.isNotBlank() } ?: className.ifBlank { "Spellbook" }
            val entries = spells.map { stored ->
                val spell = spellLookup[stored.spellId]
                CharacterSpellUiModel(
                    spellId = stored.spellId,
                    name = spell?.name ?: stored.spellId,
                    level = spell?.level ?: 0,
                    school = spell?.school?.prettyString() ?: "—",
                    castingTime = spell?.castingTime ?: "",
                )
            }.sortedWith(
                compareBy<CharacterSpellUiModel> { it.level }.thenBy { it.name.lowercase() }
            )
            SpellcastingGroupUiModel(
                sourceKey = sourceKey,
                sourceLabel = displayLabel,
                spells = entries,
            )
        }
        .sortedBy { it.sourceLabel.lowercase() }

    val normalizedSlots = if (spellSlots.isEmpty()) defaultSpellSlots() else spellSlots
    val slots = normalizedSlots
        .sortedBy { it.level }
        .map { slot ->
            SpellSlotUiModel(
                level = slot.level,
                total = slot.total,
                expended = slot.expended.coerceIn(0, slot.total.coerceAtLeast(0)),
            )
        }

    return SpellsTabState(
        spellcastingGroups = spellGroups,
        spellSlots = slots,
        canAddSpells = true,
    )
}

private fun CharacterSheet.applyInlineEdits(edits: CharacterSheetEditingState): CharacterSheet {
    val newMaxHp = edits.maxHp.toIntOrNull()?.coerceAtLeast(1) ?: maxHitPoints
    val newCurrentHp = edits.currentHp.toIntOrNull()?.coerceIn(0, newMaxHp) ?: currentHitPoints.coerceIn(0, newMaxHp)
    val newTempHp = edits.tempHp.toIntOrNull()?.coerceAtLeast(0) ?: temporaryHitPoints
    return copy(
        maxHitPoints = newMaxHp,
        currentHitPoints = newCurrentHp,
        temporaryHitPoints = newTempHp,
        speed = edits.speed.trim(),
        hitDice = edits.hitDice.trim(),
        senses = edits.senses.trim(),
        languages = edits.languages.trim(),
        proficiencies = edits.proficiencies.trim(),
        equipment = edits.equipment.trim(),
    )
}

private fun CharacterSheet.updateSpellSlot(
    level: Int,
    transform: (SpellSlotState) -> SpellSlotState,
): CharacterSheet {
    val normalized = if (spellSlots.isEmpty()) defaultSpellSlots() else spellSlots
    val slotMap = normalized.associateBy { it.level }.toMutableMap()
    val current = slotMap[level] ?: SpellSlotState(level = level)
    slotMap[level] = transform(current)
    return copy(spellSlots = slotMap.values.sortedBy { it.level })
}

private fun AbilityScores.scoreFor(ability: Ability): Int = when (ability) {
    Ability.STR -> strength
    Ability.DEX -> dexterity
    Ability.CON -> constitution
    Ability.INT -> intelligence
    Ability.WIS -> wisdom
    Ability.CHA -> charisma
}

private fun String.filterDigits(): String = filter { it.isDigit() }

internal fun CharacterSheet.clearDeathSavesIfConscious(): CharacterSheet {
    return if (currentHitPoints > 0 && (deathSaves.successes != 0 || deathSaves.failures != 0)) {
        copy(deathSaves = DeathSaveState())
    } else {
        this
    }
}

private data class CharacterSheetUiInputs(
    val sheet: CharacterSheet? = null,
    val loaded: Boolean = false,
    val tab: CharacterSheetTab = CharacterSheetTab.Overview,
    val mode: SheetEditMode = SheetEditMode.View,
    val editing: CharacterSheetEditingState? = null,
    val spells: List<Spell> = emptyList(),
)
