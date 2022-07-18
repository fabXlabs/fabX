package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
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

    /**
     * Adds a username password identity to the user.
     *
     * Returns an error if the user already has a username password identity.
     * Returns an error if the username is already in use (by another user).
     *
     * @return error or sourcing event
     */
    fun addUsernamePasswordIdentity(
        actor: Admin,
        username: String,
        hash: String,
        gettingUserByUsername: GettingUserByUsername
    ): Either<Error, UserSourcingEvent> {
        return requireNoUsernamePasswordIdentity()
            .flatMap { requireUniqueUsername(username, gettingUserByUsername) }
            .map {
                UsernamePasswordIdentityAdded(id, aggregateVersion + 1, actor.id, username, hash)
            }
    }

    private fun requireNoUsernamePasswordIdentity(): Either<Error, Unit> {
        return identities.firstOrNull { it is UsernamePasswordIdentity }
            .toOption()
            .map {
                Error.UsernamePasswordIdentityAlreadyFound(
                    "User already has a username password identity."
                )
            }
            .toEither { }
            .swap()
    }

    private fun requireUniqueUsername(
        username: String,
        gettingUserByUsername: GettingUserByUsername
    ): Either<Error, Unit> {
        return gettingUserByUsername.getByUsername(username)
            .swap()
            .mapLeft {
                Error.UsernameAlreadyInUse(
                    "Username is already in use."
                )
            }
            .flatMap {
                if (it is Error.UserNotFoundByUsername) {
                    Unit.right()
                } else {
                    it.left()
                }
            }
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
        phoneNr: String,
        gettingUserByIdentity: GettingUserByIdentity
    ): Either<Error, UserSourcingEvent> {
        return requireUniquePhoneNr(phoneNr, gettingUserByIdentity)
            .map {
                PhoneNrIdentityAdded(id, aggregateVersion + 1, actor.id, phoneNr)
            }
    }

    private fun requireUniquePhoneNr(
        phoneNr: String,
        gettingUserByIdentity: GettingUserByIdentity
    ): Either<Error, Unit> {
        return gettingUserByIdentity.getByIdentity(PhoneNrIdentity(phoneNr))
            .swap()
            .mapLeft {
                Error.PhoneNrAlreadyInUse(
                    "Phone number is already in use."
                )
            }
            .flatMap {
                if (it is Error.UserNotFoundByIdentity) {
                    Unit.right()
                } else {
                    it.left()
                }
            }
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
        gettingQualificationById: GettingQualificationById
    ): Either<Error, UserSourcingEvent> {
        return memberQualifications.firstOrNull { it == qualificationId }
            .toOption()
            .map {
                Error.MemberQualificationAlreadyFound(
                    "User $id already has member qualification $qualificationId.",
                    qualificationId
                )
            }
            .toEither { }
            .swap()
            .flatMap { gettingQualificationById.getQualificationById(qualificationId) }
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
        gettingQualificationById: GettingQualificationById
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
                gettingQualificationById.getQualificationById(qualificationId)
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
    ): Either<Error, UserSourcingEvent> {
        return Either.conditionally(this.isAdmin != isAdmin, {
            if (isAdmin) {
                Error.UserAlreadyAdmin("User already is admin.")
            } else {
                Error.UserAlreadyNotAdmin("User already not is admin.")
            }
        }, {
            IsAdminChanged(
                id,
                aggregateVersion + 1,
                actor.id,
                isAdmin
            )
        })
    }

    fun delete(
        actor: Admin
    ): UserSourcingEvent {
        return UserDeleted(id, aggregateVersion + 1, actor.id)
    }

    fun hasIdentity(userIdentity: UserIdentity) = identities.contains(userIdentity)

    fun hasUsername(username: String) =
        identities.any { it is UsernamePasswordIdentity && it.username == username }

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