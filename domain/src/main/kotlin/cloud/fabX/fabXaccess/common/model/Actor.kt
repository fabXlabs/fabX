package cloud.fabX.fabXaccess.common.model

import java.util.UUID

interface Actor {
    val id: ActorId
    val name: String
}

interface ActorId {
    val value: UUID
}