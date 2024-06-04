package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.common.application.UuidSerializer
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data object SystemActorId : ActorId {
    @Serializable(with = UuidSerializer::class)
    override val value: UUID = UUID(0L, 0L)
}