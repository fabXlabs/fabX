package cloud.fabX.fabXaccess.qualification.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error

interface QualificationRepository {
    // TODO: getAll(): Iterable<Qualification>
    fun getById(id: QualificationId): Either<Error, Qualification>
    fun store(event: QualificationSourcingEvent): Option<Error>
}