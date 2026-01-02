package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.utils.Logger.Companion.createLogger
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

abstract class AssetDataStoreBase<T>(
    private val json: Json,
    private val path: String,
    private val context: Context,
    private val serializer: KSerializer<T>,
    override val priority: AssetLoadingPriority = AssetLoadingPriority.CRITICAL,
) : AssetDataStore<List<T>> {

    private val logger = createLogger()
    private val mutex = Mutex()
    private var asset = MutableStateFlow<Loadable<List<T>>>(Loadable.Loading)

    override val data: StateFlow<Loadable<List<T>>>
        get() = asset.asStateFlow()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun initialize() {
        logger.info { "Trying to load static asset: $path" }
        mutex.withLock {
            if (asset.value is Loadable.Ready) {
                logger.info { "Static asset [$path] is already loaded" }
                return
            }

            asset.value = Loadable.Loading
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    context.assets.open(path).use {
                        json.decodeFromStream(
                            deserializer = ListSerializer(elementSerializer = serializer),
                            stream = it,
                        )
                    }
                }
            }
            result.onSuccess {
                asset.value = Loadable.Ready(it)
                logger.info { "Static asset [$path] is successfully loaded" }
            }.onFailure { error ->
                asset.value = Loadable.Error(cause = error)
                logger.error(error) { "Failed to load static asset [$path]" }
            }
        }
    }
}
