package cloud.fabX.fabXaccess.common.rest

import java.nio.ByteBuffer
import java.util.UUID

fun UUID.toByteArray(): ByteArray {
    val b = ByteBuffer.wrap(ByteArray(16))
    b.putLong(mostSignificantBits)
    b.putLong(leastSignificantBits)
    return b.array()
}