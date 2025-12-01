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
import com.github.arhor.spellbindr.ui.components.SelectableGrid
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.AlignmentsViewModel.State

@Composable
fun AlignmentsRoute(
    state: State,
    onAlignmentClick: (String) -> Unit,
) {
    AlignmentsScreen(
        state = state,
        onAlignmentClick = onAlignmentClick,
    )
}

@Composable
private fun AlignmentsScreen(
    state: State,
    onAlignmentClick: (String) -> Unit,
) {
    SelectableGrid(
        items = state.alignments,
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
