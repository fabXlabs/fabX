package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
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

    private val name: String
        get() = "$firstName $lastName"

    companion object {
        // TODO return Option<User> (e.g. None if User was deleted)
        fun fromSourcingEvents(events: Iterable<UserSourcingEvent>): User {
            events.assertIsNotEmpty()
            events.assertAggregateVersionStartsWithOne()
            events.assertAggregateVersionIncreasesOneByOne()

            val userCreatedEvent = events.first()

            if (userCreatedEvent !is UserCreated) {
                throw EventHistoryDoesNotStartWithUserCreated(
                    "Event history starts with ${userCreatedEvent}, not a UserCreated event."
                )
            }

            return events.fold(
                User(
                    userCreatedEvent.aggregateRootId,
                    userCreatedEvent.aggregateVersion,
                    userCreatedEvent.firstName,
                    userCreatedEvent.lastName,
                    userCreatedEvent.wikiName,
                    userCreatedEvent.phoneNumber,
                    false,
                    null,
                    listOf(),
                    null,
                    false
                )
            ) { user, event ->
                user.apply(event)
            }
        }
    }

    fun apply(sourcingEvent: UserSourcingEvent): User = sourcingEvent.processBy(EventHandler(), this)

    fun changePersonalInformation(
        actor: Admin,
        firstName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        lastName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        wikiName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        phoneNumber: ChangeableValue<String?> = ChangeableValue.LeaveAsIs
    ): UserSourcingEvent {
        return UserPersonalInformationChanged(
            id,
            aggregateVersion + 1,
            actor.id,
            firstName,
            lastName,
            wikiName,
            phoneNumber
        )
    }

    fun changeLockState(
        actor: Admin,
        locked: ChangeableValue<Boolean> = ChangeableValue.LeaveAsIs,
        notes: ChangeableValue<String?> = ChangeableValue.LeaveAsIs
    ): UserSourcingEvent {
        return UserLockStateChanged(id, aggregateVersion + 1, actor.id, locked, notes)
    }

    fun asMember(): Member = Member(id, name, memberQualifications)

    fun asInstructor(): Either<Error, Instructor> =
        Option.fromNullable(instructorQualifications)
            .map { Instructor(id, name, it) }
            .toEither { Error.UserNotInstructor("User $id is not an instructor.") }

    fun asAdmin(): Either<Error, Admin> = Either.conditionally(
        isAdmin,
        { Error.UserNotAdmin("User $id is not an admin.") },
        { Admin(id, name) }
    )

    private class EventHandler : UserSourcingEvent.EventHandler {

        override fun handle(event: UserCreated, user: User): User = User(
            id = event.aggregateRootId,
            aggregateVersion = event.aggregateVersion,
            firstName = event.firstName,
            lastName = event.lastName,
            wikiName = event.wikiName,
            phoneNumber = event.phoneNumber,
            locked = false,
            notes = null,
            memberQualifications = listOf(),
            instructorQualifications = null,
            isAdmin = false
        )

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

    class EventHistoryDoesNotStartWithUserCreated(message: String) : Exception(message)
}