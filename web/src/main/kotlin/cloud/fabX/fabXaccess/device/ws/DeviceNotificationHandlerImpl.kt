package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.device.application.UpdatingDevicePinStatus
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DevicePinStatus
import cloud.fabX.fabXaccess.user.application.LoggingUnlockedTool
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import kotlin.time.Clock

class DeviceNotificationHandlerImpl(
    private val loggingUnlockedTool: LoggingUnlockedTool,
    private val updatingDevicePinStatus: UpdatingDevicePinStatus,
    private val authenticationService: AuthenticationService,
    private val clock: Clock
) : DeviceNotificationHandler {

    override suspend fun handle(actor: DeviceActor, notification: ToolUnlockedNotification): Either<Error, Unit> {
        val correlationId = newCorrelationId()

        return authenticationService
            .augmentDeviceActorOnBehalfOfUser(
                actor,
                notification.cardIdentity,
                notification.phoneNrIdentity,
                correlationId
            )
            .flatMap {
                loggingUnlockedTool.logUnlockedTool(it, ToolId.fromString(notification.toolId), correlationId)
            }
    }

    override suspend fun handle(actor: DeviceActor, notification: PinStatusNotification): Either<Error, Unit> {
        val correlationId = newCorrelationId()

        return updatingDevicePinStatus.updateDevicePinStatus(
            actor,
            correlationId,
            DevicePinStatus(
                actor.deviceId,
                notification.inputPins,
                clock.now()
            )
        )
    }
}