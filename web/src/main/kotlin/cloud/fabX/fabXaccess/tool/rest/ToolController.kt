package cloud.fabX.fabXaccess.tool.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readAdminAuthentication
import cloud.fabX.fabXaccess.common.rest.readBody
import cloud.fabX.fabXaccess.common.rest.readMemberAuthentication
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.common.rest.toDomain
import cloud.fabX.fabXaccess.common.rest.withAdminAuthRespond
import cloud.fabX.fabXaccess.common.rest.withMemberAuthRespond
import cloud.fabX.fabXaccess.tool.application.AddingTool
import cloud.fabX.fabXaccess.tool.application.ChangingThumbnail
import cloud.fabX.fabXaccess.tool.application.ChangingTool
import cloud.fabX.fabXaccess.tool.application.DeletingTool
import cloud.fabX.fabXaccess.tool.application.GettingTool
import io.ktor.http.CacheControl
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class ToolController(
    private val gettingTool: GettingTool,
    private val addingTool: AddingTool,
    private val changingTool: ChangingTool,
    private val changingThumbnail: ChangingThumbnail,
    private val deletingTool: DeletingTool
) {

    val routes: Route.() -> Unit = {
        route("/tool") {
            get("") {
                call.respondWithErrorHandler(
                    readMemberAuthentication()
                        .map { member ->
                            gettingTool
                                .getAll(
                                    member,
                                    newCorrelationId()
                                )
                                .map { it.toRestModel() }
                        }
                )
            }

            get("/{id}") {
                readId { id ->
                    withMemberAuthRespond { member ->
                        gettingTool
                            .getById(
                                member,
                                newCorrelationId(),
                                id
                            )
                            .map { it.toRestModel() }
                    }
                }
            }

            post("") {
                readBody<ToolCreationDetails>()?.let {
                    withAdminAuthRespond { admin ->
                        addingTool
                            .addTool(
                                admin,
                                newCorrelationId(),
                                it.name,
                                it.type.toDomainModel(),
                                it.requires2FA,
                                it.time,
                                it.idleState.toDomainModel(),
                                it.wikiLink,
                                it.requiredQualifications.map(QualificationId::fromString).toSet()
                            )
                            .map { it.serialize() }
                    }
                }
            }

            put("/{id}") {
                readBody<ToolDetails>()?.let {
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            changingTool.changeToolDetails(
                                admin,
                                newCorrelationId(),
                                id,
                                it.name.toDomain(),
                                it.type.toDomain { ChangeableValue.ChangeToValueToolType(it.toDomainModel()) },
                                it.requires2FA.toDomain(),
                                it.time.toDomain(),
                                it.idleState.toDomain { ChangeableValue.ChangeToValueIdleState(it.toDomainModel()) },
                                it.enabled.toDomain(),
                                it.notes.toDomain(),
                                it.wikiLink.toDomain(),
                                it.requiredQualifications.toDomain {
                                    ChangeableValue.ChangeToValueQualificationSet(
                                        it.map(QualificationId::fromString).toSet()
                                    )
                                }
                            )
                        }
                    }
                }
            }

            post("/{id}/thumbnail") {
                readBody<ByteArray>()?.let {
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            changingThumbnail.changeToolThumbnail(
                                admin,
                                newCorrelationId(),
                                id,
                                it
                            )
                        }
                    }
                }
            }

            get("/{id}/thumbnail") {
                readId { id ->
                    call.respondWithErrorHandler(
                        readAdminAuthentication()
                            .flatMap { admin ->
                                gettingTool
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

            delete("/{id}") {
                readId { id ->
                    withAdminAuthRespond { admin ->
                        deletingTool.deleteTool(
                            admin,
                            newCorrelationId(),
                            id
                        )
                    }
                }
            }
        }
    }

    private suspend inline fun RoutingContext.readId(
        function: (ToolId) -> Unit
    ) {
        readUUIDParameter("id")
            ?.let { ToolId(it) }
            ?.let { id ->
                function(id)
            }
    }
}