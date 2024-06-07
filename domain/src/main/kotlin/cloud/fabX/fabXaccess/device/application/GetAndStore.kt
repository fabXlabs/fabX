package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.DeviceSourcingEvent

suspend inline fun DeviceRepository.getAndStoreMap(
    deviceId: DeviceId,
    actor: Actor,
    correlationId: CorrelationId,
    log: Logger,
    functionName: String,
    domainFunction: (Device) -> DeviceSourcingEvent
): Either<Error, Unit> {
    return this.getAndStoreFlatMap(deviceId, actor, correlationId, log, functionName) {
        domainFunction(it).right()
    }
}

suspend inline fun DeviceRepository.getAndStoreFlatMap(
    deviceId: DeviceId,
    actor: Actor,
    correlationId: CorrelationId,
    log: Logger,
    functionName: String,
    domainFunction: (Device) -> Either<Error, DeviceSourcingEvent>
): Either<Error, Unit> {
    return log.logError(actor, correlationId, functionName) {
        getById(deviceId)
            .flatMap { domainFunction(it) }
            .flatMap { store(it) }
    }
}

suspend inline fun <R> Logger.logError(
    actor: Actor,
    correlationId: CorrelationId,
    functionName: String,
    function: () -> Either<Error, R>
): Either<Error, R> {
    this.debug("$functionName (actor: $actor, correlationId: $correlationId)...")
    return function()
        .onRight { this.debug("...$functionName done") }
        .onLeft { this.error("...$functionName error: $it") }
}