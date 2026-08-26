package io.github.arhor.dnd.companion.ui.feature.character.sheet.components.tabs.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.arhor.dnd.companion.ui.feature.character.sheet.components.ProgressionSummaryCard
import io.github.arhor.dnd.companion.ui.feature.character.sheet.model.CharacterHeaderUiState
import io.github.arhor.dnd.companion.ui.feature.character.sheet.model.CharacterSheetEditingState
import io.github.arhor.dnd.companion.ui.feature.character.sheet.model.OverviewTabState
import io.github.arhor.dnd.companion.ui.feature.character.sheet.model.ProgressionSummaryUiModel
import io.github.arhor.dnd.companion.ui.feature.character.sheet.model.SheetEditMode

@Composable
fun OverviewTab(
    header: CharacterHeaderUiState,
    overview: OverviewTabState,
    progression: ProgressionSummaryUiModel,
    editMode: SheetEditMode,
    editingState: CharacterSheetEditingState?,
    onLevelUp: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CombatOverviewCard(
            header = header,
            abilities = overview.abilities,
        )
        SavingThrowsCard(
            abilities = overview.abilities,
        )
        ProgressionSummaryCard(
            progression = progression,
            onLevelUp = onLevelUp,
        )
    }
}

@Preview
@Composable
fun OverviewTabPreview() {

}
