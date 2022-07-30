package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.tool.rest.IdleState
import cloud.fabX.fabXaccess.tool.rest.ToolType
import cloud.fabX.fabXaccess.tool.rest.toDomainModel
import kotlinx.serialization.Serializable

@Serializable
data class ChangeableValue<T>(
    val newValue: T
)

@Suppress("UNCHECKED_CAST")
inline fun <reified T> ChangeableValue<T>?.toDomain(): cloud.fabX.fabXaccess.common.model.ChangeableValue<T> {
    return if (this != null) {
        when (newValue) {
            is Int -> cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueInt(newValue)
            is String -> cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueString(newValue)
            is String? -> cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueOptionalString(newValue)
            is ToolType -> cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueToolType(newValue.toDomainModel())
            is IdleState -> cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueIdleState(newValue.toDomainModel())
            is Boolean -> cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueBoolean(newValue)
            else -> throw IllegalArgumentException("")
        } as cloud.fabX.fabXaccess.common.model.ChangeableValue<T>
    } else {
        cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs
    }
}

fun <T, S> ChangeableValue<T>?.toDomain(
    map: (T) -> cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValue<S>
): cloud.fabX.fabXaccess.common.model.ChangeableValue<S> {
    return if (this != null) {
        map(this.newValue)
    } else {
        cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs
    }
}