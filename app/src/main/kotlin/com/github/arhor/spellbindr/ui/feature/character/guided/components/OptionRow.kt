package com.github.arhor.spellbindr.ui.feature.character.guided.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Choice

@Composable
internal fun OptionRow(
    id: String,
    label: String,
    disabledOptions: Map<String, String>,
    selected: Set<String>,
    isSingleSelect: Boolean,
    choice: Choice,
    onToggle: (String) -> Unit,
) {
    val alreadySelectedReason = disabledOptions[id]
    val isSelectedHere = id in selected
    val isAlreadyHave = alreadySelectedReason != null && !isSelectedHere
    val enabled = when {
        isSelectedHere -> true
        alreadySelectedReason != null -> false
        isSingleSelect -> true
        selected.size >= choice.choose -> false
        else -> true
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isSingleSelect) {
            RadioButton(
                selected = isSelectedHere,
                onClick = { onToggle(id) },
                enabled = enabled,
            )
        } else {
            Checkbox(
                checked = isSelectedHere || isAlreadyHave,
                onCheckedChange = { onToggle(id) },
                enabled = enabled,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            if (!isSelectedHere && alreadySelectedReason != null) {
                Text(
                    text = "Already have - $alreadySelectedReason",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
