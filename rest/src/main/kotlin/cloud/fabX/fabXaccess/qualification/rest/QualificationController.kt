package cloud.fabX.fabXaccess.qualification.rest

import cloud.fabX.fabXaccess.RestModule
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.requireUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get

class QualificationController(
    private val gettingQualification: GettingQualification
) {

    val routes: Route.() -> Unit = {
        get("/qualification") {
            call.respond(
                gettingQualification
                    .getAll(
                        RestModule.fakeActor,
                        newCorrelationId()
                    )
                    .map { it.toRestModel() }
            )
        }

        get("/qualification/{id}") {
            val id = QualificationId(requireUUIDParameter("id") { return@get })

            call.respondWithErrorHandler(
                gettingQualification.getById(RestModule.fakeActor, newCorrelationId(), id)
                    .map { it.toRestModel() }
            )
        }
    }
}