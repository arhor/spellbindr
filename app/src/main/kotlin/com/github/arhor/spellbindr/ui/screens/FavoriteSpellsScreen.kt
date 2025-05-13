package com.github.arhor.spellbindr.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.viewmodel.SpellListViewModel
import com.github.arhor.spellbindr.data.model.SpellList
import com.github.arhor.spellbindr.data.model.Spell

@Composable
fun FavoriteSpellsScreen(
    onSpellClick: (String) -> Unit = {},
    spellListViewModel: SpellListViewModel = hiltViewModel()
) {
    val favorites by spellListViewModel.state.collectAsState()
    val favoriteSpellNames = favorites?.spellNames?.toSet() ?: emptySet()
    val favoriteSpells = spellListViewModel.getFavoriteSpells()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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