package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.SourcingEvent

interface UserRepository {
    fun getById(id: UserId): User?
    fun store(event: SourcingEvent)
}