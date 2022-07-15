package cloud.fabX.fabXaccess.tool.model

import cloud.fabX.fabXaccess.common.model.EntityId
import java.util.UUID

/**
 * Technical (artificial) ID of a Tool.
 */
data class ToolId(override val value: UUID) : EntityId<UUID>

typealias ToolIdFactory = () -> ToolId

/**
 * Returns a new ToolId.
 *
 * @return a ToolId of a random UUID
 */
fun newToolId(): ToolId {
    return ToolId(UUID.randomUUID())
}