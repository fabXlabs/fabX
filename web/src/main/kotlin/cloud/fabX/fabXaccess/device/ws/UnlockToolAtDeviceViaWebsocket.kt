package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.model.UnlockToolAtDevice

class UnlockToolAtDeviceViaWebsocket(
    private val deviceWebsocketController: DeviceWebsocketController
) : UnlockToolAtDevice {

    override suspend fun unlockTool(
        deviceId: DeviceId,
        toolId: ToolId,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        val commandId = deviceWebsocketController.newCommandId()

        return deviceWebsocketController.setupReceivingDeviceResponse(deviceId, commandId, correlationId)
            .flatMap {
                deviceWebsocketController.sendCommand(
                    deviceId,
                    UnlockTool(commandId, toolId.serialize()),
                    correlationId
                )
            }
            .flatMap {
                deviceWebsocketController.receiveDeviceResponse(deviceId, commandId, correlationId)
            }
            .flatMap {
                if (it !is ToolUnlockResponse) {
                    Error.UnexpectedDeviceResponse(
                        "Unexpected device response type.",
                        deviceId,
                        it.toString(),
                        correlationId
                    ).left()
                } else {
                    Unit.right()
                }
            }
    }
}