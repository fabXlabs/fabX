package cloud.fabX.fabXaccess.user.rest

import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.Principal

data class ErrorPrincipal(val error: Error) : Principal