package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterHeaderUiState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetEditingState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetTab
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.OverviewTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SkillsTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellsTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.WeaponCatalogUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.WeaponEditorState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.WeaponsTabState

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
    data class Failure(
        val errorMessage: String,
    ) : CharacterSheetUiState
}
