package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.common.application.UuidSerializer
import java.util.UUID
import kotlinx.serialization.Serializable

interface Actor {
    val id: ActorId
    val name: String
}

sealed interface ActorId {
    @Serializable(with = UuidSerializer::class)
    val value: UUID
}