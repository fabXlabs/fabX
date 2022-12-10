package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.common.model.Logger
import org.slf4j.Marker

class Slf4jLogger(private val delegate: Logger) : org.slf4j.Logger {
    override fun getName(): String {
        return this::class.java.name
    }

    override fun isTraceEnabled(): Boolean = true

    override fun isTraceEnabled(marker: Marker?): Boolean = true

    override fun trace(msg: String) {
        delegate.trace(msg)
    }

    override fun trace(format: String, arg: Any) {
        delegate.trace(format, arg)
    }

    override fun trace(format: String?, arg1: Any?, arg2: Any?) {
        delegate.trace(format!!, arg1!!, arg2!!)
    }

    override fun trace(format: String, vararg arguments: Any) {
        delegate.trace(format, arguments)
    }

    override fun trace(msg: String, t: Throwable) {
        delegate.trace(msg, t)
    }

    override fun trace(marker: Marker?, msg: String?) {
        delegate.trace(msg!!)
    }

    override fun trace(marker: Marker?, format: String?, arg: Any?) {
        delegate.trace(format!!, arg!!)
    }

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        delegate.trace(format!!, arg1!!, arg2!!)
    }

    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) {
        delegate.trace(format!!, argArray)
    }

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {
        delegate.trace(msg!!, t!!)
    }

    override fun isDebugEnabled(): Boolean = true

    override fun isDebugEnabled(marker: Marker?): Boolean = true

    override fun debug(msg: String) {
        delegate.debug(msg)
    }

    override fun debug(format: String, arg: Any) {
        delegate.debug(format, arg)
    }

    override fun debug(format: String?, arg1: Any?, arg2: Any?) {
        delegate.debug(format!!, arg1!!, arg2!!)
    }

    override fun debug(format: String, vararg arguments: Any) {
        delegate.debug(format, arguments)
    }

    override fun debug(msg: String, t: Throwable) {
        delegate.debug(msg, t)
    }

    override fun debug(marker: Marker?, msg: String?) {
        delegate.debug(msg!!)
    }

    override fun debug(marker: Marker?, format: String?, arg: Any?) {
        delegate.debug(format!!, arg!!)
    }

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        delegate.debug(format!!, arg1!!, arg2!!)
    }

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
        delegate.debug(format!!, arguments)
    }

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {
        delegate.debug(msg!!, t!!)
    }

    override fun isInfoEnabled(): Boolean = true

    override fun isInfoEnabled(marker: Marker?): Boolean = true

    override fun info(msg: String) {
        delegate.info(msg)
    }

    override fun info(format: String, arg: Any) {
        delegate.info(format, arg)
    }

    override fun info(format: String?, arg1: Any?, arg2: Any?) {
        delegate.info(format!!, arg1!!, arg2!!)
    }

    override fun info(format: String, vararg arguments: Any) {
        delegate.info(format, arguments)
    }

    override fun info(msg: String, t: Throwable) {
        delegate.info(msg, t)
    }

    override fun info(marker: Marker?, msg: String?) {
        delegate.info(msg!!)
    }

    override fun info(marker: Marker?, format: String?, arg: Any?) {
        delegate.info(format!!, arg!!)
    }

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        delegate.info(format!!, arg1!!, arg2!!)
    }

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
        delegate.info(format!!, arguments)
    }

    override fun info(marker: Marker?, msg: String?, t: Throwable?) {
        delegate.info(msg!!, t!!)
    }

    override fun isWarnEnabled(): Boolean = true

    override fun isWarnEnabled(marker: Marker?): Boolean = true

    override fun warn(msg: String) {
        delegate.warn(msg)
    }

    override fun warn(format: String, arg: Any) {
        delegate.warn(format, arg)
    }

    override fun warn(format: String, vararg arguments: Any) {
        delegate.warn(format, arguments)
    }

    override fun warn(format: String?, arg1: Any?, arg2: Any?) {
        delegate.warn(format!!, arg1!!, arg2!!)
    }

    override fun warn(msg: String, t: Throwable) {
        delegate.warn(msg, t)
    }

    override fun warn(marker: Marker?, msg: String?) {
        delegate.warn(msg!!)
    }

    override fun warn(marker: Marker?, format: String?, arg: Any?) {
        delegate.warn(format!!, arg!!)
    }

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        delegate.warn(format!!, arg1!!, arg2!!)
    }

    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {
        delegate.warn(format!!, arguments)
    }

    override fun warn(marker: Marker?, msg: String?, t: Throwable?) {
        delegate.warn(msg!!, t!!)
    }

    override fun isErrorEnabled(): Boolean = true

    override fun isErrorEnabled(marker: Marker?): Boolean = true

    override fun error(msg: String) {
        delegate.error(msg)
    }

    override fun error(format: String, arg: Any) {
        delegate.error(format, arg)
    }

    override fun error(format: String?, arg1: Any?, arg2: Any?) {
        delegate.error(format!!, arg1!!, arg2!!)
    }

    override fun error(format: String, vararg arguments: Any) {
        delegate.error(format, arguments)
    }

    override fun error(msg: String, t: Throwable) {
        delegate.error(msg, t)
    }

    override fun error(marker: Marker?, msg: String?) {
        delegate.error(msg!!)
    }

    override fun error(marker: Marker?, format: String?, arg: Any?) {
        delegate.error(format!!, arg!!)
    }

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        delegate.error(format!!, arg1!!, arg2!!)
    }

    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
        delegate.error(format!!, arguments)
    }

    override fun error(marker: Marker?, msg: String?, t: Throwable?) {
        delegate.error(msg!!, t!!)
    }
}