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
import com.github.arhor.spellbindr.ui.feature.characters.list.CharactersListRoute
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetRoute
import com.github.arhor.spellbindr.ui.feature.characters.spellpicker.CharacterSpellPickerRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumAlignmentsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumClassesRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumConditionsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumEquipmentRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumFeaturesRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumRacesRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumSectionsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumSpellsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumTraitsRoute
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
                onCharacterSelected = { controller.navigate(AppDestination.CharacterSheet(it)) },
                onCreateCharacter = { controller.navigate(AppDestination.CharacterEditor()) },
            )
        }
        composable<AppDestination.CharacterSheet> { navEntry ->
            CharacterSheetRoute(
                vm = hiltViewModel(navEntry),
                savedStateHandle = navEntry.savedStateHandle,
                args = navEntry.toRoute<AppDestination.CharacterSheet>(),
                onOpenSpellDetail = { spellId -> controller.navigate(AppDestination.SpellDetail(spellId)) },
                onAddSpells = { charId -> controller.navigate(AppDestination.CharacterSpellPicker(charId)) },
                onOpenFullEditor = { charId -> controller.navigate(AppDestination.CharacterEditor(charId)) },
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
            CompendiumSectionsRoute(
                onNavigateToSpells = { controller.navigate(AppDestination.CompendiumSpells) },
                onNavigateToConditions = { controller.navigate(AppDestination.CompendiumConditions) },
                onNavigateToAlignments = { controller.navigate(AppDestination.CompendiumAlignments) },
                onNavigateToRaces = { controller.navigate(AppDestination.CompendiumRaces) },
                onNavigateToTraits = { controller.navigate(AppDestination.CompendiumTraits) },
                onNavigateToFeatures = { controller.navigate(AppDestination.CompendiumFeatures) },
                onNavigateToClasses = { controller.navigate(AppDestination.CompendiumClasses) },
                onNavigateToEquipment = { controller.navigate(AppDestination.CompendiumEquipment) },
            )
        }
        composable<AppDestination.CompendiumSpells> { navEntry ->
            CompendiumSpellsRoute(
                vm = hiltViewModel(navEntry),
                onSpellSelected = { controller.navigate(AppDestination.SpellDetail(it.id, it.name)) },
                onBack = controller::navigateUp,
            )
        }
        composable<AppDestination.CompendiumConditions> { navEntry ->
            CompendiumConditionsRoute(
                vm = hiltViewModel(navEntry),
                onBack = controller::navigateUp,
            )
        }
        composable<AppDestination.CompendiumAlignments> { navEntry ->
            CompendiumAlignmentsRoute(
                vm = hiltViewModel(navEntry),
                onBack = controller::navigateUp,
            )
        }
        composable<AppDestination.CompendiumRaces> { navEntry ->
            CompendiumRacesRoute(
                vm = hiltViewModel(navEntry),
                onBack = controller::navigateUp,
            )
        }
        composable<AppDestination.CompendiumTraits> {
            CompendiumTraitsRoute(onBack = controller::navigateUp)
        }
        composable<AppDestination.CompendiumFeatures> {
            CompendiumFeaturesRoute(onBack = controller::navigateUp)
        }
        composable<AppDestination.CompendiumClasses> {
            CompendiumClassesRoute(onBack = controller::navigateUp)
        }
        composable<AppDestination.CompendiumEquipment> {
            CompendiumEquipmentRoute(onBack = controller::navigateUp)
        }
        composable<AppDestination.SpellDetail> { navEntry ->
            SpellDetailRoute(
                vm = hiltViewModel(navEntry),
                args = navEntry.toRoute<AppDestination.SpellDetail>(),
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
