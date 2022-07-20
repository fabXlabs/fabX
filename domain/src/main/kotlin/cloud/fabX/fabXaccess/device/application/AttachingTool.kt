package cloud.fabX.fabXaccess.device.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceId
import cloud.fabX.fabXaccess.tool.model.ToolId
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service for attaching a tool to a device.
 */
class AttachingTool {

    private val log = logger()
    private val deviceRepository = DomainModule.deviceRepository()
    private val toolRepository = DomainModule.toolRepository()

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
                    toolRepository
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