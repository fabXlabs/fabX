package cloud.fabX.fabXaccess.qualification.model

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId

interface QualificationRepository : GettingQualificationById {
    suspend fun getAll(): Set<Qualification>
    suspend fun getById(id: QualificationId): Either<Error, Qualification>
    suspend fun store(event: QualificationSourcingEvent): Either<Error, Unit>

    override suspend fun getQualificationById(id: QualificationId): Either<Error, Qualification> = getById(id)
}

fun interface GettingQualificationById {
    suspend fun getQualificationById(id: QualificationId): Either<Error, Qualification>
}