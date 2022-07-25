package cloud.fabX.fabXaccess.qualification.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId

interface QualificationRepository : GettingQualificationById {
    fun getAll(): Set<Qualification>
    fun getById(id: QualificationId): Either<Error, Qualification>
    fun store(event: QualificationSourcingEvent): Option<Error>

    override fun getQualificationById(id: QualificationId): Either<Error, Qualification> = getById(id)
}

fun interface GettingQualificationById {
    fun getQualificationById(id: QualificationId): Either<Error, Qualification>
}