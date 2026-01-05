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
    data class CharacterSheet(
        val characterId: String,
        val initialName: String? = null,
        val initialSubtitle: String? = null,
    ) : AppDestination(title = "Character")

    @Serializable
    data class CharacterEditor(val characterId: String? = null) : AppDestination(title = "Character Editor")

    @Serializable
    data object GuidedCharacterSetup : AppDestination(title = "Guided setup")

    @Serializable
    data class CharacterSpellPicker(val characterId: String) : AppDestination(title = "Add Spells")

    @Serializable
    data object CompendiumSections : AppDestination(title = "Compendium")

    @Serializable
    data object Spells : AppDestination(title = "Spells")

    @Serializable
    data object Conditions : AppDestination(title = "Conditions")

    @Serializable
    data object Alignments : AppDestination(title = "Alignments")

    @Serializable
    data object Races : AppDestination(title = "Races")

    @Serializable
    data class SpellDetails(
        val spellId: String,
    ) : AppDestination(title = "Spell Details")

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
        destination = AppDestination.CompendiumSections,
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

const val CHARACTER_SPELL_SELECTION_RESULT_KEY = "character_spell_selection_result"
