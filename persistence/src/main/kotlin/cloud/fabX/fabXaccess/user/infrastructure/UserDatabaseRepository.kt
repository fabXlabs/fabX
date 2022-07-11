package cloud.fabX.fabXaccess.user.infrastructure

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.valueToChangeTo
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserId
import cloud.fabX.fabXaccess.user.model.UserLockStateChanged
import cloud.fabX.fabXaccess.user.model.UserPersonalInformationChanged
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent

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

        override fun handle(event: UserPersonalInformationChanged, dao: MutableList<User>) {
            val originalUser = dao.first { it.id == event.aggregateRootId }

            val updatedUser = originalUser.copy(
                firstName = event.firstName.valueToChangeTo(originalUser.firstName),
                lastName = event.lastName.valueToChangeTo(originalUser.lastName),
                wikiName = event.wikiName.valueToChangeTo(originalUser.wikiName),
                phoneNumber = event.phoneNumber.valueToChangeTo(originalUser.phoneNumber)
            )

            dao.remove(originalUser)
            dao.add(updatedUser)
        }

        override fun handle(event: UserLockStateChanged, dao: MutableList<User>) {
            val originalUser = dao.first { it.id == event.aggregateRootId }

            val updatedUser = originalUser.copy(
                locked = event.locked.valueToChangeTo(originalUser.locked),
                notes = event.notes.valueToChangeTo(originalUser.notes)
            )

            dao.remove(originalUser)
            dao.add(updatedUser)
        }
    }
}