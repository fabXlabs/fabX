package cloud.fabX.fabXaccess.user.infrastructure

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.application.domainSerializersModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.GettingSoftDeletedUsers
import cloud.fabX.fabXaccess.user.model.GettingUserByCardId
import cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.GettingUserByUsername
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.GettingUsersByInstructorQualification
import cloud.fabX.fabXaccess.user.model.GettingUsersByMemberQualification
import cloud.fabX.fabXaccess.user.model.HardDeletingUser
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserDeleted
import cloud.fabX.fabXaccess.user.model.UserIdentity
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object UserSourcingEventDAO : Table("UserSourcingEvent") {
    val aggregateRootId = uuid("aggregate_root_id")
    val aggregateVersion = long("aggregate_version")
    val actorId = uuid("actor_id")
    val correlationId = uuid("correlation_id")
    val timestamp = timestamp("timestamp")
    val data = jsonb<UserSourcingEvent>("data", Json { serializersModule = domainSerializersModule })
}

open class UserDatabaseRepository(private val db: Database) :
    UserRepository,
    GettingUserByIdentity,
    GettingUserByUsername,
    GettingUserByCardId,
    GettingUserByWikiName,
    GettingUsersByMemberQualification,
    GettingUsersByInstructorQualification,
    GettingSoftDeletedUsers,
    HardDeletingUser {

    override suspend fun getAll(): Set<User> {
        return transaction {
            UserSourcingEventDAO
                .selectAll()
                .orderBy(UserSourcingEventDAO.aggregateVersion, order = SortOrder.ASC)
                .asSequence()
                .map {
                    it[UserSourcingEventDAO.data]
                }
                .groupBy { it.aggregateRootId }
                .map { User.fromSourcingEvents(it.value) }
                .filter { it.isSome() }
                .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
                .toSet()
        }
    }

    override suspend fun getById(id: UserId): Either<Error, User> =
        getSourcingEventsById(id)
            .flatMap {
                User.fromSourcingEvents(it)
                    .toEither {
                        Error.UserNotFound(
                            "User with id $id not found.",
                            id
                        )
                    }
            }

    override suspend fun store(event: UserSourcingEvent): Either<Error, Unit> {
        return transaction {
            val previousVersion = getVersionById(event.aggregateRootId)

            if (previousVersion != null
                && event.aggregateVersion != previousVersion + 1
            ) {
                Error.VersionConflict(
                    "Previous version of user ${event.aggregateRootId} is $previousVersion, " +
                            "desired new version is ${event.aggregateVersion}."
                ).left()
            } else {
                UserSourcingEventDAO.insert {
                    it[aggregateRootId] = event.aggregateRootId.value
                    it[aggregateVersion] = event.aggregateVersion
                    it[actorId] = event.actorId.value
                    it[correlationId] = event.correlationId.id
                    it[timestamp] = event.timestamp.toJavaInstant()
                    it[data] = event
                }

                Unit.right()
            }
        }
    }

    override suspend fun getSourcingEvents(): List<UserSourcingEvent> {
        return transaction {
            UserSourcingEventDAO.selectAll()
                .orderBy(UserSourcingEventDAO.timestamp, SortOrder.ASC)
                .orderBy(UserSourcingEventDAO.aggregateVersion, SortOrder.ASC)
                .map {
                    it[UserSourcingEventDAO.data]
                }
        }
    }

    override suspend fun getSourcingEventsById(id: UserId): Either<Error, List<UserSourcingEvent>> {
        val events = transaction {
            UserSourcingEventDAO
                .selectAll()
                .where { UserSourcingEventDAO.aggregateRootId.eq(id.value) }
                .orderBy(UserSourcingEventDAO.aggregateVersion)
                .map {
                    it[UserSourcingEventDAO.data]
                }
        }

        return if (events.isNotEmpty()) {
            events.right()
        } else {
            Error.UserNotFound(
                "User with id $id not found.",
                id
            ).left()
        }
    }

    @Suppress("UnusedReceiverParameter") // supposed to be executed within Transaction
    private fun Transaction.getVersionById(id: UserId): Long? {
        return UserSourcingEventDAO
            .select(UserSourcingEventDAO.aggregateVersion)
            .where { UserSourcingEventDAO.aggregateRootId.eq(id.value) }
            .orderBy(UserSourcingEventDAO.aggregateVersion, order = SortOrder.DESC)
            .limit(1)
            .map { it[UserSourcingEventDAO.aggregateVersion] }
            .maxOfOrNull { it }
    }

    override suspend fun getByIdentity(identity: UserIdentity): Either<Error, User> =
        getAll()
            .firstOrNull { it.hasIdentity(identity) }
            .toOption()
            .toEither { Error.UserNotFoundByIdentity("Not able to find user for given identity.") }

    override suspend fun getByUsername(username: String): Either<Error, User> =
        getAll()
            .firstOrNull { it.hasUsername(username) }
            .toOption()
            .toEither { Error.UserNotFoundByUsername("Not able to find user for given username.") }

    override suspend fun getByCardId(cardId: String): Either<Error, User> =
        getAll()
            .firstOrNull { it.hasCardId(cardId) }
            .toOption()
            .toEither { Error.UserNotFoundByCardId("Not able to find user for given card id.") }

    override suspend fun getByWikiName(wikiName: String): Either<Error, User> =
        getAll()
            .firstOrNull { it.wikiName == wikiName }
            .toOption()
            .toEither { Error.UserNotFoundByWikiName("Not able to find user for given wiki name.") }

    override suspend fun getByMemberQualification(qualificationId: QualificationId): Set<User> =
        getAll()
            .filter { it.asMember().hasQualification(qualificationId) }
            .toSet()

    override suspend fun getByInstructorQualification(qualificationId: QualificationId): Set<User> =
        getAll()
            .filter {
                it.asInstructor()
                    .fold({ false }, { instructor -> instructor.hasQualification(qualificationId) })
            }
            .toSet()

    override suspend fun getSoftDeleted(): Set<User> {
        return transaction {
            val deletedUserIds = UserSourcingEventDAO
                .selectAll()
                .asSequence()
                .map {
                    it[UserSourcingEventDAO.data]
                }
                .filter { it is UserDeleted }
                .map { it.aggregateRootId }
                .map { it.value }
                .toSet()

            UserSourcingEventDAO
                .selectAll()
                .where { UserSourcingEventDAO.aggregateRootId inList deletedUserIds }
                .orderBy(UserSourcingEventDAO.aggregateVersion, order = SortOrder.ASC)
                .asSequence()
                .map {
                    it[UserSourcingEventDAO.data]
                }
                .groupBy { it.aggregateRootId }
                // drop last event (= deleted event) to get the user in its final state
                .mapValues { e -> e.value.dropLast(1) }
                .map { User.fromSourcingEvents(it.value) }
                .filter { it.isSome() }
                .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
                .toSet()
        }
    }

    override suspend fun hardDelete(id: UserId): Either<Error, Int> {
        val deletedRows = transaction {
            UserSourcingEventDAO.deleteWhere {
                aggregateRootId eq id.value
            }
        }
        return if (deletedRows <= 0) {
            Error.UserNotFound(
                "User with id $id not found.",
                id
            ).left()
        } else {
            deletedRows.right()
        }
    }

    private suspend fun <T> transaction(statement: Transaction.() -> T): T = withContext(Dispatchers.IO) {
        transaction(db) {
            statement()
        }
    }
}