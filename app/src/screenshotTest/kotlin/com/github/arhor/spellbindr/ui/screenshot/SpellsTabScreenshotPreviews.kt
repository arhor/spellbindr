package com.github.arhor.spellbindr.ui.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.github.arhor.spellbindr.ui.feature.character.sheet.CharacterSheetUiState
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.CharacterSheetContent
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetTab
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.PactSlotUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellLevelUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellSlotUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellcastingClassUiModel

@PreviewTest
@Preview(widthDp = 390, heightDp = 585)
@Composable
fun SpellsTab_Screenshot() {
    ScreenshotHarness {
        val state = (CharacterSheetPreviewData.uiState as CharacterSheetUiState.Content)
            .copy(
                selectedTab = CharacterSheetTab.Spells,
                spells = CharacterSheetPreviewData.spells.copy(
                    concentration = null,
                    sharedSlots = listOf(
                        SpellSlotUiModel(level = 1, total = 4, expended = 3),
                        SpellSlotUiModel(level = 2, total = 3, expended = 1),
                        SpellSlotUiModel(level = 3, total = 1, expended = 0),
                        SpellSlotUiModel(level = 4, total = 0, expended = 0),
                    ),
                    pactSlots = PactSlotUiModel(
                        slotLevel = 2,
                        total = 2,
                        expended = 0,
                        isConfigured = true,
                    ),
                    spellcastingClasses = referenceLikeSpellcastingClasses(),
                ),
            )
        CharacterSheetContent(
            state = state,
            header = state.header,
            onTabSelected = {},
            onAddSpellsClick = {},
            onSpellSelected = {},
            onSpellRemoved = { _, _ -> },
            onCastSpellClick = {},
            onLongRestClick = {},
            onShortRestClick = {},
            onConfigureSlotsClick = {},
            onSpellSlotToggle = { _, _ -> },
            onSpellSlotTotalChanged = { _, _ -> },
            onPactSlotToggle = {},
            onPactSlotTotalChanged = {},
            onPactSlotLevelChanged = {},
            onConcentrationClear = {},
            onAddWeaponClick = {},
            onWeaponSelected = {},
            modifier = Modifier,
        )
    }
}

private fun referenceLikeSpellcastingClasses(): List<SpellcastingClassUiModel> = listOf(
    SpellcastingClassUiModel(
        sourceKey = "wizard",
        name = "Wizard",
        isUnassigned = false,
        spellcastingAbility = "INT",
        spellSaveDc = 15,
        spellAttackBonus = 7,
        spellLevels = listOf(
            SpellLevelUiModel(
                level = 0,
                spells = listOf(
                    CharacterSpellUiModel(
                        spellId = "fire_bolt",
                        name = "Fire Bolt",
                        level = 0,
                        school = "Evocation",
                        castingTime = "Action",
                        range = "120 ft",
                        components = listOf("V", "S", "M"),
                        ritual = false,
                        concentration = false,
                        sourceClass = "Wizard",
                        sourceLabel = "Wizard",
                        sourceKey = "wizard",
                    ),
                    CharacterSpellUiModel(
                        spellId = "minor_illusion",
                        name = "Minor Illusion",
                        level = 0,
                        school = "Illusion",
                        castingTime = "Action",
                        range = "30 ft",
                        components = listOf("S", "M"),
                        ritual = false,
                        concentration = false,
                        sourceClass = "Wizard",
                        sourceLabel = "Wizard",
                        sourceKey = "wizard",
                    ),
                    CharacterSpellUiModel(
                        spellId = "magic_missile",
                        name = "Magic Missile",
                        level = 0,
                        school = "Conjuration",
                        castingTime = "Action",
                        range = "",
                        components = listOf("V", "S", "M"),
                        ritual = false,
                        concentration = false,
                        sourceClass = "Wizard",
                        sourceLabel = "Wizard",
                        sourceKey = "wizard",
                    ),
                ),
            ),
        ),
    ),
    SpellcastingClassUiModel(
        sourceKey = "paladin",
        name = "Paladin",
        isUnassigned = false,
        spellcastingAbility = "CHA",
        spellSaveDc = 13,
        spellAttackBonus = 5,
        spellLevels = listOf(
            SpellLevelUiModel(
                level = 1,
                spells = listOf(
                    CharacterSpellUiModel(
                        spellId = "burning_hands",
                        name = "Burning Hands",
                        level = 1,
                        school = "Evocation",
                        castingTime = "Action",
                        range = "15 ft cone",
                        components = listOf("V", "S", "M"),
                        ritual = false,
                        concentration = false,
                        sourceClass = "Paladin",
                        sourceLabel = "Paladin",
                        sourceKey = "paladin",
                    ),
                    CharacterSpellUiModel(
                        spellId = "detect_magic",
                        name = "Detect Magic",
                        level = 1,
                        school = "Divination",
                        castingTime = "Action",
                        range = "10 min",
                        components = listOf("V", "S", "M"),
                        ritual = true,
                        concentration = false,
                        sourceClass = "Paladin",
                        sourceLabel = "Paladin",
                        sourceKey = "paladin",
                    ),
                ),
            ),
        ),
    ),
    SpellcastingClassUiModel(
        sourceKey = "warlock",
        name = "Warlock",
        isUnassigned = false,
        spellcastingAbility = "CHA",
        spellSaveDc = 14,
        spellAttackBonus = 6,
        spellLevels = listOf(
            SpellLevelUiModel(
                level = 0,
                spells = listOf(
                    CharacterSpellUiModel(
                        spellId = "burning_hands",
                        name = "Burning Hands",
                        level = 0,
                        school = "Evocation",
                        castingTime = "Action",
                        range = "15 ft cone",
                        components = listOf("V", "S", "M"),
                        ritual = false,
                        concentration = false,
                        sourceClass = "Warlock",
                        sourceLabel = "Warlock",
                        sourceKey = "warlock",
                    ),
                ),
            ),
            SpellLevelUiModel(
                level = 1,
                spells = listOf(
                    CharacterSpellUiModel(
                        spellId = "detect_magic",
                        name = "Detect Magic",
                        level = 1,
                        school = "Divination",
                        castingTime = "Action",
                        range = "10 min",
                        components = listOf("V", "S", "M"),
                        ritual = true,
                        concentration = false,
                        sourceClass = "Warlock",
                        sourceLabel = "Warlock",
                        sourceKey = "warlock",
                    ),
                ),
            ),
        ),
    ),
)
