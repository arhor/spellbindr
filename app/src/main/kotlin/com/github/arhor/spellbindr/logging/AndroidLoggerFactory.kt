package com.github.arhor.spellbindr.logging

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android-specific implementation of the [LoggerFactory] interface.
 *
 * This factory creates and manages [AndroidLogger] instances, using a [ConcurrentHashMap]
 * to cache loggers by their tag to ensure thread safety and avoid redundant object creation.
 *
 * @property cache Internal storage for reused [Logger] instances keyed by their string tag.
 */
@Singleton
class AndroidLoggerFactory @Inject constructor() : LoggerFactory {
    private val cache = ConcurrentHashMap<String, Logger>()

    override fun getLogger(tag: String): Logger = cache.getOrPut(key = tag) { AndroidLogger(tag = tag) }
}
