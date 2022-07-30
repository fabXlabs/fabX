package cloud.fabX.fabXaccess.tool.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.ToolId

interface ToolRepository : GettingToolById {
    suspend fun getAll(): Set<Tool>
    suspend fun getById(id: ToolId): Either<Error, Tool>
    suspend fun store(event: ToolSourcingEvent): Option<Error>

    override suspend fun getToolById(id: ToolId): Either<Error, Tool> = getById(id)
}

fun interface GettingToolById {
    suspend fun getToolById(id: ToolId): Either<Error, Tool>
}

fun interface GettingToolsByQualificationId {
    /**
     * Returns all tools which require the qualification (potentially among other qualifications)
     * given by its id. Returns an empty set if no tools require the qualification.
     *
     * @return (potentially empty) set of tools which require the qualification
     */
    suspend fun getToolsByQualificationId(id: QualificationId): Set<Tool>
}