package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.common.model.valueToChangeTo
import cloud.fabX.fabXaccess.qualification.model.QualificationId

// TODO PhoneNumberIdentification (maybe remove phone number from "personal details"?)
//      with phone number must be unique rule

// TODO wikiName must be unique rule

data class User internal constructor(
    override val id: UserId,
    override val aggregateVersion: Long,
    val firstName: String,
    val lastName: String,
    val wikiName: String,
    val locked: Boolean,
    val notes: String?,
    private val identities: Set<UserIdentity>,
    private val memberQualifications: Set<QualificationId>,
    private val instructorQualifications: Set<QualificationId>?,
    private val isAdmin: Boolean
) : AggregateRootEntity<UserId> {

    private val name: String
        get() = "$firstName $lastName"

    companion object {
        fun addNew(
            actor: Admin,
            firstName: String,
            lastName: String,
            wikiName: String
        ): UserSourcingEvent {
            return UserCreated(
                DomainModule.userIdFactory().invoke(),
                actor.id,
                firstName,
                lastName,
                wikiName
            )
        }

        fun fromSourcingEvents(events: Iterable<UserSourcingEvent>): Option<User> {
            events.assertIsNotEmpty()
            events.assertAggregateVersionStartsWithOne()
            events.assertAggregateVersionIncreasesOneByOne()

            val userCreatedEvent = events.first()

            if (userCreatedEvent !is UserCreated) {
                throw EventHistoryDoesNotStartWithUserCreated(
                    "Event history starts with ${userCreatedEvent}, not a UserCreated event."
                )
            }

            return events.fold(None) { result: Option<User>, event ->
                event.processBy(EventHandler(), result)
            }
        }
    }

    fun apply(sourcingEvent: UserSourcingEvent): Option<User> = sourcingEvent.processBy(EventHandler(), Some(this))

    fun changePersonalInformation(
        actor: Admin,
        firstName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        lastName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        wikiName: ChangeableValue<String> = ChangeableValue.LeaveAsIs
    ): UserSourcingEvent {
        return UserPersonalInformationChanged(
            id,
            aggregateVersion + 1,
            actor.id,
            firstName,
            lastName,
            wikiName
        )
    }

    fun changeLockState(
        actor: Admin,
        locked: ChangeableValue<Boolean> = ChangeableValue.LeaveAsIs,
        notes: ChangeableValue<String?> = ChangeableValue.LeaveAsIs
    ): UserSourcingEvent {
        return UserLockStateChanged(id, aggregateVersion + 1, actor.id, locked, notes)
    }

    fun addUsernamePasswordIdentity(
        actor: Admin,
        username: String,
        password: String
    ): UserSourcingEvent {
        // TODO username must be unique rule
        // TODO password hashing rule
        return UsernamePasswordIdentityAdded(id, aggregateVersion + 1, actor.id, username, password)
    }

    fun delete(
        actor: Admin
    ): UserSourcingEvent {
        return UserDeleted(id, aggregateVersion + 1, actor.id)
    }

    fun hasIdentity(userIdentity: UserIdentity) = identities.contains(userIdentity)

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

        override fun handle(event: UserCreated, user: Option<User>): Option<User> {
            if (user.isDefined()) {
                throw AccumulatorNotEmptyForUserCreatedEventHandler(
                    "Handler for UserCreated is given $user."
                )
            }

            return Some(
                User(
                    id = event.aggregateRootId,
                    aggregateVersion = event.aggregateVersion,
                    firstName = event.firstName,
                    lastName = event.lastName,
                    wikiName = event.wikiName,
                    locked = false,
                    notes = null,
                    identities = setOf(),
                    memberQualifications = setOf(),
                    instructorQualifications = null,
                    isAdmin = false
                )
            )
        }

        override fun handle(
            event: UserPersonalInformationChanged,
            user: Option<User>
        ): Option<User> =
            requireSomeUserWithSameIdAnd(event, user) { e, u ->
                Some(
                    u.copy(
                        aggregateVersion = e.aggregateVersion,
                        firstName = e.firstName.valueToChangeTo(u.firstName),
                        lastName = e.lastName.valueToChangeTo(u.lastName),
                        wikiName = e.wikiName.valueToChangeTo(u.wikiName),
                    )
                )
            }

        override fun handle(event: UserLockStateChanged, user: Option<User>): Option<User> =
            requireSomeUserWithSameIdAnd(event, user) { e, u ->
                Some(
                    u.copy(
                        aggregateVersion = e.aggregateVersion,
                        locked = e.locked.valueToChangeTo(u.locked),
                        notes = e.notes.valueToChangeTo(u.notes)
                    )
                )
            }

        override fun handle(event: UsernamePasswordIdentityAdded, user: Option<User>): Option<User> =
            requireSomeUserWithSameIdAnd(event, user) { e, u ->
                Some(
                    u.copy(
                        aggregateVersion = e.aggregateVersion,
                        identities = u.identities + UsernamePasswordIdentity(
                            e.username,
                            e.password
                        )
                    )
                )
            }

        override fun handle(event: UserDeleted, user: Option<User>): Option<User> =
            requireSomeUserWithSameIdAnd(event, user) { _, _ ->
                None
            }

        private fun <E : UserSourcingEvent> requireSomeUserWithSameIdAnd(
            event: E,
            user: Option<User>,
            and: (E, User) -> Option<User>
        ): Option<User> {
            if (user.map { it.id != event.aggregateRootId }.getOrElse { false }) {
                throw EventAggregateRootIdDoesNotMatchUserId(
                    "Event $event cannot be applied to $user. Aggregate root id does not match."
                )
            }

            return user.flatMap { and(event, it) }
        }

        class EventAggregateRootIdDoesNotMatchUserId(message: String) : Exception(message)
        class AccumulatorNotEmptyForUserCreatedEventHandler(message: String) : Exception(message)
    }

    class EventHistoryDoesNotStartWithUserCreated(message: String) : Exception(message)
}