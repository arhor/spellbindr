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
import com.github.arhor.spellbindr.ui.feature.characters.editor.CharacterEditorRoute
import com.github.arhor.spellbindr.ui.feature.characters.list.CharacterListItem
import com.github.arhor.spellbindr.ui.feature.characters.list.CharactersListRoute
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetRoute
import com.github.arhor.spellbindr.ui.feature.characters.spellpicker.CharacterSpellPickerRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumRoute
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.AlignmentsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.conditions.ConditionsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.races.RacesRoute
import com.github.arhor.spellbindr.ui.feature.compendium.spells.CompendiumSpellsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.spells.details.SpellDetailRoute
import com.github.arhor.spellbindr.ui.feature.dice.DiceRollerRoute
import com.github.arhor.spellbindr.ui.feature.settings.SettingsRoute

/**
 * Main navigation graph for the Spellbindr application.
 *
 * Defines all composable routes and handles navigation arguments.
 * Uses typed destinations from [AppDestination].
 */
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
        composable<AppDestination.CharactersHome> { navEntry ->
            CharactersListRoute(
                vm = hiltViewModel(navEntry),
                onCharacterSelected = { character ->
                    controller.navigate(
                        AppDestination.CharacterSheet(
                            characterId = character.id,
                            initialName = character.name.ifBlank { null },
                            initialSubtitle = character.initialSubtitle(),
                        ),
                    )
                },
                onCreateCharacter = { controller.navigate(AppDestination.CharacterEditor()) },
            )
        }
        composable<AppDestination.CharacterSheet> { navEntry ->
            CharacterSheetRoute(
                vm = hiltViewModel(navEntry),
                savedStateHandle = navEntry.savedStateHandle,
                args = navEntry.toRoute<AppDestination.CharacterSheet>(),
                onOpenSpellDetail = { controller.navigate(AppDestination.SpellDetail(it)) },
                onAddSpells = { controller.navigate(AppDestination.CharacterSpellPicker(it)) },
                onOpenFullEditor = { controller.navigate(AppDestination.CharacterEditor(it)) },
                onCharacterDeleted = controller::navigateUp,
                onBack = controller::navigateUp,
            )
        }
        composable<AppDestination.CharacterEditor> { navEntry ->
            CharacterEditorRoute(
                vm = hiltViewModel(navEntry),
                onBack = controller::navigateUp,
                onFinished = controller::navigateUp,
            )
        }
        composable<AppDestination.CompendiumSections> {
            CompendiumRoute(
                controller = controller,
            )
        }
        composable<AppDestination.CompendiumSpells> { navEntry ->
            CompendiumSpellsRoute(
                vm = hiltViewModel(navEntry),
                onSpellSelected = { controller.navigate(AppDestination.SpellDetail(it.id)) },
                onBack = controller::navigateUp,
            )
        }
        composable<AppDestination.CompendiumConditions> { navEntry ->
            ConditionsRoute(
                vm = hiltViewModel(navEntry),
                onBack = controller::navigateUp,
            )
        }
        composable<AppDestination.CompendiumAlignments> { navEntry ->
            AlignmentsRoute(vm = hiltViewModel(navEntry), onBack = controller::navigateUp)
        }
        composable<AppDestination.CompendiumRaces> { navEntry ->
            RacesRoute(vm = hiltViewModel(navEntry), onBack = controller::navigateUp)
        }
        composable<AppDestination.SpellDetail> { navEntry ->
            SpellDetailRoute(
                vm = hiltViewModel(navEntry),
                spellId = navEntry.toRoute<AppDestination.SpellDetail>().spellId,
                onBack = controller::navigateUp,
            )
        }
        composable<AppDestination.CharacterSpellPicker> { navEntry ->
            CharacterSpellPickerRoute(
                vm = hiltViewModel(navEntry),
                onBack = controller::navigateUp,
                onSpellSelected = {
                    controller.previousBackStackEntry?.savedStateHandle?.set(
                        CHARACTER_SPELL_SELECTION_RESULT_KEY,
                        it,
                    )
                    controller.navigateUp()
                },
            )
        }
        composable<AppDestination.Dice> { navEntry ->
            DiceRollerRoute(
                vm = hiltViewModel(navEntry),
            )
        }
        composable<AppDestination.Settings> { navEntry ->
            SettingsRoute(
                vm = hiltViewModel(navEntry),
            )
        }
    }
}

private fun CharacterListItem.initialSubtitle(): String {
    return buildString {
        append("Level ${level.coerceAtLeast(1)}")
        if (className.isNotBlank()) {
            append(' ')
            append(className)
        }
    }
}
