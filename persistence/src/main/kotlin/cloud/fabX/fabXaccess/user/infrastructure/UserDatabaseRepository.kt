package cloud.fabX.fabXaccess.user.infrastructure

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.valueToChangeTo
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserId
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent
import cloud.fabX.fabXaccess.user.model.UserValuesChanged

class UserDatabaseRepository(private val eventHandler: EventHandler) : UserRepository {
    private var users = mutableListOf<User>()

    override fun getById(id: UserId): Either<Error, User> {
        return Either.fromNullable(users.firstOrNull { it.id == id })
            .mapLeft { Error.UserNotFoundError("User with id $id not found") }
    }

    override fun store(event: UserSourcingEvent) {
        event.processBy(eventHandler, users)
    }

    class EventHandler : UserSourcingEvent.EventHandler {

        override fun handle(event: UserValuesChanged, dao: MutableList<User>) {
            val originalUser = dao.first { it.id == event.aggregateRootId }

            val updatedUser = originalUser.copy(
                firstName = event.firstName.valueToChangeTo(originalUser.firstName)
            )

            dao.remove(originalUser)
            dao.add(updatedUser)
        }
    }
}