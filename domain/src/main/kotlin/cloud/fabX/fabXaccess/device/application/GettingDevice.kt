package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to get devices.
 */
class GettingDevice(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun getAll(
        actor: Admin,
        correlationId: CorrelationId
    ): Set<Device> {
        log.debug("getAll (actor: $actor, correlationId: $correlationId)...")

        return deviceRepository.getAll()
    }

    suspend fun getAll(
        actor: SystemActor,
        correlationId: CorrelationId
    ): Set<Device> {
        // no logging as it is used by metrics endpoint
        return deviceRepository.getAll()
    }

    suspend fun getById(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId
    ): Either<Error, Device> {
        log.debug("getById (actor: $actor, correlationId: $correlationId)...")

        return deviceRepository.getById(deviceId)
    }

    suspend fun getMe(
        actor: DeviceActor,
        correlationId: CorrelationId
    ): Either<Error, Device> {
        log.debug("getMe (actor: $actor, correlationId: $correlationId)...")

        return deviceRepository.getById(actor.deviceId)
    }
}