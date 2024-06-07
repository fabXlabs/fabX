package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolById
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

/**
 * Service for attaching a tool to a device.
 */
class AttachingTool(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val gettingToolById: GettingToolById,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun attachTool(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        pin: Int,
        toolId: ToolId
    ): Either<Error, Unit> =
        deviceRepository.getAndStoreFlatMap(deviceId, actor, correlationId, log, "attachTool") {
            it.attachTool(
                actor,
                clock,
                correlationId,
                pin,
                toolId,
                gettingToolById
            )
        }
}