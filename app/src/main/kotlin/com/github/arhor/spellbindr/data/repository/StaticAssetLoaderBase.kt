package com.github.arhor.spellbindr.data.repository

import android.content.Context
import com.github.arhor.spellbindr.data.model.StaticAsset
import com.github.arhor.spellbindr.util.Logger.Companion.createLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

abstract class StaticAssetLoaderBase<T, M>(
    protected val context: Context,
    protected val json: Json,
    protected val path: String,
    protected val serializer: KSerializer<StaticAsset<T, M>>
) : StaticAssetLoader {

    protected var asset = null as List<T>?

    private val mutex = Mutex()
    protected val logger = createLogger()

    override suspend fun loadAsset() {
        logger.info { "Trying to load static asset: $path" }
        if (asset == null) {
            mutex.withLock {
                if (asset == null) {
                    asset = withContext(Dispatchers.IO) {
                        context.assets.open(path)
                            .bufferedReader()
                            .use { it.readText() }
                            .let { json.decodeFromString(serializer, it).data }
                    }
                    logger.info { "Static asset [$path] is successfully loaded" }
                    return
                }
            }
        }
        logger.info { "Static asset [$path] is already loaded" }
    }

    protected suspend inline fun getAsset(): List<T> {
        loadAsset()
        return asset!!
    }
}
