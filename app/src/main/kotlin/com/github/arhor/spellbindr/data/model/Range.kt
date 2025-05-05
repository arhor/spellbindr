package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable(with = Range.Serializer::class)
sealed class Range {
    data object Touch : Range()

    data object Sight : Range()

    data object Unlimited : Range()

    data class Self(val area: AreaOfEffect? = null) : Range()

    data class Feet(val distance: Int, val area: AreaOfEffect? = null) : Range()

    data class Miles(val distance: Int, val area: AreaOfEffect? = null) : Range()

    data class Special(val description: String) : Range()

    companion object Serializer : KSerializer<Range> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Range")

        override fun serialize(encoder: Encoder, value: Range) {
            val json = when (value) {
                is Touch -> buildJsonObject { put("type", "Touch") }
                is Unlimited -> buildJsonObject { put("type", "Unlimited") }
                is Self -> buildJsonObject {
                    put("type", "Self")
                    value.area?.let { area ->
                        put("area", buildJsonObject {
                            put("size", area.size.toString())
                            put("type", area.type.name)
                        })
                    }
                }
                is Feet -> buildJsonObject {
                    put("type", "Feet")
                    put("distance", value.distance.toString())
                    value.area?.let { area ->
                        put("area", buildJsonObject {
                            put("size", area.size.toString())
                            put("type", area.type.name)
                        })
                    }
                }
                is Miles -> buildJsonObject {
                    put("type", "Miles")
                    put("distance", value.distance.toString())
                    value.area?.let { area ->
                        put("area", buildJsonObject {
                            put("size", area.size.toString())
                            put("type", area.type.name)
                        })
                    }
                }
                is Sight -> buildJsonObject {
                    put("type", "Sight")
                }
                is Special -> buildJsonObject {
                    put("type", "Special")
                    put("description", value.description)
                }
            }
            encoder.encodeSerializableValue(JsonObject.serializer(), json)
        }

        override fun deserialize(decoder: Decoder): Range {
            val json = decoder.decodeSerializableValue(JsonObject.serializer())
            val type = json["type"]?.jsonPrimitive?.content ?: throw IllegalArgumentException("Missing 'type' field in Range")

            return when (type) {
                "Touch" -> Touch
                "Unlimited" -> Unlimited
                "Self" -> {
                    val area = json["area"]?.jsonObject?.let { areaJson ->
                        val size = areaJson["size"]?.jsonPrimitive?.content?.toIntOrNull()
                            ?: throw IllegalArgumentException("Missing 'size' field in AreaOfEffect")
                        val areaType = areaJson["type"]?.jsonPrimitive?.content?.let { typeStr ->
                            try {
                                AreaOfEffect.Type.valueOf(typeStr)
                            } catch (e: IllegalArgumentException) {
                                throw IllegalArgumentException("Invalid area type: $typeStr", e)
                            }
                        } ?: throw IllegalArgumentException("Missing 'type' field in AreaOfEffect")
                        AreaOfEffect(size, areaType)
                    }
                    Self(area)
                }
                "Feet" -> {
                    val distance = json["distance"]?.jsonPrimitive?.content?.toIntOrNull()
                        ?: throw IllegalArgumentException("Missing 'distance' field in Feet range")
                    val area = json["area"]?.jsonObject?.let { areaJson ->
                        val size = areaJson["size"]?.jsonPrimitive?.content?.toIntOrNull()
                            ?: throw IllegalArgumentException("Missing 'size' field in AreaOfEffect")
                        val areaType = areaJson["type"]?.jsonPrimitive?.content?.let { typeStr ->
                            try {
                                AreaOfEffect.Type.valueOf(typeStr)
                            } catch (e: IllegalArgumentException) {
                                throw IllegalArgumentException("Invalid area type: $typeStr", e)
                            }
                        } ?: throw IllegalArgumentException("Missing 'type' field in AreaOfEffect")
                        AreaOfEffect(size, areaType)
                    }
                    Feet(distance, area)
                }
                "Miles" -> {
                    val distance = json["distance"]?.jsonPrimitive?.content?.toIntOrNull()
                        ?: throw IllegalArgumentException("Missing 'distance' field in Miles range")
                    val area = json["area"]?.jsonObject?.let { areaJson ->
                        val size = areaJson["size"]?.jsonPrimitive?.content?.toIntOrNull()
                            ?: throw IllegalArgumentException("Missing 'size' field in AreaOfEffect")
                        val areaType = areaJson["type"]?.jsonPrimitive?.content?.let { typeStr ->
                            try {
                                AreaOfEffect.Type.valueOf(typeStr)
                            } catch (e: IllegalArgumentException) {
                                throw IllegalArgumentException("Invalid area type: $typeStr", e)
                            }
                        } ?: throw IllegalArgumentException("Missing 'type' field in AreaOfEffect")
                        AreaOfEffect(size, areaType)
                    }
                    Miles(distance, area)
                }
                "Sight" -> {
                    Sight
                }
                "Special" -> {
                    val description = json["description"]?.jsonPrimitive?.content
                        ?: "N/A"
                    Special(description )
                }
                else -> throw IllegalArgumentException("Unknown range type: $type")
            }
        }
    }
}
