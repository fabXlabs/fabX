import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}
