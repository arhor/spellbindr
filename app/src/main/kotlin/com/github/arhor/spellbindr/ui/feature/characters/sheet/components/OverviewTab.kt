package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterHeaderUiState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetEditingState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.OverviewTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetCallbacks
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode

@Composable
fun OverviewTab(
    header: CharacterHeaderUiState,
    overview: OverviewTabState,
    editMode: SheetEditMode,
    editingState: CharacterSheetEditingState?,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CombatOverviewCard(
            header = header,
            abilities = overview.abilities,
        )
        SavingThrowsCard(
            abilities = overview.abilities,
        )
    }
}

@Preview
@Composable
fun OverviewTabPreview() {

}
