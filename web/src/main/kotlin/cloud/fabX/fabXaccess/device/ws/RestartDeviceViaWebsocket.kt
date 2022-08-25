package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.RestartDevice

class RestartDeviceViaWebsocket(
    private val deviceWebsocketController: DeviceWebsocketController
) : RestartDevice {

    override suspend fun restartDevice(
        deviceId: DeviceId,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        val commandId = deviceWebsocketController.newCommandId()

        return deviceWebsocketController.setupReceivingDeviceResponse(deviceId, commandId, correlationId)
            .flatMap {
                deviceWebsocketController.sendCommand(
                    deviceId,
                    RestartDevice(commandId),
                    correlationId
                )
            }
            .flatMap {
                deviceWebsocketController.receiveDeviceResponse(deviceId, commandId, correlationId)
            }
            .flatMap {
                Either.conditionally(
                    it is DeviceRestartResponse,
                    {
                        Error.UnexpectedDeviceResponse(
                            "Unexpected device response type.",
                            deviceId,
                            it.toString(),
                            correlationId
                        )
                    },
                    {}
                )
            }

    }
}