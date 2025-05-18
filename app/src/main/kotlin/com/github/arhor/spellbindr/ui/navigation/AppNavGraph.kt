package com.github.arhor.spellbindr.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationWizardScreen
import com.github.arhor.spellbindr.ui.screens.characters.search.CharacterListScreen
import com.github.arhor.spellbindr.ui.screens.spells.details.SpellDetailScreen
import com.github.arhor.spellbindr.ui.screens.spells.favorites.FavoriteSpellsScreen
import com.github.arhor.spellbindr.ui.screens.spells.search.SpellSearchScreen

@Composable
fun AppNavGraph() {
    val controller = rememberNavController()
    val stackEntry by controller.currentBackStackEntryAsState()
    val currentDestination = stackEntry?.destination

    Scaffold(
        bottomBar = {
            AppNavBar(
                onItemClick = {
                    controller.navigate(it) {
                        popUpTo(controller.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                isItemSelected = { it inSameHierarchyWith currentDestination },
            )
        }
    ) { innerPadding ->
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_stars),
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                contentDescription = "Global background",
            )
            NavHost(
                navController = controller,
                startDestination = AppRoute.Spells,
                modifier = Modifier.padding(innerPadding),
            ) {
                navigation<AppRoute.Spells>(
                    startDestination = AppRoute.Spells.Search
                ) {
                    composable<AppRoute.Spells.Search> {
                        SpellSearchScreen(
                            onFavorClick = { controller.navigate(route = AppRoute.Spells.Favorite) },
                            onSpellClick = { controller.navigate(route = AppRoute.Spells.Details(it)) },
                        )
                    }
                    composable<AppRoute.Spells.Favorite> {
                        FavoriteSpellsScreen(
                            onBackClick = { controller.navigateUp() },
                            onSpellClick = { controller.navigate(route = AppRoute.Spells.Details(it)) },
                        )
                    }
                    composable<AppRoute.Spells.Details> {
                        SpellDetailScreen(
                            spellName = it.toRoute<AppRoute.Spells.Details>().spellName,
                            onBackClick = { controller.navigateUp() },
                        )
                    }
                }

                navigation<AppRoute.Characters>(
                    startDestination = AppRoute.Characters.Search
                ) {
                    composable<AppRoute.Characters.Search> {
                        CharacterListScreen(
                            onCreateNewCharacter = { controller.navigate(route = AppRoute.Characters.Create) }
                        )
                    }
                    composable<AppRoute.Characters.Create> {
                        CharacterCreationWizardScreen()
                    }

                    navigation<AppRoute.Characters.Create>(
                        startDestination = AppRoute.Characters.Create.NameAndBackground
                    ) {
                        composable<AppRoute.Characters.Create.NameAndBackground> {
                            NameAndBackgroundScreen(
                                onNext = { controller.navigate(route = AppRoute.Characters.Create.Race) }
                            )
                        }
                        composable<AppRoute.Characters.Create.Race> {
                            RaceSelectionScreen(
                                onNext = { controller.navigate(route = AppRoute.Characters.Create.Class) }
                            )
                        }
                        composable<AppRoute.Characters.Create.Class> {
                            ClassSelectionScreen(
                                onNext = { controller.navigate(route = AppRoute.Characters.Create.Abilities) }
                            )
                        }
                        composable<AppRoute.Characters.Create.Abilities> {
                            AbilitiesScreen(
                                onNext = { controller.navigate(route = AppRoute.Characters.Create.Skills) }
                            )
                        }
                        composable<AppRoute.Characters.Create.Skills> {
                            SkillsScreen(
                                onNext = { controller.navigate(route = AppRoute.Characters.Create.Equipment) }
                            )
                        }
                        composable<AppRoute.Characters.Create.Equipment> {
                            EquipmentScreen(
                                onNext = { controller.navigate(route = AppRoute.Characters.Create.Spells) }
                            )
                        }
                        composable<AppRoute.Characters.Create.Spells> {
                            SpellsScreen(
                                onNext = { controller.navigate(route = AppRoute.Characters.Create.Appearance) }
                            )
                        }
                        composable<AppRoute.Characters.Create.Appearance> {
                            AppearanceScreen(
                                onNext = { controller.navigate(route = AppRoute.Characters.Create.Summary) }
                            )
                        }
                        composable<AppRoute.Characters.Create.Summary> {
                            SummaryScreen(
                                onFinish = { controller.popBackStack(route = AppRoute.Characters, inclusive = false) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NameAndBackgroundScreen(onNext: () -> Unit = {}) {
}

@Composable
fun RaceSelectionScreen(onNext: () -> Unit = {}) {
}

@Composable
fun ClassSelectionScreen(onNext: () -> Unit = {}) {
}

@Composable
fun AbilitiesScreen(onNext: () -> Unit = {}) {
}

@Composable
fun SkillsScreen(onNext: () -> Unit = {}) {
}

@Composable
fun EquipmentScreen(onNext: () -> Unit = {}) {
}

@Composable
fun SpellsScreen(onNext: () -> Unit = {}) {
}

@Composable
fun AppearanceScreen(onNext: () -> Unit = {}) {
}

@Composable
fun SummaryScreen(onFinish: () -> Unit = {}) {
}
