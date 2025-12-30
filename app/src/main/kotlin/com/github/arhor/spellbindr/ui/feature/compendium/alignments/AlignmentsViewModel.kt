package com.github.arhor.spellbindr.ui.feature.compendium.alignments

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.usecase.ObserveAlignmentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Stable
@HiltViewModel
class AlignmentsViewModel @Inject constructor(
    observeAlignmentsUseCase: ObserveAlignmentsUseCase,
) : ViewModel() {

    @Immutable
    data class AlignmentsState(
        val alignments: List<Alignment> = emptyList(),
        val expandedItemName: String? = null,
    )

    private val alignmentSelection = MutableStateFlow<String?>(null)

    val state = combine(
        observeAlignmentsUseCase(),
        alignmentSelection,
    ) { alignments, expandedItemName ->
        AlignmentsState(
            alignments = alignments,
            expandedItemName = expandedItemName,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlignmentsState())

    fun onAlignmentClick(alignmentName: String) {
        alignmentSelection.update { current ->
            if (current == alignmentName) {
                null
            } else {
                alignmentName
            }
        }
    }
}
