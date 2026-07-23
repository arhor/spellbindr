package com.github.arhor.spellbindr.ui.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells.SpellsTabPreview

@PreviewTest
@Preview
@Composable
fun SpellsTabPreview_Screenshot() {
    ScreenshotHarness {
        SpellsTabPreview()
    }
}
