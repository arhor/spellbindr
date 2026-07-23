package com.github.arhor.spellbindr.logging

/**
 * Logs a message at the [LogLevel.DEBUG] level.
 *
 * The [message] argument is a lazy-evaluated lambda, which is only invoked
 * if the DEBUG log level is currently enabled for this logger.
 *
 * @param message a lambda returning the message to be logged
 */
inline fun Logger.debug(message: () -> String) {
    if (isEnabled(level = LogLevel.DEBUG)) {
        log(
            level = LogLevel.DEBUG,
            message = message(),
        )
    }
}

/**
 * Logs a message at the DEBUG level with an optional [throwable].
 * The [message] is lazily evaluated only if the DEBUG log level is enabled.
 *
 * @param throwable the exception to log, or null if none
 * @param message the function producing the log message
 */
inline fun Logger.debug(
    throwable: Throwable?,
    message: () -> String,
) {
    if (isEnabled(level = LogLevel.DEBUG)) {
        log(
            level = LogLevel.DEBUG,
            message = message(),
            throwable = throwable,
        )
    }
}

/**
 * Logs a message at the [LogLevel.INFO] level.
 *
 * The [message] argument is a lazy-evaluated lambda, which is only executed
 * if the logger is enabled for the INFO level.
 *
 * @param message a lambda returning the message to be logged
 */
inline fun Logger.info(message: () -> String) {
    if (isEnabled(level = LogLevel.INFO)) {
        log(
            level = LogLevel.INFO,
            message = message(),
        )
    }
}

/**
 * Logs a message at the INFO level with an optional [throwable].
 * The [message] is evaluated lazily only if INFO logging is enabled.
 *
 * @param throwable an optional exception to be logged
 * @param message a lambda returning the message to be logged
 */
inline fun Logger.info(
    throwable: Throwable?,
    message: () -> String,
) {
    if (isEnabled(level = LogLevel.INFO)) {
        log(
            level = LogLevel.INFO,
            message = message(),
            throwable = throwable,
        )
    }
}

/**
 * Logs a message at the WARN level.
 *
 * This is an inline function that lazily evaluates the message. The message lambda is only
 * invoked if the WARN log level is enabled, which avoids unnecessary string construction.
 *
 * @param message The lambda function that provides the log message.
 */
inline fun Logger.warn(message: () -> String) {
    if (isEnabled(level = LogLevel.WARN)) {
        log(
            level = LogLevel.WARN,
            message = message(),
        )
    }
}

/**
 * Logs a message with the WARN log level and an optional throwable.
 * The message is only evaluated if the WARN log level is enabled.
 *
 * @param throwable the exception to log, or null if none
 * @param message the lazy-evaluated message to log
 */
inline fun Logger.warn(
    throwable: Throwable?,
    message: () -> String,
) {
    if (isEnabled(level = LogLevel.WARN)) {
        log(
            level = LogLevel.WARN,
            message = message(),
            throwable = throwable,
        )
    }
}

/**
 * Logs a message at the ERROR level if the level is enabled.
 * The message is lazily evaluated only if the ERROR level is active.
 *
 * @param message the function producing the log message
 */
inline fun Logger.error(message: () -> String) {
    if (isEnabled(level = LogLevel.ERROR)) {
        log(
            level = LogLevel.ERROR,
            message = message(),
        )
    }
}

/**
 * Logs a message at the ERROR level with an optional [throwable],
 * but only if the ERROR log level is enabled.
 *
 * @param throwable the exception to log, if any.
 * @param message the lazy-evaluated message to be logged.
 */
inline fun Logger.error(
    throwable: Throwable?,
    message: () -> String,
) {
    if (isEnabled(level = LogLevel.ERROR)) {
        log(
            level = LogLevel.ERROR,
            message = message(),
            throwable = throwable,
        )
    }
}
