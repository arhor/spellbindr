package com.github.arhor.spellbindr.util

internal inline fun <T> List<T>?.filterOrEmpty(test: (T) -> Boolean): List<T> = this?.filter(test) ?: emptyList()
