package cloud.fabX.fabXaccess.qualification.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationSourcingEvent

class QualificationInMemoryRepository : QualificationRepository {
    private val events = mutableListOf<QualificationSourcingEvent>()

    override fun getAll(): Set<Qualification> {
        return events
            .sortedBy { it.aggregateVersion }
            .groupBy { it.aggregateRootId }
            .map { Qualification.fromSourcingEvents(it.value) }
            .filter { it.isDefined() }
            .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
            .toSet()
    }

    override fun getById(id: QualificationId): Either<Error, Qualification> {
        val e = events
            .filter { it.aggregateRootId == id }
            .sortedBy { it.aggregateVersion }

        return if (e.isNotEmpty()) {
            Qualification.fromSourcingEvents(e)
                .toEither {
                    Error.QualificationNotFound(
                        "Qualification with id $id not found.",
                        id
                    )
                }
        } else {
            Error.QualificationNotFound(
                "Qualification with id $id not found.",
                id
            ).left()
        }
    }

    override fun store(event: QualificationSourcingEvent): Option<Error> {
        val previousVersion = getVersionById(event.aggregateRootId)

        return if (previousVersion != null
            && event.aggregateVersion != previousVersion + 1
        ) {
            Some(
                Error.VersionConflict(
                    "Previous version of qualification ${event.aggregateRootId} is $previousVersion, " +
                            "desired new version is ${event.aggregateVersion}."
                )
            )
        } else {
            events.add(event)
            None
        }
    }

    private fun getVersionById(id: QualificationId): Long? {
        return events
            .filter { it.aggregateRootId == id }
            .maxOfOrNull { it.aggregateVersion }
    }
}