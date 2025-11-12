package com.github.arhor.spellbindr.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed class AppDestination(open val title: String) {
    @Serializable
    data object CharactersHome
        : AppDestination(title = "Characters")

    @Serializable
    data class CharacterSheet(val characterId: String) : AppDestination(title = "Character Sheet")

    @Serializable
    data object CharacterCreate : AppDestination(title = "Create Character")

    @Serializable
    data class CharacterLevelUp(val characterId: String) : AppDestination(title = "Level Up")

    @Serializable
    data object Compendium : AppDestination(title = "Compendium")

    @Serializable
    data class SpellDetail(val spellId: String) : AppDestination(title = "Spell Details")

    @Serializable
    data class MonsterDetail(val monsterId: String) : AppDestination(title = "Monster Details")

    @Serializable
    data class RuleDetail(val ruleId: String) : AppDestination(title = "Rule Details")

    @Serializable
    data object Dice : AppDestination(title = "Dice")
}

data class BottomNavItem(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
)

val BottomNavItems = listOf(
    BottomNavItem(
        destination = AppDestination.CharactersHome,
        label = "Characters",
        icon = Icons.Outlined.Groups,
    ),
    BottomNavItem(
        destination = AppDestination.Compendium,
        label = "Compendium",
        icon = Icons.AutoMirrored.Outlined.MenuBook,
    ),
    BottomNavItem(
        destination = AppDestination.Dice,
        label = "Dice",
        icon = Icons.Outlined.Casino,
    ),
)
