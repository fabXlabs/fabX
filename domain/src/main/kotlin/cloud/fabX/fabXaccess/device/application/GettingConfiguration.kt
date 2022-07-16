package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.sequenceEither
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.tool.model.Tool

/**
 * Service for a device to get its configuration.
 */
class GettingConfiguration {

    private val log = logger()
    private val deviceRepository = DomainModule.deviceRepository()
    private val toolRepository = DomainModule.toolRepository()

    fun getConfiguration(
        actor: DeviceActor
    ): Either<Error, Result> {
        log.debug("getConfiguration...")

        return deviceRepository
            .getById(actor.deviceId)
            .flatMap {
                it.attachedTools.entries
                    .map { e ->
                        toolRepository.getById(e.value)
                            .map { tool -> e.key to tool }
                    }
                    .sequenceEither()
                    .map { entries -> entries.toMap() }
                    .map { pinToTool -> Result(device = it, attachedTools = pinToTool) }
            }
            .tap { log.debug("...getConfiguration successful") }
            .tapLeft { log.error("...getConfiguration error: $it") }
    }

    data class Result(
        val device: Device,
        val attachedTools: Map<Int, Tool> // pin to tool
    )
}