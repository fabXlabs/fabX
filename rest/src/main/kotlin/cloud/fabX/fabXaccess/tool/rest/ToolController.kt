package cloud.fabX.fabXaccess.tool.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.readMemberAuthentication
import cloud.fabX.fabXaccess.common.rest.readUUIDParameter
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.tool.application.GettingTool
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

class ToolController(
    private val gettingTool: GettingTool
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
        }
    }
}