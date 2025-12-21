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
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.feature.characters.CHARACTER_SPELL_SELECTION_RESULT_KEY
import com.github.arhor.spellbindr.ui.feature.characters.CharacterEditorRoute
import com.github.arhor.spellbindr.ui.feature.characters.CharacterSpellPickerRoute
import com.github.arhor.spellbindr.ui.feature.characters.CharactersListRoute
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumRoute
import com.github.arhor.spellbindr.ui.feature.compendium.spells.details.SpellDetailRoute
import com.github.arhor.spellbindr.ui.feature.dice.DiceRollerRoute
import com.github.arhor.spellbindr.ui.feature.settings.SettingsRoute
import com.github.arhor.spellbindr.ui.feature.characters.CharacterListItem

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
            CharactersListRoute(
                vm = hiltViewModel(it),
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
        composable<AppDestination.CharacterSheet> {
            val args = it.toRoute<AppDestination.CharacterSheet>()
            CharacterSheetRoute(
                vm = hiltViewModel(it),
                savedStateHandle = it.savedStateHandle,
                args = args,
                onOpenSpellDetail = { spellId -> controller.navigate(AppDestination.SpellDetail(spellId)) },
                onAddSpells = { charId -> controller.navigate(AppDestination.CharacterSpellPicker(charId)) },
                onOpenFullEditor = { charId -> controller.navigate(AppDestination.CharacterEditor(charId)) },
                onCharacterDeleted = controller::navigateUp,
                onBack = controller::navigateUp,
            )
        }
        composable<AppDestination.CharacterEditor> {
            CharacterEditorRoute(
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
            val args = it.toRoute<AppDestination.SpellDetail>()
            SpellDetailRoute(
                vm = hiltViewModel(it),
                spellId = args.spellId,
                initialName = args.initialName,
                onBack = controller::navigateUp,
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
            SettingsRoute(
                vm = hiltViewModel(it),
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
