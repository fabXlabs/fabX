package cloud.fabX.fabXaccess.qualification.rest

import cloud.fabX.fabXaccess.RestModule
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readBody
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import io.ktor.application.call
import io.ktor.response.respond
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
                call.respond(
                    gettingQualification
                        .getAll(
                            RestModule.fakeActor,
                            newCorrelationId()
                        )
                        .map { it.toRestModel() }
                )
            }

            get("/{id}") {
                readUUIDParameter("id")
                    ?.let { QualificationId(it) }
                    ?.let { id ->
                        call.respondWithErrorHandler(
                            gettingQualification.getById(RestModule.fakeActor, newCorrelationId(), id)
                                .map { it.toRestModel() }
                        )
                    }
            }

            post("") {
                readBody<QualificationCreationDetails>()?.let {
                    call.respondWithErrorHandler(
                        addingQualification.addQualification(
                            RestModule.fakeActor,
                            newCorrelationId(),
                            it.name,
                            it.description,
                            it.colour,
                            it.orderNr
                        )
                    )
                }
            }
        }
    }
}