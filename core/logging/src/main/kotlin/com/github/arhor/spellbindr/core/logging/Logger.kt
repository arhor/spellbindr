package com.github.arhor.spellbindr.core.logging

import android.util.Log
import kotlin.reflect.KClass

@JvmInline
@Suppress("UNUSED", "NOTHING_TO_INLINE")
value class Logger(val tag: String) {
    constructor(type: Class<*>) : this(tag = type.simpleName ?: "<unknown>")
    constructor(type: KClass<*>) : this(type = type.let { if (it.isCompanion) it.java.enclosingClass else it.java })

    inline fun info(msg: () -> String) {
        if (Log.isLoggable(tag, Log.INFO)) {
            Log.i(tag, msg())
        }
    }

    inline fun info(t: Throwable, msg: () -> String) {
        if (Log.isLoggable(tag, Log.INFO)) {
            Log.i(tag, msg(), t)
        }
    }

    inline fun debug(msg: () -> String) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, msg())
        }
    }

    inline fun debug(t: Throwable, msg: () -> String) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, msg(), t)
        }
    }

    inline fun error(msg: () -> String) {
        if (Log.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, msg())
        }
    }

    inline fun error(t: Throwable, msg: () -> String) {
        if (Log.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, msg(), t)
        }
    }

    companion object {
        inline fun Any.createLogger(): Logger = Logger(type = this::class)
        inline fun <reified T> createLogger(): Logger = Logger(type = T::class)
    }
}
