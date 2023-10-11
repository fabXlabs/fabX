package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import arrow.core.right
import arrow.core.sequence
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolById
import cloud.fabX.fabXaccess.tool.model.Tool
import kotlinx.datetime.Clock

/**
 * Service for a device to get its configuration.
 *
 * As side effect, the device's actual firmware version is updated (in the backend).
 */
class GettingConfiguration(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val gettingToolById: GettingToolById,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun getConfiguration(
        actor: DeviceActor,
        correlationId: CorrelationId,
        actualFirmwareVersion: String
    ): Either<Error, Result> {
        log.debug("getConfiguration...")

        return deviceRepository
            .getById(actor.deviceId)
            .flatMap { device ->
                if (device.actualFirmwareVersion != actualFirmwareVersion) {
                    device.setActualFirmwareVersion(
                        actor,
                        clock,
                        correlationId,
                        actualFirmwareVersion
                    ).flatMap { sourcingEvent ->
                        deviceRepository.store(sourcingEvent)
                            .toEither {}
                            .swap()
                            .flatMap {
                                device.apply(sourcingEvent)
                                    .toEither {
                                        Error.DeviceNotFound(
                                            "Device with id ${device.id} not found after applying event.",
                                            device.id
                                        )
                                    }
                            }
                    }
                } else {
                    device.right()
                }
            }
            .flatMap {
                it.attachedTools.entries
                    .map { e ->
                        gettingToolById.getToolById(e.value)
                            .map { tool -> e.key to tool }
                    }
                    .let { eithers -> either { eithers.bindAll() } }
                    .map { entries -> entries.toMap() }
                    .map { pinToTool -> Result(device = it, attachedTools = pinToTool) }
            }
            .onRight { log.debug("...getConfiguration successful") }
            .onLeft { log.error("...getConfiguration error: $it") }
    }

    data class Result(
        val device: Device,
        val attachedTools: Map<Int, Tool> // pin to tool
    )
}