package com.github.arhor.spellbindr.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.viewmodel.SpellSearchResultListViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpellSearchResultList(
    spells: List<Spell>,
    onSpellClick: (String) -> Unit,
    onSpellFavor: (String) -> Unit,
    viewModel: SpellSearchResultListViewModel = hiltViewModel(),
) {
    val spellsByLevel = spells.groupBy { it.level }.toSortedMap()
    val expandedState = viewModel.expandedState

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
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = { expandedState[level] = !expanded })
                        .padding(vertical = 8.dp, horizontal = 16.dp),
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
                        onFavor = { onSpellFavor(spell.name) }
                    )
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
