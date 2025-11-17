package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun DeathSavesCard(
    state: DeathSaveUiState,
    onSuccessChanged: (Int) -> Unit,
    onFailureChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Death Saves",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            DeathSaveTrack(
                label = "Successes",
                count = state.successes,
                color = MaterialTheme.colorScheme.primary,
                onCountChanged = onSuccessChanged,
            )
            Spacer(modifier = Modifier.height(8.dp))
            DeathSaveTrack(
                label = "Failures",
                count = state.failures,
                color = MaterialTheme.colorScheme.error,
                onCountChanged = onFailureChanged,
            )
        }
    }
}

@Composable
private fun DeathSaveTrack(
    label: String,
    count: Int,
    color: Color,
    onCountChanged: (Int) -> Unit,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index ->
                val isFilled = index < count
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (isFilled) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 1.dp,
                            color = if (isFilled) color else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        )
                        .padding(6.dp)
                        .clickable { onCountChanged(if (isFilled) index else index + 1) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isFilled) {
                        Icon(
                            imageVector = if (label == "Failures") Icons.Rounded.Close else Icons.Rounded.Check,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DeathSavesCardPreview() {
    AppTheme {
        DeathSavesCard(
            state = CharacterSheetPreviewData.overview.deathSaves,
            onSuccessChanged = {},
            onFailureChanged = {},
        )
    }
}
