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
    uiState: AlignmentsUiState,
    dispatch: AlignmentsDispatch = {},
) {
    when (uiState) {
        is AlignmentsUiState.Loading -> LoadingIndicator()
        is AlignmentsUiState.Failure -> ErrorMessage(uiState.errorMessage)
        is AlignmentsUiState.Content -> AlignmentsContent(uiState, dispatch)
    }
}

@Composable
private fun AlignmentsContent(
    uiState: AlignmentsUiState.Content,
    dispatch: AlignmentsDispatch,
) {
    SelectableGrid(
        items = uiState.alignments,
        key = { it.id },
        smallContent = ::AlignmentTileSmall,
        largeContent = ::AlignmentTileLarge,
        onItemClick = { dispatch(AlignmentsIntent.AlignmentClicked(it.id)) },
    )
}

@Composable
@PreviewLightDark
private fun AlignmentsScreenPreview() {
    AppTheme {
        AlignmentsScreen(
            uiState = AlignmentsUiState.Content(
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
