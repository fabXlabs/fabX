package cloud.fabX.fabXaccess.common.rest

import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull

internal fun Assert<Error>.isError(
    type: String,
    message: String,
    parameters: Map<String, String> = mapOf(),
    correlationId: String? = null
) = all {
        transform { it.type }.isEqualTo(type)
        transform { it.message }.isEqualTo(message)
        transform { it.parameters }.isEqualTo(parameters)
        if (correlationId != null) {
            transform { it.correlationId }.isNotNull().isEqualTo(correlationId)
        }
    }