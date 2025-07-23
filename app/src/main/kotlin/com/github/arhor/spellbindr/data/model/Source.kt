package com.github.arhor.spellbindr.data.model

enum class Source(val fullName: String) {
    PHB("Player's Hand Book"),
    SCAG("Sword Coast Adventurer's Guide"),
    EGTW("Explorer's Guide to Wildemount"),
    ;

    companion object {
        private val booksByFullName = entries.associateBy { it.fullName }

        fun fromFullName(fullName: String) = booksByFullName[fullName]
    }
}
