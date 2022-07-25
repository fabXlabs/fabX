package cloud.fabX.fabXaccess.device.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readAdminAuthentication
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.device.application.GettingDevice
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

class DeviceController(
    private val gettingDevice: GettingDevice
) {

    val routes: Route.() -> Unit = {
        route("/device") {
            get("") {
                call.respondWithErrorHandler(
                    readAdminAuthentication()
                        .map { admin ->
                            gettingDevice
                                .getAll(
                                    admin,
                                    newCorrelationId()
                                )
                                .map { it.toRestModel() }
                        }
                )
            }

            get("/{id}") {
                readUUIDParameter("id")
                    ?.let { DeviceId(it) }
                    ?.let { id ->
                        call.respondWithErrorHandler(
                            readAdminAuthentication()
                                .flatMap { admin ->
                                    gettingDevice
                                        .getById(
                                            admin,
                                            newCorrelationId(),
                                            id
                                        )
                                        .map { it.toRestModel() }
                                }
                        )
                    }
            }
        }
    }
}