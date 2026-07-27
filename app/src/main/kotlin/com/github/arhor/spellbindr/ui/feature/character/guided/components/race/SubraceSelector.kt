@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.github.arhor.spellbindr.ui.feature.character.guided.components.race

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Race

@Composable
internal fun SubraceSelector(
    subraces: List<Race.Subrace>,
    selectedSubraceId: String?,
    onSubraceSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (subraces.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        maxItemsInEachRow = 2,
    ) {
        subraces.forEach { subrace ->
            val selected = subrace.id == selectedSubraceId
            FilterChip(
                selected = selected,
                onClick = { onSubraceSelected(subrace.id) },
                label = { Text(subrace.name, maxLines = 1) },
            )
        }
    }
}
