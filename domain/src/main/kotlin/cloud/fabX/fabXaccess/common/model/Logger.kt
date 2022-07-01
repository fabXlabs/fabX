package cloud.fabX.fabXaccess.common.model

/**
 * Interface for a logger implementation.
 *
 * Heavily inspired by org.slf4j.Logger
 */
interface Logger {
    fun trace(msg: String)
    fun trace(format: String, arg: Any)
    fun trace(format: String, vararg arguments: Any)
    fun trace(msg: String, t: Throwable)

    fun debug(msg: String)
    fun debug(format: String, arg: Any)
    fun debug(format: String, vararg arguments: Any)
    fun debug(msg: String, t: Throwable)

    fun info(msg: String)
    fun info(format: String, arg: Any)
    fun info(format: String, vararg arguments: Any)
    fun info(msg: String, t: Throwable)

    fun warn(msg: String)
    fun warn(format: String, arg: Any)
    fun warn(format: String, vararg arguments: Any)
    fun warn(msg: String, t: Throwable)

    fun error(msg: String)
    fun error(format: String, arg: Any)
    fun error(format: String, vararg arguments: Any)
    fun error(msg: String, t: Throwable)
}