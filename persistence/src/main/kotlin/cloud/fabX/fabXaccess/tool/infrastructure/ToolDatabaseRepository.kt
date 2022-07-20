package cloud.fabX.fabXaccess.tool.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.ToolSourcingEvent

class ToolDatabaseRepository : ToolRepository, GettingToolsByQualificationId {
    private var events = mutableListOf<ToolSourcingEvent>()

    override fun getAll(): Set<Tool> {
        return events
            .sortedBy { it.aggregateVersion }
            .groupBy { it.aggregateRootId }
            .map { Tool.fromSourcingEvents(it.value) }
            .filter { it.isDefined() }
            .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
            .toSet()
    }

    override fun getById(id: ToolId): Either<Error, Tool> {
        val e = events
            .filter { it.aggregateRootId == id }
            .sortedBy { it.aggregateVersion }

        return if (e.isNotEmpty()) {
            Tool.fromSourcingEvents(e)
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
        val previousVersion = getVersionById(event.aggregateRootId)

        return if (previousVersion != null
            && event.aggregateVersion != previousVersion + 1
        ) {
            Some(
                Error.VersionConflict(
                    "Previous version of tool ${event.aggregateRootId} is $previousVersion, " +
                            "desired new version is ${event.aggregateVersion}."
                )
            )
        } else {
            events.add(event)
            None
        }
    }

    private fun getVersionById(id: ToolId): Long? {
        return events
            .filter { it.aggregateRootId == id }
            .maxOfOrNull { it.aggregateVersion }
    }

    override fun getToolsByQualificationId(id: QualificationId): Set<Tool> =
        getAll()
            .filter { it.requiredQualifications.contains(id) }
            .toSet()
}