package com.github.arhor.spellbindr.ui.feature.compendium.spells.search

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.theme.AppTheme

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

@Preview
@Composable
private fun SpellCardLightPreview() {
    SpellCardPreview(isDarkTheme = false)
}

@Preview
@Composable
private fun SpellCardDarkPreview() {
    SpellCardPreview(isDarkTheme = true)
}

@Composable
private fun SpellCardPreview(isDarkTheme: Boolean) {
    AppTheme(isDarkTheme = isDarkTheme) {
        SpellCard(
            spell = Spell(
                id = "arcane_blast",
                name = "Arcane Blast",
                desc = listOf("A burst of pure arcane energy strikes a foe."),
                level = 2,
                range = "60 ft",
                ritual = false,
                school = EntityRef(id = "evocation"),
                duration = "Instant",
                castingTime = "1 action",
                classes = listOf(EntityRef(id = "wizard")),
                components = listOf("V", "S"),
                concentration = false,
                source = "PHB",
            ),
            onClick = {},
        )
    }
}
