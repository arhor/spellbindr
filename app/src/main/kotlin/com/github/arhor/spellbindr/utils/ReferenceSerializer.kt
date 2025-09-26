package com.github.arhor.spellbindr.utils

import com.github.arhor.spellbindr.data.model.next.Reference
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ReferenceSerializer : KSerializer<Reference> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Reference", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Reference) {
        encoder.encodeString(value.id)
    }

    override fun deserialize(decoder: Decoder): Reference {
        return Reference(decoder.decodeString())
    }
}

