package cloud.fabX.fabXaccess.qualification.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId

interface QualificationRepository : GettingQualificationById {
    suspend fun getAll(): Set<Qualification>
    suspend fun getById(id: QualificationId): Either<Error, Qualification>
    suspend fun store(event: QualificationSourcingEvent): Option<Error>

    override suspend fun getQualificationById(id: QualificationId): Either<Error, Qualification> = getById(id)
}

fun interface GettingQualificationById {
    suspend fun getQualificationById(id: QualificationId): Either<Error, Qualification>
}