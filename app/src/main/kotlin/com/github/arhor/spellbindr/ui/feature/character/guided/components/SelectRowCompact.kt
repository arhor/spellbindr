package com.github.arhor.spellbindr.ui.feature.character.guided.components

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun SelectRowCompact(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(onClick = onClick, enabled = !selected) {
        Text(label)
    }
}
