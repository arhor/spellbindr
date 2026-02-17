package com.github.arhor.spellbindr.ui.feature.character.sheet

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetTab
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellSlotPool

/**
 * Represents user intents for the Character Sheet screen.
 */
sealed interface CharacterSheetIntent {
    data class TabSelected(val tab: CharacterSheetTab) : CharacterSheetIntent
    data object AddSpellsClicked : CharacterSheetIntent
    data class SpellSelected(val spellId: String) : CharacterSheetIntent
    data class SpellRemoved(val spellId: String, val sourceClass: String) : CharacterSheetIntent
    data class CastSpellClicked(val spellId: String) : CharacterSheetIntent
    data object LongRestClicked : CharacterSheetIntent
    data object ShortRestClicked : CharacterSheetIntent
    data object ConfigureSlotsClicked : CharacterSheetIntent
    data class SpellSlotToggled(val level: Int, val slotIndex: Int) : CharacterSheetIntent
    data class SpellSlotTotalChanged(val level: Int, val total: Int) : CharacterSheetIntent
    data class PactSlotToggled(val slotIndex: Int) : CharacterSheetIntent
    data class PactSlotTotalChanged(val total: Int) : CharacterSheetIntent
    data class PactSlotLevelChanged(val level: Int) : CharacterSheetIntent
    data object ConcentrationCleared : CharacterSheetIntent
    data object AddWeaponClicked : CharacterSheetIntent
    data class WeaponSelected(val id: String) : CharacterSheetIntent
    data class WeaponDeleted(val id: String) : CharacterSheetIntent
    data object WeaponEditorDismissed : CharacterSheetIntent
    data class WeaponNameChanged(val value: String) : CharacterSheetIntent
    data class WeaponAbilityChanged(val abilityId: AbilityId) : CharacterSheetIntent
    data class WeaponUseAbilityForDamageChanged(val value: Boolean) : CharacterSheetIntent
    data class WeaponProficiencyChanged(val value: Boolean) : CharacterSheetIntent
    data class WeaponDiceCountChanged(val value: String) : CharacterSheetIntent
    data class WeaponDieSizeChanged(val value: String) : CharacterSheetIntent
    data class WeaponDamageTypeChanged(val value: DamageType) : CharacterSheetIntent
    data object WeaponSaved : CharacterSheetIntent
    data object WeaponCatalogOpened : CharacterSheetIntent
    data object WeaponCatalogClosed : CharacterSheetIntent
    data class WeaponCatalogItemSelected(val id: String) : CharacterSheetIntent
    data object EnterEditMode : CharacterSheetIntent
    data object CancelEditMode : CharacterSheetIntent
    data object SaveEditsClicked : CharacterSheetIntent
    data object OpenFullEditorClicked : CharacterSheetIntent
    data object DeleteCharacterClicked : CharacterSheetIntent
    data object LongRestConfirmed : CharacterSheetIntent
    data object ShortRestConfirmed : CharacterSheetIntent
    data object CastSheetDismissed : CharacterSheetIntent
    data class CastConfirmed(
        val pool: SpellSlotPool?,
        val slotLevel: Int?,
        val castAsRitual: Boolean,
    ) : CharacterSheetIntent

    data class SpellsAssigned(val assignments: List<CharacterSpellAssignment>) : CharacterSheetIntent
}

/**
 * Dispatch function for [CharacterSheetIntent] events.
 */
typealias CharacterSheetDispatch = (CharacterSheetIntent) -> Unit
