package com.github.arhor.spellbindr.ui.feature.character.guided.components.race

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
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
    focused: Boolean,
    selectedSubraceId: String?,
    onSelect: () -> Unit,
    onSubraceSelected: (String) -> Unit,
    onViewDetails: () -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val scrim = MaterialTheme.colorScheme.scrim
    val conciseSummary = summary.conciseDescription()
    val accessibilityActions = buildList {
        onPrevious?.let { previous ->
            add(CustomAccessibilityAction("Previous race") {
                previous()
                true
            })
        }
        onNext?.let { next ->
            add(CustomAccessibilityAction("Next race") {
                next()
                true
            })
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (focused) {
                    Modifier
                        .semantics {
                            contentDescription = buildString {
                                append(summary.name)
                                append(", ")
                                append(position)
                                append(" of ")
                                append(totalCount)
                                conciseSummary.takeIf { it.isNotBlank() }?.let {
                                    append(". ")
                                    append(it)
                                }
                            }
                            stateDescription = if (selected) "Selected" else "Not selected"
                            customActions = accessibilityActions
                        }
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = onSelect,
                        )
                } else {
                    Modifier
                },
            ),
    ) {
        RaceArtwork(raceId = race.id, modifier = Modifier.fillMaxSize())
        if (focused) {
            RaceSummaryPanel(
                summary = summary,
                subraces = race.subraces,
                selectedSubraceId = selectedSubraceId,
                onSubraceSelected = onSubraceSelected,
                onViewDetails = onViewDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .drawWithCache {
                        onDrawBehind {
                            drawRect(
                                Brush.verticalGradient(
                                    0f to scrim.copy(alpha = 0f),
                                    0.30f to scrim.copy(alpha = 0.72f),
                                    1f to scrim.copy(alpha = 0.94f),
                                ),
                            )
                        }
                    }
                    .padding(top = 38.dp),
            )
        }
    }
}
