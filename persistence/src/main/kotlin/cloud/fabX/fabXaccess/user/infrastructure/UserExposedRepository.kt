package cloud.fabX.fabXaccess.user.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import cloud.fabX.fabXaccess.common.jsonb
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object UserSourcingEventDAO : Table("UserSourcingEvent") {
    val aggregateRootId = uuid("aggregate_root_id")
    val aggregateVersion = long("aggregate_version")
    val actorId = uuid("actor_id")
    val correlationId = uuid("correlation_id")
    val timestamp = timestamp("timestamp")
    val data = jsonb("data", UserSourcingEvent.serializer())
}

class UserExposedRepository(private val db: Database) : UserRepository {
    override fun getAll(): Set<User> {
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
                .filter { it.isDefined() }
                .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
                .toSet()
        }
    }

    override fun getById(id: UserId): Either<Error, User> {
        val events = transaction {
            UserSourcingEventDAO
                .select {
                    UserSourcingEventDAO.aggregateRootId.eq(id.value)
                }
                .orderBy(UserSourcingEventDAO.aggregateVersion)
                .map {
                    it[UserSourcingEventDAO.data]
                }
        }

        return if (events.isNotEmpty()) {
            User.fromSourcingEvents(events)
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
        return transaction {
            val previousVersion = getVersionById(event.aggregateRootId)

            if (previousVersion != null
                && event.aggregateVersion != previousVersion + 1
            ) {
                Some(
                    Error.VersionConflict(
                        "Previous version of user ${event.aggregateRootId} is $previousVersion, " +
                                "desired new version is ${event.aggregateVersion}."
                    )
                )
            } else {
                UserSourcingEventDAO.insert {
                    it[aggregateRootId] = event.aggregateRootId.value
                    it[aggregateVersion] = event.aggregateVersion
                    it[actorId] = event.actorId.value
                    it[correlationId] = event.correlationId.id
                    it[timestamp] = event.timestamp.toJavaInstant()
                    it[data] = event
                }

                None
            }
        }
    }

    fun getSourcingEvents(): List<UserSourcingEvent> {
        return transaction {
            UserSourcingEventDAO.selectAll()
                // TODO order by something different (timestamp is informative, nothing to depend on)
                .orderBy(UserSourcingEventDAO.timestamp, SortOrder.ASC)
                .map {
                    it[UserSourcingEventDAO.data]
                }
        }
    }

    @Suppress("unused") // supposed to be executed within Transaction
    private fun Transaction.getVersionById(id: UserId): Long? {
        return UserSourcingEventDAO
            .slice(UserSourcingEventDAO.aggregateVersion)
            .select {
                UserSourcingEventDAO.aggregateRootId.eq(id.value)
            }
            .orderBy(UserSourcingEventDAO.aggregateVersion, order = SortOrder.DESC)
            .limit(1)
            .map { it[UserSourcingEventDAO.aggregateVersion] }
            .maxOfOrNull { it }
    }

    private fun <T> transaction(statement: Transaction.() -> T): T = transaction(db) {
        addLogger(StdOutSqlLogger)
        statement()
    }
}