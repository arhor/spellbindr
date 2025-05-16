package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.Spell

@Composable
fun SpellCard(
    spell: Spell,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(40.dp)
                .clip(CircleShape)
                .background(color = Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            SpellIcon(
                spellName = spell.name,
                size = 40.dp,
                iconSize = 30.dp
            )
        }

        Text(
            text = spell.name,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
