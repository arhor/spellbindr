package com.github.arhor.spellbindr.ui.feature.compendium.alignments

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.components.SelectableGrid
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.components.AlignmentTileLarge
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.components.AlignmentTileSmall
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.github.arhor.spellbindr.domain.model.Alignment as AlignmentModel

@Composable
internal fun AlignmentsScreen(
    state: AlignmentsUiState,
    onAlignmentClick: (String) -> Unit = {},
) {
    when (state) {
        is AlignmentsUiState.Loading -> LoadingIndicator()
        is AlignmentsUiState.Content -> AlignmentsContent(state, onAlignmentClick)
        is AlignmentsUiState.Error -> ErrorMessage(state.errorMessage)
    }
}

@Composable
private fun AlignmentsContent(
    state: AlignmentsUiState.Content,
    onAlignmentClick: (String) -> Unit,
) {
    SelectableGrid(
        items = state.alignments,
        key = { it.id },
        smallContent = ::AlignmentTileSmall,
        largeContent = ::AlignmentTileLarge,
        onItemClick = { onAlignmentClick(it.id) },
    )
}

@Composable
@PreviewLightDark
private fun AlignmentsScreenPreview() {
    AppTheme {
        AlignmentsScreen(
            state = AlignmentsUiState.Content(
                alignments = listOf(
                    AlignmentModel(
                        id = "lg",
                        name = "Lawful Good",
                        desc = "Acts with compassion, honor, and duty.",
                        abbr = "LG"
                    ),
                    AlignmentModel(
                        id = "tn",
                        name = "True Neutral",
                        desc = "Balances ideals between good and evil, order and chaos.",
                        abbr = "TN"
                    ),
                    AlignmentModel(
                        id = "ce",
                        name = "Chaotic Evil",
                        desc = "Seeks personal gain regardless of consequence.",
                        abbr = "CE"
                    ),
                ),
            ),
        )
    }
}
