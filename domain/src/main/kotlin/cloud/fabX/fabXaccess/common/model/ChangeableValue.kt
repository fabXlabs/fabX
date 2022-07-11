package cloud.fabX.fabXaccess.common.model

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

fun <T> ChangeableValue<T>.valueToChangeTo(previous: T): T = when(this) {
    is ChangeableValue.ChangeToValue -> value
    ChangeableValue.LeaveAsIs -> previous
}