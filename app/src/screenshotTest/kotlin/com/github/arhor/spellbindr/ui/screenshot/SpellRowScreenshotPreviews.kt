package com.github.arhor.spellbindr.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells.SpellRow
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SheetEditMode

@PreviewTest
@Preview(
    name = "Light",
    widthDp = 390,
    heightDp = 110,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    widthDp = 390,
    heightDp = 110,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun SpellRow_View_Screenshot() {
    ScreenshotHarness {
        SpellRow(
            spell = fireBolt,
            editMode = SheetEditMode.View,
            onClick = {},
            canCast = true,
            onCastClick = {},
            onRemove = {},
            modifier = Modifier,
        )
    }
}

@PreviewTest
@Preview(widthDp = 390, heightDp = 160)
@Composable
fun SpellRow_DenseDisabled_Screenshot() {
    ScreenshotHarness {
        SpellRow(
            spell = denseSpell,
            editMode = SheetEditMode.View,
            onClick = {},
            canCast = false,
            onCastClick = {},
            onRemove = {},
            modifier = Modifier,
        )
    }
}

@PreviewTest
@Preview(widthDp = 390, heightDp = 120)
@Composable
fun SpellRow_Edit_Screenshot() {
    ScreenshotHarness {
        SpellRow(
            spell = fireBolt,
            editMode = SheetEditMode.Edit,
            onClick = {},
            canCast = true,
            onCastClick = {},
            onRemove = {},
            modifier = Modifier,
        )
    }
}

@PreviewTest
@Preview(widthDp = 320, heightDp = 220, fontScale = 1.5f)
@Composable
fun SpellRow_LargeFont_Screenshot() {
    ScreenshotHarness {
        SpellRow(
            spell = denseSpell,
            editMode = SheetEditMode.View,
            onClick = {},
            canCast = true,
            onCastClick = {},
            onRemove = {},
            modifier = Modifier,
        )
    }
}

private val fireBolt = CharacterSpellUiModel(
    spellId = "fire_bolt",
    name = "Fire Bolt",
    level = 0,
    school = "Evocation",
    castingTime = "Action",
    range = "120 ft",
    components = listOf("V", "S"),
    ritual = false,
    concentration = false,
    sourceClass = "Wizard",
    sourceLabel = "Wizard",
    sourceKey = "wizard",
)

private val denseSpell = CharacterSpellUiModel(
    spellId = "protection_from_evil_and_good",
    name = "Protection from Evil and Good",
    level = 1,
    school = "Abjuration",
    castingTime = "Action",
    range = "Touch",
    components = listOf("V", "S", "M"),
    ritual = true,
    concentration = true,
    sourceClass = "Wizard",
    sourceLabel = "Wizard",
    sourceKey = "wizard",
)
