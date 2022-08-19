package cloud.fabX.fabXaccess.common.application

fun ByteArray.toHex(): String = joinToString(separator = "") { "%02x".format(it) }
