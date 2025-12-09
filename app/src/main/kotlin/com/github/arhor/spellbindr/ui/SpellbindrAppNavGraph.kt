package com.github.arhor.spellbindr.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.ui.feature.characters.CHARACTER_SPELL_SELECTION_RESULT_KEY
import com.github.arhor.spellbindr.ui.feature.characters.CharacterEditorScreen
import com.github.arhor.spellbindr.ui.feature.characters.CharacterSpellPickerRoute
import com.github.arhor.spellbindr.ui.feature.characters.CharactersListScreen
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumRoute
import com.github.arhor.spellbindr.ui.feature.compendium.spells.details.SpellDetailScreen
import com.github.arhor.spellbindr.ui.feature.dice.DiceRollerRoute
import com.github.arhor.spellbindr.ui.feature.settings.SettingsScreen
import com.github.arhor.spellbindr.ui.navigation.AppDestination

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
                onCharacterSelected = { characterId -> controller.navigate(AppDestination.CharacterSheet(characterId)) },
                onCreateCharacter = { controller.navigate(AppDestination.CharacterEditor()) },
            )
        }
        composable<AppDestination.CharacterSheet> { entry ->
            CharacterSheetRoute(
                vm = hiltViewModel(entry),
                savedStateHandle = entry.savedStateHandle,
                onBack = controller::navigateUp,
                onOpenSpellDetail = { controller.navigate(AppDestination.SpellDetail(it)) },
                onAddSpells = { controller.navigate(AppDestination.CharacterSpellPicker(characterId = it)) },
                onOpenFullEditor = { controller.navigate(AppDestination.CharacterEditor(characterId = it)) },
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
                onSpellSelected = { id -> controller.navigate(AppDestination.SpellDetail(id)) },
            )
        }
        composable<AppDestination.SpellDetail> {
            SpellDetailScreen(
                vm = hiltViewModel(it),
                spellId = it.toRoute<AppDestination.SpellDetail>().spellId,
                onBackClick = controller::navigateUp,
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
