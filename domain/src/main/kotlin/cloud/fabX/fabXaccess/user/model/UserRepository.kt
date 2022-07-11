package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.Error

interface UserRepository {
    fun getById(id: UserId): Either<Error, User>
    fun store(event: UserSourcingEvent)
}