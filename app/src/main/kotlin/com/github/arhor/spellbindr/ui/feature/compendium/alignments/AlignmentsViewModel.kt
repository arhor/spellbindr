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

    private val selectedItemIdState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AlignmentsUiState> = combine(
        observeAlignments(),
        selectedItemIdState,
    ) { alignments, selectedId ->
        when (alignments) {
            is Loadable.Loading -> {
                AlignmentsUiState.Loading
            }

            is Loadable.Ready -> {
                AlignmentsUiState.Content(alignments.data, selectedId)
            }

            is Loadable.Error -> {
                AlignmentsUiState.Error(alignments.errorMessage ?: "Failed to load alignments")
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlignmentsUiState.Loading)

    fun onAlignmentClick(alignment: Alignment) {
        selectedItemIdState.update {
            if (it != alignment.id) {
                alignment.id
            } else {
                null
            }
        }
    }
}
