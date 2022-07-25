package cloud.fabX.fabXaccess.common.rest

import kotlinx.serialization.Serializable

@Serializable
data class ChangeableValue<T>(
    val newValue: T
)

fun <T> ChangeableValue<T>?.toDomain(): cloud.fabX.fabXaccess.common.model.ChangeableValue<T> {
    return if (this != null) {
        cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValue(this.newValue)
    } else {
        cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs
    }
}

fun <T, S> ChangeableValue<T>?.toDomain(
    map: (T) -> S
): cloud.fabX.fabXaccess.common.model.ChangeableValue<S> {
    return if (this != null) {
        cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValue(map(this.newValue))
    } else {
        cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs
    }
}