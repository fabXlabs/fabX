package cloud.fabX.fabXaccess.qualification.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readAdminAuthentication
import cloud.fabX.fabXaccess.common.rest.readBody
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

class QualificationController(
    private val gettingQualification: GettingQualification,
    private val addingQualification: AddingQualification
) {

    val routes: Route.() -> Unit = {
        route("/qualification") {
            get("") {
                call.respondWithErrorHandler(
                    // TODO allow any authentication
                    readAdminAuthentication()
                        .map { admin ->
                            gettingQualification
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
                    ?.let { QualificationId(it) }
                    ?.let { id ->
                        call.respondWithErrorHandler(
                            // TODO allow any authentication
                            readAdminAuthentication()
                                .flatMap { admin ->
                                    gettingQualification.getById(admin, newCorrelationId(), id)
                                        .map { it.toRestModel() }
                                }
                        )
                    }
            }

            post("") {
                readBody<QualificationCreationDetails>()?.let {
                    call.respondWithErrorHandler(
                        readAdminAuthentication()
                            .flatMap { admin ->
                                addingQualification.addQualification(
                                    admin,
                                    newCorrelationId(),
                                    it.name,
                                    it.description,
                                    it.colour,
                                    it.orderNr
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