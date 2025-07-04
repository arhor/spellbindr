package com.github.arhor.spellbindr.data.datasource.local.assets

import android.content.Context
import com.github.arhor.spellbindr.util.Logger.Companion.createLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

abstract class StaticAssetDataStoreBase<T>(
    private val json: Json,
    private val path: String,
    private val context: Context,
    private val serializer: KSerializer<T>,
) : StaticAssetDataStore<List<T>> {

    private val logger = createLogger()
    private val mutex = Mutex()
    private var asset = MutableStateFlow<List<T>?>(null)

    override val data: StateFlow<List<T>?>
        get() = asset.asStateFlow()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun initialize() {
        logger.info { "Trying to load static asset: $path" }
        if (asset.value == null) {
            mutex.withLock {
                if (asset.value == null) {
                    asset.value = withContext(Dispatchers.IO) {
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
}
