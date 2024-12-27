package cloud.fabX.fabXaccess.device.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readAdminAuthentication
import cloud.fabX.fabXaccess.common.rest.readBody
import cloud.fabX.fabXaccess.common.rest.readIntParameter
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.common.rest.toDomain
import cloud.fabX.fabXaccess.common.rest.withAdminAuthRespond
import cloud.fabX.fabXaccess.device.application.AddingCardIdentityAtDevice
import cloud.fabX.fabXaccess.device.application.AddingDevice
import cloud.fabX.fabXaccess.device.application.AttachingTool
import cloud.fabX.fabXaccess.device.application.ChangingDevice
import cloud.fabX.fabXaccess.device.application.ChangingFirmwareVersion
import cloud.fabX.fabXaccess.device.application.ChangingThumbnail
import cloud.fabX.fabXaccess.device.application.DeletingDevice
import cloud.fabX.fabXaccess.device.application.DetachingTool
import cloud.fabX.fabXaccess.device.application.GettingDevice
import cloud.fabX.fabXaccess.device.application.RestartingDevice
import cloud.fabX.fabXaccess.device.application.UnlockingTool
import cloud.fabX.fabXaccess.device.application.UpdatingDeviceFirmware
import io.ktor.http.CacheControl
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class DeviceController(
    private val gettingDevice: GettingDevice,
    private val addingDevice: AddingDevice,
    private val changingDevice: ChangingDevice,
    private val changingThumbnail: ChangingThumbnail,
    private val changingFirmwareVersion: ChangingFirmwareVersion,
    private val deletingDevice: DeletingDevice,
    private val attachingTool: AttachingTool,
    private val detachingTool: DetachingTool,
    private val unlockingTool: UnlockingTool,
    private val restartingDevice: RestartingDevice,
    private val addingCardIdentityAtDevice: AddingCardIdentityAtDevice,
    private val updatingDeviceFirmware: UpdatingDeviceFirmware
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
                readId { id ->
                    withAdminAuthRespond { admin ->
                        gettingDevice
                            .getById(
                                admin,
                                newCorrelationId(),
                                id
                            )
                            .map { it.toRestModel() }
                    }
                }
            }

            post("") {
                readBody<DeviceCreationDetails>()?.let {
                    withAdminAuthRespond { admin ->
                        addingDevice.addDevice(
                            admin,
                            newCorrelationId(),
                            it.name,
                            it.background,
                            it.backupBackendUrl,
                            it.mac,
                            it.secret
                        )
                            .map { it.serialize() }
                    }
                }
            }

            put("/{id}") {
                readBody<DeviceDetails>()?.let {
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            changingDevice.changeDeviceDetails(
                                admin,
                                newCorrelationId(),
                                id,
                                it.name.toDomain(),
                                it.background.toDomain(),
                                it.backupBackendUrl.toDomain()
                            )
                        }
                    }
                }
            }

            put("/{id}/desired-firmware-version") {
                readBody<DesiredFirmwareVersion>()?.let {
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            changingFirmwareVersion.changeDesiredFirmwareVersion(
                                admin,
                                newCorrelationId(),
                                id,
                                it.desiredFirmwareVersion
                            )
                        }
                    }
                }
            }

            delete("/{id}") {
                readId { id ->
                    withAdminAuthRespond { admin ->
                        deletingDevice.deleteDevice(
                            admin,
                            newCorrelationId(),
                            id
                        )
                    }
                }
            }

            route("/{id}/attached-tool") {
                put("/{pin}") {
                    readBody<ToolAttachmentDetails>()?.let {
                        readId { id ->
                            readIntParameter("pin")?.let { pin ->
                                withAdminAuthRespond { admin ->
                                    attachingTool.attachTool(
                                        admin,
                                        newCorrelationId(),
                                        id,
                                        pin,
                                        ToolId.fromString(it.toolId)
                                    )
                                }
                            }
                        }
                    }
                }

                delete("/{pin}") {
                    readId { id ->
                        readIntParameter("pin")?.let { pin ->
                            withAdminAuthRespond { admin ->
                                detachingTool.detachTool(
                                    admin,
                                    newCorrelationId(),
                                    id,
                                    pin
                                )
                            }
                        }
                    }
                }
            }

            post("/{id}/restart") {
                readId { id ->
                    withAdminAuthRespond { admin ->
                        restartingDevice.restartDevice(
                            admin,
                            newCorrelationId(),
                            id
                        )
                    }
                }
            }

            post("/{id}/update-firmware") {
                readId { id ->
                    withAdminAuthRespond { admin ->
                        updatingDeviceFirmware.updateDeviceFirmware(
                            admin,
                            newCorrelationId(),
                            id
                        )
                    }
                }
            }

            post("/{id}/unlock-tool") {
                readBody<ToolUnlockDetails>()?.let {
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            unlockingTool.unlockTool(
                                admin,
                                newCorrelationId(),
                                id,
                                ToolId.fromString(it.toolId)
                            )
                        }
                    }
                }
            }

            post("/{id}/add-user-card-identity") {
                readBody<AtDeviceCardCreationDetails>()?.let {
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            addingCardIdentityAtDevice.addCardIdentityAtDevice(
                                admin,
                                newCorrelationId(),
                                id,
                                UserId.fromString(it.userId)
                            )
                        }
                    }
                }
            }

            post("/{id}/thumbnail") {
                readBody<ByteArray>()?.let {
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            changingThumbnail.changeDeviceThumbnail(
                                admin,
                                newCorrelationId(),
                                id,
                                it
                            )
                        }
                    }
                }
            }

            get("{id}/thumbnail") {
                readId { id ->
                    call.respondWithErrorHandler(
                        readAdminAuthentication()
                            .flatMap { admin ->
                                gettingDevice
                                    .getThumbnail(
                                        admin,
                                        newCorrelationId(),
                                        id
                                    )
                            },
                        cacheControl = CacheControl.MaxAge(
                            60,
                            mustRevalidate = true,
                            visibility = CacheControl.Visibility.Private
                        )
                    )
                }
            }
        }
    }

    private suspend inline fun RoutingContext.readId(
        function: (DeviceId) -> Unit
    ) {
        readUUIDParameter("id")
            ?.let { DeviceId(it) }
            ?.let { id ->
                function(id)
            }
    }
}
