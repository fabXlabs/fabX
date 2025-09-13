package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import arrow.core.right
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.TaggedCounter
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolById
import cloud.fabX.fabXaccess.tool.model.Tool
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service for a device to get its configuration.
 *
 * As side effect, the device's actual firmware version is updated (in the backend).
 */
@OptIn(ExperimentalTime::class)
class GettingConfiguration(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val gettingToolById: GettingToolById,
    private val gettingConfigurationCounter: TaggedCounter<DeviceId>,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun getConfiguration(
        actor: DeviceActor,
        correlationId: CorrelationId,
        actualFirmwareVersion: String
    ): Either<Error, Result> =
        log.logError(actor, correlationId, "getConfiguration") {
            deviceRepository
                .getById(actor.deviceId)
                .flatMap {
                    updateActualFirmwareVersionIfRequired(
                        actor,
                        correlationId,
                        it,
                        actualFirmwareVersion
                    )
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
                .onRight {
                    gettingConfigurationCounter.increment(it.device.id, {
                        mapOf(
                            "deviceId" to it.device.id.serialize(),
                            "deviceName" to it.device.name
                        )
                    })
                }
        }

    private suspend fun updateActualFirmwareVersionIfRequired(
        actor: DeviceActor,
        correlationId: CorrelationId,
        device: Device,
        actualFirmwareVersion: String
    ): Either<Error, Device> =
        if (device.actualFirmwareVersion != actualFirmwareVersion) {
            device.setActualFirmwareVersion(
                actor,
                clock,
                correlationId,
                actualFirmwareVersion
            ).flatMap { sourcingEvent ->
                deviceRepository.store(sourcingEvent)
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

    data class Result(
        val device: Device,
        val attachedTools: Map<Int, Tool> // pin to tool
    )
}