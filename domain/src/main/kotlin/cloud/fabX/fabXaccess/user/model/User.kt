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
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.common.model.biFlatmap
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById

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
            correlationId: CorrelationId,
            firstName: String,
            lastName: String,
            wikiName: String,
            gettingUserByWikiName: GettingUserByWikiName
        ): Either<Error, UserSourcingEvent> {
            return requireUniqueWikiName(wikiName, gettingUserByWikiName)
                .map {
                    UserCreated(
                        DomainModule.userIdFactory().invoke(),
                        actor.id,
                        correlationId,
                        firstName,
                        lastName,
                        wikiName
                    )
                }
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

        private fun requireUniqueWikiName(
            wikiName: String,
            gettingUserByWikiName: GettingUserByWikiName
        ): Either<Error, Unit> {
            return gettingUserByWikiName.getByWikiName(wikiName)
                .swap()
                .mapLeft {
                    Error.WikiNameAlreadyInUse(
                        "Wiki name is already in use."
                    )
                }
                .flatMap {
                    if (it is Error.UserNotFoundByWikiName) {
                        Unit.right()
                    } else {
                        it.left()
                    }
                }
        }
    }

    fun apply(sourcingEvent: UserSourcingEvent): Option<User> = sourcingEvent.processBy(UserEventHandler(), Some(this))

    fun changePersonalInformation(
        actor: Admin,
        correlationId: CorrelationId,
        firstName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        lastName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        wikiName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        gettingUserByWikiName: GettingUserByWikiName
    ): Either<Error, UserSourcingEvent> {
        return wikiName.biFlatmap(
            { Unit.right() },
            { requireUniqueWikiName(it, gettingUserByWikiName) }
        ).map {
            UserPersonalInformationChanged(
                id,
                aggregateVersion + 1,
                actor.id,
                correlationId,
                firstName,
                lastName,
                wikiName
            )
        }
    }

    fun changeLockState(
        actor: Admin,
        correlationId: CorrelationId,
        locked: ChangeableValue<Boolean> = ChangeableValue.LeaveAsIs,
        notes: ChangeableValue<String?> = ChangeableValue.LeaveAsIs
    ): UserSourcingEvent {
        return UserLockStateChanged(
            id,
            aggregateVersion + 1,
            actor.id,
            correlationId,
            locked,
            notes
        )
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
        correlationId: CorrelationId,
        username: String,
        hash: String,
        gettingUserByUsername: GettingUserByUsername
    ): Either<Error, UserSourcingEvent> {
        return requireNoUsernamePasswordIdentity()
            .flatMap { requireUniqueUsername(username, gettingUserByUsername) }
            .flatMap { UsernamePasswordIdentity.fromUnvalidated(username, hash) }
            .map {
                UsernamePasswordIdentityAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    correlationId,
                    it.username,
                    it.hash
                )
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
        correlationId: CorrelationId,
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
                    correlationId,
                    username
                )
            }
    }

    fun addCardIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        cardId: String,
        cardSecret: String,
        gettingUserByCardId: GettingUserByCardId
    ): Either<Error, UserSourcingEvent> {
        return requireUniqueCardId(cardId, gettingUserByCardId)
            .flatMap { CardIdentity.fromUnvalidated(cardId, cardSecret) }
            .map {
                CardIdentityAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    correlationId,
                    it.cardId,
                    it.cardSecret
                )
            }
    }

    private fun requireUniqueCardId(
        cardId: String,
        gettingUserByCardId: GettingUserByCardId
    ): Either<Error, Unit> {
        return gettingUserByCardId.getByCardId(cardId)
            .swap()
            .mapLeft {
                Error.CardIdAlreadyInUse(
                    "Card id is already in use."
                )
            }
            .flatMap {
                if (it is Error.UserNotFoundByCardId) {
                    Unit.right()
                } else {
                    it.left()
                }
            }
    }

    /**
     * Removes the user's identity given by the card id.
     *
     * @return error if no identity with given card id exists, sourcing event otherwise
     */
    fun removeCardIdentity(
        actor: Admin,
        correlationId: CorrelationId,
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
                    correlationId,
                    cardId
                )
            }
    }

    fun addPhoneNrIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        phoneNr: String,
        gettingUserByIdentity: GettingUserByIdentity
    ): Either<Error, UserSourcingEvent> {
        return requireUniquePhoneNr(phoneNr, gettingUserByIdentity)
            .flatMap { PhoneNrIdentity.fromUnvalidated(phoneNr) }
            .map {
                PhoneNrIdentityAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    correlationId,
                    it.phoneNr
                )
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
        correlationId: CorrelationId,
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
                    correlationId,
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
        actor: Instructor,
        correlationId: CorrelationId,
        qualificationId: QualificationId,
        gettingQualificationById: GettingQualificationById
    ): Either<Error, UserSourcingEvent> {
        return requireInstructorWithQualification(actor, qualificationId)
            .flatMap {
                memberQualifications.firstOrNull { it == qualificationId }
                    .toOption()
                    .map {
                        Error.MemberQualificationAlreadyFound(
                            "User $id already has member qualification $qualificationId.",
                            qualificationId
                        )
                    }
                    .toEither { }
                    .swap()
            }
            .flatMap { gettingQualificationById.getQualificationById(qualificationId) }
            .map {
                MemberQualificationAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    correlationId,
                    qualificationId
                )
            }
    }

    private fun requireInstructorWithQualification(
        actor: Instructor,
        qualificationId: QualificationId
    ): Either<Error, Unit> {
        return Either.conditionally(
            actor.hasQualification(qualificationId),
            {
                Error.InstructorPermissionNotFound(
                    "Actor not has instructor permission for qualification $qualificationId.",
                    qualificationId
                )
            },
            {}
        )
    }

    /**
     * Removes the user's member qualification given by the qualification id.
     *
     * @return error if the user does not have the member qualification, sourcing event otherwise
     */
    fun removeMemberQualification(
        actor: Admin,
        correlationId: CorrelationId,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> =
        removeMemberQualification(actor.id, correlationId, qualificationId)

    /**
     * Removes the user's member qualification (triggered by a domain event).
     */
    internal fun removeMemberQualification(
        domainEvent: DomainEvent,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> =
        removeMemberQualification(domainEvent.actorId, domainEvent.correlationId, qualificationId)

    private fun removeMemberQualification(
        actorId: ActorId,
        correlationId: CorrelationId,
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
                    actorId,
                    correlationId,
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
        correlationId: CorrelationId,
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
                    correlationId,
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
        correlationId: CorrelationId,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> =
        removeInstructorQualification(actor.id, correlationId, qualificationId)

    /**
     * Removes the user's instructor qualification (triggered by a domain event).
     */
    internal fun removeInstructorQualification(
        domainEvent: DomainEvent,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> =
        removeInstructorQualification(domainEvent.actorId, domainEvent.correlationId, qualificationId)

    private fun removeInstructorQualification(
        actorId: ActorId,
        correlationId: CorrelationId,
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
                    actorId,
                    correlationId,
                    qualificationId
                )
            }
    }

    /**
     * Changes whether the user is admin or not.
     */
    fun changeIsAdmin(
        actor: Admin,
        correlationId: CorrelationId,
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
                correlationId,
                isAdmin
            )
        })
    }

    fun delete(
        actor: Admin,
        correlationId: CorrelationId
    ): UserSourcingEvent {
        return UserDeleted(id, aggregateVersion + 1, actor.id, correlationId)
    }

    fun hasIdentity(userIdentity: UserIdentity) = identities.contains(userIdentity)

    fun hasUsername(username: String) =
        identities.any { it is UsernamePasswordIdentity && it.username == username }

    fun hasCardId(cardId: String) =
        identities.any { it is CardIdentity && it.cardId == cardId }

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