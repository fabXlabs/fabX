package cloud.fabX.fabXaccess.tool.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.application.domainSerializersModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.ToolSourcingEvent
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

object ToolSourcingEventDAO : Table("ToolSourcingEvent") {
    val aggregateRootId = uuid("aggregate_root_id")
    val aggregateVersion = long("aggregate_version")
    val actorId = uuid("actor_id")
    val correlationId = uuid("correlation_id")
    val timestamp = timestamp("timestamp")
    val data = jsonb<ToolSourcingEvent>("data", Json { serializersModule = domainSerializersModule })
}

open class ToolDatabaseRepository(
    loggerFactory: LoggerFactory,
    private val db: Database
) : ToolRepository, GettingToolsByQualificationId {

    private val log: Logger = loggerFactory.invoke(this::class.java)

    override suspend fun getAll(): Set<Tool> {
        return transaction {
            log.debug("getting all tools from database...")

            val result = ToolSourcingEventDAO
                .selectAll()
                .orderBy(ToolSourcingEventDAO.aggregateVersion, order = SortOrder.ASC)
                .asSequence()
                .map {
                    it[ToolSourcingEventDAO.data]
                }
                .groupBy { it.aggregateRootId }
                .map { Tool.fromSourcingEvents(it.value) }
                .filter { it.isSome() }
                .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
                .toSet()

            log.debug("... done getting all tools from database")

            result
        }
    }

    override suspend fun getById(id: ToolId): Either<Error, Tool> {
        val events = transaction {
            log.debug("getting tools by id from database...")

            val result = ToolSourcingEventDAO
                .selectAll()
                .where { ToolSourcingEventDAO.aggregateRootId.eq(id.value) }
                .orderBy(ToolSourcingEventDAO.aggregateVersion)
                .map {
                    it[ToolSourcingEventDAO.data]
                }

            log.debug("...done getting tools by id from database")

            result
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

    override suspend fun store(event: ToolSourcingEvent): Option<Error> {
        return transaction {
            log.debug("storing event in database...")

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

                log.debug("...done storing event in database")
                None
            }
        }
    }

    suspend fun getSourcingEvents(): List<ToolSourcingEvent> {
        return transaction {
            log.debug("getting all sourcing events from database...")

            val result = ToolSourcingEventDAO.selectAll()
                .orderBy(ToolSourcingEventDAO.timestamp, SortOrder.ASC)
                .orderBy(ToolSourcingEventDAO.aggregateVersion, SortOrder.ASC)
                .map {
                    it[ToolSourcingEventDAO.data]
                }

            log.debug("...done getting all sourcing events from database")

            result
        }
    }

    @Suppress("UnusedReceiverParameter") // supposed to be executed within Transaction
    private fun Transaction.getVersionById(id: ToolId): Long? {
        return ToolSourcingEventDAO
            .select(ToolSourcingEventDAO.aggregateVersion)
            .where { ToolSourcingEventDAO.aggregateRootId.eq(id.value) }
            .orderBy(ToolSourcingEventDAO.aggregateVersion, order = SortOrder.DESC)
            .limit(1)
            .map { it[ToolSourcingEventDAO.aggregateVersion] }
            .maxOfOrNull { it }
    }

    private suspend fun <T> transaction(statement: Transaction.() -> T): T = withContext(Dispatchers.IO) {
        transaction(db) {
            statement()
        }
    }

    override suspend fun getToolsByQualificationId(id: QualificationId): Set<Tool> =
        getAll()
            .filter { it.requiredQualifications.contains(id) }
            .toSet()
}