package com.github.arhor.spellbindr.logging

import android.util.Log

/**
 * Implementation of the [Logger] interface that delegates logging to the standard [android.util.Log] utility.
 *
 * @property tag The tag used to identify the source of a log message in the log output.
 */
internal class AndroidLogger(
    private val tag: String,
) : Logger {

    override fun isEnabled(level: LogLevel): Boolean =
        Log.isLoggable(tag, level.toAndroidPriority())

    override fun log(
        level: LogLevel,
        message: String,
        throwable: Throwable?,
    ) {
        when (level) {
            LogLevel.DEBUG -> {
                if (throwable == null) {
                    Log.d(tag, message)
                } else {
                    Log.d(tag, message, throwable)
                }
            }

            LogLevel.INFO -> {
                if (throwable == null) {
                    Log.i(tag, message)
                } else {
                    Log.i(tag, message, throwable)
                }
            }

            LogLevel.WARN -> {
                if (throwable == null) {
                    Log.w(tag, message)
                } else {
                    Log.w(tag, message, throwable)
                }
            }

            LogLevel.ERROR -> {
                if (throwable == null) {
                    Log.e(tag, message)
                } else {
                    Log.e(tag, message, throwable)
                }
            }
        }
    }
}

private fun LogLevel.toAndroidPriority(): Int =
    when (this) {
        LogLevel.DEBUG -> Log.DEBUG
        LogLevel.INFO -> Log.INFO
        LogLevel.WARN -> Log.WARN
        LogLevel.ERROR -> Log.ERROR
    }
