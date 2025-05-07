package com.github.arhor.spellbindr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.viewmodel.SpellDetailViewModel

@Composable
fun SpellDetailScreen(
    spellName: String?,
    viewModel: SpellDetailViewModel = hiltViewModel(),
) {
    if (spellName != null) {
        viewModel.loadSpellByName(spellName)
    }
    val spell by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        spell?.let {
            Text(text = it.name, style = MaterialTheme.typography.headlineLarge)
            Text(text = "Level: ${it.level}")
            Text(text = "School: ${it.school}")
            Text(text = "Range: ${it.range}")
            Text(text = "Casting Time: ${it.castingTime}")
            Text(text = "Duration: ${it.duration}")
            Text(text = "Components: ${it.components.joinToString()}")
            Text(text = "Classes: ${it.classes.joinToString()}")
            Text(text = "Ritual: ${it.ritual}")
            Text(text = "Concentration: ${it.concentration}")
            it.damage?.let {
                Text(text = "Damage: $it")
            }
            Text(text = "Source: ${it.source.book}${it.source.page?.let { ", p.$it" } ?: ""}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it.desc, style = MaterialTheme.typography.bodyLarge)
            if (!it.higherLevel.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "At Higher Levels: ${it.higherLevel}")
            }
        } ?: run {
            Text("Loading or spell not found")
        }
    }
}
