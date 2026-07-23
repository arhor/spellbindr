package com.github.arhor.spellbindr.ui.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.ui.feature.character.guided.StandardArrayAssign

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun StandardArrayAssign_Empty_Screenshot() {
    ScreenshotHarness {
        StandardArrayAssign(
            assignments = AbilityIds.standardOrder.associateWith { null },
            onStandardArrayAssigned = { _, _ -> },
        )
    }
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun StandardArrayAssign_Partial_Screenshot() {
    ScreenshotHarness {
        StandardArrayAssign(
            assignments = mapOf(
                AbilityIds.STR to 15,
                AbilityIds.DEX to 14,
                AbilityIds.CON to null,
                AbilityIds.INT to 12,
                AbilityIds.WIS to null,
                AbilityIds.CHA to null,
            ),
            onStandardArrayAssigned = { _, _ -> },
        )
    }
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun StandardArrayAssign_Complete_Screenshot() {
    ScreenshotHarness {
        StandardArrayAssign(
            assignments = mapOf(
                AbilityIds.STR to 15,
                AbilityIds.DEX to 14,
                AbilityIds.CON to 13,
                AbilityIds.INT to 12,
                AbilityIds.WIS to 10,
                AbilityIds.CHA to 8,
            ),
            onStandardArrayAssigned = { _, _ -> },
        )
    }
}
