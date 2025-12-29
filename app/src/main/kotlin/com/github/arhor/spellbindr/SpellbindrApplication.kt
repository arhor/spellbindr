package com.github.arhor.spellbindr

import android.app.Application
import com.github.arhor.spellbindr.data.local.assets.AssetBootstrapper
import com.github.arhor.spellbindr.di.ApplicationScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltAndroidApp
class SpellbindrApplication : Application() {

    @Inject
    lateinit var assetBootstrapper: AssetBootstrapper

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        assetBootstrapper.start(applicationScope)
    }
}
