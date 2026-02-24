package com.github.arhor.spellbindr.ui.feature.character

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.weapons.WeaponCatalogDialog
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.weapons.WeaponEditorDialog
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.WeaponCatalogUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.WeaponEditorState
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeaponCatalogSelectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `WeaponCatalogDialog should prefill editor while allowing manual edits`() {
        // Given
        val catalog = listOf(
            WeaponCatalogUiModel(
                id = "greatsword",
                name = "Greatsword",
                category = EquipmentCategory.MARTIAL,
                categories = setOf(EquipmentCategory.MARTIAL, EquipmentCategory.MELEE),
                damageDiceCount = 2,
                damageDieSize = 8,
                damageType = DamageType.SLASHING,
            ),
            WeaponCatalogUiModel(
                id = "shortbow",
                name = "Shortbow",
                category = EquipmentCategory.SIMPLE,
                categories = setOf(EquipmentCategory.SIMPLE, EquipmentCategory.RANGED),
                damageDiceCount = 1,
                damageDieSize = 6,
                damageType = DamageType.PIERCING,
            ),
        )
        var editorState by mutableStateOf(WeaponEditorState())
        var isCatalogVisible by mutableStateOf(false)

        // When
        composeTestRule.setContent {
            AppTheme {
                WeaponEditorDialog(
                    editorState = editorState,
                    onDismiss = {},
                    onNameChange = { editorState = editorState.copy(name = it) },
                    onCatalogOpen = { isCatalogVisible = true },
                    onAbilityChange = { editorState = editorState.copy(abilityId = it) },
                    onUseAbilityForDamageChange = { editorState = editorState.copy(useAbilityForDamage = it) },
                    onProficiencyChange = { editorState = editorState.copy(proficient = it) },
                    onDiceCountChange = { editorState = editorState.copy(damageDiceCount = it) },
                    onDieSizeChange = { editorState = editorState.copy(damageDieSize = it) },
                    onDamageTypeChange = { editorState = editorState.copy(damageType = it) },
                    onDelete = {},
                    onSave = {},
                )
                if (isCatalogVisible) {
                    WeaponCatalogDialog(
                        catalog = catalog,
                        onDismiss = { isCatalogVisible = false },
                        onItemSelected = { id ->
                            val entry = catalog.first { it.id == id }
                            editorState = editorState.copy(
                                catalogId = entry.id,
                                name = entry.name,
                                category = entry.category,
                                categories = entry.categories,
                                damageDiceCount = entry.damageDiceCount.toString(),
                                damageDieSize = entry.damageDieSize.toString(),
                                damageType = entry.damageType,
                            )
                            isCatalogVisible = false
                        },
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Select weapon").performClick()
        val catalogVisible = composeTestRule.onAllNodesWithText("Weapon catalog").fetchSemanticsNodes().isNotEmpty()
        composeTestRule.onNodeWithText("Greatsword").performClick()

        // Then
        assertThat(catalogVisible).isTrue()
        composeTestRule.onNodeWithTag("WeaponNameField", useUnmergedTree = true)
            .assertTextEquals("Greatsword")
        composeTestRule.onNodeWithTag("WeaponDiceCountField", useUnmergedTree = true)
            .assertTextEquals("2")
        composeTestRule.onNodeWithTag("WeaponDieSizeField", useUnmergedTree = true)
            .assertTextEquals("8")

        composeTestRule.onNodeWithTag("WeaponNameField", useUnmergedTree = true).performTextClearance()
        composeTestRule.onNodeWithTag("WeaponNameField", useUnmergedTree = true)
            .performTextInput("Custom blade")
        composeTestRule.onNodeWithTag("WeaponNameField", useUnmergedTree = true)
            .assertTextEquals("Custom blade")
    }
}
