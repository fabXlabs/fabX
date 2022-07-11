package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.Actor

/**
 * An acting administrator.
 */
class Admin : Actor {
    override fun toString(): String {
        return "Admin()"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}