package com.github.arhor.spellbindr.utils

fun String.toTitleCase(separator: Char = ' '): String =
    this.splitToSequence(separator)
        .map(String::trim)
        .filter(String::isNotEmpty)
        .joinToString(separator = " ") { it.toCapitalCase() }

fun String.toCapitalCase(): String =
    this.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
