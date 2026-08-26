package io.github.arhor.dnd.companion.ui.feature.character.sheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import io.github.arhor.dnd.companion.ui.feature.character.sheet.components.ProgressionSummaryCard
import io.github.arhor.dnd.companion.ui.feature.character.sheet.model.ProgressionSummaryUiModel
import io.github.arhor.dnd.companion.ui.screenshot.ScreenshotHarness

@PreviewTest
@Preview(widthDp = 360, heightDp = 300)
@Composable
fun ProgressionSummaryCardScreenshot_Managed() {
    ScreenshotHarness {
        ProgressionSummaryCard(
            progression = ProgressionSummaryUiModel.Managed(
                totalLevel = 3,
                classes = "Fighter 2 / Wizard 1",
                levels = listOf(
                    "1. Fighter 1",
                    "2. Wizard 1",
                    "3. Fighter 2",
                ),
            ),
        )
    }
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 180)
@Composable
fun ProgressionSummaryCardScreenshot_Unmanaged() {
    ScreenshotHarness {
        ProgressionSummaryCard(
            progression = ProgressionSummaryUiModel.Unmanaged,
        )
    }
}
