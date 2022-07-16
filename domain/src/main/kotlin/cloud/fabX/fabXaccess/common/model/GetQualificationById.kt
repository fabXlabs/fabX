package cloud.fabX.fabXaccess.common.model

import arrow.core.Either
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationId

fun interface GetQualificationById {
    fun getQualificationById(id: QualificationId): Either<Error, Qualification>
}