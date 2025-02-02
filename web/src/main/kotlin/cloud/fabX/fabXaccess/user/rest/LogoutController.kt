package cloud.fabX.fabXaccess.user.rest

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

class LogoutController(
    private val cookieDomain: String,
    private val cookiePath: String
) {
    val routes: Route.() -> Unit = {
        route("/logout") {
            get("") {
                call.response.cookies.append(
                    "FABX_AUTH",
                    "",
                    maxAge = -1,
                    domain = cookieDomain,
                    path = cookiePath,
                    secure = true,
                    httpOnly = true
                )
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
