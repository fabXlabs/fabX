package cloud.fabX.fabXaccess.tool.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.application.logError
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to handle changing tool thumbnail.
 */
class ChangingThumbnail(
    loggerFactory: LoggerFactory,
    private val toolRepository: ToolRepository
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun changeToolThumbnail(
        actor: Admin,
        correlationId: CorrelationId,
        toolId: ToolId,
        thumbnail: ByteArray
    ): Either<Error, Unit> =
        log.logError(actor, correlationId, "changeToolThumbnail") {
            toolRepository.getById(toolId)
                .flatMap { it.changeThumbnail(actor, correlationId, thumbnail) }
                .flatMap { toolRepository.storeThumbnail(toolId, actor.id, it) }
        }
}