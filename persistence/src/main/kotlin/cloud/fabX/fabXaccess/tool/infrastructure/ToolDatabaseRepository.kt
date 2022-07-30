package cloud.fabX.fabXaccess.tool.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import cloud.fabX.fabXaccess.common.jsonb
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.ToolSourcingEvent
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ToolSourcingEventDAO : Table("ToolSourcingEvent") {
    val aggregateRootId = uuid("aggregate_root_id")
    val aggregateVersion = long("aggregate_version")
    val actorId = uuid("actor_id")
    val correlationId = uuid("correlation_id")
    val timestamp = timestamp("timestamp")
    val data = jsonb("data", ToolSourcingEvent.serializer())
}

class ToolDatabaseRepository(private val db: Database) : ToolRepository, GettingToolsByQualificationId {

    override fun getAll(): Set<Tool> {
        return transaction {
            ToolSourcingEventDAO
                .selectAll()
                .orderBy(ToolSourcingEventDAO.aggregateVersion, order = SortOrder.ASC)
                .asSequence()
                .map {
                    it[ToolSourcingEventDAO.data]
                }
                .groupBy { it.aggregateRootId }
                .map { Tool.fromSourcingEvents(it.value) }
                .filter { it.isDefined() }
                .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
                .toSet()
        }
    }

    override fun getById(id: ToolId): Either<Error, Tool> {
        val events = transaction {
            ToolSourcingEventDAO
                .select {
                    ToolSourcingEventDAO.aggregateRootId.eq(id.value)
                }
                .orderBy(ToolSourcingEventDAO.aggregateVersion)
                .map {
                    it[ToolSourcingEventDAO.data]
                }
        }

        return if (events.isNotEmpty()) {
            Tool.fromSourcingEvents(events)
                .toEither {
                    Error.ToolNotFound(
                        "Tool with id $id not found.",
                        id
                    )
                }
        } else {
            Error.ToolNotFound(
                "Tool with id $id not found.",
                id
            ).left()
        }
    }

    override fun store(event: ToolSourcingEvent): Option<Error> {
        return transaction {
            val previousVersion = getVersionById(event.aggregateRootId)

            if (previousVersion != null
                && event.aggregateVersion != previousVersion + 1
            ) {
                Some(
                    Error.VersionConflict(
                        "Previous version of tool ${event.aggregateRootId} is $previousVersion, " +
                                "desired new version is ${event.aggregateVersion}."
                    )
                )
            } else {
                ToolSourcingEventDAO.insert {
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

    fun getSourcingEvents(): List<ToolSourcingEvent> {
        return transaction {
            ToolSourcingEventDAO.selectAll()
                .orderBy(ToolSourcingEventDAO.timestamp, SortOrder.ASC)
                .orderBy(ToolSourcingEventDAO.aggregateVersion, SortOrder.ASC)
                .map {
                    it[ToolSourcingEventDAO.data]
                }
        }
    }

    @Suppress("unused") // supposed to be executed within Transaction
    private fun Transaction.getVersionById(id: ToolId): Long? {
        return ToolSourcingEventDAO
            .slice(ToolSourcingEventDAO.aggregateVersion)
            .select {
                ToolSourcingEventDAO.aggregateRootId.eq(id.value)
            }
            .orderBy(ToolSourcingEventDAO.aggregateVersion, order = SortOrder.DESC)
            .limit(1)
            .map { it[ToolSourcingEventDAO.aggregateVersion] }
            .maxOfOrNull { it }
    }

    private fun <T> transaction(statement: Transaction.() -> T): T = transaction(db) {
        statement()
    }

    override fun getToolsByQualificationId(id: QualificationId): Set<Tool> =
        getAll()
            .filter { it.requiredQualifications.contains(id) }
            .toSet()
}