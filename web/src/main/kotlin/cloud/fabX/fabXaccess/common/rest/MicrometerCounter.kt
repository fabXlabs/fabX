package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.common.model.Counter
import io.micrometer.core.instrument.Tag
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

/**
 * A counter with static tags.
 */
class MicrometerCounter(
    appMicrometerRegistry: PrometheusMeterRegistry,
    name: String,
    description: String,
    tags: Collection<Tag>
) : Counter {

    private val counter: io.micrometer.core.instrument.Counter = io.micrometer.core.instrument.Counter.builder(name)
        .description(description)
        .tags(tags)
        .register(appMicrometerRegistry)

    override fun increment(double: Double) {
        counter.increment(double)
    }
}