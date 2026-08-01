package com.github.arhor.spellbindr.data.local.database

import com.github.arhor.spellbindr.domain.model.ProgressionState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterProgressionJsonCodec @Inject constructor(
    private val json: Json,
) {

    fun encode(state: ProgressionState): String = json.encodeToString(state)

    fun decode(value: String): ProgressionState = json.decodeFromJsonElement(
        json.parseToJsonElement(value).withStableProgressionDiscriminators(),
    )
}

private fun JsonElement.withStableProgressionDiscriminators(): JsonElement = when (this) {
    is JsonArray -> JsonArray(map(JsonElement::withStableProgressionDiscriminators))
    is JsonObject -> JsonObject(
        mapValues { (key, value) ->
            if (key == "type" && value is JsonPrimitive && value.isString) {
                JsonPrimitive(legacyProgressionDiscriminatorAliases[value.content] ?: value.content)
            } else {
                value.withStableProgressionDiscriminators()
            }
        },
    )

    else -> this
}

private val legacyProgressionDiscriminatorAliases = mapOf(
    "com.github.arhor.spellbindr.domain.model.ProgressionState.Managed" to "managed",
    "com.github.arhor.spellbindr.domain.model.ProgressionState.Unmanaged" to "unmanaged",
    "com.github.arhor.spellbindr.domain.model.HitPointGain.Fixed" to "fixed",
    "com.github.arhor.spellbindr.domain.model.HitPointGain.Rolled" to "rolled",
    "com.github.arhor.spellbindr.domain.model.HitPointGain.Manual" to "manual",
    "com.github.arhor.spellbindr.domain.model.AbilityScoreDecision.Increase" to "ability-score-increase",
    "com.github.arhor.spellbindr.domain.model.AbilityScoreDecision.Feat" to "feat",
)
