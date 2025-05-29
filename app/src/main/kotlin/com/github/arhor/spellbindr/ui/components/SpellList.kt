package com.github.arhor.spellbindr.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.next.model.Spell
import com.github.arhor.spellbindr.ui.theme.CardBg

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpellList(
    spells: List<Spell>,
    onSpellClick: (String) -> Unit,
) {
    var expandedAll by remember(spells) { mutableStateOf(true) }
    val expandedState = remember(spells) { mutableStateMapOf<Int, Boolean>() }
    val spellsByLevel = spells.groupBy { it.level }.toSortedMap()

    fun toggleExpandAll() {
        expandedAll = !expandedAll
        spellsByLevel.forEach { (level, _) ->
            expandedState[level] = expandedAll
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            spellsByLevel.forEach { (level, spellsForLevel) ->
                val expanded = expandedState[level] != false

                stickyHeader {
                    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = SpellSearchResultListShapes.GroupHeader)
                            .background(color = CardBg, shape = SpellSearchResultListShapes.GroupHeader)
                            .clickable { expandedState[level] = !expanded }
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
                    items(spellsForLevel) { spell ->
                        SpellCard(
                            spell = spell,
                            onClick = { onSpellClick(spell.name) },
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(7.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = ::toggleExpandAll,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            if (expandedAll) {
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
