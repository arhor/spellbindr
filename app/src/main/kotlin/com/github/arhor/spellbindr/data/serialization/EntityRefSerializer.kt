package com.github.arhor.spellbindr.data.serialization

import com.github.arhor.spellbindr.domain.model.EntityRef
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object EntityRefSerializer : KSerializer<EntityRef> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("EntityRef", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EntityRef) {
        encoder.encodeString(value.id)
    }

    override fun deserialize(decoder: Decoder): EntityRef {
        return EntityRef(decoder.decodeString())
    }
}
