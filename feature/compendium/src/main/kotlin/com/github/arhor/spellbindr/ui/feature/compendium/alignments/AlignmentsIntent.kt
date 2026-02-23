package com.github.arhor.spellbindr.ui.feature.compendium.alignments

/**
 * Represents user intents for the Alignments screen.
 */
sealed interface AlignmentsIntent {
    /**
     * Intent emitted when an alignment item is clicked.
     */
    data class AlignmentClicked(val alignmentId: String) : AlignmentsIntent
}

/**
 * Dispatch function for [AlignmentsIntent] events.
 */
typealias AlignmentsDispatch = (AlignmentsIntent) -> Unit
