package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.common.model.TaggedCounter
import io.ktor.util.collections.ConcurrentMap
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Tag
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

/**
 * A (or technically multiple) counter with dynamic tags.
 */
class MicrometerTaggedCounter<T>(
    private val appMicrometerRegistry: PrometheusMeterRegistry,
    private val name: String,
    private val description: String
) : TaggedCounter<T> {

    private val counters = ConcurrentMap<T, Counter>()

    override suspend fun increment(key: T, tags: suspend () -> Map<String, String>, double: Double) {
        counters
            .getOrPut(key) {
                Counter.builder(name)
                    .description(description)
                    .tags(tags().map { Tag.of(it.key, it.value) })
                    .register(appMicrometerRegistry)
            }
            .increment(double)
    }
}