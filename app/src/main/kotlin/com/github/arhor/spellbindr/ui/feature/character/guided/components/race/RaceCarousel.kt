@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.github.arhor.spellbindr.ui.feature.character.guided.components.race

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
internal fun RaceCarousel(
    races: List<Race>,
    traitsById: Map<String, Trait>,
    selectedRaceId: String?,
    selectedSubraceId: String?,
    onRaceSelected: (String) -> Unit,
    onSubraceSelected: (raceId: String, subraceId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (races.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No races are available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val initialPage = races.indexOfFirst { it.id == selectedRaceId }.takeIf { it >= 0 } ?: 0
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { races.size },
    )
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    val currentSelectedRaceId by rememberUpdatedState(selectedRaceId)
    val pageShape = RoundedCornerShape(16.dp)
    var userDragStartPage by remember { mutableStateOf<Int?>(null) }
    var detailsRaceId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(isDragged) {
        if (isDragged && userDragStartPage == null) {
            userDragStartPage = pagerState.currentPage
        }
    }
    LaunchedEffect(pagerState, races) {
        snapshotFlow { pagerState.isScrollInProgress to pagerState.settledPage }
            .distinctUntilChanged()
            .collect { (scrolling, settledPage) ->
                val startPage = userDragStartPage
                if (!scrolling && startPage != null) {
                    userDragStartPage = null
                    if (startPage != settledPage) {
                        races.getOrNull(settledPage)?.id
                            ?.takeIf { it != currentSelectedRaceId }
                            ?.let(onRaceSelected)
                    }
                }
            }
    }
    LaunchedEffect(selectedRaceId, races) {
        val selectedIndex = races.indexOfFirst { it.id == selectedRaceId }
        if (selectedIndex >= 0 && selectedIndex != pagerState.currentPage) {
            userDragStartPage = null
            // Deliberately avoids animation so state changes also work with reduced-motion settings.
            pagerState.scrollToPage(selectedIndex)
        }
    }

    fun selectPage(index: Int) {
        races.getOrNull(index)?.id?.let { raceId ->
            userDragStartPage = null
            onRaceSelected(raceId)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSpacing = 12.dp,
                beyondViewportPageCount = 1,
                key = { races[it].id },
            ) { page ->
                val race = races[page]
                val isSelected = race.id == selectedRaceId
                val subraceId = selectedSubraceId.takeIf { isSelected }
                val summary = remember(race, traitsById, subraceId) {
                    raceSummaryUiModel(
                        race = race,
                        traitsById = traitsById,
                        selectedSubraceId = subraceId,
                    )
                }
                RaceCarouselPage(
                    race = race,
                    summary = summary,
                    position = page + 1,
                    totalCount = races.size,
                    selected = isSelected,
                    focused = page == pagerState.currentPage,
                    selectedSubraceId = subraceId,
                    onSelect = { selectPage(page) },
                    onSubraceSelected = { onSubraceSelected(race.id, it) },
                    onViewDetails = { detailsRaceId = race.id },
                    onPrevious = (page - 1).takeIf { it >= 0 }?.let { previous ->
                        { selectPage(previous) }
                    },
                    onNext = (page + 1).takeIf { it < races.size }?.let { next ->
                        { selectPage(next) }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val distanceFromCenter = (
                                (pagerState.currentPage - page) +
                                    pagerState.currentPageOffsetFraction
                                ).let(::kotlin.math.abs)
                                .coerceIn(0f, 1f)
                            val prominence = 1f - distanceFromCenter
                            val scale = 0.94f + (0.06f * prominence)
                            scaleX = scale
                            scaleY = scale
                            alpha = 0.72f + (0.28f * prominence)
                            shape = pageShape
                            clip = true
                        },
                )
            }

            CarouselArrow(
                onClick = { selectPage(pagerState.currentPage - 1) },
                enabled = pagerState.currentPage > 0,
                previous = true,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            CarouselArrow(
                onClick = { selectPage(pagerState.currentPage + 1) },
                enabled = pagerState.currentPage < races.lastIndex,
                previous = false,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }

        RaceCarouselPagination(
            currentPage = pagerState.currentPage,
            pageCount = races.size,
        )
    }

    detailsRaceId?.let { raceId ->
        val race = races.firstOrNull { it.id == raceId }
        if (race == null) {
            detailsRaceId = null
        } else {
            RaceDetailsDialog(
                summary = raceSummaryUiModel(
                    race = race,
                    traitsById = traitsById,
                    selectedSubraceId = selectedSubraceId.takeIf { race.id == selectedRaceId },
                ),
                onDismissRequest = { detailsRaceId = null },
            )
        }
    }
}

@Composable
private fun RaceCarouselPagination(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .semantics {
                contentDescription = "Race ${currentPage + 1} of $pageCount"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
    ) {
        repeat(pageCount) { index ->
            Text(
                text = if (index == currentPage) "●" else "○",
                style = MaterialTheme.typography.labelSmall,
                color = if (index == currentPage) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
            )
        }
    }
}

@Composable
private fun CarouselArrow(
    onClick: () -> Unit,
    enabled: Boolean,
    previous: Boolean,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.scrim.copy(alpha = if (enabled) 0.48f else 0.24f),
            shape = CircleShape,
        ),
    ) {
        Icon(
            imageVector = if (previous) {
                Icons.AutoMirrored.Outlined.ArrowBack
            } else {
                Icons.AutoMirrored.Outlined.ArrowForward
            },
            contentDescription = if (previous) "Previous race" else "Next race",
            tint = contentColor.copy(alpha = if (enabled) 1f else 0.38f),
        )
    }
}
