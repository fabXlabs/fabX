package cloud.fabX.fabXaccess.qualification.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import cloud.fabX.fabXaccess.common.jsonb
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationSourcingEvent
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

object QualificationSourcingEventDAO : Table("QualificationSourcingEvent") {
    val aggregateRootId = uuid("aggregate_root_id")
    val aggregateVersion = long("aggregate_version")
    val actorId = uuid("actor_id")
    val correlationId = uuid("correlation_id")
    val timestamp = timestamp("timestamp")
    val data = jsonb("data", QualificationSourcingEvent.serializer())
}

class QualificationDatabaseRepository(private val db: Database) : QualificationRepository {

    override fun getAll(): Set<Qualification> {
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
                .filter { it.isDefined() }
                .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
                .toSet()
        }
    }

    override fun getById(id: QualificationId): Either<Error, Qualification> {
        val events = transaction {
            QualificationSourcingEventDAO
                .select {
                    QualificationSourcingEventDAO.aggregateRootId.eq(id.value)
                }
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

    override fun store(event: QualificationSourcingEvent): Option<Error> {
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

    fun getSourcingEvents(): List<QualificationSourcingEvent> {
        return transaction {
            QualificationSourcingEventDAO.selectAll()
                // TODO order by something different (timestamp is informative, nothing to depend on)
                .orderBy(QualificationSourcingEventDAO.timestamp, SortOrder.ASC)
                .map {
                    it[QualificationSourcingEventDAO.data]
                }
        }
    }

    @Suppress("unused") // supposed to be executed within Transaction
    private fun Transaction.getVersionById(id: QualificationId): Long? {
        return QualificationSourcingEventDAO
            .slice(QualificationSourcingEventDAO.aggregateVersion)
            .select {
                QualificationSourcingEventDAO.aggregateRootId.eq(id.value)
            }
            .orderBy(QualificationSourcingEventDAO.aggregateVersion, order = SortOrder.DESC)
            .limit(1)
            .map { it[QualificationSourcingEventDAO.aggregateVersion] }
            .maxOfOrNull { it }
    }

    private fun <T> transaction(statement: Transaction.() -> T): T = transaction(db) {
        addLogger(StdOutSqlLogger)
        statement()
    }
}