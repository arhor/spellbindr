package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.DeathSaveState
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.Weapon
import com.github.arhor.spellbindr.domain.model.abbreviation
import com.github.arhor.spellbindr.domain.model.defaultSpellSlots
import com.github.arhor.spellbindr.domain.usecase.DeleteCharacterUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveWeaponCatalogUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ToggleSpellSlotUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateHitPointsUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateWeaponListUseCase
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetTab
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.utils.signed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the Character Sheet screen.
 *
 * Manages the state of the character sheet, including:
 * - Loading and displaying character data.
 * - Handling edits (inline and modal).
 * - Managing spell slots and HP.
 * - Handling equipment and weapon management.
 * - Persisting changes to the repository.
 *
 * The UI state is exposed via [uiState] and side effects via [effects].
 */
@HiltViewModel
class CharacterSheetViewModel @Inject constructor(
    private val deleteCharacterUseCase: DeleteCharacterUseCase,
    private val loadCharacterSheetUseCase: LoadCharacterSheetUseCase,
    private val observeAllSpellsUseCase: ObserveAllSpellsUseCase,
    private val observeWeaponCatalogUseCase: ObserveWeaponCatalogUseCase,
    private val saveCharacterSheetUseCase: SaveCharacterSheetUseCase,
    private val updateHitPointsUseCase: UpdateHitPointsUseCase,
    private val toggleSpellSlotUseCase: ToggleSpellSlotUseCase,
    private val updateWeaponListUseCase: UpdateWeaponListUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    sealed interface CharacterSheetEffect {
        data object CharacterDeleted : CharacterSheetEffect
    }

    private val characterId: String? = savedStateHandle.get<String>("characterId")
    private val _uiState = MutableStateFlow<CharacterSheetUiState>(
        if (characterId == null) CharacterSheetUiState.Error("Missing character id") else CharacterSheetUiState.Loading,
    )
    val uiState: StateFlow<CharacterSheetUiState> = _uiState
    private val _effects = MutableSharedFlow<CharacterSheetEffect>()
    val effects = _effects.asSharedFlow()

    private var hasLoaded = characterId == null
    private var currentSheet: CharacterSheet? = null
    private var cachedSpells: List<Spell> = emptyList()
    private var weaponCatalog: List<WeaponCatalogUiModel> = emptyList()
    private var selectedTab: CharacterSheetTab = CharacterSheetTab.Overview
    private var selectedSpellSourceId: String? = null
    private var editMode: SheetEditMode = SheetEditMode.View
    private var editingState: CharacterSheetEditingState? = null
    private var weaponEditorState: WeaponEditorState? = null
    private var isWeaponCatalogVisible: Boolean = false
    private var errorMessage: String? = if (characterId == null) "Missing character id" else null

    init {
        if (characterId == null) {
            renderState()
        } else {
            observeCharacter(characterId)
            observeSpells()
            observeWeaponCatalog()
        }
    }

    fun selectTab(tab: CharacterSheetTab) {
        if (tab != selectedTab) {
            selectedTab = tab
            renderState()
        }
    }

    fun enterEditMode() {
        val sheet = currentSheet ?: return
        editingState = CharacterSheetEditingState.fromSheet(sheet)
        editMode = SheetEditMode.Editing
        errorMessage = null
        renderState()
    }

    fun cancelEditMode() {
        editingState = null
        editMode = SheetEditMode.View
        renderState()
    }

    fun saveInlineEdits() {
        val edits = editingState ?: return
        updateSheet { sheet -> sheet.applyInlineEdits(edits) }
        cancelEditMode()
    }

    fun adjustCurrentHp(delta: Int) {
        updateSheet { sheet ->
            updateHitPointsUseCase(sheet, UpdateHitPointsUseCase.Action.AdjustCurrentHp(delta))
        }
    }

    fun setTemporaryHp(value: Int) {
        updateSheet { sheet ->
            updateHitPointsUseCase(sheet, UpdateHitPointsUseCase.Action.SetTemporaryHp(value))
        }
    }

    fun setMaxHp(value: String) {
        updateEditingState { it.copy(maxHp = value.filterDigits()) }
    }

    fun setCurrentHp(value: String) {
        updateEditingState { it.copy(currentHp = value.filterDigits()) }
    }

    fun setTempHp(value: String) {
        updateEditingState { it.copy(tempHp = value.filterDigits()) }
    }

    fun setSpeed(value: String) {
        updateEditingState { it.copy(speed = value) }
    }

    fun setHitDice(value: String) {
        updateEditingState { it.copy(hitDice = value) }
    }

    fun setSenses(value: String) {
        updateEditingState { it.copy(senses = value) }
    }

    fun setLanguages(value: String) {
        updateEditingState { it.copy(languages = value) }
    }

    fun setProficiencies(value: String) {
        updateEditingState { it.copy(proficiencies = value) }
    }

    fun setEquipment(value: String) {
        updateEditingState { it.copy(equipment = value) }
    }

    fun setDeathSaveSuccesses(count: Int) {
        updateSheet { sheet ->
            updateHitPointsUseCase(sheet, UpdateHitPointsUseCase.Action.SetDeathSaveSuccesses(count))
        }
    }

    fun setDeathSaveFailures(count: Int) {
        updateSheet { sheet ->
            updateHitPointsUseCase(sheet, UpdateHitPointsUseCase.Action.SetDeathSaveFailures(count))
        }
    }

    fun toggleSpellSlot(level: Int, slotIndex: Int) {
        updateSheet { sheet ->
            toggleSpellSlotUseCase(sheet, ToggleSpellSlotUseCase.Action.Toggle(level, slotIndex))
        }
    }

    fun setSpellSlotTotal(level: Int, total: Int) {
        updateSheet { sheet ->
            toggleSpellSlotUseCase(sheet, ToggleSpellSlotUseCase.Action.SetTotal(level, total))
        }
    }

    fun togglePactSlot(slotIndex: Int) {
        updateSheet { sheet ->
            toggleSpellSlotUseCase(sheet, ToggleSpellSlotUseCase.Action.TogglePact(slotIndex))
        }
    }

    fun setPactSlotTotal(total: Int) {
        updateSheet { sheet ->
            toggleSpellSlotUseCase(sheet, ToggleSpellSlotUseCase.Action.SetPactTotal(total))
        }
    }

    fun clearConcentration() {
        updateSheet { sheet -> sheet.copy(concentrationSpellId = null) }
    }

    fun selectSpellSource(sourceId: String?) {
        if (selectedSpellSourceId != sourceId) {
            selectedSpellSourceId = sourceId
            renderState()
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

    fun startNewWeapon() {
        weaponEditorState = WeaponEditorState()
        renderState()
    }

    fun selectWeapon(id: String) {
        val sheet = currentSheet ?: return
        sheet.weapons.firstOrNull { it.id == id }
            ?.let { weapon ->
                weaponEditorState = WeaponEditorState.fromWeapon(weapon)
                renderState()
            }
    }

    fun deleteWeapon(id: String) {
        updateSheet { sheet ->
            updateWeaponListUseCase(sheet, UpdateWeaponListUseCase.Action.Delete(id))
        }
        if (weaponEditorState?.id == id) {
            weaponEditorState = null
            renderState()
        }
    }

    fun dismissWeaponEditor() {
        weaponEditorState = null
        renderState()
    }

    fun setWeaponName(value: String) {
        updateWeaponEditor { it.copy(name = value) }
    }

    fun setWeaponAbility(abilityId: AbilityId) {
        updateWeaponEditor { it.copy(abilityId = abilityId) }
    }

    fun setWeaponUseAbilityForDamage(enabled: Boolean) {
        updateWeaponEditor { it.copy(useAbilityForDamage = enabled) }
    }

    fun setWeaponProficiency(proficient: Boolean) {
        updateWeaponEditor { it.copy(proficient = proficient) }
    }

    fun setWeaponDiceCount(value: String) {
        updateWeaponEditor { it.copy(damageDiceCount = value) }
    }

    fun setWeaponDieSize(value: String) {
        updateWeaponEditor { it.copy(damageDieSize = value) }
    }

    fun setWeaponDamageType(damageType: DamageType) {
        updateWeaponEditor { it.copy(damageType = damageType) }
    }

    fun saveWeapon() {
        val editor = weaponEditorState ?: return
        val weapon = editor.toWeapon()
        if (weapon.name.isBlank()) return

        updateSheet { sheet ->
            updateWeaponListUseCase(sheet, UpdateWeaponListUseCase.Action.Save(weapon))
        }
        weaponEditorState = null
        renderState()
    }

    fun openWeaponCatalog() {
        isWeaponCatalogVisible = true
        renderState()
    }

    fun closeWeaponCatalog() {
        isWeaponCatalogVisible = false
        renderState()
    }

    fun selectWeaponFromCatalog(id: String) {
        val selected = weaponCatalog.firstOrNull { entry -> entry.id == id } ?: return
        weaponEditorState = selected.toEditorState()
        isWeaponCatalogVisible = false
        renderState()
    }

    fun deleteCharacter() {
        val id = currentSheet?.id ?: characterId ?: return
        viewModelScope.launch {
            errorMessage = null
            renderState()
            runCatching { deleteCharacterUseCase(id) }
                .onSuccess { _effects.emit(CharacterSheetEffect.CharacterDeleted) }
                .onFailure { throwable ->
                    errorMessage = throwable.message ?: "Unable to delete character"
                    renderState()
                }
        }
    }

    private fun observeCharacter(id: String) {
        loadCharacterSheetUseCase(id)
            .onEach { sheet ->
                hasLoaded = true
                currentSheet = sheet
                errorMessage = null
                if (sheet == null) {
                    editMode = SheetEditMode.View
                    editingState = null
                    weaponEditorState = null
                }
                renderState()
            }
            .catch { throwable ->
                hasLoaded = true
                currentSheet = null
                editMode = SheetEditMode.View
                editingState = null
                weaponEditorState = null
                errorMessage = throwable.message ?: "Unable to load character"
                renderState()
            }
            .launchIn(viewModelScope)
    }

    private fun observeSpells() {
        observeAllSpellsUseCase()
            .onEach { spells ->
                cachedSpells = spells
                renderState()
            }
            .launchIn(viewModelScope)
    }

    private fun observeWeaponCatalog() {
        observeWeaponCatalogUseCase()
            .map { entries -> entries.map { entry -> entry.toUiModel() } }
            .onEach { entries ->
                weaponCatalog = entries
                renderState()
            }
            .launchIn(viewModelScope)
    }

    private fun updateEditingState(transform: (CharacterSheetEditingState) -> CharacterSheetEditingState) {
        editingState = editingState?.let(transform)
        renderState()
    }

    private fun updateSheet(transform: (CharacterSheet) -> CharacterSheet) {
        val sheet = currentSheet ?: return
        val transformed = transform(sheet)
        val updated = transformed.clearDeathSavesIfConscious()
        currentSheet = updated
        errorMessage = null
        hasLoaded = true
        renderState()
        persist(updated)
    }

    private fun updateWeaponEditor(transform: (WeaponEditorState) -> WeaponEditorState) {
        weaponEditorState = weaponEditorState?.let(transform)
        renderState()
    }

    private fun renderState() {
        val sheet = currentSheet
        _uiState.update {
            when {
                !hasLoaded -> CharacterSheetUiState.Loading
                sheet == null -> CharacterSheetUiState.Error(errorMessage ?: "Character not found")
                else -> {
                    val spellsState = sheet.toSpellsState(cachedSpells, selectedSpellSourceId)
                    selectedSpellSourceId = spellsState.selectedSourceId
                    CharacterSheetUiState.Content(
                        characterId = sheet.id,
                        selectedTab = selectedTab,
                        editMode = editMode,
                        header = sheet.toHeaderState(),
                        overview = sheet.toOverviewState(),
                        skills = sheet.toSkillsState(),
                        spells = spellsState,
                        weapons = sheet.toWeaponsState(),
                        weaponCatalog = weaponCatalog,
                        isWeaponCatalogVisible = isWeaponCatalogVisible,
                        editingState = editingState.takeIf { editMode == SheetEditMode.Editing },
                        weaponEditorState = weaponEditorState,
                        errorMessage = errorMessage,
                    )
                }
            }
        }
    }

    private fun persist(updated: CharacterSheet) {
        if (characterId == null) return
        viewModelScope.launch {
            runCatching { saveCharacterSheetUseCase(updated) }
                .onFailure { throwable ->
                    errorMessage = throwable.message ?: "Unable to save changes"
                    renderState()
                }
        }
    }
}

/**
 * Exposes the UI state for the character sheet.
 */
sealed interface CharacterSheetUiState {
    @Immutable
    data object Loading : CharacterSheetUiState

    @Immutable
    data class Content(
        val characterId: String,
        val selectedTab: CharacterSheetTab,
        val editMode: SheetEditMode,
        val header: CharacterHeaderUiState,
        val overview: OverviewTabState,
        val skills: SkillsTabState,
        val spells: SpellsTabState,
        val weapons: WeaponsTabState,
        val weaponCatalog: List<WeaponCatalogUiModel>,
        val isWeaponCatalogVisible: Boolean,
        val editingState: CharacterSheetEditingState?,
        val weaponEditorState: WeaponEditorState?,
        val errorMessage: String?,
    ) : CharacterSheetUiState

    @Immutable
    data class Error(
        val message: String,
    ) : CharacterSheetUiState
}

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
    val abilityId: AbilityId,
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
    val spellLevels: List<SpellLevelUiModel>,
    val canAddSpells: Boolean,
    val sharedSlots: List<SpellSlotUiModel>,
    val pactSlots: PactSlotUiModel?,
    val concentration: ConcentrationUiModel?,
    val sourceFilters: List<SpellSourceFilterUiModel>,
    val selectedSourceId: String?,
    val showSourceBadges: Boolean,
    val showSourceFilters: Boolean,
)

@Immutable
data class SpellSourceFilterUiModel(
    val id: String?,
    val label: String,
)

@Immutable
data class PactSlotUiModel(
    val slotLevel: Int?,
    val total: Int,
    val expended: Int,
    val isConfigured: Boolean,
)

@Immutable
data class ConcentrationUiModel(
    val spellId: String,
    val label: String,
)

@Immutable
data class SpellLevelUiModel(
    val level: Int,
    val label: String,
    val spellSlot: SpellSlotUiModel?,
    val spells: List<CharacterSpellUiModel>,
)

@Immutable
data class CharacterSpellUiModel(
    val spellId: String,
    val name: String,
    val level: Int,
    val school: String,
    val castingTime: String,
    val sourceClass: String,
    val sourceLabel: String,
    val sourceKey: String,
)

@Immutable
data class SpellSlotUiModel(
    val level: Int,
    val total: Int,
    val expended: Int,
)

@Immutable
data class WeaponsTabState(
    val weapons: List<WeaponUiModel>,
)

@Immutable
data class WeaponCatalogUiModel(
    val id: String,
    val name: String,
    val category: EquipmentCategory?,
    val categories: Set<EquipmentCategory>,
    val damageDiceCount: Int,
    val damageDieSize: Int,
    val damageType: DamageType,
)

@Immutable
data class WeaponUiModel(
    val id: String,
    val name: String,
    val attackBonusLabel: String,
    val damageLabel: String,
    val damageType: DamageType,
)

@Immutable
data class WeaponEditorState(
    val id: String? = null,
    val catalogId: String? = null,
    val name: String = "",
    val category: EquipmentCategory? = null,
    val categories: Set<EquipmentCategory> = emptySet(),
    val abilityId: AbilityId = AbilityIds.STR,
    val proficient: Boolean = false,
    val useAbilityForDamage: Boolean = true,
    val damageDiceCount: String = "1",
    val damageDieSize: String = "6",
    val damageType: DamageType = DamageType.SLASHING,
) {
    fun toWeapon(): Weapon = Weapon(
        id = id ?: UUID.randomUUID().toString(),
        catalogId = catalogId,
        name = name.trim(),
        category = category,
        categories = categories,
        abilityId = abilityId,
        proficient = proficient,
        damageDiceCount = damageDiceCount.toIntOrNull()?.coerceAtLeast(1) ?: 1,
        damageDieSize = damageDieSize.toIntOrNull()?.coerceAtLeast(1) ?: 6,
        useAbilityForDamage = useAbilityForDamage,
        damageType = damageType,
    )

    companion object {
        fun fromWeapon(weapon: Weapon): WeaponEditorState = WeaponEditorState(
            id = weapon.id,
            catalogId = weapon.catalogId,
            name = weapon.name,
            category = weapon.category,
            categories = weapon.categories,
            abilityId = weapon.abilityId,
            proficient = weapon.proficient,
            useAbilityForDamage = weapon.useAbilityForDamage,
            damageDiceCount = weapon.damageDiceCount.toString(),
            damageDieSize = weapon.damageDieSize.toString(),
            damageType = weapon.damageType,
        )
    }
}

private fun com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry.toUiModel(): WeaponCatalogUiModel {
    val category = categories.firstOrNull { it != EquipmentCategory.WEAPON }
        ?: categories.firstOrNull()
    return WeaponCatalogUiModel(
        id = id,
        name = name,
        category = category,
        categories = categories,
        damageDiceCount = damageDiceNum,
        damageDieSize = damageDieSize,
        damageType = damageType,
    )
}

private fun WeaponCatalogUiModel.toEditorState(): WeaponEditorState = WeaponEditorState(
    catalogId = id,
    name = name,
    category = category,
    categories = categories,
    damageDiceCount = damageDiceCount.toString(),
    damageDieSize = damageDieSize.toString(),
    damageType = damageType,
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
    val savingThrowLookup = savingThrows.associateBy { it.abilityId }
    val abilityModels = AbilityIds.standardOrder.map { abilityId ->
        val modifier = abilityScores.modifierFor(abilityId)
        val entry = savingThrowLookup[abilityId]
        val proficiencyBonusValue = if (entry?.proficient == true) proficiencyBonus else 0
        AbilityUiModel(
            abilityId = abilityId,
            label = abilityId.abbreviation(),
            score = abilityScores.scoreFor(abilityId),
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
            abilityAbbreviation = skill.abilityAbbreviation,
            totalBonus = abilityScores.modifierFor(skill.abilityId) + proficiencyBonus + (entry?.bonus ?: 0),
            proficient = entry?.proficient ?: false,
            expertise = entry?.expertise ?: false,
        )
    }
    return SkillsTabState(models)
}

private fun CharacterSheet.toSpellsState(
    allSpells: List<Spell>,
    selectedSourceId: String?,
): SpellsTabState {
    val spellLookup = allSpells.associateBy { it.id }
    val normalizedSlots = if (spellSlots.isEmpty()) defaultSpellSlots() else spellSlots
    val allSharedSlots = normalizedSlots
        .sortedBy { it.level }
        .map { slot ->
            SpellSlotUiModel(
                level = slot.level,
                total = slot.total,
                expended = slot.expended.coerceIn(0, slot.total.coerceAtLeast(0)),
            )
        }
    val slotsByLevel = allSharedSlots.associateBy { it.level }

    val spellEntries = characterSpells.map { stored ->
        val spell = spellLookup[stored.spellId]
        val sourceClass = stored.sourceClass.trim()
        CharacterSpellUiModel(
            spellId = stored.spellId,
            name = spell?.name ?: stored.spellId,
            level = spell?.level ?: 0,
            school = spell?.school?.prettyString() ?: "—",
            castingTime = spell?.castingTime ?: "",
            sourceClass = sourceClass,
            sourceLabel = formatSourceLabel(sourceClass),
            sourceKey = normalizeSourceKey(sourceClass),
        )
    }

    val sources = spellEntries
        .associate { it.sourceKey to it.sourceLabel }
        .toList()
        .sortedBy { it.second.lowercase() }
    val sourceIds = sources.map { it.first }.toSet()
    val resolvedSelectedSourceId = selectedSourceId?.takeIf { it in sourceIds }
    val filteredEntries = if (resolvedSelectedSourceId == null) {
        spellEntries
    } else {
        spellEntries.filter { it.sourceKey == resolvedSelectedSourceId }
    }
    val spellsByLevel = filteredEntries.groupBy { it.level }

    val spellLevels = buildList {
        spellsByLevel[0]?.let { cantrips ->
            add(
                SpellLevelUiModel(
                    level = 0,
                    label = "Cantrips",
                    spellSlot = null,
                    spells = cantrips.sortedBy { it.name.lowercase() },
                )
            )
        }

        (1..9).forEach { level ->
            add(
                SpellLevelUiModel(
                    level = level,
                    label = "Level $level",
                    spellSlot = slotsByLevel[level],
                    spells = spellsByLevel[level]
                        .orEmpty()
                        .sortedWith(
                            compareBy<CharacterSpellUiModel> { it.name.lowercase() }
                                .thenBy { it.sourceLabel.lowercase() },
                        ),
                )
            )
        }
    }

    val pactSlotUi = when {
        sources.any { (key, label) ->
            key.contains("warlock") || label.contains("warlock", ignoreCase = true)
        } || pactSlots != null -> {
            pactSlots?.let { slot ->
                PactSlotUiModel(
                    slotLevel = slot.slotLevel,
                    total = slot.total,
                    expended = slot.expended.coerceIn(0, slot.total.coerceAtLeast(0)),
                    isConfigured = true,
                )
            } ?: PactSlotUiModel(
                slotLevel = null,
                total = 0,
                expended = 0,
                isConfigured = false,
            )
        }

        else -> null
    }

    val concentrationUi = concentrationSpellId?.let { spellId ->
        val label = spellLookup[spellId]?.name ?: spellId
        ConcentrationUiModel(spellId = spellId, label = label)
    }

    val sourceFilters = buildList {
        add(SpellSourceFilterUiModel(id = null, label = "All"))
        sources.forEach { (id, label) ->
            add(SpellSourceFilterUiModel(id = id, label = label))
        }
    }

    val showSourceFilters = sources.size > 1
    val showSourceBadges = sources.size > 1

    val highestSpellLevel = spellEntries.maxOfOrNull { it.level }?.coerceAtLeast(1) ?: 1
    val highestSlotLevel = allSharedSlots.filter { it.total > 0 }.maxOfOrNull { it.level } ?: 1
    val maxDisplayedSlotLevel = maxOf(highestSpellLevel, highestSlotLevel)
    val sharedSlots = allSharedSlots.filter { it.level <= maxDisplayedSlotLevel }

    return SpellsTabState(
        spellLevels = spellLevels,
        canAddSpells = true,
        sharedSlots = sharedSlots,
        pactSlots = pactSlotUi,
        concentration = concentrationUi,
        sourceFilters = sourceFilters,
        selectedSourceId = resolvedSelectedSourceId,
        showSourceBadges = showSourceBadges,
        showSourceFilters = showSourceFilters,
    )
}

private fun normalizeSourceKey(sourceClass: String): String {
    val normalized = sourceClass.trim().lowercase()
    return if (normalized.isBlank()) UNASSIGNED_SOURCE_KEY else normalized
}

private fun formatSourceLabel(sourceClass: String): String {
    val trimmed = sourceClass.trim()
    if (trimmed.isBlank()) return UNASSIGNED_SOURCE_LABEL
    return trimmed
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { char -> char.titlecase() }
        }
}

