package com.github.arhor.spellbindr.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.arhor.spellbindr.ui.feature.characters.list.CharacterListItem
import kotlinx.serialization.Serializable

@Serializable
sealed class AppDestination(open val title: String) {
    @Serializable
    data object CharactersHome : AppDestination(title = "Characters")

    @Serializable
    data class CharacterSheet(
        val character: CharacterListItem,
    ) : AppDestination(title = "Character")

    @Serializable
    data class CharacterEditor(val characterId: String? = null) : AppDestination(title = "Character Editor")

    @Serializable
    data class CharacterSpellPicker(val characterId: String) : AppDestination(title = "Add Spells")

    @Serializable
    data object Compendium : AppDestination(title = "Compendium")

    @Serializable
    data class SpellDetail(
        val spellId: String,
        val initialName: String? = null,
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

const val CHARACTER_SPELL_SELECTION_RESULT_KEY = "character_spell_selection_result"
