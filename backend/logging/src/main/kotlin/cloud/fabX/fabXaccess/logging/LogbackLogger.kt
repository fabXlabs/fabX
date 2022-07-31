package cloud.fabX.fabXaccess.logging

import cloud.fabX.fabXaccess.common.model.Logger

class LogbackLogger(private val delegate: org.slf4j.Logger) : Logger {

    override fun trace(msg: String) {
        delegate.trace(msg)
    }

    override fun trace(format: String, arg: Any) {
        delegate.trace(format, arg)
    }

    override fun trace(format: String, vararg arguments: Any) {
        delegate.trace(format, arguments)
    }

    override fun trace(msg: String, t: Throwable) {
        delegate.trace(msg, t)
    }

    override fun debug(msg: String) {
        delegate.debug(msg)
    }

    override fun debug(format: String, arg: Any) {
        delegate.debug(format, arg)
    }

    override fun debug(format: String, vararg arguments: Any) {
        delegate.debug(format, arguments)
    }

    override fun debug(msg: String, t: Throwable) {
        delegate.debug(msg, t)
    }

    override fun info(msg: String) {
        delegate.info(msg)
    }

    override fun info(format: String, arg: Any) {
        delegate.info(format, arg)
    }

    override fun info(format: String, vararg arguments: Any) {
        delegate.info(format, arguments)
    }

    override fun info(msg: String, t: Throwable) {
        delegate.info(msg, t)
    }

    override fun warn(msg: String) {
        delegate.warn(msg)
    }

    override fun warn(format: String, arg: Any) {
        delegate.warn(format, arg)
    }

    override fun warn(format: String, vararg arguments: Any) {
        delegate.warn(format, arguments)
    }

    override fun warn(msg: String, t: Throwable) {
        delegate.warn(msg, t)
    }

    override fun error(msg: String) {
        delegate.error(msg)
    }

    override fun error(format: String, arg: Any) {
        delegate.error(format, arg)
    }

    override fun error(format: String, vararg arguments: Any) {
        delegate.error(format, arguments)
    }

    override fun error(msg: String, t: Throwable) {
        delegate.error(msg, t)
    }
}