package com.github.arhor.spellbindr

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * A custom [AndroidJUnitRunner] used to set up the instrumented application class for tests.
 * This runner replaces the default application with [HiltTestApplication] to enable Hilt
 * dependency injection in UI tests.
 */
class HiltApplicationTestRunner : AndroidJUnitRunner() {
    /**
     * Creates a new instance of the application.
     *
     * This method is overridden to ensure that the HiltTestApplication is used for instrumented tests.
     * This is necessary for Hilt to properly inject dependencies during testing.
     *
     * @param cl The ClassLoader to use for loading the application class.
     * @param className The name of the application class to instantiate. This parameter is ignored,
     *                  and [HiltTestApplication] is always used.
     * @param context The Context in which the application is running.
     * @return A new instance of the [Application], specifically [HiltTestApplication].
     */
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
