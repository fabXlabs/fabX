package cloud.fabX.fabXaccess.device.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readAdminAuthentication
import cloud.fabX.fabXaccess.common.rest.readBody
import cloud.fabX.fabXaccess.common.rest.readIntParameter
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.common.rest.toDomain
import cloud.fabX.fabXaccess.device.application.AddingDevice
import cloud.fabX.fabXaccess.device.application.AttachingTool
import cloud.fabX.fabXaccess.device.application.ChangingDevice
import cloud.fabX.fabXaccess.device.application.DeletingDevice
import cloud.fabX.fabXaccess.device.application.DetachingTool
import cloud.fabX.fabXaccess.device.application.GettingDevice
import cloud.fabX.fabXaccess.device.application.UnlockingTool
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class DeviceController(
    private val gettingDevice: GettingDevice,
    private val addingDevice: AddingDevice,
    private val changingDevice: ChangingDevice,
    private val deletingDevice: DeletingDevice,
    private val attachingTool: AttachingTool,
    private val detachingTool: DetachingTool,
    private val unlockingTool: UnlockingTool
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

            delete("/{id}") {
                readUUIDParameter("id")
                    ?.let { DeviceId(it) }
                    ?.let { id ->
                        call.respondWithErrorHandler(
                            readAdminAuthentication()
                                .flatMap { admin ->
                                    deletingDevice.deleteDevice(
                                        admin,
                                        newCorrelationId(),
                                        id
                                    )
                                        .toEither { }
                                        .swap()
                                }
                        )
                    }
            }

            route("/{id}/attached-tool") {
                put("/{pin}") {
                    readBody<ToolAttachmentDetails>()
                        ?.let {
                            readUUIDParameter("id")
                                ?.let { DeviceId(it) }
                                ?.let { id ->
                                    readIntParameter("pin")
                                        ?.let { pin ->
                                            call.respondWithErrorHandler(
                                                readAdminAuthentication()
                                                    .flatMap { admin ->
                                                        attachingTool.attachTool(
                                                            admin,
                                                            newCorrelationId(),
                                                            id,
                                                            pin,
                                                            ToolId.fromString(it.toolId)
                                                        )
                                                            .toEither { }
                                                            .swap()
                                                    }
                                            )
                                        }
                                }
                        }
                }

                delete("/{pin}") {
                    readUUIDParameter("id")
                        ?.let { DeviceId(it) }
                        ?.let { id ->
                            readIntParameter("pin")
                                ?.let { pin ->
                                    call.respondWithErrorHandler(
                                        readAdminAuthentication()
                                            .flatMap { admin ->
                                                detachingTool.detachTool(
                                                    admin,
                                                    newCorrelationId(),
                                                    id,
                                                    pin
                                                )
                                                    .toEither { }
                                                    .swap()
                                            }
                                    )
                                }
                        }
                }
            }

            post("/{id}/unlock-tool") {
                readBody<ToolUnlockDetails>()
                    ?.let {
                        readUUIDParameter("id")
                            ?.let { DeviceId(it) }
                            ?.let { id ->
                                call.respondWithErrorHandler(
                                    readAdminAuthentication()
                                        .flatMap { admin ->
                                            unlockingTool.unlockTool(
                                                admin,
                                                newCorrelationId(),
                                                id,
                                                ToolId.fromString(it.toolId)
                                            )
                                        }
                                )
                            }
                    }
            }
        }
    }
}