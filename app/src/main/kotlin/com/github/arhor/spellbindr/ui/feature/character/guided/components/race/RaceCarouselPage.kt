package com.github.arhor.spellbindr.ui.feature.character.guided.components.race

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Race

@Composable
internal fun RaceCarouselPage(
    race: Race,
    summary: RaceSummaryUiModel,
    position: Int,
    totalCount: Int,
    selected: Boolean,
    selectedSubraceId: String?,
    onSelect: () -> Unit,
    onSubraceSelected: (String) -> Unit,
    onViewDetails: () -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val artworkWeight = if (LocalDensity.current.fontScale >= LARGE_FONT_SCALE) 0.22f else 0.30f
    val accessibilityActions = buildList {
        onPrevious?.let { previous ->
            add(CustomAccessibilityAction(label = "Previous race") {
                previous()
                true
            })
        }
        onNext?.let { next ->
            add(CustomAccessibilityAction(label = "Next race") {
                next()
                true
            })
        }
    }

    Card(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = buildString {
                    append(summary.name)
                    append(", ")
                    append(position)
                    append(" of ")
                    append(totalCount)
                    val conciseSummary = summary.conciseDescription()
                    if (conciseSummary.isNotBlank()) {
                        append(". ")
                        append(conciseSummary)
                    }
                }
                stateDescription = if (selected) "Selected" else "Not selected"
                customActions = accessibilityActions
            }
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(artworkWeight),
            ) {
                RaceArtwork(
                    raceId = race.id,
                    modifier = Modifier.fillMaxSize(),
                )
                if (selected) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = "Selected",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        )
                    }
                }
            }
            RaceSummaryPanel(
                summary = summary,
                subraces = race.subraces,
                selectedSubraceId = selectedSubraceId,
                onSubraceSelected = onSubraceSelected,
                onViewDetails = onViewDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f - artworkWeight),
            )
        }
    }
}

private const val LARGE_FONT_SCALE = 1.2f
