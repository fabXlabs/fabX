package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

/**
 * Service for detaching a tool from a device.
 */
class DetachingTool(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun detachTool(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        pin: Int
    ): Either<Error, Unit> =
        deviceRepository.getAndStoreFlatMap(deviceId, actor, correlationId, log, "detachTool") {
            it.detachTool(actor, clock, correlationId, pin)
        }

    internal suspend fun detachTool(
        domainEvent: DomainEvent,
        deviceId: DeviceId,
        pin: Int
    ): Either<Error, Unit> =
        deviceRepository.getAndStoreFlatMap(deviceId, SystemActor, domainEvent.correlationId, log, "detachTool") {
            it.detachTool(domainEvent, clock, pin)
        }
}