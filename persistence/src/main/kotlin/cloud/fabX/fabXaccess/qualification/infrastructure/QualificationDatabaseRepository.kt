package cloud.fabX.fabXaccess.qualification.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import cloud.fabX.fabXaccess.common.application.domainSerializersModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationSourcingEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object QualificationSourcingEventDAO : Table("QualificationSourcingEvent") {
    val aggregateRootId = uuid("aggregate_root_id")
    val aggregateVersion = long("aggregate_version")
    val actorId = uuid("actor_id")
    val correlationId = uuid("correlation_id")
    val timestamp = timestamp("timestamp")
    val data = jsonb<QualificationSourcingEvent>("data", Json { serializersModule = domainSerializersModule })
}

open class QualificationDatabaseRepository(private val db: Database) : QualificationRepository {

    override suspend fun getAll(): Set<Qualification> {
        return transaction {
            QualificationSourcingEventDAO
                .selectAll()
                .orderBy(QualificationSourcingEventDAO.aggregateVersion, order = SortOrder.ASC)
                .asSequence()
                .map {
                    it[QualificationSourcingEventDAO.data]
                }
                .groupBy { it.aggregateRootId }
                .map { Qualification.fromSourcingEvents(it.value) }
                .filter { it.isSome() }
                .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
                .toSet()
        }
    }

    override suspend fun getById(id: QualificationId): Either<Error, Qualification> {
        val events = transaction {
            QualificationSourcingEventDAO
                .selectAll()
                .where { QualificationSourcingEventDAO.aggregateRootId.eq(id.value) }
                .orderBy(QualificationSourcingEventDAO.aggregateVersion)
                .map {
                    it[QualificationSourcingEventDAO.data]
                }
        }

        return if (events.isNotEmpty()) {
            Qualification.fromSourcingEvents(events)
                .toEither {
                    Error.QualificationNotFound(
                        "Qualification with id $id not found.",
                        id
                    )
                }
        } else {
            Error.QualificationNotFound(
                "Qualification with id $id not found.",
                id
            ).left()
        }
    }

    override suspend fun store(event: QualificationSourcingEvent): Option<Error> {
        return transaction {
            val previousVersion = getVersionById(event.aggregateRootId)

            if (previousVersion != null
                && event.aggregateVersion != previousVersion + 1
            ) {
                Some(
                    Error.VersionConflict(
                        "Previous version of qualification ${event.aggregateRootId} is $previousVersion, " +
                                "desired new version is ${event.aggregateVersion}."
                    )
                )
            } else {
                QualificationSourcingEventDAO.insert {
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

    suspend fun getSourcingEvents(): List<QualificationSourcingEvent> {
        return transaction {
            QualificationSourcingEventDAO.selectAll()
                .orderBy(QualificationSourcingEventDAO.timestamp, SortOrder.ASC)
                .orderBy(QualificationSourcingEventDAO.aggregateVersion, SortOrder.ASC)
                .map {
                    it[QualificationSourcingEventDAO.data]
                }
        }
    }

    @Suppress("UnusedReceiverParameter") // supposed to be executed within Transaction
    private fun Transaction.getVersionById(id: QualificationId): Long? {
        return QualificationSourcingEventDAO
            .select(QualificationSourcingEventDAO.aggregateVersion)
            .where { QualificationSourcingEventDAO.aggregateRootId.eq(id.value) }
            .orderBy(QualificationSourcingEventDAO.aggregateVersion, order = SortOrder.DESC)
            .limit(1)
            .map { it[QualificationSourcingEventDAO.aggregateVersion] }
            .maxOfOrNull { it }
    }

    private suspend fun <T> transaction(statement: Transaction.() -> T): T = withContext(Dispatchers.IO) {
        transaction(db) {
            statement()
        }
    }
}