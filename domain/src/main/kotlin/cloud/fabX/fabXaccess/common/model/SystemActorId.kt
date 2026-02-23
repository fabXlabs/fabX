package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.common.application.UuidSerializer
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data object SystemActorId : ActorId {
    @Serializable(with = UuidSerializer::class)
    override val value: Uuid = Uuid.fromLongs(0L, 0L)
}