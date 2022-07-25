package cloud.fabX.fabXaccess.common.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import arrow.core.some

/**
 * Represents a value which can be changed to a new value or left as is.
 */
sealed class ChangeableValue<out T> {
    object LeaveAsIs : ChangeableValue<Nothing>() {
        override fun toString(): String {
            return "LeaveAsIs"
        }
    }

    data class ChangeToValue<T>(val value: T) : ChangeableValue<T>()
}

fun <T> ChangeableValue<T>.valueToChangeTo(previous: T): T = when (this) {
    is ChangeableValue.ChangeToValue -> value
    ChangeableValue.LeaveAsIs -> previous
}

fun <T> ChangeableValue<T>.asOption(): Option<T> = when (this) {
    is ChangeableValue.ChangeToValue -> value.some()
    ChangeableValue.LeaveAsIs -> None
}

fun <T, L, R> ChangeableValue<T>.bimap(
    mapLeaveAsIs: () -> L,
    mapChangeToValue: (T) -> R
): Either<L, R> = when (this) {
    is ChangeableValue.ChangeToValue -> mapChangeToValue(value).right()
    ChangeableValue.LeaveAsIs -> mapLeaveAsIs().left()
}

fun <T, L, R> ChangeableValue<T>.biFlatmap(
    mapLeaveAsIs: () -> Either<L, R>,
    mapChangeToValue: (T) -> Either<L, R>
): Either<L, R> = when (this) {
    is ChangeableValue.ChangeToValue -> mapChangeToValue(value)
    ChangeableValue.LeaveAsIs -> mapLeaveAsIs()
}