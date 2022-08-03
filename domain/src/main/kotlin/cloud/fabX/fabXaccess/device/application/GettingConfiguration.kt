package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.sequenceEither
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolById
import cloud.fabX.fabXaccess.tool.model.Tool

/**
 * Service for a device to get its configuration.
 */
class GettingConfiguration(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val gettingToolById: GettingToolById
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun getConfiguration(
        actor: DeviceActor
    ): Either<Error, Result> {
        log.debug("getConfiguration...")

        return deviceRepository
            .getById(actor.deviceId)
            .flatMap {
                it.attachedTools.entries
                    .map { e ->
                        gettingToolById.getToolById(e.value)
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