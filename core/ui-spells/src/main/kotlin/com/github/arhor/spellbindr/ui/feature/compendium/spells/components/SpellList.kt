package com.github.arhor.spellbindr.ui.feature.compendium.spells.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellList(
    spells: List<Spell>,
    onSpellClick: (Spell) -> Unit = {},
) {
    val spellsGroups = remember(spells) { spells.groupBy(Spell::level).toSortedMap() }
    val foldedGroups = remember { mutableStateSetOf<Int>() }
    val unfoldGroups = remember { mutableStateOf(true) }

    fun onSpellGroupClick(level: Int) {
        if (level in foldedGroups) {
            foldedGroups.remove(level)
        } else {
            foldedGroups.add(level)
        }
    }

    fun onUnfoldAllClick() {
        unfoldGroups.value = !unfoldGroups.value
        if (unfoldGroups.value) {
            foldedGroups.addAll(spellsGroups.keys)
        } else {
            foldedGroups.clear()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            spellsGroups.forEach { (level, spellsForLevel) ->
                val expanded = level !in foldedGroups

                stickyHeader(key = "spell-header-$level") {
                    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = SpellSearchResultListShapes.GroupHeader)
                            .clickable { onSpellGroupClick(level) }
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = SpellSearchResultListShapes.GroupHeader
                            )
                            .padding(vertical = 10.dp, horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lvl. $level",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.graphicsLayer { rotationZ = rotationAngle }
                        )
                    }
                }

                if (expanded) {
                    items(spellsForLevel, key = { it.id }) { spell ->
                        SpellCard(
                            spell = spell,
                            onClick = { onSpellClick(spell) },
                        )
                    }
                }

                item(key = "spell-spacer-$level") {
                    Spacer(modifier = Modifier.height(7.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = ::onUnfoldAllClick,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            if (unfoldGroups.value) {
                Icon(Icons.Rounded.UnfoldLess, "Collapse")
            } else {
                Icon(Icons.Rounded.UnfoldMore, "Expand")
            }
        }
    }
}

private object SpellSearchResultListShapes {
    val GroupHeader = RoundedCornerShape(12.dp)
}

@Composable
@PreviewLightDark
private fun SpellListPreview() {
    AppTheme {
        SpellList(
            spells = listOf(
                Spell(
                    id = "mage_hand",
                    name = "Mage Hand",
                    desc = listOf("A spectral hand appears"),
                    level = 0,
                    range = "30 ft",
                    ritual = false,
                    school = EntityRef(id = "conjuration"),
                    duration = "1 minute",
                    castingTime = "1 action",
                    classes = listOf(EntityRef(id = "wizard")),
                    components = listOf("V", "S"),
                    concentration = false,
                    source = "PHB",
                ),
                Spell(
                    id = "burning_hands",
                    name = "Burning Hands",
                    desc = listOf("Fire erupts from your hands"),
                    level = 1,
                    range = "Self",
                    ritual = false,
                    school = EntityRef(id = "evocation"),
                    duration = "Instant",
                    castingTime = "1 action",
                    classes = listOf(EntityRef(id = "wizard")),
                    components = listOf("V", "S"),
                    concentration = false,
                    source = "PHB",
                )
            ),
        )
    }
}
