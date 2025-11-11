package com.github.arhor.spellbindr.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed class AppDestination(open val title: String)

@Serializable
data object CharactersHomeDestination : AppDestination(title = "Characters")

@Serializable
data class CharacterSheetDestination(val characterId: String) :
    AppDestination(title = "Character Sheet")

@Serializable
data object CharacterCreateDestination : AppDestination(title = "Create Character")

@Serializable
data class CharacterLevelUpDestination(val characterId: String) :
    AppDestination(title = "Level Up")

@Serializable
data object LibraryDestination : AppDestination(title = "Library")

@Serializable
data class SpellDetailDestination(val spellId: String) :
    AppDestination(title = "Spell Details")

@Serializable
data class MonsterDetailDestination(val monsterId: String) :
    AppDestination(title = "Monster Details")

@Serializable
data class RuleDetailDestination(val ruleId: String) :
    AppDestination(title = "Rule Details")

@Serializable
data object DiceDestination : AppDestination(title = "Dice")

data class BottomNavItem(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
)

val BottomNavItems = listOf(
    BottomNavItem(
        destination = CharactersHomeDestination,
        label = "Characters",
        icon = Icons.Outlined.Groups,
    ),
    BottomNavItem(
        destination = LibraryDestination,
        label = "Library",
        icon = Icons.AutoMirrored.Outlined.MenuBook,
    ),
    BottomNavItem(
        destination = DiceDestination,
        label = "Dice",
        icon = Icons.Outlined.Casino,
    ),
)
