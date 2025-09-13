package cloud.fabX.fabXaccess.common.application

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun Instant.withSecondPrecision(): Instant {
    return Instant.fromEpochSeconds(this.epochSeconds)
}
