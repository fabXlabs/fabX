package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.flatMap
import arrow.core.toOption
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.GetQualificationById
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.qualification.model.QualificationId

// TODO wikiName must be unique rule

data class User internal constructor(
    override val id: UserId,
    override val aggregateVersion: Long,
    val firstName: String,
    val lastName: String,
    val wikiName: String,
    val locked: Boolean,
    val notes: String?,
    internal val identities: Set<UserIdentity>,
    internal val memberQualifications: Set<QualificationId>,
    internal val instructorQualifications: Set<QualificationId>?,
    internal val isAdmin: Boolean
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
                event.processBy(UserEventHandler(), result)
            }
        }
    }

    fun apply(sourcingEvent: UserSourcingEvent): Option<User> = sourcingEvent.processBy(UserEventHandler(), Some(this))

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
        hash: String
    ): UserSourcingEvent {
        // TODO username must be unique rule
        // TODO at most one UsernamePasswordIdentity rule
        return UsernamePasswordIdentityAdded(id, aggregateVersion + 1, actor.id, username, hash)
    }

    /**
     * Removes the user's identity given by the username.
     *
     * @return error if no identity with given username exists, sourcing event otherwise
     */
    fun removeUsernamePasswordIdentity(
        actor: Admin,
        username: String
    ): Either<Error, UserSourcingEvent> {
        return identities.firstOrNull { it is UsernamePasswordIdentity && it.username == username }
            .toOption()
            .toEither {
                Error.UserIdentityNotFound(
                    "Not able to find identity with username \"$username\".",
                    mapOf("username" to username)
                )
            }
            .map {
                UsernamePasswordIdentityRemoved(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    username
                )
            }
    }

    fun addCardIdentity(
        actor: Admin,
        cardId: String,
        cardSecret: String
    ): UserSourcingEvent {
        // TODO card id must be unique rule
        return CardIdentityAdded(id, aggregateVersion + 1, actor.id, cardId, cardSecret)
    }

    /**
     * Removes the user's identity given by the card id.
     *
     * @return error if no identity with given card id exists, sourcing event otherwise
     */
    fun removeCardIdentity(
        actor: Admin,
        cardId: String
    ): Either<Error, UserSourcingEvent> {
        return identities.firstOrNull { it is CardIdentity && it.cardId == cardId }
            .toOption()
            .toEither {
                Error.UserIdentityNotFound(
                    "Not able to find identity with card id $cardId.",
                    mapOf("cardId" to cardId)
                )
            }
            .map {
                CardIdentityRemoved(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    cardId
                )
            }
    }

    fun addPhoneNrIdentity(
        actor: Admin,
        phoneNr: String
    ): UserSourcingEvent {
        // TODO phone number must be unique rule
        return PhoneNrIdentityAdded(id, aggregateVersion + 1, actor.id, phoneNr)
    }

    /**
     * Removes the user's identity given by the phone number.
     *
     * @return error if no identity with given card exists, sourcing event otherwise
     */
    fun removePhoneNrIdentity(
        actor: Admin,
        phoneNr: String
    ): Either<Error, UserSourcingEvent> {
        return identities.firstOrNull { it is PhoneNrIdentity && it.phoneNr == phoneNr }
            .toOption()
            .toEither {
                Error.UserIdentityNotFound(
                    "Not able to find identity with phone number $phoneNr.",
                    mapOf("phoneNr" to phoneNr)
                )
            }
            .map {
                PhoneNrIdentityRemoved(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    phoneNr
                )
            }
    }

    /**
     * Adds a member qualification given by its id to the user.
     *
     * @return error if the qualification cannot be found, sourcing event otherwise
     */
    fun addMemberQualification(
        actor: Admin, // TODO replace by instructor
        qualificationId: QualificationId,
        getQualificationById: GetQualificationById
    ): Either<Error, UserSourcingEvent> {
        // TODO error if user already has member qualification?
        return getQualificationById.getQualificationById(qualificationId)
            .map {
                MemberQualificationAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    qualificationId
                )
            }
    }

    /**
     * Removes the user's member qualification given by the qualification id.
     *
     * @return error if the user does not have the member qualification, sourcing event otherwise
     */
    fun removeMemberQualification(
        actor: Admin,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> {
        return memberQualifications.firstOrNull { it == qualificationId }
            .toOption()
            .toEither {
                Error.MemberQualificationNotFound(
                    "Not able to find member qualification with id $qualificationId.",
                    qualificationId
                )
            }
            .map {
                MemberQualificationRemoved(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    qualificationId
                )
            }
    }

    /**
     * Adds an instructor qualification given by its id to the user.
     *
     * @return error if the qualification cannot be found, sourcing event otherwise
     */
    fun addInstructorQualification(
        actor: Admin,
        qualificationId: QualificationId,
        getQualificationById: GetQualificationById
    ): Either<Error, UserSourcingEvent> {
        return instructorQualifications?.firstOrNull { it == qualificationId }
            .toOption()
            .map {
                Error.InstructorQualificationAlreadyFound(
                    "User $id already has instructor qualification $qualificationId.",
                    qualificationId
                )
            }
            .toEither { }
            .swap()
            .flatMap {
                getQualificationById.getQualificationById(qualificationId)
            }
            .map {
                InstructorQualificationAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    qualificationId
                )
            }
    }

    /**
     * Removes the user's instructor qualification given by the qualification id.
     *
     * @return error if the user does not have the instructor qualification, sourcing event otherwise
     */
    fun removeInstructorQualification(
        actor: Admin,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> {
        return instructorQualifications?.firstOrNull { it == qualificationId }
            .toOption()
            .toEither {
                Error.InstructorQualificationNotFound(
                    "Not able to find instructor qualification with id $qualificationId.",
                    qualificationId
                )
            }
            .map {
                InstructorQualificationRemoved(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    qualificationId
                )
            }
    }

    /**
     * Changes whether the user is admin or not.
     */
    fun changeIsAdmin(
        actor: Admin,
        isAdmin: Boolean
    ): UserSourcingEvent {
        // TODO error if new value is equal to old value? or just not return a sourcing event?
        return IsAdminChanged(
            id,
            aggregateVersion + 1,
            actor.id,
            isAdmin
        )
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

    class EventHistoryDoesNotStartWithUserCreated(message: String) : Exception(message)
}