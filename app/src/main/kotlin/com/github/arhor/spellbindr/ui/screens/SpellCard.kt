package com.github.arhor.spellbindr.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.Spell

@Composable
fun SpellCard(
    spell: Spell,
    onClick: () -> Unit = {},
    onFavor: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Lvl. ${spell.level}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 16.dp),
        )
        Text(
            text = spell.name,
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(
            onClick = onFavor,
            content = { Icon(Icons.Outlined.Add, contentDescription = "Add spell to list") }
        )
    }
}
