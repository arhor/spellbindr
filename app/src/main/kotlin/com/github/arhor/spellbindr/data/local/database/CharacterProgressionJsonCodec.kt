package com.github.arhor.spellbindr.data.local.database

import com.github.arhor.spellbindr.domain.model.ProgressionState
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterProgressionJsonCodec @Inject constructor(
    private val json: Json,
) {

    fun encode(state: ProgressionState): String = json.encodeToString(state)

    fun decode(json: String): ProgressionState = this.json.decodeFromString(json)
}
