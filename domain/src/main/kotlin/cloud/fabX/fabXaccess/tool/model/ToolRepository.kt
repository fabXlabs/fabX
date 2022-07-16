package cloud.fabX.fabXaccess.tool.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.GetToolById

interface ToolRepository : GetToolById {
    fun getAll(): Set<Tool>
    fun getById(id: ToolId): Either<Error, Tool>
    fun store(event: ToolSourcingEvent): Option<Error>

    override fun getToolById(id: ToolId): Either<Error, Tool> = getById(id)
}