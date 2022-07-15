package cloud.fabX.fabXaccess.tool.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error

interface ToolRepository {
    fun getAll(): Set<Tool>
    fun getById(id: ToolId): Either<Error, Tool>
    fun store(event: ToolSourcingEvent): Option<Error>
}