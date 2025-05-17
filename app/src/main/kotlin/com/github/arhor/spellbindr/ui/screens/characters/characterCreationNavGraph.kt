package com.github.arhor.spellbindr.ui.screens.characters

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

enum class CharacterCreationStep {
    NAME_AND_BACKGROUND, RACE, CLASS, ABILITIES, SKILLS, EQUIPMENT, SPELLS, APPEARANCE, SUMMARY, ;

    override fun toString(): String = name.lowercase()
}

sealed class CharacterCreationDestination(val step: CharacterCreationStep) {
    object NameAndBackground : CharacterCreationDestination(CharacterCreationStep.NAME_AND_BACKGROUND)
    object Race : CharacterCreationDestination(CharacterCreationStep.RACE)
    object Class : CharacterCreationDestination(CharacterCreationStep.CLASS)
    object Abilities : CharacterCreationDestination(CharacterCreationStep.ABILITIES)
    object Skills : CharacterCreationDestination(CharacterCreationStep.SKILLS)
    object Equipment : CharacterCreationDestination(CharacterCreationStep.EQUIPMENT)
    object Spells : CharacterCreationDestination(CharacterCreationStep.SPELLS)
    object Appearance : CharacterCreationDestination(CharacterCreationStep.APPEARANCE)
    object Summary : CharacterCreationDestination(CharacterCreationStep.SUMMARY)
}

fun NavGraphBuilder.characterCreationNavGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = CharacterCreationDestination.NameAndBackground.step.toString(), route = "character_creation"
    ) {
        composable(CharacterCreationDestination.NameAndBackground.step.toString()) {
            NameAndBackgroundScreen(onNext = { navController.navigate(CharacterCreationDestination.Race.step.toString()) })
        }
        composable(CharacterCreationDestination.Race.step.toString()) {
            RaceSelectionScreen(onNext = { navController.navigate(CharacterCreationDestination.Class.step.toString()) })
        }
        composable(CharacterCreationDestination.Class.step.toString()) {
            ClassSelectionScreen(onNext = { navController.navigate("abilities") })
        }
        composable("abilities") {
            AbilitiesScreen(onNext = { navController.navigate("skills") })
        }
        composable("skills") {
            SkillsScreen(onNext = { navController.navigate("equipment") })
        }
        composable("equipment") {
            EquipmentScreen(onNext = { navController.navigate("spells") })
        }
        composable("spells") {
            SpellsScreen(onNext = { navController.navigate("appearance") })
        }
        composable("appearance") {
            AppearanceScreen(onNext = { navController.navigate("summary") })
        }
        composable("summary") {
            SummaryScreen(onFinish = { navController.popBackStack("character_list", false) })
        }
    }
}

@Composable
fun NameAndBackgroundScreen(onNext: () -> Unit) {
}

@Composable
fun RaceSelectionScreen(onNext: () -> Unit) {
}

@Composable
fun ClassSelectionScreen(onNext: () -> Unit) {
}

@Composable
fun AbilitiesScreen(onNext: () -> Unit) {
}

@Composable
fun SkillsScreen(onNext: () -> Unit) {
}

@Composable
fun EquipmentScreen(onNext: () -> Unit) {
}

@Composable
fun SpellsScreen(onNext: () -> Unit) {
}

@Composable
fun AppearanceScreen(onNext: () -> Unit) {
}

@Composable
fun SummaryScreen(onFinish: () -> Unit) {
}

