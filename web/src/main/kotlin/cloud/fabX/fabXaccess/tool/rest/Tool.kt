package cloud.fabX.fabXaccess.tool.rest

import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import kotlinx.serialization.Serializable

@Serializable
data class Tool(
    val id: String,
    val aggregateVersion: Long,
    val name: String,
    val type: ToolType,
    val requires2FA: Boolean,
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
    requires2FA = requires2FA,
    time = time,
    idleState = idleState.toRestModel(),
    enabled = enabled,
    wikiLink = wikiLink,
    requiredQualifications = requiredQualifications.map { it.serialize() }.toSet()
)

enum class ToolType {
    UNLOCK,
    KEEP
}

fun cloud.fabX.fabXaccess.tool.model.ToolType.toRestModel(): ToolType {
    return when (this) {
        cloud.fabX.fabXaccess.tool.model.ToolType.UNLOCK -> ToolType.UNLOCK
        cloud.fabX.fabXaccess.tool.model.ToolType.KEEP -> ToolType.KEEP
    }
}

fun ToolType.toDomainModel(): cloud.fabX.fabXaccess.tool.model.ToolType {
    return when (this) {
        ToolType.UNLOCK -> cloud.fabX.fabXaccess.tool.model.ToolType.UNLOCK
        ToolType.KEEP -> cloud.fabX.fabXaccess.tool.model.ToolType.KEEP
    }
}

enum class IdleState {
    IDLE_LOW,
    IDLE_HIGH
}

fun cloud.fabX.fabXaccess.tool.model.IdleState.toRestModel(): IdleState {
    return when (this) {
        cloud.fabX.fabXaccess.tool.model.IdleState.IDLE_LOW -> IdleState.IDLE_LOW
        cloud.fabX.fabXaccess.tool.model.IdleState.IDLE_HIGH -> IdleState.IDLE_HIGH
    }
}

fun IdleState.toDomainModel(): cloud.fabX.fabXaccess.tool.model.IdleState {
    return when (this) {
        IdleState.IDLE_LOW -> cloud.fabX.fabXaccess.tool.model.IdleState.IDLE_LOW
        IdleState.IDLE_HIGH -> cloud.fabX.fabXaccess.tool.model.IdleState.IDLE_HIGH
    }
}

@Serializable
data class ToolCreationDetails(
    val name: String,
    val type: ToolType,
    val requires2FA: Boolean,
    val time: Int,
    val idleState: IdleState,
    val wikiLink: String,
    val requiredQualifications: Set<String>
)

@Serializable
data class ToolDetails(
    val name: ChangeableValue<String>?,
    val type: ChangeableValue<ToolType>?,
    val requires2FA: ChangeableValue<Boolean>?,
    val time: ChangeableValue<Int>?,
    val idleState: ChangeableValue<IdleState>?,
    val enabled: ChangeableValue<Boolean>?,
    val wikiLink: ChangeableValue<String>?,
    val requiredQualifications: ChangeableValue<Set<String>>?
)