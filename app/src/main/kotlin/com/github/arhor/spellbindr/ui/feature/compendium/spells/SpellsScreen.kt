package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellList
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellSearchInput
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellsScreen(
    uiState: SpellsUiState,
    onQueryChanged: (String) -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onSpellClick: (Spell) -> Unit = {},
    onClassToggled: (EntityRef) -> Unit = {},
) {
    when (uiState) {
        is SpellsUiState.Loading -> LoadingIndicator()
        is SpellsUiState.Failure -> ErrorMessage(uiState.errorMessage)
        is SpellsUiState.Content -> SpellSearchContent(
            uiState = uiState,
            onQueryChanged = onQueryChanged,
            onFavoriteClick = onFavoriteClick,
            onSpellClick = onSpellClick,
            onClassToggled = onClassToggled,
        )
    }
}

@Composable
private fun SpellSearchContent(
    uiState: SpellsUiState.Content,
    onQueryChanged: (String) -> Unit,
    onFavoriteClick: () -> Unit,
    onSpellClick: (Spell) -> Unit,
    onClassToggled: (EntityRef) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SpellSearchInput(
            query = uiState.query,
            onQueryChanged = onQueryChanged,
            showFavorite = uiState.showFavoriteOnly,
            onFavoriteClick = onFavoriteClick,
        )

        if (uiState.castingClasses.isNotEmpty()) {
            SpellClassFilterRow(
                castingClasses = uiState.castingClasses,
                selectedClasses = uiState.selectedClasses,
                onClassToggled = onClassToggled,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        SpellList(
            spells = uiState.spells,
            onSpellClick = onSpellClick,
        )
    }
}

@Composable
private fun SpellClassFilterRow(
    castingClasses: List<EntityRef>,
    selectedClasses: Set<EntityRef>,
    onClassToggled: (EntityRef) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        castingClasses.forEach { spellClass ->
            val selected = spellClass in selectedClasses
            FilterChip(
                selected = selected,
                onClick = { onClassToggled(spellClass) },
                label = { Text(spellClass.prettyString()) },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun SpellSearchScreenPreview() {
    AppTheme {
        SpellsScreen(
            uiState = SpellsUiState.Content(
                query = "heal",
                castingClasses = listOf(EntityRef(id = "cleric")),
                spells = listOf(
                    Spell(
                        id = "healing_word",
                        name = "Healing Word",
                        desc = listOf("A creature of your choice that you can see regains hit points."),
                        level = 1,
                        range = "60 ft",
                        ritual = false,
                        school = EntityRef(id = "evocation"),
                        duration = "Instant",
                        castingTime = "1 bonus action",
                        classes = listOf(EntityRef(id = "cleric")),
                        components = listOf("V"),
                        concentration = false,
                        source = "PHB",
                    )
                ),
                showFavoriteOnly = false,
                selectedClasses = emptySet(),
            ),
        )
    }
}
