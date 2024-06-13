package cloud.fabX.fabXaccess.qualification.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readAdminAuthentication
import cloud.fabX.fabXaccess.common.rest.readBody
import cloud.fabX.fabXaccess.common.rest.readMemberAuthentication
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.common.rest.toDomain
import cloud.fabX.fabXaccess.common.rest.withAdminAuthRespond
import cloud.fabX.fabXaccess.common.rest.withMemberAuthRespond
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.ChangingQualification
import cloud.fabX.fabXaccess.qualification.application.DeletingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.util.pipeline.PipelineContext

class QualificationController(
    private val gettingQualification: GettingQualification,
    private val addingQualification: AddingQualification,
    private val changingQualification: ChangingQualification,
    private val deletingQualification: DeletingQualification
) {

    val routes: Route.() -> Unit = {
        route("/qualification") {
            get("") {
                call.respondWithErrorHandler(
                    readMemberAuthentication()
                        .map { member ->
                            gettingQualification
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
                        gettingQualification.getById(member, newCorrelationId(), id)
                            .map { it.toRestModel() }
                    }
                }
            }

            post("") {
                readBody<QualificationCreationDetails>()?.let {
                    withAdminAuthRespond { admin ->
                        addingQualification
                            .addQualification(
                                admin,
                                newCorrelationId(),
                                it.name,
                                it.description,
                                it.colour,
                                it.orderNr
                            )
                            .map { it.serialize() }
                    }
                }
            }

            put("/{id}") {
                readBody<QualificationDetails>()?.let {
                    readId { id ->
                        withAdminAuthRespond { admin ->
                            changingQualification.changeQualificationDetails(
                                admin,
                                newCorrelationId(),
                                id,
                                it.name.toDomain(),
                                it.description.toDomain(),
                                it.colour.toDomain(),
                                it.orderNr.toDomain()
                            )
                        }
                    }
                }
            }

            delete("/{id}") {
                readId { id ->
                    call.respondWithErrorHandler(
                        readAdminAuthentication()
                            .flatMap { admin ->
                                deletingQualification.deleteQualification(
                                    admin,
                                    newCorrelationId(),
                                    id
                                )
                            }
                    )
                }
            }
        }
    }

    private suspend inline fun PipelineContext<*, ApplicationCall>.readId(
        function: (QualificationId) -> Unit
    ) {
        readUUIDParameter("id")
            ?.let { QualificationId(it) }
            ?.let { id ->
                function(id)
            }
    }
}