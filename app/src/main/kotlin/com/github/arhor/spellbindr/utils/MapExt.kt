package com.github.arhor.spellbindr.utils

/**
 * Creates a shallow copy of the map, applying the provided initialization block to the mutable copy.
 *
 * This function allows for modifications to the map during the copying process.
 * The original map remains unchanged.
 *
 * @param K the type of map keys.
 * @param V the type of map values.
 * @param init a lambda function with the mutable map as its receiver, allowing for modifications.
 * @return A new read-only map containing the copied elements with the applied modifications.
 */
fun <K, V> Map<K, V>.copy(init: MutableMap<K, V>.() -> Unit): Map<K, V> = toMutableMap().apply(init)
