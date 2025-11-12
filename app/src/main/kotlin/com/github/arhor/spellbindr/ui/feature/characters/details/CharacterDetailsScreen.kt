package com.github.arhor.spellbindr.ui.feature.characters.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.WithAppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailsScreen(
    onBackClick: () -> Unit,
    viewModel: CharacterDetailsViewModel = hiltViewModel(),
) {
    val character by viewModel.character.collectAsState()

    WithAppTopBar(
        AppTopBarConfig(
            visible = true,
            title = { Text(character?.name ?: "Character Details") },
            navigation = AppTopBarNavigation.Back(onBackClick),
        )
    ) {
        character?.let { char ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailRow("Race:", char.race.id)
                char.subrace?.let { DetailRow("Subrace:", it.id) }
                DetailRow("Class:", char.classes.keys.first().id)
                DetailRow("Background:", char.background.id)
                char.alignment?.let { DetailRow("Alignment:", it.id) }
            }
        }
    }

}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
} 
