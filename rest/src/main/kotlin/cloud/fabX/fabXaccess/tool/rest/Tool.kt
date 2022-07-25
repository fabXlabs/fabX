package cloud.fabX.fabXaccess.tool.rest

import kotlinx.serialization.Serializable

@Serializable
data class Tool(
    val id: String,
    val aggregateVersion: Long,
    val name: String,
    val type: ToolType,
    val time: Int,
    val idleState: IdleState,
    val enabled: Boolean,
    val wikiLink: String,
    val requiredQualifications: Set<String>
)

fun cloud.fabX.fabXaccess.tool.model.Tool.toRestModel(): Tool = Tool(
    id = id.serialize(),
    aggregateVersion = aggregateVersion,
    name = name,
    type = type.toRestModel(),
    time = time,
    idleState = idleState.toRestModel(),
    enabled = enabled,
    wikiLink = wikiLink,
    requiredQualifications = requiredQualifications.map { it.serialize() }.toSet()
)

fun cloud.fabX.fabXaccess.tool.model.ToolType.toRestModel(): ToolType {
    return when (this) {
        cloud.fabX.fabXaccess.tool.model.ToolType.UNLOCK -> ToolType.UNLOCK
        cloud.fabX.fabXaccess.tool.model.ToolType.KEEP -> ToolType.KEEP
    }
}

fun cloud.fabX.fabXaccess.tool.model.IdleState.toRestModel(): IdleState {
    return when (this) {
        cloud.fabX.fabXaccess.tool.model.IdleState.IDLE_LOW -> IdleState.IDLE_LOW
        cloud.fabX.fabXaccess.tool.model.IdleState.IDLE_HIGH -> IdleState.IDLE_HIGH
    }
}

enum class ToolType {
    UNLOCK,
    KEEP
}

enum class IdleState {
    IDLE_LOW,
    IDLE_HIGH
}
