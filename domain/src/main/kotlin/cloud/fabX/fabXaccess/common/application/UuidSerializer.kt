package cloud.fabX.fabXaccess.common.application

import kotlin.uuid.Uuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// TODO can this be removed?
object UuidSerializer : KSerializer<Uuid> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Uuid {
        return Uuid.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Uuid) {
        encoder.encodeString(value.toString())
    }
}
