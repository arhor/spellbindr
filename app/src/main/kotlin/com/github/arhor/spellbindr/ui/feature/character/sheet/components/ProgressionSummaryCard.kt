package com.github.arhor.spellbindr.ui.feature.character.sheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.ProgressionSummaryUiModel

@Composable
internal fun ProgressionSummaryCard(
    progression: ProgressionSummaryUiModel,
    onLevelUp: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Level progression",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when (progression) {
                is ProgressionSummaryUiModel.Managed -> ManagedProgressionSummary(progression, onLevelUp)
                is ProgressionSummaryUiModel.Unmanaged -> UnmanagedProgressionSummary(progression)
            }
        }
    }
}

@Composable
private fun ManagedProgressionSummary(
    progression: ProgressionSummaryUiModel.Managed,
    onLevelUp: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Managed",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Level ${progression.totalLevel}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Text(
        text = progression.classes,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Level history",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        progression.levels.forEach { level ->
            Text(
                text = level,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
    Button(onClick = onLevelUp, enabled = progression.totalLevel < 20, modifier = Modifier.fillMaxWidth()) {
        Text(if (progression.totalLevel >= 20) "Maximum level reached" else "Level up")
    }
}

@Composable
private fun UnmanagedProgressionSummary(
    progression: ProgressionSummaryUiModel.Unmanaged,
) {
    Text(
        text = "Unmanaged",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = progression.message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
        Text("Set up level progression")
    }
}
