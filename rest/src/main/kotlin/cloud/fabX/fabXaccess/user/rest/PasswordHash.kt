package cloud.fabX.fabXaccess.user.rest

import io.ktor.util.getDigestFunction
import java.util.Base64

private val digestFunction = getDigestFunction("SHA-256") { "fabXfabXfabX${it.length}" }

internal fun hash(password: String): String {
    val hash = digestFunction.invoke(password)
    return Base64.getEncoder().encodeToString(hash)
}