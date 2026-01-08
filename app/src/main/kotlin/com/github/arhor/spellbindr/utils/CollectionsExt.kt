package com.github.arhor.spellbindr.utils

/**
 * Returns a list containing only elements for which the provided [selector] function
 * returns a non-null value.
 *
 * @param T the type of elements in the iterable.
 * @param R the type of the value returned by the selector.
 * @param selector a function that maps each element to a nullable value used for filtering.
 * @return a list of elements where the selector result is not null.
 */
inline fun <T, R> Iterable<T>.filterNotNullBy(selector: (T) -> R?): List<T> = filter { selector(it) != null }
