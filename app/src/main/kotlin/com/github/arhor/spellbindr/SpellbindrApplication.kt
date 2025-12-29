package com.github.arhor.spellbindr

import android.app.Application
import com.github.arhor.spellbindr.domain.AssetBootstrapper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltAndroidApp
class SpellbindrApplication : Application() {

    @Inject
    lateinit var assetBootstrapper: AssetBootstrapper

    @Inject
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        assetBootstrapper.start(applicationScope)
    }
}
