package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.newUserId

class RestClass {
    fun useDomain(): User {
        return User(newUserId(), "first", "last", "wiki", "phone", false, null)
    }
}