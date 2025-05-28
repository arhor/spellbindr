package com.github.arhor.spellbindr.data.next.repository

import android.content.Context
import com.github.arhor.spellbindr.util.Logger.Companion.createLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

abstract class StaticAssetLoaderBase<T>(
    protected val context: Context,
    protected val json: Json,
    protected val path: String,
    protected val serializer: KSerializer<T>
) : StaticAssetLoader {

    protected var asset = null as List<T>?

    private val mutex = Mutex()
    protected val logger = createLogger()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun loadAsset() {
        logger.info { "Trying to load static asset: $path" }
        if (asset == null) {
            mutex.withLock {
                if (asset == null) {
                    asset = withContext(Dispatchers.IO) {
                        context.assets.open(path).use {
                            json.decodeFromStream(
                                deserializer = ListSerializer(elementSerializer = serializer),
                                stream = it,
                            )
                        }
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
