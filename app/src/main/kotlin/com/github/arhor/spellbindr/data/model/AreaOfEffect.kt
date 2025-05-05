package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable(with = AreaOfEffect.Serializer::class)
data class AreaOfEffect(
    val size: Int,
    val type: Type,
) {
    enum class Type {
        CUBE,
        SPHERE,
        CYLINDER,
        CONE,
        LINE,
        CIRCLE
    }

    companion object Serializer : KSerializer<AreaOfEffect> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AreaOfEffect")

        override fun serialize(encoder: Encoder, value: AreaOfEffect) {
            val json = buildJsonObject {
                put("size", value.size)
                put("type", value.type.name)
            }
            encoder.encodeSerializableValue(JsonObject.serializer(), json)
        }

        override fun deserialize(decoder: Decoder): AreaOfEffect {
            val json = decoder.decodeSerializableValue(JsonObject.serializer())
            val size = json["size"]?.jsonPrimitive?.content?.toIntOrNull()
                ?: throw IllegalArgumentException("Missing 'size' field in AreaOfEffect")
            val type = json["type"]?.jsonPrimitive?.content?.let { typeStr ->
                try {
                    Type.valueOf(typeStr)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Invalid area type: $typeStr", e)
                }
            } ?: throw IllegalArgumentException("Missing 'type' field in AreaOfEffect")

            return AreaOfEffect(size, type)
        }
    }
}
