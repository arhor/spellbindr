package com.github.arhor.spellbindr.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class CaseInsensitiveEnumSerializer<T : Enum<T>>(
    private val enumValues: Array<T>,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Enum", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(value.name.lowercase())
    }

    override fun deserialize(decoder: Decoder): T {
        val value = decoder.decodeString()
        return enumValues.firstOrNull { it.name.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown enum value: $value")
    }

    companion object {
        inline operator fun <reified T : Enum<T>> invoke(): KSerializer<T> =
            CaseInsensitiveEnumSerializer(enumValues<T>())
    }
}

