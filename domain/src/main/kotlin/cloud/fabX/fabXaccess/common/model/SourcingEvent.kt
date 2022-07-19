package cloud.fabX.fabXaccess.common.model

import kotlinx.datetime.Instant

// TODO add a trace id
interface SourcingEvent {
    val aggregateRootId: EntityId<*>
    val aggregateVersion: Long
    val actorId: ActorId
    val timestamp: Instant
}

fun <E : SourcingEvent> Iterable<E>.assertIsNotEmpty() {
    if (count() == 0) {
        throw IterableIsEmpty("No sourcing events contained in iterable.")
    }
}

fun <E : SourcingEvent> Iterable<E>.assertAggregateVersionStartsWithOne() {
    if (first().aggregateVersion != 1L) {
        throw AggregateVersionDoesNotStartWithOne(
            "Aggregate version starts with ${first().aggregateVersion} but has to start with 1."
        )
    }
}

fun <E : SourcingEvent> Iterable<E>.assertAggregateVersionIncreasesOneByOne() {
    val list = toList()

    val increasesOneByOne = (1 until list.size).all { list[it].aggregateVersion == list[it - 1].aggregateVersion + 1 }
    if (!increasesOneByOne) {
        throw AggregateVersionDoesNotIncreaseOneByOne("Aggregate version does not increase one by one for $this.")
    }
}

class IterableIsEmpty(message: String) : Exception(message)
class AggregateVersionDoesNotStartWithOne(message: String) : Exception(message)
class AggregateVersionDoesNotIncreaseOneByOne(message: String) : Exception(message)