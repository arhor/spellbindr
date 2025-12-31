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

    private val selectedId = MutableStateFlow<String?>(null)
    private val alignmentsData = observeAlignments()
        .stateIn(viewModelScope, sharingStrategy, Loadable.Loading)

    val uiState: StateFlow<AlignmentsUiState> = combine(alignmentsData, selectedId, ::toUiState)
        .stateIn(viewModelScope, sharingStrategy, AlignmentsUiState.Loading)

    fun onAlignmentClick(alignmentId: String) {
        selectedId.update {
            if (it != alignmentId) {
                alignmentId
            } else {
                null
            }
        }
    }

    private fun toUiState(
        alignments: Loadable<List<Alignment>>,
        selectedId: String?
    ): AlignmentsUiState = when (alignments) {
        is Loadable.Loading -> {
            AlignmentsUiState.Loading
        }

        is Loadable.Ready -> {
            AlignmentsUiState.Content(alignments.data, selectedId)
        }

        is Loadable.Error -> {
            AlignmentsUiState.Error(alignments.cause.message ?: "Failed to load alignments")
        }
    }

    companion object {
        private val sharingStrategy = SharingStarted.WhileSubscribed(5_000)
    }
}
