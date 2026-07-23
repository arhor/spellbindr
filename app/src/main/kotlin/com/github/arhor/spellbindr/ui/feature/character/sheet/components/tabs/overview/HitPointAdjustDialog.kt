package com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.HitPointSummary
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.github.arhor.spellbindr.utils.signed

@Composable
internal fun HitPointAdjustDialog(
    hitPoints: HitPointSummary,
    onAdjustHp: (Int) -> Unit,
    onTempHpChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        title = { Text("Adjust hit points") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${hitPoints.current} / ${hitPoints.max}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Column {
                    Text(
                        text = "Modify HP",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(-5, -1, 1, 5).forEach { delta ->
                            val label = signed(delta)
                            HpAdjustButton(
                                label = label,
                                onClick = { onAdjustHp(delta) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Column {
                    Text(
                        text = "Temporary HP ${hitPoints.temporary}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AssistChip(
                            onClick = { onTempHpChanged((hitPoints.temporary - 1).coerceAtLeast(0)) },
                            label = { Text("-1") },
                            enabled = hitPoints.temporary > 0,
                        )
                        AssistChip(
                            onClick = { onTempHpChanged(hitPoints.temporary + 1) },
                            label = { Text("+1") },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun HpAdjustButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
        contentPadding = ButtonDefaults.ContentPadding.run {
            PaddingValues(
                start = 12.dp,
                top = calculateTopPadding(),
                end = 12.dp,
                bottom = calculateBottomPadding(),
            )
        },
    ) {
        Text(label)
    }
}

@Preview
@Composable
private fun HitPointAdjustDialogPreview() {
    AppTheme {
        HitPointAdjustDialog(
            hitPoints = CharacterSheetPreviewData.header.hitPoints,
            onAdjustHp = {},
            onTempHpChanged = {},
            onDismiss = {},
        )
    }
}
