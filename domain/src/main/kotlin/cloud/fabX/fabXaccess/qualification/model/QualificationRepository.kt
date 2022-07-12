package cloud.fabX.fabXaccess.qualification.model

import arrow.core.Either
import arrow.core.Option

interface QualificationRepository {
    fun getById(id: QualificationId): Either<Error, Qualification>
    fun store(event: QualificationSourcingEvent): Option<Error>
}