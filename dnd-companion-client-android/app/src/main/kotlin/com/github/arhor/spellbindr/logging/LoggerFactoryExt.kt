package com.github.arhor.spellbindr.logging

import org.jetbrains.annotations.VisibleForTesting

/**
 * Returns a [Logger] instance using a tag derived from the provided [instance] context.
 *
 * This extension function utilizes Kotlin context receivers to automatically determine
 * the appropriate logging tag based on the class of the object it is called within.
 *
 * @return A [Logger] initialized with a tag representing the class of [instance].
 */
context(instance: Any)
fun LoggerFactory.getLogger(): Logger = getLogger(tag = tagOf(type = instance.javaClass))

/**
 * Generates a logging tag for the provided [type].
 *
 * If the provided class is a Kotlin `Companion` object, it resolves to the enclosing class.
 * It prioritizes using the simple name of the class, falling back to a derived name
 * from the canonical or full class name if the simple name is blank (e.g., for anonymous classes).
 *
 * @param type the class to generate a tag for
 * @return a string representing the tag for the given class
 */
@VisibleForTesting
internal fun tagOf(type: Class<*>): String {
    val resolvedType = resolveType(type = type)
    val simpleName = resolvedType.simpleName

    if (simpleName.isNotBlank()) {
        return simpleName
    }

    return fallbackName(type = resolvedType)
}

/**
 * Resolves the provided [type] to a more appropriate class for logging purposes.
 * Specifically, if the [type] represents a Kotlin companion object, this method
 * returns its enclosing class; otherwise, it returns the [type] as is.
 *
 * @param type the class to resolve
 * @return the resolved class, or the enclosing class if the input is a companion object
 */
private fun resolveType(type: Class<*>): Class<*> {
    val enclosingClass = type.enclosingClass

    return if (type.simpleName == "Companion" && enclosingClass != null) {
        enclosingClass
    } else {
        type
    }
}

/**
 * Provides a fallback name for the given [type] if its `simpleName` is empty,
 * which typically occurs with anonymous classes. It attempts to extract the
 * last part of the canonical or binary name.
 *
 * @param type the class for which to generate a fallback name
 * @return the extracted class name, or "<unknown>" if no name can be determined
 */
private fun fallbackName(type: Class<*>): String {
    val className = type.canonicalName ?: type.name

    return className
        .substringAfterLast('.')
        .substringAfterLast('$')
        .ifBlank { "<unknown>" }
}
