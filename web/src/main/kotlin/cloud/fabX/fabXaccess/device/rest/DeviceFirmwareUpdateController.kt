package cloud.fabX.fabXaccess.device.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.handleError
import cloud.fabX.fabXaccess.common.rest.readDeviceAuthentication
import cloud.fabX.fabXaccess.device.application.GettingDevice
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.io.File

class DeviceFirmwareUpdateController(
    loggerFactory: LoggerFactory,
    private val gettingDevice: GettingDevice,
    private val firmwareDirectory: File
) {
    private val log = loggerFactory.invoke(this::class.java)

    val routes: Route.() -> Unit = {
        route("/device/me") {
            get("/firmware-update") {
                readDeviceAuthentication()
                    .flatMap {
                        gettingDevice.getMe(
                            it,
                            newCorrelationId()
                        )
                    }
                    .onRight {
                        val actualFirmwareVersion = call.request.headers["X-ESP32-Version"]
                        if (actualFirmwareVersion != it.desiredFirmwareVersion) {
                            log.debug("upgrading device firmware from $actualFirmwareVersion -> ${it.desiredFirmwareVersion}")

                            val firmwareFile = File(firmwareDirectory, "${it.desiredFirmwareVersion}.bin")
                            log.debug("reading firmware file from disk: $firmwareFile")

                            if (firmwareFile.isFile) {
                                call.respondFile(firmwareFile)
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Cannot read firmware file from disk (${firmwareFile.path}).")
                            }
                        } else {
                            call.respond(HttpStatusCode.NoContent)
                        }
                    }
                    .onLeft { call.handleError(it) }
            }
        }
    }
}