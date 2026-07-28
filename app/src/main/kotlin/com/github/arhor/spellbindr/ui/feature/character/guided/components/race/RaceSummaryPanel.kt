package com.github.arhor.spellbindr.ui.feature.character.guided.components.race

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Race

@Composable
internal fun RaceSummaryPanel(
    summary: RaceSummaryUiModel,
    subraces: List<Race.Subrace>,
    selectedSubraceId: String?,
    onSubraceSelected: (String) -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = summary.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onViewDetails) {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                Text("Details")
            }
        }

        val mechanics = buildList {
            summary.size?.let { add("Size $it") }
            summary.speedFeet?.let { add("Speed $it ft") }
        }.joinToString("  •  ")
        if (mechanics.isNotEmpty()) {
            Text(
                text = mechanics,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                maxLines = 1,
            )
        }
        if (summary.abilityBonuses.isNotEmpty()) {
            Text(
                text = summary.abilityBonuses.joinToString("  •  "),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (subraces.isNotEmpty()) {
            Text(
                text = "Choose a subrace",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
            )
            SubraceSelector(
                subraces = subraces,
                selectedSubraceId = selectedSubraceId,
                onSubraceSelected = onSubraceSelected,
            )
        }
        if (summary.definingTraits.isNotEmpty()) {
            Text(
                text = summary.definingTraits.joinToString("  •  "),
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.82f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (summary.deferredChoices.isNotEmpty()) {
            Text(
                text = summary.deferredChoices.joinToString("  •  ") { it.label },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
