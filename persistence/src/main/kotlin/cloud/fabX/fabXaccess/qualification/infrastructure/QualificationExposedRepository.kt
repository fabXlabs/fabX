package cloud.fabX.fabXaccess.qualification.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.left
import cloud.fabX.fabXaccess.common.jsonb
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationSourcingEvent
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.deleteAll
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

class QualificationExposedRepository(private val db: Database) : QualificationRepository {

    init {
        transaction {
            // TODO database migration tool
            SchemaUtils.createMissingTablesAndColumns(QualificationSourcingEventDAO)

            // TODO remove following line
            QualificationSourcingEventDAO.deleteAll()
        }
    }

    private fun <T> transaction(statement: Transaction.() -> T): T = transaction(db) {
        addLogger(StdOutSqlLogger)
        statement()
    }

    override fun getAll(): Set<Qualification> {
        TODO("Not yet implemented")
    }

    override fun getById(id: QualificationId): Either<Error, Qualification> {
        val events = transaction {
            QualificationSourcingEventDAO.select {
                QualificationSourcingEventDAO.aggregateRootId.eq(id.value)
            }.sortedBy {
                QualificationSourcingEventDAO.aggregateVersion
            }.map {
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

    fun getSourcingEvents(): List<QualificationSourcingEvent> {
        return transaction {
            addLogger(StdOutSqlLogger)

            QualificationSourcingEventDAO.selectAll().map {
                it[QualificationSourcingEventDAO.data]
            }
        }
    }

    override fun store(event: QualificationSourcingEvent): Option<Error> {
        return transaction {
            addLogger(StdOutSqlLogger)

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