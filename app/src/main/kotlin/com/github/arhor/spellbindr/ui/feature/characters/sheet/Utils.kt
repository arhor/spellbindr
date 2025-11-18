package com.github.arhor.spellbindr.ui.feature.characters.sheet

fun formatBonus(value: Int): String =
    if (value >= 0) "+$value" else value.toString()

fun listOfNotBlank(vararg values: String?): List<String> =
    values.mapNotNull { value -> value?.takeIf { it.isNotBlank() }?.trim() }
