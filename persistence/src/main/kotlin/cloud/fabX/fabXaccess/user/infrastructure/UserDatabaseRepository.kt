package cloud.fabX.fabXaccess.user.infrastructure

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserId
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent

class UserDatabaseRepository : UserRepository {
    private var events = mutableListOf<UserSourcingEvent>()

    override fun getById(id: UserId): Either<Error, User> {
        val e = events
            .filter { it.aggregateRootId == id }
            .sortedBy { it.aggregateVersion }

        return if (e.isNotEmpty()) {
            User.fromSourcingEvents(id, e).right()
        } else {
            Error.UserNotFoundError("User with id $id not found").left()
        }
    }

    override fun store(event: UserSourcingEvent) {
        TODO("get previous aggregate version number if exists")
        TODO("check if aggregate version number of event is previous+1")
        // TODO if not return error, NOT throw
        //      -> user should see some kind of error message
        events.add(event)
    }

    private fun getVersionById(id: UserId): Long? {
        TODO()
    }
}