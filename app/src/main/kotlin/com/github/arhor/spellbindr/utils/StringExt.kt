package com.github.arhor.spellbindr.utils

fun String.toTitleCase(separator: Char = ' '): String =
    this.lowercase()
        .splitToSequence(separator)
        .map(String::trim)
        .filter(String::isNotEmpty)
        .joinToString(separator = " ") { it.replaceFirstChar(Char::titlecase) }
