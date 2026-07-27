@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.github.arhor.spellbindr.ui.feature.character.guided.components.race

import androidx.compose.foundation.interaction.collectIsDraggedAsState
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

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
                modifier = Modifier.fillMaxSize(),
            )
        }

        RaceCarouselNavigation(
            currentPage = pagerState.currentPage,
            pageCount = races.size,
            onPrevious = { selectPage(pagerState.currentPage - 1) },
            onNext = { selectPage(pagerState.currentPage + 1) },
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
private fun RaceCarouselNavigation(
    currentPage: Int,
    pageCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onPrevious,
            enabled = currentPage > 0,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Previous race",
            )
        }
        Row(
            modifier = Modifier.semantics {
                contentDescription = "Race ${currentPage + 1} of $pageCount"
            },
            horizontalArrangement = Arrangement.spacedBy(5.dp),
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
        IconButton(
            onClick = onNext,
            enabled = currentPage < pageCount - 1,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "Next race",
            )
        }
    }
}
