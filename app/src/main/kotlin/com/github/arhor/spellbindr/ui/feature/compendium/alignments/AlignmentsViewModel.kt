package com.github.arhor.spellbindr.ui.feature.compendium.alignments

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveAlignmentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Stable
@HiltViewModel
class AlignmentsViewModel @Inject constructor(
    private val observeAlignments: ObserveAlignmentsUseCase,
) : ViewModel() {

    private val expandedItemName: MutableStateFlow<String?> = MutableStateFlow(null)
    private val alignmentsLoadable: StateFlow<Loadable<List<Alignment>>> =
        observeAlignments().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loadable.Loading)

    val uiState: StateFlow<AlignmentsUiState> =
        combine(alignmentsLoadable, expandedItemName) { loadable, selected ->
            when (loadable) {
                is Loadable.Loading -> AlignmentsUiState.Loading
                is Loadable.Ready -> AlignmentsUiState.Content(loadable.data, selected)
                is Loadable.Error -> AlignmentsUiState.Error(loadable.cause.message ?: "Failed to load alignments")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlignmentsUiState.Loading,
        )

    fun onAlignmentClick(alignmentName: String) {
        expandedItemName.update { current ->
            if (current == alignmentName) null else alignmentName
        }
    }
}
