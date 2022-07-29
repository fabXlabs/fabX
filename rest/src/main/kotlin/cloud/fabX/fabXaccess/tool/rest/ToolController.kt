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
import cloud.fabX.fabXaccess.tool.application.AddingTool
import cloud.fabX.fabXaccess.tool.application.ChangingTool
import cloud.fabX.fabXaccess.tool.application.DeletingTool
import cloud.fabX.fabXaccess.tool.application.GettingTool
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route

class ToolController(
    private val gettingTool: GettingTool,
    private val addingTool: AddingTool,
    private val changingTool: ChangingTool,
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
                readUUIDParameter("id")
                    ?.let { ToolId(it) }
                    ?.let { id ->
                        call.respondWithErrorHandler(
                            readMemberAuthentication()
                                .flatMap { member ->
                                    gettingTool
                                        .getById(
                                            member,
                                            newCorrelationId(),
                                            id
                                        )
                                        .map { it.toRestModel() }
                                }
                        )
                    }
            }

            post("") {
                readBody<ToolCreationDetails>()?.let {
                    call.respondWithErrorHandler(
                        readAdminAuthentication()
                            .flatMap { admin ->
                                addingTool.addTool(
                                    admin,
                                    newCorrelationId(),
                                    it.name,
                                    it.type.toDomainModel(),
                                    it.time,
                                    it.idleState.toDomainModel(),
                                    it.wikiLink,
                                    it.requiredQualifications.map(QualificationId::fromString).toSet()
                                )
                            }
                            .map { it.serialize() }
                    )
                }
            }

            put("/{id}") {
                readBody<ToolDetails>()?.let {
                    readUUIDParameter("id")
                        ?.let { ToolId(it) }
                        ?.let { id ->
                            call.respondWithErrorHandler(
                                readAdminAuthentication()
                                    .flatMap { admin ->
                                        changingTool.changeToolDetails(
                                            admin,
                                            newCorrelationId(),
                                            id,
                                            it.name.toDomain(),
                                            it.type.toDomain { ChangeableValue.ChangeToValueToolType(it.toDomainModel()) },
                                            it.time.toDomain(),
                                            it.idleState.toDomain { ChangeableValue.ChangeToValueIdleState(it.toDomainModel()) },
                                            it.enabled.toDomain(),
                                            it.wikiLink.toDomain(),
                                            it.requiredQualifications.toDomain {
                                                ChangeableValue.ChangeToValueQualificationSet(
                                                    it.map(QualificationId::fromString).toSet()
                                                )
                                            }
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
                    ?.let { ToolId(it) }
                    ?.let { id ->
                        call.respondWithErrorHandler(
                            readAdminAuthentication()
                                .flatMap { admin ->
                                    deletingTool.deleteTool(
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
        }
    }
}