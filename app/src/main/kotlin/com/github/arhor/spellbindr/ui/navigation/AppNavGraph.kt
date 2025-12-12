package com.github.arhor.spellbindr.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.ui.feature.characters.CHARACTER_SPELL_SELECTION_RESULT_KEY
import com.github.arhor.spellbindr.ui.feature.characters.CharacterEditorScreen
import com.github.arhor.spellbindr.ui.feature.characters.CharacterSpellPickerRoute
import com.github.arhor.spellbindr.ui.feature.characters.CharactersListScreen
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumRoute
import com.github.arhor.spellbindr.ui.feature.compendium.spells.details.SpellDetailScreen
import com.github.arhor.spellbindr.ui.feature.dice.DiceRollerRoute
import com.github.arhor.spellbindr.ui.feature.settings.SettingsScreen

@Composable
fun SpellbindrAppNavGraph(
    controller: NavHostController,
    innerPadding: PaddingValues,
) {
    NavHost(
        navController = controller,
        startDestination = AppDestination.CharactersHome,
        modifier = Modifier.padding(innerPadding),
    ) {
        composable<AppDestination.CharactersHome> {
            CharactersListScreen(
                vm = hiltViewModel(it),
                onCharacterSelected = { character ->
                    controller.navigate(
                        AppDestination.CharacterSheet(
                            characterId = character.id,
                            initialName = character.name,
                            initialSubtitle = character.headline.ifBlank { null },
                        ),
                    )
                },
                onCreateCharacter = { controller.navigate(AppDestination.CharacterEditor()) },
            )
        }
        composable<AppDestination.CharacterSheet> {
            CharacterSheetRoute(
                vm = hiltViewModel(it),
                savedStateHandle = it.savedStateHandle,
                onOpenSpellDetail = { spellId -> controller.navigate(AppDestination.SpellDetail(spellId)) },
                onAddSpells = { charId -> controller.navigate(AppDestination.CharacterSpellPicker(charId)) },
                onOpenFullEditor = { charId -> controller.navigate(AppDestination.CharacterEditor(charId)) },
                onCharacterDeleted = controller::navigateUp,
            )
        }
        composable<AppDestination.CharacterEditor> {
            CharacterEditorScreen(
                vm = hiltViewModel(it),
                onBack = controller::navigateUp,
                onFinished = controller::navigateUp,
            )
        }
        composable<AppDestination.Compendium> {
            CompendiumRoute(
                vm = hiltViewModel(it),
                onSpellSelected = { spell: Spell ->
                    controller.navigate(
                        AppDestination.SpellDetail(
                            spellId = spell.id,
                            initialName = spell.name,
                        ),
                    )
                },
            )
        }
        composable<AppDestination.SpellDetail> {
            SpellDetailScreen(
                vm = hiltViewModel(it),
                spellId = it.toRoute<AppDestination.SpellDetail>().spellId,
            )
        }
        composable<AppDestination.CharacterSpellPicker> {
            CharacterSpellPickerRoute(
                vm = hiltViewModel(it),
                onBack = controller::navigateUp,
                onSpellSelected = { assignments ->
                    controller.previousBackStackEntry?.savedStateHandle?.set(
                        CHARACTER_SPELL_SELECTION_RESULT_KEY,
                        ArrayList(assignments),
                    )
                    controller.navigateUp()
                },
            )
        }
        composable<AppDestination.Dice> {
            DiceRollerRoute(
                vm = hiltViewModel(it),
            )
        }
        composable<AppDestination.Settings> {
            SettingsScreen(
                vm = hiltViewModel(it),
            )
        }
    }
}
