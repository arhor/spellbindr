package com.github.arhor.spellbindr.ui.screens

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
import com.github.arhor.spellbindr.ui.spells.search.SpellSearchResultList
import com.github.arhor.spellbindr.viewmodel.SpellListViewModel

@Composable
fun FavoriteSpellsScreen(
    onSpellClick: (String) -> Unit = {},
    spellListViewModel: SpellListViewModel = hiltViewModel()
) {
    val favorites by spellListViewModel.state.collectAsState()
    val favoriteSpellNames = favorites?.spellNames?.toSet() ?: emptySet()
    val favoriteSpells = spellListViewModel.getFavoriteSpells()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Favorite Spells", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        SpellSearchResultList(
            spells = favoriteSpells,
            onSpellClick = onSpellClick,
            onSpellFavor = { spellListViewModel.toggleFavorite(it) },
            favoriteSpellNames = favoriteSpellNames
        )
    }
} 
