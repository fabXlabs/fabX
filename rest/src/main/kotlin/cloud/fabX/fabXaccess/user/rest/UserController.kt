package cloud.fabX.fabXaccess.user.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readAdminAuthentication
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.user.application.GettingUser
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

class UserController(
    private val gettingUser: GettingUser
) {

    val routes: Route.() -> Unit = {
        route("/user") {
            get("") {
                call.respondWithErrorHandler(
                    readAdminAuthentication()
                        .map { admin ->
                            gettingUser
                                .getAll(
                                    admin,
                                    newCorrelationId()
                                )
                                .map { it.toRestModel() }
                        }
                )
            }
            get("{id}") {
                readUUIDParameter("id")
                    ?.let { UserId(it) }
                    ?.let { id ->
                        call.respondWithErrorHandler(
                            readAdminAuthentication()
                                .flatMap { admin ->
                                    gettingUser
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
            // TODO create user
            // TODO change user details
        }
    }
}