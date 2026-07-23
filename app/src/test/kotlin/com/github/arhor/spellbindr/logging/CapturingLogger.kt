package com.github.arhor.spellbindr.logging

/**
 * A [Logger] implementation that captures log entries into an internal list for inspection.
 *
 * This class is primarily intended for testing purposes, allowing verification of log
 * messages, levels, and associated exceptions. It can be converted into a [LoggerFactory]
 * to inject this capturing behavior into components under test.
 *
 * @property enabledLevels The set of log levels for which logging is enabled. Defaults to all levels.
 */
class CapturingLogger(
    private val enabledLevels: Set<LogLevel> = LogLevel.entries.toSet(),
) : Logger, AutoCloseable {
    private val _entries = mutableListOf<CapturedLogEntry>()

    val entries: List<CapturedLogEntry>
        get() = _entries

    override fun isEnabled(level: LogLevel): Boolean = level in enabledLevels

    override fun log(level: LogLevel, message: String, throwable: Throwable?) {
        _entries.add(
            CapturedLogEntry(
                level = level,
                message = message,
                throwable = throwable,
            ),
        )
    }

    override fun close() {
        clear()
    }

    /**
     * Removes all captured log entries from this logger.
     */
    fun clear() {
        _entries.clear()
    }
}
