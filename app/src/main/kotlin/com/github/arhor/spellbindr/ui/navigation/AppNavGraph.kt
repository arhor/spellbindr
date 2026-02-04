package com.github.arhor.spellbindr.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.ui.feature.character.editor.CharacterEditorRoute
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupRoute
import com.github.arhor.spellbindr.ui.feature.character.list.CharactersListRoute
import com.github.arhor.spellbindr.ui.feature.character.list.model.CharacterListItem
import com.github.arhor.spellbindr.ui.feature.character.list.model.CreateCharacterMode
import com.github.arhor.spellbindr.ui.feature.character.sheet.CharacterSheetRoute
import com.github.arhor.spellbindr.ui.feature.character.spellpicker.CharacterSpellPickerRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumSections
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.AlignmentsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.conditions.ConditionsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.races.RacesRoute
import com.github.arhor.spellbindr.ui.feature.compendium.spelldetails.SpellDetailsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.spells.SpellsRoute
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
        charactersNavGraph(controller)
        compendiumNavGraph(controller)
        composable<AppDestination.Dice> {
            DiceRollerRoute()
        }
        composable<AppDestination.Settings> {
            SettingsRoute()
        }
    }
}

private fun NavGraphBuilder.charactersNavGraph(controller: NavHostController) {
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
            onCreateCharacter = { mode ->
                when (mode) {
                    CreateCharacterMode.GuidedSetup -> controller.navigate(AppDestination.GuidedCharacterSetup)
                    CreateCharacterMode.ManualEntry -> controller.navigate(AppDestination.CharacterEditor())
                }
            },
        )
    }
    composable<AppDestination.CharacterEditor> { navEntry ->
        CharacterEditorRoute(
            vm = hiltViewModel(navEntry),
            onBack = controller::navigateUp,
            onFinished = controller::navigateUp,
        )
    }
    composable<AppDestination.GuidedCharacterSetup> {
        GuidedCharacterSetupRoute(
            onBack = controller::navigateUp,
            onFinished = { characterId ->
                controller.navigate(
                    AppDestination.CharacterSheet(
                        characterId = characterId,
                        initialName = null,
                        initialSubtitle = "Level 1",
                    ),
                ) {
                    popUpTo(AppDestination.GuidedCharacterSetup) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
        )
    }
    composable<AppDestination.CharacterSheet> { navEntry ->
        CharacterSheetRoute(
            vm = hiltViewModel(navEntry),
            savedStateHandle = navEntry.savedStateHandle,
            args = navEntry.toRoute<AppDestination.CharacterSheet>(),
            onOpenSpellDetail = { controller.navigate(AppDestination.SpellDetails(it)) },
            onAddSpells = { controller.navigate(AppDestination.CharacterSpellPicker(it)) },
            onOpenFullEditor = { controller.navigate(AppDestination.CharacterEditor(it)) },
            onCharacterDeleted = controller::navigateUp,
            onBack = controller::navigateUp,
        )
    }
    composable<AppDestination.CharacterSpellPicker> {
        CharacterSpellPickerRoute(
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
}

private fun NavGraphBuilder.compendiumNavGraph(controller: NavHostController) {
    composable<AppDestination.CompendiumSections> {
        CompendiumRoute(
            onSectionClick = { controller.navigate(it.toAppDestination()) },
        )
    }
    composable<AppDestination.Spells> {
        SpellsRoute(
            onSpellSelected = { controller.navigate(AppDestination.SpellDetails(it.id)) },
            onBack = controller::navigateUp,
        )
    }
    composable<AppDestination.SpellDetails> {
        SpellDetailsRoute(
            onBack = controller::navigateUp,
        )
    }
    composable<AppDestination.Conditions> {
        ConditionsRoute(
            onBack = controller::navigateUp,
        )
    }
    composable<AppDestination.Alignments> {
        AlignmentsRoute(
            onBack = controller::navigateUp,
        )
    }
    composable<AppDestination.Races> {
        RacesRoute(
            onBack = controller::navigateUp,
        )
    }
}

private fun CompendiumSections.toAppDestination(): AppDestination = when (this) {
    CompendiumSections.SPELLS -> AppDestination.Spells
    CompendiumSections.CONDITIONS -> AppDestination.Conditions
    CompendiumSections.ALIGNMENTS -> AppDestination.Alignments
    CompendiumSections.RACES -> AppDestination.Races
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
