package com.github.arhor.spellbindr.utils

import java.util.Objects

/**
 * Compares two objects for inequality.
 *
 * @param other the object to compare with the current one
 * @return `true` if this object is not equal to [other], `false` otherwise
 */
fun <T> T.notEquals(other: T): Boolean = !Objects.equals(this, other)


fun <T> isNotEqualTo(other: T): (T) -> Boolean = { !Objects.equals(it, other) }


infix fun <T> T.isNotEqualTo(other: T): T? = if (this == other) this else null
