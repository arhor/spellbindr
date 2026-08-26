package io.github.arhor.dnd.companion.data.local.database

import io.github.arhor.dnd.companion.domain.model.ProgressionState
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
    "io.github.arhor.dnd.companion.domain.model.ProgressionState.Managed" to "managed",
    "io.github.arhor.dnd.companion.domain.model.ProgressionState.Unmanaged" to "unmanaged",
    "io.github.arhor.dnd.companion.domain.model.HitPointGain.Fixed" to "fixed",
    "io.github.arhor.dnd.companion.domain.model.HitPointGain.Rolled" to "rolled",
    "io.github.arhor.dnd.companion.domain.model.HitPointGain.Manual" to "manual",
    "io.github.arhor.dnd.companion.domain.model.AbilityScoreDecision.Increase" to "ability-score-increase",
    "io.github.arhor.dnd.companion.domain.model.AbilityScoreDecision.Feat" to "feat",
)
