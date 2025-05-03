package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.viewmodel.CharacterViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CharacterListScreen(
    innerPadding: PaddingValues,
    viewModel: CharacterViewModel = koinViewModel(),
) {
    val characters by viewModel.characters.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Greeting(
            name = "Android",
            modifier = Modifier.padding(innerPadding),
        )

        Button(onClick = { viewModel.addSampleCharacter() }) {
            Text("Add Character")
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(characters) { character ->
                Text("${character.name} - ${character.classType} (Lv. ${character.level})")
            }
        }
    }
}
