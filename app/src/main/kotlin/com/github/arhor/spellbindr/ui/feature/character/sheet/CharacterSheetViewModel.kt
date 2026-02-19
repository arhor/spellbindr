package com.github.arhor.spellbindr.ui.feature.character.sheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.defaultSpellSlots
import com.github.arhor.spellbindr.domain.usecase.DeleteCharacterUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellcastingClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveWeaponCatalogUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ToggleSpellSlotUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateHitPointsUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateWeaponListUseCase
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CastSlotOptionUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetEditingState
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetTab
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellSlotPool
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.WeaponCatalogUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.WeaponEditorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val observeSpellcastingClassesUseCase: ObserveSpellcastingClassesUseCase,
    private val observeWeaponCatalogUseCase: ObserveWeaponCatalogUseCase,
    private val saveCharacterSheetUseCase: SaveCharacterSheetUseCase,
    private val updateHitPointsUseCase: UpdateHitPointsUseCase,
    private val toggleSpellSlotUseCase: ToggleSpellSlotUseCase,
    private val updateWeaponListUseCase: UpdateWeaponListUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val characterId: String? = savedStateHandle.get<String>("characterId")
    private val _uiState = MutableStateFlow<CharacterSheetUiState>(
        if (characterId == null) CharacterSheetUiState.Failure("Missing character id") else CharacterSheetUiState.Loading,
    )
    val uiState: StateFlow<CharacterSheetUiState> = _uiState
    private val _effects = MutableSharedFlow<CharacterSheetEffect>()
    val effects = _effects.asSharedFlow()

    private var hasLoaded = characterId == null
    private var currentSheet: CharacterSheet? = null
    private var cachedSpells: List<Spell> = emptyList()
    private var cachedSpellcastingClasses: List<CharacterClass> = emptyList()
    private var weaponCatalog: List<WeaponCatalogUiModel> = emptyList()
    private var selectedTab: CharacterSheetTab = CharacterSheetTab.Overview
    private var editMode: SheetEditMode = SheetEditMode.View
    private var editingState: CharacterSheetEditingState? = null
    private var weaponEditorState: WeaponEditorState? = null
    private var isWeaponCatalogVisible: Boolean = false
    private var castingSpellId: String? = null
    private var errorMessage: String? = if (characterId == null) "Missing character id" else null
    private var persistJob: Job? = null

    init {
        if (characterId == null) {
            renderState()
        } else {
            observeCharacter(characterId)
            observeSpells()
            observeSpellcastingClasses()
            observeWeaponCatalog()
        }
    }

    fun dispatch(intent: CharacterSheetIntent) {
        when (intent) {
            is CharacterSheetIntent.TabSelected -> selectTab(intent.tab)
            CharacterSheetIntent.AddSpellsClicked -> Unit
            is CharacterSheetIntent.SpellSelected -> Unit
            is CharacterSheetIntent.SpellRemoved -> removeSpell(intent.spellId, intent.sourceClass)
            is CharacterSheetIntent.CastSpellClicked -> startCasting(intent.spellId)
            CharacterSheetIntent.LongRestClicked -> Unit
            CharacterSheetIntent.ShortRestClicked -> Unit
            CharacterSheetIntent.ConfigureSlotsClicked -> enterEditMode()
            is CharacterSheetIntent.SpellSlotToggled -> toggleSpellSlot(intent.level, intent.slotIndex)
            is CharacterSheetIntent.SpellSlotTotalChanged -> setSpellSlotTotal(intent.level, intent.total)
            is CharacterSheetIntent.PactSlotToggled -> togglePactSlot(intent.slotIndex)
            is CharacterSheetIntent.PactSlotTotalChanged -> setPactSlotTotal(intent.total)
            is CharacterSheetIntent.PactSlotLevelChanged -> setPactSlotLevel(intent.level)
            CharacterSheetIntent.ConcentrationCleared -> clearConcentration()
            CharacterSheetIntent.AddWeaponClicked -> startNewWeapon()
            is CharacterSheetIntent.WeaponSelected -> selectWeapon(intent.id)
            is CharacterSheetIntent.WeaponDeleted -> deleteWeapon(intent.id)
            CharacterSheetIntent.WeaponEditorDismissed -> dismissWeaponEditor()
            is CharacterSheetIntent.WeaponNameChanged -> setWeaponName(intent.value)
            is CharacterSheetIntent.WeaponAbilityChanged -> setWeaponAbility(intent.abilityId)
            is CharacterSheetIntent.WeaponUseAbilityForDamageChanged -> setWeaponUseAbilityForDamage(intent.value)
            is CharacterSheetIntent.WeaponProficiencyChanged -> setWeaponProficiency(intent.value)
            is CharacterSheetIntent.WeaponDiceCountChanged -> setWeaponDiceCount(intent.value)
            is CharacterSheetIntent.WeaponDieSizeChanged -> setWeaponDieSize(intent.value)
            is CharacterSheetIntent.WeaponDamageTypeChanged -> setWeaponDamageType(intent.value)
            CharacterSheetIntent.WeaponSaved -> saveWeapon()
            CharacterSheetIntent.WeaponCatalogOpened -> openWeaponCatalog()
            CharacterSheetIntent.WeaponCatalogClosed -> closeWeaponCatalog()
            is CharacterSheetIntent.WeaponCatalogItemSelected -> selectWeaponFromCatalog(intent.id)
            CharacterSheetIntent.EnterEditMode -> enterEditMode()
            CharacterSheetIntent.CancelEditMode -> cancelEditMode()
            CharacterSheetIntent.SaveEditsClicked -> saveInlineEdits()
            CharacterSheetIntent.OpenFullEditorClicked -> Unit
            CharacterSheetIntent.DeleteCharacterClicked -> deleteCharacter()
            CharacterSheetIntent.LongRestConfirmed -> longRest()
            CharacterSheetIntent.ShortRestConfirmed -> shortRest()
            CharacterSheetIntent.CastSheetDismissed -> dismissCasting()
            is CharacterSheetIntent.CastConfirmed -> {
                confirmCast(
                    pool = intent.pool,
                    slotLevel = intent.slotLevel,
                    castAsRitual = intent.castAsRitual,
                )
            }

            is CharacterSheetIntent.SpellsAssigned -> addSpells(intent.assignments)
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
        editMode = SheetEditMode.Edit
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

    fun startCasting(spellId: String) {
        castingSpellId = spellId
        errorMessage = null
        renderState()
    }

    fun dismissCasting() {
        if (castingSpellId != null) {
            castingSpellId = null
            renderState()
        }
    }

    fun confirmCast(
        pool: SpellSlotPool?,
        slotLevel: Int?,
        castAsRitual: Boolean,
    ) {
        val spellId = castingSpellId ?: return
        castingSpellId = null
        updateSheet { sheet ->
            val spell = cachedSpells.firstOrNull { it.id == spellId }
            val spellLevel = spell?.level ?: 0
            val slotOptions = buildCastSlotOptions(sheet, spell, spellLevel)
            val defaultOption = slotOptions
                .filter { it.enabled }
                .minWithOrNull(
                    compareBy<CastSlotOptionUiModel> { it.slotLevel }
                        .thenBy { if (it.pool == SpellSlotPool.Pact) 0 else 1 }
                )

            val resolvedPool = pool ?: defaultOption?.pool
            val resolvedSlotLevel = slotLevel ?: defaultOption?.slotLevel

            val castAsRitualEffective = castAsRitual && spell?.ritual == true
            val shouldSpendSlot = !castAsRitualEffective && spellLevel > 0

            val updatedSheet = if (!shouldSpendSlot) {
                sheet
            } else {
                when (resolvedPool) {
                    SpellSlotPool.Pact -> {
                        val pact = sheet.pactSlots
                        if (pact == null || pact.total <= 0 || pact.expended >= pact.total) {
                            sheet
                        } else if (pact.slotLevel < spellLevel) {
                            sheet
                        } else {
                            toggleSpellSlotUseCase(sheet, ToggleSpellSlotUseCase.Action.TogglePact(pact.expended))
                        }
                    }

                    SpellSlotPool.Shared -> {
                        val normalizedSlots = if (sheet.spellSlots.isEmpty()) defaultSpellSlots() else sheet.spellSlots
                        val slot = normalizedSlots.firstOrNull { it.level == resolvedSlotLevel }
                        if (resolvedSlotLevel == null || resolvedSlotLevel < spellLevel) {
                            sheet
                        } else if (slot == null || slot.total <= 0 || slot.expended >= slot.total) {
                            sheet
                        } else {
                            toggleSpellSlotUseCase(
                                sheet,
                                ToggleSpellSlotUseCase.Action.Toggle(slot.level, slot.expended)
                            )
                        }
                    }

                    null -> sheet
                }
            }

            if (spell?.concentration == true) {
                updatedSheet.copy(concentrationSpellId = spellId)
            } else {
                updatedSheet
            }
        }
    }

    fun longRest() {
        castingSpellId = null
        updateSheet { sheet -> toggleSpellSlotUseCase(sheet, ToggleSpellSlotUseCase.Action.LongRest) }
    }

    fun shortRest() {
        castingSpellId = null
        updateSheet { sheet -> toggleSpellSlotUseCase(sheet, ToggleSpellSlotUseCase.Action.ShortRest) }
    }

    fun setPactSlotLevel(level: Int) {
        updateSheet { sheet -> toggleSpellSlotUseCase(sheet, ToggleSpellSlotUseCase.Action.SetPactSlotLevel(level)) }
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
            .onEach { state ->
                when (state) {
                    is Loadable.Content -> {
                        cachedSpells = state.data
                        if (errorMessage == SPELLS_ERROR_MESSAGE) {
                            errorMessage = null
                        }
                    }

                    is Loadable.Failure -> {
                        errorMessage = SPELLS_ERROR_MESSAGE
                    }

                    is Loadable.Loading -> Unit
                }
                renderState()
            }
            .catch {
                errorMessage = SPELLS_ERROR_MESSAGE
                renderState()
            }
            .launchIn(viewModelScope)
    }

    private fun observeSpellcastingClasses() {
        observeSpellcastingClassesUseCase()
            .onEach { state ->
                when (state) {
                    is Loadable.Content -> {
                        cachedSpellcastingClasses = state.data
                        if (errorMessage == SPELLCASTING_CLASSES_ERROR_MESSAGE) {
                            errorMessage = null
                        }
                    }

                    is Loadable.Failure -> {
                        errorMessage = SPELLCASTING_CLASSES_ERROR_MESSAGE
                    }

                    is Loadable.Loading -> Unit
                }
                renderState()
            }
            .catch {
                errorMessage = SPELLCASTING_CLASSES_ERROR_MESSAGE
                renderState()
            }
            .launchIn(viewModelScope)
    }

    private fun observeWeaponCatalog() {
        observeWeaponCatalogUseCase()
            .onEach { state ->
                when (state) {
                    is Loadable.Content -> {
                        weaponCatalog = state.data.map { entry -> entry.toUiModel() }
                        if (errorMessage == WEAPON_CATALOG_ERROR_MESSAGE) {
                            errorMessage = null
                        }
                    }

                    is Loadable.Failure -> {
                        errorMessage = WEAPON_CATALOG_ERROR_MESSAGE
                    }

                    is Loadable.Loading -> Unit
                }
                renderState()
            }
            .catch {
                errorMessage = WEAPON_CATALOG_ERROR_MESSAGE
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
                sheet == null -> CharacterSheetUiState.Failure(errorMessage ?: "Character not found")
                else -> {
                    val spellsState = sheet.toSpellsState(cachedSpells, cachedSpellcastingClasses)
                    val castSpell = castingSpellId?.let { spellId -> sheet.toCastSpellState(spellId, cachedSpells) }
                    CharacterSheetUiState.Content(
                        characterId = sheet.id,
                        selectedTab = selectedTab,
                        editMode = editMode,
                        header = sheet.toHeaderState(),
                        overview = sheet.toOverviewState(),
                        skills = sheet.toSkillsState(),
                        spells = spellsState,
                        castSpell = castSpell,
                        weapons = sheet.toWeaponsState(),
                        weaponCatalog = weaponCatalog,
                        isWeaponCatalogVisible = isWeaponCatalogVisible,
                        editingState = editingState.takeIf { editMode == SheetEditMode.Edit },
                        weaponEditorState = weaponEditorState,
                        errorMessage = errorMessage,
                    )
                }
            }
        }
    }

    private fun persist(updated: CharacterSheet) {
        if (characterId == null) return
        persistJob?.cancel()
        persistJob = viewModelScope.launch {
            delay(SAVE_DEBOUNCE_MS)
            runCatching { saveCharacterSheetUseCase(updated) }
                .onFailure { throwable ->
                    errorMessage = throwable.message ?: "Unable to save changes"
                    renderState()
                }
        }
    }

    companion object {
        private const val SAVE_DEBOUNCE_MS = 150L
        private const val SPELLS_ERROR_MESSAGE = "Unable to load spells"
        private const val SPELLCASTING_CLASSES_ERROR_MESSAGE = "Unable to load spellcasting classes"
        private const val WEAPON_CATALOG_ERROR_MESSAGE = "Unable to load weapon catalog"
    }
}

