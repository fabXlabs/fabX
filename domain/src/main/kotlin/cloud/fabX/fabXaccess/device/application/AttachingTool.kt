package cloud.fabX.fabXaccess.device.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolById
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service for attaching a tool to a device.
 */
class AttachingTool(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val gettingToolById: GettingToolById
) {
    private val log = loggerFactory.invoke(this::class.java)

    fun attachTool(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        pin: Int,
        toolId: ToolId
    ): Option<Error> {
        log.debug("attachTool...")

        return deviceRepository.getById(deviceId)
            .flatMap {
                it.attachTool(
                    actor,
                    correlationId,
                    pin,
                    toolId,
                    gettingToolById
                )
            }
            .flatMap {
                deviceRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...attachTool done") }
            .tap { log.error("...attachTool error: $it") }
    }
}