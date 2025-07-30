package com.github.arhor.spellbindr.ui.screens.library.alignments

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Alignment
import com.github.arhor.spellbindr.data.repository.AlignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@Stable
@HiltViewModel
class AlignmentsViewModel @Inject constructor(
    alignmentRepository: AlignmentRepository,
) : ViewModel() {

    @Immutable
    data class State(
        val alignments: List<Alignment> = emptyList(),
        val expandedItemName: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            alignmentRepository.allAlignments.collect { data ->
                _state.update { it.copy(alignments = data) }
            }
        }
    }

    fun handleAlignmentClick(alignmentName: String) {
        _state.update {
            it.copy(
                expandedItemName = if (it.expandedItemName == alignmentName) {
                    null
                } else {
                    alignmentName
                }
            )
        }
    }
} 