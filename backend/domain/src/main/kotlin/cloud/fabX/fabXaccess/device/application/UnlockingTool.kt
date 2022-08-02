package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.UnlockToolAtDevice
import cloud.fabX.fabXaccess.tool.model.GettingToolById
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to programmatically unlock a tool.
 */
class UnlockingTool(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val toolRepository: GettingToolById,
    private val unlockingToolAtDevice: UnlockToolAtDevice
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun unlockTool(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        toolId: ToolId
    ): Either<Error, Unit> {
        log.debug("unlockTool (actor: $actor, correlationId: $correlationId)...")

        return deviceRepository.getById(deviceId)
            .flatMap { requireToolAttachedToDevice(it, toolId, correlationId) }
            .flatMap { toolRepository.getToolById(toolId) }
            .flatMap { requireToolTypeUnlock(it, correlationId) }
            .flatMap { unlockingToolAtDevice.unlockTool(deviceId, toolId, correlationId) }
            .tap { log.debug("...unlockTool done") }
            .tapLeft { log.error("...unlockTool error: $it") }
    }

    private fun requireToolAttachedToDevice(
        device: Device,
        toolId: ToolId,
        correlationId: CorrelationId
    ): Either<Error, Unit> =
        Either.conditionally(device.hasAttachedTool(toolId),
            {
                Error.ToolNotAttachedToDevice(
                    "Tool $toolId not attached to device ${device.id}.",
                    device.id,
                    toolId,
                    correlationId
                )
            },
            {}
        )

    private fun requireToolTypeUnlock(
        tool: Tool,
        correlationId: CorrelationId
    ): Either<Error, Unit> =
        Either.conditionally(tool.type == ToolType.UNLOCK, {
            Error.ToolTypeNotUnlock(
                "Cannot remotely unlock: Tool type is not unlock.",
                tool.id,
                tool.type,
                correlationId
            )
        }, {})
}