package com.github.arhor.spellbindr.ui.feature.compendium.alignments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.github.arhor.spellbindr.domain.model.Alignment as AlignmentModel
import com.github.arhor.spellbindr.ui.components.SelectableGrid
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun AlignmentsRoute(
    state: AlignmentsViewModel.AlignmentsState,
    onAlignmentClick: (String) -> Unit,
) {
    AlignmentsScreen(
        state = state,
        onAlignmentClick = onAlignmentClick,
    )
}

@Composable
private fun AlignmentsScreen(
    state: AlignmentsViewModel.AlignmentsState,
    onAlignmentClick: (String) -> Unit,
) {
    SelectableGrid(
        items = state.alignments,
        key = { it.name },
        smallContent = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it.abbr,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
        },
        largeContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = it.desc,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        onItemClick = { onAlignmentClick(it.name) },
    )
}

@Preview
@Composable
private fun AlignmentsScreenPreview() {
    AlignmentsPreviewContent(isDarkTheme = false)
}

@Preview
@Composable
private fun AlignmentsScreenDarkPreview() {
    AlignmentsPreviewContent(isDarkTheme = true)
}

@Composable
private fun AlignmentsPreviewContent(isDarkTheme: Boolean) {
    val alignments = listOf(
        AlignmentModel(id = "lg", name = "Lawful Good", desc = "Acts with compassion, honor, and duty.", abbr = "LG"),
        AlignmentModel(id = "tn", name = "True Neutral", desc = "Balances ideals between good and evil, order and chaos.", abbr = "TN"),
        AlignmentModel(id = "ce", name = "Chaotic Evil", desc = "Seeks personal gain regardless of consequence.", abbr = "CE"),
    )

    AppTheme(isDarkTheme = isDarkTheme) {
        AlignmentsScreen(
            state = AlignmentsViewModel.AlignmentsState(
                alignments = alignments,
            ),
            onAlignmentClick = {},
        )
    }
}
