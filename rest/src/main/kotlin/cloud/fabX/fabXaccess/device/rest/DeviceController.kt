package cloud.fabX.fabXaccess.device.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readAdminAuthentication
import cloud.fabX.fabXaccess.common.rest.readBody
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.common.rest.toDomain
import cloud.fabX.fabXaccess.device.application.AddingDevice
import cloud.fabX.fabXaccess.device.application.ChangingDevice
import cloud.fabX.fabXaccess.device.application.GettingDevice
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route

class DeviceController(
    private val gettingDevice: GettingDevice,
    private val addingDevice: AddingDevice,
    private val changingDevice: ChangingDevice
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

            post("") {
                readBody<DeviceCreationDetails>()?.let {
                    call.respondWithErrorHandler(
                        readAdminAuthentication()
                            .flatMap { admin ->
                                addingDevice.addDevice(
                                    admin,
                                    newCorrelationId(),
                                    it.name,
                                    it.background,
                                    it.backupBackendUrl,
                                    MacSecretIdentity(it.mac, it.secret)
                                )
                            }
                            .map { it.serialize() }
                    )
                }
            }

            put("/{id}") {
                readBody<DeviceDetails>()?.let {
                    readUUIDParameter("id")
                        ?.let { DeviceId(it) }
                        ?.let { id ->
                            call.respondWithErrorHandler(
                                readAdminAuthentication()
                                    .flatMap { admin ->
                                        changingDevice.changeDeviceDetails(
                                            admin,
                                            newCorrelationId(),
                                            id,
                                            it.name.toDomain(),
                                            it.background.toDomain(),
                                            it.backupBackendUrl.toDomain()
                                        )
                                            .toEither { }
                                            .swap()
                                    }
                            )
                        }
                }
            }
        }
    }
}