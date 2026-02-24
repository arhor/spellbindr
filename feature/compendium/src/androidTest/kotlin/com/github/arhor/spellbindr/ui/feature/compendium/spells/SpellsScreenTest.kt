package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpellsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val wizard = EntityRef("wizard")
    private val cleric = EntityRef("cleric")

    private val spells = listOf(
        buildSpell(
            id = "magic_missile",
            name = "Magic Missile",
            classes = listOf(wizard),
        ),
        buildSpell(
            id = "healing_word",
            name = "Healing Word",
            classes = listOf(cleric),
        ),
    )

    @Test
    fun `render should show loading indicator when state is loading`() {
        // Given
        val state = SpellsUiState.Loading

        // When
        composeTestRule.setContent {
            AppTheme {
                SpellsScreen(uiState = state)
            }
        }

        // Then
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun `render should show error message when state is failure`() {
        // Given
        val state = SpellsUiState.Failure("Failed to load spells.")

        // When
        composeTestRule.setContent {
            AppTheme {
                SpellsScreen(uiState = state)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Failed to load spells.").assertIsDisplayed()
    }

    @Test
    fun `render should hide class filters when casting classes are empty`() {
        // Given
        val state = contentState(castingClasses = emptyList())

        // When
        composeTestRule.setContent {
            AppTheme {
                SpellsScreen(uiState = state)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Wizard").assertDoesNotExist()
    }

    @Test
    fun `input should call onQueryChanged when user types`() {
        // Given
        var capturedIntent: SpellsIntent? = null
        val state = contentState(query = "")

        // When
        composeTestRule.setContent {
            AppTheme {
                SpellsScreen(
                    uiState = state,
                    dispatch = { intent -> capturedIntent = intent },
                )
            }
        }
        composeTestRule.onNodeWithText("Search spell by name").performTextInput("Magic")

        // Then
        composeTestRule.runOnIdle {
            assertThat(capturedIntent).isEqualTo(SpellsIntent.QueryChanged("Magic"))
        }
    }

    @Test
    fun `click should call onFavoriteClick when favorite icon tapped`() {
        // Given
        var capturedIntent: SpellsIntent? = null
        val state = contentState(showFavoriteOnly = false)

        // When
        composeTestRule.setContent {
            AppTheme {
                SpellsScreen(
                    uiState = state,
                    dispatch = { intent -> capturedIntent = intent },
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Favorites: OFF").performClick()

        // Then
        composeTestRule.runOnIdle {
            assertThat(capturedIntent).isEqualTo(SpellsIntent.FavoritesToggled)
        }
    }

    @Test
    fun `click should call onClassToggled when class chip tapped`() {
        // Given
        var capturedIntent: SpellsIntent? = null
        val state = contentState(castingClasses = listOf(wizard, cleric))

        // When
        composeTestRule.setContent {
            AppTheme {
                SpellsScreen(
                    uiState = state,
                    dispatch = { intent -> capturedIntent = intent },
                )
            }
        }
        composeTestRule.onNodeWithText("Wizard").performClick()

        // Then
        composeTestRule.runOnIdle {
            assertThat(capturedIntent).isEqualTo(SpellsIntent.ClassFilterToggled(wizard))
        }
    }

    @Test
    fun `click should call onSpellClick when spell card tapped`() {
        // Given
        var capturedIntent: SpellsIntent? = null
        val state = contentState(spells = spells)

        // When
        composeTestRule.setContent {
            AppTheme {
                SpellsScreen(
                    uiState = state,
                    dispatch = { intent -> capturedIntent = intent },
                )
            }
        }
        composeTestRule.onNodeWithText("Magic Missile").performClick()

        // Then
        composeTestRule.runOnIdle {
            assertThat(capturedIntent).isEqualTo(SpellsIntent.SpellClicked(spells[0]))
        }
    }

    private fun contentState(
        query: String = "",
        spells: List<Spell> = this.spells,
        showFavoriteOnly: Boolean = false,
        castingClasses: List<EntityRef> = listOf(wizard, cleric),
        selectedClasses: Set<EntityRef> = emptySet(),
    ): SpellsUiState.Content = SpellsUiState.Content(
        query = query,
        spells = spells,
        showFavoriteOnly = showFavoriteOnly,
        castingClasses = castingClasses,
        selectedClasses = selectedClasses,
    )

    private fun buildSpell(
        id: String,
        name: String,
        classes: List<EntityRef>,
    ): Spell = Spell(
        id = id,
        name = name,
        desc = listOf("desc"),
        level = 1,
        range = "60 ft",
        ritual = false,
        school = EntityRef("evocation"),
        duration = "Instant",
        castingTime = "1 action",
        classes = classes,
        components = listOf("V"),
        concentration = false,
        source = "PHB",
    )
}
