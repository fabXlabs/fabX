package cloud.fabX.fabXaccess.common.model

import kotlinx.datetime.Instant

interface SourcingEvent {
    val aggregateRootId: EntityId<*>
    val aggregateVersion: Long
    val actorId: ActorId
    val timestamp: Instant
}

fun <E : SourcingEvent> Iterable<E>.assertAggregateVersionIncreasesOneByOne() {
    val list = toList()

    val increasesOneByOne = (1 until list.size).all { list[it].aggregateVersion == list[it - 1].aggregateVersion + 1 }
    if (!increasesOneByOne) {
        throw AggregateVersionDoesNotIncreaseOneByOne("Aggregate version does not increase one by one for $this.")
    }
}

class AggregateVersionDoesNotIncreaseOneByOne(message: String) : Exception(message)