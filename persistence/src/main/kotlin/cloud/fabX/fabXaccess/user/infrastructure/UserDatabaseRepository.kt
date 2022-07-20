package cloud.fabX.fabXaccess.user.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.user.model.GettingUserByCardId
import cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.GettingUserByUsername
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.GettingUsersByInstructorQualification
import cloud.fabX.fabXaccess.user.model.GettingUsersByMemberQualification
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserId
import cloud.fabX.fabXaccess.user.model.UserIdentity
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent

class UserDatabaseRepository :
    UserRepository,
    GettingUserByIdentity,
    GettingUserByUsername,
    GettingUserByCardId,
    GettingUserByWikiName,
    GettingUsersByMemberQualification,
    GettingUsersByInstructorQualification {
    private var events = mutableListOf<UserSourcingEvent>()

    override fun getAll(): Set<User> {
        return events
            .sortedBy { it.aggregateVersion }
            .groupBy { it.aggregateRootId }
            .map { User.fromSourcingEvents(it.value) }
            .filter { it.isDefined() }
            .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
            .toSet()
    }

    override fun getById(id: UserId): Either<Error, User> {
        val e = events
            .filter { it.aggregateRootId == id }
            .sortedBy { it.aggregateVersion }

        return if (e.isNotEmpty()) {
            User.fromSourcingEvents(e)
                .toEither {
                    Error.UserNotFound(
                        "User with id $id not found.",
                        id
                    )
                }
        } else {
            Error.UserNotFound(
                "User with id $id not found.",
                id
            ).left()
        }
    }

    override fun store(event: UserSourcingEvent): Option<Error> {
        val previousVersion = getVersionById(event.aggregateRootId)

        return if (previousVersion != null
            && event.aggregateVersion != previousVersion + 1
        ) {
            Some(
                Error.VersionConflict(
                    "Previous version of user ${event.aggregateRootId} is $previousVersion, " +
                            "desired new version is ${event.aggregateVersion}."
                )
            )
        } else {
            events.add(event)
            None
        }
    }

    private fun getVersionById(id: UserId): Long? {
        return events
            .filter { it.aggregateRootId == id }
            .maxOfOrNull { it.aggregateVersion }
    }

    override fun getByIdentity(identity: UserIdentity): Either<Error, User> =
        getAll()
            .firstOrNull { it.hasIdentity(identity) }
            .toOption()
            .toEither { Error.UserNotFoundByIdentity("Not able to find user for given identity.") }

    override fun getByUsername(username: String): Either<Error, User> =
        getAll()
            .firstOrNull { it.hasUsername(username) }
            .toOption()
            .toEither { Error.UserNotFoundByUsername("Not able to find user for given username.") }

    override fun getByCardId(cardId: String): Either<Error, User> =
        getAll()
            .firstOrNull { it.hasCardId(cardId) }
            .toOption()
            .toEither { Error.UserNotFoundByCardId("Not able to find user for given card id.") }

    override fun getByWikiName(wikiName: String): Either<Error, User> =
        getAll()
            .firstOrNull { it.wikiName == wikiName }
            .toOption()
            .toEither { Error.UserNotFoundByWikiName("Not able to find user for given wiki name.") }

    override fun getByMemberQualification(qualificationId: QualificationId): Set<User> =
        getAll()
            .filter { it.asMember().hasQualification(qualificationId) }
            .toSet()

    override fun getByInstructorQualification(qualificationId: QualificationId): Set<User> {
        return getAll()
            .filter {
                it.asInstructor()
                    .fold({ false }, { instructor -> instructor.hasQualification(qualificationId) })
            }
            .toSet()
    }
}