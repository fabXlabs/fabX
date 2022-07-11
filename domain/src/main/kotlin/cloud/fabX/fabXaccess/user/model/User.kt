package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.valueToChangeTo
import cloud.fabX.fabXaccess.qualification.model.QualificationId

data class User internal constructor(
    override val id: UserId,
    override val aggregateVersion: Long,
    val firstName: String,
    val lastName: String,
    val wikiName: String,
    val phoneNumber: String?,
    val locked: Boolean,
    val notes: String?,
    private val memberQualifications: List<QualificationId>,
    private val instructorQualifications: List<QualificationId>?,
    private val isAdmin: Boolean
) : AggregateRootEntity<UserId> {

    companion object {
        fun fromSourcingEvents(id: UserId, events: Iterable<UserSourcingEvent>): User {
            val defaultFirstName = "defaultFirstName"
            val defaultLastName = "defaultLastName"
            val defaultWikiName = "defaultWikiName"

            events.assertAggregateVersionIncreasesOneByOne()

            val user = events.fold(
                User(
                    id,
                    -1L,
                    defaultFirstName,
                    defaultLastName,
                    defaultWikiName,
                    null,
                    false,
                    null,
                    listOf(),
                    null,
                    false
                )
            ) { user, event ->
                user.apply(event)
            }

            if (user.aggregateVersion == -1L
                || user.firstName == defaultFirstName
                || user.lastName == defaultLastName
                || user.wikiName == defaultWikiName) {
                throw UserNotHasMandatoryDefaultValuesReplaced("User $user has default values after applying all sourcing events.")
            }

            return user
        }

        class UserNotHasMandatoryDefaultValuesReplaced(message: String): Exception(message)
    }

    fun apply(sourcingEvent: UserSourcingEvent): User = sourcingEvent.processBy(EventHandler(), this)

    fun changePersonalInformation(
        firstName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        lastName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        wikiName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        phoneNumber: ChangeableValue<String?> = ChangeableValue.LeaveAsIs
    ): UserSourcingEvent {
        return UserPersonalInformationChanged(id, aggregateVersion + 1, firstName, lastName, wikiName, phoneNumber)
    }

    fun changeLockState(
        locked: ChangeableValue<Boolean> = ChangeableValue.LeaveAsIs,
        notes: ChangeableValue<String?> = ChangeableValue.LeaveAsIs
    ): UserSourcingEvent {
        return UserLockStateChanged(id, aggregateVersion + 1, locked, notes)
    }

    fun asMember(): Member = Member(id, memberQualifications)

    fun asInstructor(): Either<Error, Instructor> =
        Option.fromNullable(instructorQualifications)
            .map { Instructor(id, it) }
            .toEither { Error.UserNotInstructor("User $id is not an instructor.") }

    fun asAdmin(): Either<Error, Admin> = Either.conditionally(
        isAdmin,
        { Error.UserNotAdmin("User $id is not an admin.") },
        { Admin(id) }
    )

    private class EventHandler : UserSourcingEvent.EventHandler {
        override fun handle(event: UserPersonalInformationChanged, user: User): User =
            requireSameUserIdAnd(event, user) { e, u ->
                u.copy(
                    aggregateVersion = e.aggregateVersion,
                    firstName = e.firstName.valueToChangeTo(u.firstName),
                    lastName = e.lastName.valueToChangeTo(u.lastName),
                    wikiName = e.wikiName.valueToChangeTo(u.wikiName),
                    phoneNumber = e.phoneNumber.valueToChangeTo(u.phoneNumber)
                )
            }

        override fun handle(event: UserLockStateChanged, user: User): User =
            requireSameUserIdAnd(event, user) { e, u ->
                u.copy(
                    aggregateVersion = e.aggregateVersion,
                    locked = e.locked.valueToChangeTo(u.locked),
                    notes = e.notes.valueToChangeTo(u.notes)
                )
            }

        private fun <E : UserSourcingEvent> requireSameUserIdAnd(event: E, user: User, and: (E, User) -> User): User {
            if (event.aggregateRootId != user.id) {
                throw EventAggregateRootIdDoesNotMatchUserId("Event $event cannot be applied to $user.")
            }
            return and(event, user)
        }

        class EventAggregateRootIdDoesNotMatchUserId(message: String) : Exception(message)
    }
}