private const val UNASSIGNED_SOURCE_KEY = "__unassigned__"
private const val UNASSIGNED_SOURCE_LABEL = "Unassigned"

internal fun CharacterSheet.toWeaponsState(): WeaponsTabState {
    val scores = abilityScores
    val proficiency = proficiencyBonus

    return WeaponsTabState(
        weapons = weapons.map { weapon ->
            val abilityModifier = scores.modifierFor(weapon.abilityId)
            val attackBonus = abilityModifier + if (weapon.proficient) proficiency else 0
            val damageBonus = if (weapon.useAbilityForDamage) abilityModifier else 0
            val damagePart = if (damageBonus == 0) {
                "${weapon.damageDiceCount}d${weapon.damageDieSize}"
            } else {
                "${weapon.damageDiceCount}d${weapon.damageDieSize}${signed(damageBonus)}"
            }

            WeaponUiModel(
                id = weapon.id,
                name = weapon.name.ifBlank { "Unnamed weapon" },
                attackBonusLabel = "ATK ${signed(attackBonus)}",
                damageLabel = "DMG $damagePart",
                damageType = weapon.damageType,
            )
        },
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

private fun AbilityScores.scoreFor(abilityId: AbilityId): Int = when (abilityId.lowercase()) {
    AbilityIds.STR -> strength
    AbilityIds.DEX -> dexterity
    AbilityIds.CON -> constitution
    AbilityIds.INT -> intelligence
    AbilityIds.WIS -> wisdom
    AbilityIds.CHA -> charisma
    else -> 0
}

private fun String.filterDigits(): String = filter { it.isDigit() }

internal fun CharacterSheet.clearDeathSavesIfConscious(): CharacterSheet {
    return if (currentHitPoints > 0 && (deathSaves.successes != 0 || deathSaves.failures != 0)) {
        copy(deathSaves = DeathSaveState())
    } else {
        this
    }
}
