package cloud.fabX.fabXaccess.common.model

import arrow.core.Either
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolId

fun interface GetToolById {
    fun getToolById(id: ToolId): Either<Error, Tool>
}