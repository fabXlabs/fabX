package cloud.fabX.fabXaccess.user.rest

import cloud.fabX.fabXaccess.common.model.Error
import io.ktor.server.auth.Principal

data class ErrorPrincipal(val error: Error) : Principal