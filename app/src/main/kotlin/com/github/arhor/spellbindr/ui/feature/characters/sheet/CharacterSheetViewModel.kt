package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.predefined.DamageType
import com.github.arhor.spellbindr.domain.model.Ability
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.DeathSaveState
import com.github.arhor.spellbindr.domain.model.Weapon
import com.github.arhor.spellbindr.domain.model.defaultSpellSlots
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.usecase.DeleteCharacterUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ToggleSpellSlotUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateHitPointsUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateWeaponListUseCase
import com.github.arhor.spellbindr.ui.feature.characters.CharacterSpellAssignment
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetTab
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.utils.signed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CharacterSheetViewModel @Inject constructor(
    private val deleteCharacterUseCase: DeleteCharacterUseCase,
    private val loadCharacterSheetUseCase: LoadCharacterSheetUseCase,
    private val observeAllSpellsUseCase: ObserveAllSpellsUseCase,
    private val saveCharacterSheetUseCase: SaveCharacterSheetUseCase,
    private val updateHitPointsUseCase: UpdateHitPointsUseCase,
    private val toggleSpellSlotUseCase: ToggleSpellSlotUseCase,
    private val updateWeaponListUseCase: UpdateWeaponListUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    sealed interface CharacterSheetUiAction {
        data class TabSelected(val tab: CharacterSheetTab) : CharacterSheetUiAction
        data object EnterEdit : CharacterSheetUiAction
        data object CancelEdit : CharacterSheetUiAction
        data object SaveInlineEdits : CharacterSheetUiAction
        data class MaxHpEdited(val value: String) : CharacterSheetUiAction
        data class CurrentHpEdited(val value: String) : CharacterSheetUiAction
        data class TemporaryHpEdited(val value: String) : CharacterSheetUiAction
        data class SpeedEdited(val value: String) : CharacterSheetUiAction
        data class HitDiceEdited(val value: String) : CharacterSheetUiAction
        data class SensesEdited(val value: String) : CharacterSheetUiAction
        data class LanguagesEdited(val value: String) : CharacterSheetUiAction
        data class ProficienciesEdited(val value: String) : CharacterSheetUiAction
        data class EquipmentEdited(val value: String) : CharacterSheetUiAction
        data class AdjustCurrentHp(val delta: Int) : CharacterSheetUiAction
        data class TempHpChanged(val value: Int) : CharacterSheetUiAction
        data class DeathSaveSuccessesChanged(val count: Int) : CharacterSheetUiAction
        data class DeathSaveFailuresChanged(val count: Int) : CharacterSheetUiAction
        data class SpellSlotToggled(val level: Int, val slotIndex: Int) : CharacterSheetUiAction
        data class SpellSlotTotalChanged(val level: Int, val total: Int) : CharacterSheetUiAction
        data class SpellRemoved(val spellId: String, val sourceClass: String) : CharacterSheetUiAction
        data class AddSpells(val assignments: List<CharacterSpellAssignment>) : CharacterSheetUiAction
        data object AddWeaponClicked : CharacterSheetUiAction
        data class WeaponSelected(val id: String) : CharacterSheetUiAction
        data class WeaponDeleted(val id: String) : CharacterSheetUiAction
        data object WeaponEditorDismissed : CharacterSheetUiAction
        data class WeaponNameChanged(val value: String) : CharacterSheetUiAction
        data class WeaponAbilityChanged(val ability: Ability) : CharacterSheetUiAction
        data class WeaponUseAbilityForDamageChanged(val enabled: Boolean) : CharacterSheetUiAction
        data class WeaponProficiencyChanged(val proficient: Boolean) : CharacterSheetUiAction
        data class WeaponDiceCountChanged(val value: String) : CharacterSheetUiAction
        data class WeaponDieSizeChanged(val value: String) : CharacterSheetUiAction
        data class WeaponDamageTypeChanged(val damageType: DamageType) : CharacterSheetUiAction
        data object WeaponSaved : CharacterSheetUiAction
        data object DeleteCharacter : CharacterSheetUiAction
    }

    sealed interface CharacterSheetUiEvent {
        data class SheetLoaded(val sheet: CharacterSheet?, val loaded: Boolean) : CharacterSheetUiEvent
        data class SpellsLoaded(val spells: List<Spell>) : CharacterSheetUiEvent
        data class SelectedTabChanged(val tab: CharacterSheetTab) : CharacterSheetUiEvent
        data class EditModeChanged(val mode: SheetEditMode) : CharacterSheetUiEvent
        data class EditingStateChanged(val state: CharacterSheetEditingState?) : CharacterSheetUiEvent
        data class WeaponEditorChanged(val state: WeaponEditorState?) : CharacterSheetUiEvent
        data class ErrorChanged(val message: String?) : CharacterSheetUiEvent
    }

    sealed interface CharacterSheetEffect {
        data object CharacterDeleted : CharacterSheetEffect
    }

    @Immutable
    data class CharacterSheetUiData(
        val characterId: String? = null,
        val selectedTab: CharacterSheetTab = CharacterSheetTab.Overview,
        val editMode: SheetEditMode = SheetEditMode.View,
        val sheet: CharacterSheet? = null,
        val spells: List<Spell> = emptyList(),
        val editingState: CharacterSheetEditingState? = null,
        val weaponEditorState: WeaponEditorState? = null,
        val errorMessage: String? = null,
        val hasLoaded: Boolean = false,
    )

    private val characterId: String? = savedStateHandle.get<String>("characterId")
    private val _data = MutableStateFlow(
        CharacterSheetUiData(
            characterId = characterId,
            hasLoaded = characterId == null,
            errorMessage = if (characterId == null) "Missing character id" else null,
        ),
    )
    private val _effects = MutableSharedFlow<CharacterSheetEffect>()
    val effects = _effects.asSharedFlow()
    val uiState: StateFlow<CharacterSheetUiState> = _data
        .map { data -> data.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = _data.value.toUiState(),
        )

    init {
        observeCharacter()
        observeSpells()
    }

    fun onAction(action: CharacterSheetUiAction) {
        when (action) {
            is CharacterSheetUiAction.TabSelected -> updateData(CharacterSheetUiEvent.SelectedTabChanged(action.tab))
            CharacterSheetUiAction.EnterEdit -> enterEditMode()
            CharacterSheetUiAction.CancelEdit -> cancelEditMode()
            CharacterSheetUiAction.SaveInlineEdits -> saveInlineEdits()
            is CharacterSheetUiAction.MaxHpEdited -> updateEditingState { it.copy(maxHp = action.value.filterDigits()) }
            is CharacterSheetUiAction.CurrentHpEdited ->
                updateEditingState { it.copy(currentHp = action.value.filterDigits()) }

            is CharacterSheetUiAction.TemporaryHpEdited ->
                updateEditingState { it.copy(tempHp = action.value.filterDigits()) }

            is CharacterSheetUiAction.SpeedEdited -> updateEditingState { it.copy(speed = action.value) }
            is CharacterSheetUiAction.HitDiceEdited -> updateEditingState { it.copy(hitDice = action.value) }
            is CharacterSheetUiAction.SensesEdited -> updateEditingState { it.copy(senses = action.value) }
            is CharacterSheetUiAction.LanguagesEdited -> updateEditingState { it.copy(languages = action.value) }
            is CharacterSheetUiAction.ProficienciesEdited -> updateEditingState { it.copy(proficiencies = action.value) }
            is CharacterSheetUiAction.EquipmentEdited -> updateEditingState { it.copy(equipment = action.value) }
            is CharacterSheetUiAction.AdjustCurrentHp -> adjustCurrentHp(action.delta)
            is CharacterSheetUiAction.TempHpChanged -> setTemporaryHp(action.value)
            is CharacterSheetUiAction.DeathSaveSuccessesChanged -> setDeathSaveSuccesses(action.count)
            is CharacterSheetUiAction.DeathSaveFailuresChanged -> setDeathSaveFailures(action.count)
            is CharacterSheetUiAction.SpellSlotToggled -> toggleSpellSlot(action.level, action.slotIndex)
            is CharacterSheetUiAction.SpellSlotTotalChanged -> setSpellSlotTotal(action.level, action.total)
            is CharacterSheetUiAction.SpellRemoved -> removeSpell(action.spellId, action.sourceClass)
            is CharacterSheetUiAction.AddSpells -> addSpells(action.assignments)
            CharacterSheetUiAction.AddWeaponClicked -> updateData(
                CharacterSheetUiEvent.WeaponEditorChanged(WeaponEditorState()),
            )

            is CharacterSheetUiAction.WeaponSelected -> onWeaponSelected(action.id)
            is CharacterSheetUiAction.WeaponDeleted -> onWeaponDeleted(action.id)
            CharacterSheetUiAction.WeaponEditorDismissed -> updateData(
                CharacterSheetUiEvent.WeaponEditorChanged(null),
            )

            is CharacterSheetUiAction.WeaponNameChanged -> updateWeaponEditor { it.copy(name = action.value) }
            is CharacterSheetUiAction.WeaponAbilityChanged -> updateWeaponEditor { it.copy(ability = action.ability) }
            is CharacterSheetUiAction.WeaponUseAbilityForDamageChanged ->
                updateWeaponEditor { it.copy(useAbilityForDamage = action.enabled) }

            is CharacterSheetUiAction.WeaponProficiencyChanged ->
                updateWeaponEditor { it.copy(proficient = action.proficient) }

            is CharacterSheetUiAction.WeaponDiceCountChanged ->
                updateWeaponEditor { it.copy(damageDiceCount = action.value) }

            is CharacterSheetUiAction.WeaponDieSizeChanged ->
                updateWeaponEditor { it.copy(damageDieSize = action.value) }

            is CharacterSheetUiAction.WeaponDamageTypeChanged ->
                updateWeaponEditor { it.copy(damageType = action.damageType) }

            CharacterSheetUiAction.WeaponSaved -> onWeaponSaved()
            CharacterSheetUiAction.DeleteCharacter -> deleteCharacter()
        }
    }

    private fun updateData(event: CharacterSheetUiEvent) {
        _data.update { current -> reduce(current, event) }
    }

    private fun reduce(
        data: CharacterSheetUiData,
        event: CharacterSheetUiEvent,
    ): CharacterSheetUiData = when (event) {
        is CharacterSheetUiEvent.SheetLoaded -> data.copy(
            characterId = data.characterId ?: event.sheet?.id,
            sheet = event.sheet,
            hasLoaded = event.loaded,
            editMode = if (event.sheet == null) SheetEditMode.View else data.editMode,
            editingState = if (event.sheet == null) null else data.editingState,
            weaponEditorState = if (event.sheet == null) null else data.weaponEditorState,
        )

        is CharacterSheetUiEvent.SpellsLoaded -> data.copy(spells = event.spells)
        is CharacterSheetUiEvent.SelectedTabChanged -> data.copy(selectedTab = event.tab)
        is CharacterSheetUiEvent.EditModeChanged -> data.copy(editMode = event.mode)
        is CharacterSheetUiEvent.EditingStateChanged -> data.copy(editingState = event.state)
        is CharacterSheetUiEvent.WeaponEditorChanged -> data.copy(weaponEditorState = event.state)
        is CharacterSheetUiEvent.ErrorChanged -> data.copy(errorMessage = event.message)
    }

    private fun CharacterSheetUiData.toUiState(): CharacterSheetUiState =
        when {
            !hasLoaded -> CharacterSheetUiState.Loading(
                characterId = characterId,
                selectedTab = selectedTab,
                editMode = editMode,
            )

            sheet == null -> CharacterSheetUiState.Error(
                characterId = characterId,
                selectedTab = selectedTab,
                editMode = SheetEditMode.View,
                message = errorMessage ?: "Character not found",
            )

            else -> CharacterSheetUiState.Content(
                characterId = sheet.id,
                selectedTab = selectedTab,
                editMode = editMode,
                header = sheet.toHeaderState(),
                overview = sheet.toOverviewState(),
                skills = sheet.toSkillsState(),
                spells = sheet.toSpellsState(spells),
                weapons = sheet.toWeaponsState(),
                editingState = editingState.takeIf { editMode == SheetEditMode.Editing },
                weaponEditorState = weaponEditorState,
                errorMessage = errorMessage,
            )
        }

    private fun observeCharacter() {
        val id = characterId ?: return
        loadCharacterSheetUseCase(id)
            .onEach { sheet ->
                updateData(CharacterSheetUiEvent.SheetLoaded(sheet, loaded = true))
            }
            .catch { throwable ->
                updateData(CharacterSheetUiEvent.ErrorChanged(throwable.message ?: "Unable to load character"))
                updateData(CharacterSheetUiEvent.SheetLoaded(null, loaded = true))
            }
            .launchIn(viewModelScope)
    }

    private fun observeSpells() {
        observeAllSpellsUseCase()
            .onEach { spells -> updateData(CharacterSheetUiEvent.SpellsLoaded(spells)) }
            .launchIn(viewModelScope)
    }

    private fun deleteCharacter() {
        val id = _data.value.sheet?.id ?: _data.value.characterId ?: return
        viewModelScope.launch {
            updateData(CharacterSheetUiEvent.ErrorChanged(null))
            runCatching { deleteCharacterUseCase(id) }
                .onSuccess { _effects.emit(CharacterSheetEffect.CharacterDeleted) }
                .onFailure { throwable ->
                    updateData(CharacterSheetUiEvent.ErrorChanged(throwable.message ?: "Unable to delete character"))
                }
        }
    }

    private fun enterEditMode() {
        val sheet = _data.value.sheet ?: return
        updateData(CharacterSheetUiEvent.EditingStateChanged(CharacterSheetEditingState.fromSheet(sheet)))
        updateData(CharacterSheetUiEvent.EditModeChanged(SheetEditMode.Editing))
        updateData(CharacterSheetUiEvent.ErrorChanged(null))
    }

    private fun cancelEditMode() {
        updateData(CharacterSheetUiEvent.EditingStateChanged(null))
        updateData(CharacterSheetUiEvent.EditModeChanged(SheetEditMode.View))
    }

    private fun saveInlineEdits() {
        val edits = _data.value.editingState ?: return
        updateSheet { sheet -> sheet.applyInlineEdits(edits) }
        cancelEditMode()
    }

    private fun adjustCurrentHp(delta: Int) {
        updateSheet { sheet ->
            updateHitPointsUseCase(sheet, UpdateHitPointsUseCase.Action.AdjustCurrentHp(delta))
        }
    }

    private fun setTemporaryHp(value: Int) {
        updateSheet { sheet ->
            updateHitPointsUseCase(sheet, UpdateHitPointsUseCase.Action.SetTemporaryHp(value))
        }
    }

    private fun setDeathSaveSuccesses(count: Int) {
        updateSheet { sheet ->
            updateHitPointsUseCase(sheet, UpdateHitPointsUseCase.Action.SetDeathSaveSuccesses(count))
        }
    }

    private fun setDeathSaveFailures(count: Int) {
        updateSheet { sheet ->
            updateHitPointsUseCase(sheet, UpdateHitPointsUseCase.Action.SetDeathSaveFailures(count))
        }
    }

    private fun toggleSpellSlot(level: Int, slotIndex: Int) {
        updateSheet { sheet ->
            toggleSpellSlotUseCase(sheet, ToggleSpellSlotUseCase.Action.Toggle(level, slotIndex))
        }
    }

    private fun setSpellSlotTotal(level: Int, total: Int) {
        updateSheet { sheet ->
            toggleSpellSlotUseCase(sheet, ToggleSpellSlotUseCase.Action.SetTotal(level, total))
        }
    }

    private fun removeSpell(spellId: String, sourceClass: String) {
        updateSheet { sheet ->
            val updated = sheet.characterSpells.filterNot {
                it.spellId == spellId && it.sourceClass.equals(sourceClass, ignoreCase = true)
            }
            sheet.copy(characterSpells = updated)
        }
    }

    private fun addSpells(assignments: List<CharacterSpellAssignment>) {
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

    private fun onWeaponSelected(id: String) {
        val sheet = _data.value.sheet ?: return
        sheet.weapons.firstOrNull { it.id == id }
            ?.let { weapon -> updateData(CharacterSheetUiEvent.WeaponEditorChanged(WeaponEditorState.fromWeapon(weapon))) }
    }

    private fun onWeaponDeleted(id: String) {
        updateSheet { sheet ->
            updateWeaponListUseCase(sheet, UpdateWeaponListUseCase.Action.Delete(id))
        }
        if (_data.value.weaponEditorState?.id == id) {
            updateData(CharacterSheetUiEvent.WeaponEditorChanged(null))
        }
    }

    private fun onWeaponSaved() {
        val editor = _data.value.weaponEditorState ?: return
        val weapon = editor.toWeapon()
        if (weapon.name.isBlank()) return

        updateSheet { sheet ->
            updateWeaponListUseCase(sheet, UpdateWeaponListUseCase.Action.Save(weapon))
        }
        updateData(CharacterSheetUiEvent.WeaponEditorChanged(null))
    }

    private fun updateEditingState(transform: (CharacterSheetEditingState) -> CharacterSheetEditingState) {
        val current = _data.value.editingState ?: return
        updateData(CharacterSheetUiEvent.EditingStateChanged(transform(current)))
    }

    private fun updateSheet(transform: (CharacterSheet) -> CharacterSheet) {
        val sheet = _data.value.sheet ?: return
        val transformed = transform(sheet)
        val updated = transformed.clearDeathSavesIfConscious()
        updateData(CharacterSheetUiEvent.ErrorChanged(null))
        updateData(CharacterSheetUiEvent.SheetLoaded(updated, loaded = true))
        persist(updated)
    }

    private fun updateWeaponEditor(transform: (WeaponEditorState) -> WeaponEditorState) {
        val current = _data.value.weaponEditorState ?: return
        updateData(CharacterSheetUiEvent.WeaponEditorChanged(transform(current)))
    }

    private fun persist(updated: CharacterSheet) {
        if (characterId == null) return
        viewModelScope.launch {
            runCatching { saveCharacterSheetUseCase(updated) }
                .onFailure { throwable ->
                    updateData(CharacterSheetUiEvent.ErrorChanged(throwable.message ?: "Unable to save changes"))
                }
        }
    }
}

sealed interface CharacterSheetUiState {
    val characterId: String?
    val selectedTab: CharacterSheetTab
    val editMode: SheetEditMode

    @Immutable
    data class Loading(
        override val characterId: String?,
        override val selectedTab: CharacterSheetTab,
        override val editMode: SheetEditMode,
    ) : CharacterSheetUiState

    @Immutable
    data class Content(
        override val characterId: String,
        override val selectedTab: CharacterSheetTab,
        override val editMode: SheetEditMode,
        val header: CharacterHeaderUiState,
        val overview: OverviewTabState,
        val skills: SkillsTabState,
        val spells: SpellsTabState,
        val weapons: WeaponsTabState,
        val editingState: CharacterSheetEditingState?,
        val weaponEditorState: WeaponEditorState?,
        val errorMessage: String?,
    ) : CharacterSheetUiState

    @Immutable
    data class Error(
        override val characterId: String?,
        override val selectedTab: CharacterSheetTab,
        override val editMode: SheetEditMode,
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
    val spellLevels: List<SpellLevelUiModel>,
    val canAddSpells: Boolean,
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
    val name: String = "",
    val ability: Ability = Ability.STR,
    val proficient: Boolean = false,
    val useAbilityForDamage: Boolean = true,
    val damageDiceCount: String = "1",
    val damageDieSize: String = "6",
    val damageType: DamageType = DamageType.SLASHING,
) {
    fun toWeapon(): Weapon = Weapon(
        id = id ?: UUID.randomUUID().toString(),
        name = name.trim(),
        ability = ability,
        proficient = proficient,
        damageDiceCount = damageDiceCount.toIntOrNull()?.coerceAtLeast(1) ?: 1,
        damageDieSize = damageDieSize.toIntOrNull()?.coerceAtLeast(1) ?: 6,
        useAbilityForDamage = useAbilityForDamage,
        damageType = damageType,
    )

    companion object {
        fun fromWeapon(weapon: Weapon): WeaponEditorState = WeaponEditorState(
            id = weapon.id,
            name = weapon.name,
            ability = weapon.ability,
            proficient = weapon.proficient,
            useAbilityForDamage = weapon.useAbilityForDamage,
            damageDiceCount = weapon.damageDiceCount.toString(),
            damageDieSize = weapon.damageDieSize.toString(),
            damageType = weapon.damageType,
        )
    }
}

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
    val normalizedSlots = if (spellSlots.isEmpty()) defaultSpellSlots() else spellSlots
    val slotsByLevel = normalizedSlots
        .sortedBy { it.level }
        .associate { slot ->
            slot.level to SpellSlotUiModel(
                level = slot.level,
                total = slot.total,
                expended = slot.expended.coerceIn(0, slot.total.coerceAtLeast(0)),
            )
        }

    val spellsByLevel = characterSpells
        .map { stored ->
            val spell = spellLookup[stored.spellId]
            CharacterSpellUiModel(
                spellId = stored.spellId,
                name = spell?.name ?: stored.spellId,
                level = spell?.level ?: 0,
                school = spell?.school?.prettyString() ?: "—",
                castingTime = spell?.castingTime ?: "",
                sourceClass = stored.sourceClass,
            )
        }
        .groupBy { it.level }

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
                        .sortedWith(compareBy<CharacterSpellUiModel> { it.name.lowercase() }.thenBy { it.sourceClass.lowercase() }),
                )
            )
        }
    }

    return SpellsTabState(
        spellLevels = spellLevels,
        canAddSpells = true,
    )
}

internal fun CharacterSheet.toWeaponsState(): WeaponsTabState {
    val scores = abilityScores
    val proficiency = proficiencyBonus

    return WeaponsTabState(
        weapons = weapons.map { weapon ->
            val abilityModifier = scores.modifierFor(weapon.ability)
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
