package cloud.fabX.fabXaccess.common.application

import kotlinx.datetime.Instant

fun Instant.withSecondPrecision(): Instant {
    return Instant.fromEpochSeconds(this.epochSeconds)
}
