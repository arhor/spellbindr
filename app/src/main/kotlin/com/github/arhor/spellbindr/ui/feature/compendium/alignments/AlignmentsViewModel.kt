package com.github.arhor.spellbindr.ui.feature.compendium.alignments

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveAllAlignmentsUseCase
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
    private val observeAlignments: ObserveAllAlignmentsUseCase,
) : ViewModel() {

    private data class State(
        val selectedItemId: String? = null,
    )

    private val _state = MutableStateFlow(State())

    val uiState: StateFlow<AlignmentsUiState> = combine(
        _state,
        observeAlignments(),
    ) { state, alignments ->
        when (alignments) {
            is Loadable.Loading -> {
                AlignmentsUiState.Loading
            }

            is Loadable.Ready -> {
                AlignmentsUiState.Content(alignments.data, state.selectedItemId)
            }

            is Loadable.Error -> {
                AlignmentsUiState.Error(alignments.errorMessage ?: "Failed to load alignments")
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlignmentsUiState.Loading)

    fun onAlignmentClick(alignment: Alignment) {
        _state.update { state ->
            state.copy(
                selectedItemId = alignment.id.takeIf { it != state.selectedItemId }
            )
        }
    }
}
