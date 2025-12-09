package com.github.arhor.spellbindr.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed class AppDestination(open val title: String) {
    @Serializable
    data object CharactersHome : AppDestination(title = "Characters")

    @Serializable
    data class CharacterSheet(val characterId: String) : AppDestination(title = "Character")

    @Serializable
    data class CharacterEditor(val characterId: String? = null) : AppDestination(title = "Character Editor")

    @Serializable
    data class CharacterSpellPicker(val characterId: String) : AppDestination(title = "Add Spells")

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

    @Serializable
    data object Settings : AppDestination(title = "Settings")
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
    BottomNavItem(
        destination = AppDestination.Settings,
        label = "Settings",
        icon = Icons.Outlined.Settings,
    ),
)
