import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}