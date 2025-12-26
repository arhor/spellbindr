package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class SpellSearchScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `SpellSearchScreen should show loaded spell group and name when state is loaded`() {
        // Given
        val spell = Spell(
            id = "magic_missile",
            name = "Magic Missile",
            desc = listOf("Glowing darts of force strike targets."),
            level = 1,
            range = "120 ft",
            ritual = false,
            school = EntityRef(id = "evocation"),
            duration = "Instant",
            castingTime = "1 action",
            classes = listOf(EntityRef(id = "wizard")),
            components = listOf("V", "S"),
            concentration = false,
            source = "PHB",
        )
        val spellsByLevel = mapOf(1 to listOf(spell))
        val state = CompendiumViewModel.SpellsState(
            spellsByLevel = spellsByLevel,
            expandedSpellLevels = mapOf(1 to true),
            expandedAll = true,
            uiState = CompendiumViewModel.SpellsUiState.Loaded(
                spells = listOf(spell),
                spellsByLevel = spellsByLevel,
            ),
        )

        // When
        composeTestRule.setContent {
            AppTheme {
                SpellSearchScreen(
                    state = state,
                    onQueryChanged = {},
                    onFiltersClick = {},
                    onFavoriteClick = {},
                    onGroupToggle = {},
                    onToggleAllGroups = {},
                    onSpellClick = {},
                    onSubmitFilters = {},
                    onCancelFilters = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Lvl. 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Magic Missile").assertIsDisplayed()
    }
}
