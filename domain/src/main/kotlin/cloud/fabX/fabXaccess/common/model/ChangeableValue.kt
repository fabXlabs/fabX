package cloud.fabX.fabXaccess.common.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import arrow.core.some
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolType
import kotlinx.serialization.Serializable

/**
 * Represents a value which can be changed to a new value or left as is.
 */
@Serializable
sealed class ChangeableValue<out T> {
    @Serializable
    data object LeaveAsIs : ChangeableValue<Nothing>() {
        override fun toString(): String {
            return "LeaveAsIs"
        }
    }

    @Serializable
    sealed class ChangeToValue<T> : ChangeableValue<T>() {
        abstract val value: T
    }

    @Serializable
    data class ChangeToValueInt(override val value: Int) : ChangeToValue<Int>()

    @Serializable
    data class ChangeToValueString(override val value: String) : ChangeToValue<String>()

    @Serializable
    data class ChangeToValueOptionalString(override val value: String?) : ChangeToValue<String?>()

    @Serializable
    data class ChangeToValueBoolean(override val value: Boolean) : ChangeToValue<Boolean>()

    @Serializable
    data class ChangeToValueToolType(override val value: ToolType) : ChangeToValue<ToolType>()

    @Serializable
    data class ChangeToValueIdleState(override val value: IdleState) : ChangeToValue<IdleState>()

    @Serializable
    data class ChangeToValueQualificationSet(override val value: Set<QualificationId>) :
        ChangeToValue<Set<QualificationId>>()

}

fun <T> ChangeableValue<T>.valueToChangeTo(previous: T): T = when (this) {
    is ChangeableValue.ChangeToValue -> value
    ChangeableValue.LeaveAsIs -> previous
}

fun <T> ChangeableValue<T>.asOption(): Option<T> = when (this) {
    is ChangeableValue.ChangeToValue -> value.some()
    ChangeableValue.LeaveAsIs -> None
}

inline fun <T, L, R> ChangeableValue<T>.bimap(
    mapLeaveAsIs: () -> L,
    mapChangeToValue: (T) -> R
): Either<L, R> = when (this) {
    is ChangeableValue.ChangeToValue -> mapChangeToValue(value).right()
    ChangeableValue.LeaveAsIs -> mapLeaveAsIs().left()
}

inline fun <T, L, R> ChangeableValue<T>.biFlatmap(
    mapLeaveAsIs: () -> Either<L, R>,
    mapChangeToValue: (T) -> Either<L, R>
): Either<L, R> = when (this) {
    is ChangeableValue.ChangeToValue -> mapChangeToValue(value)
    ChangeableValue.LeaveAsIs -> mapLeaveAsIs()
}