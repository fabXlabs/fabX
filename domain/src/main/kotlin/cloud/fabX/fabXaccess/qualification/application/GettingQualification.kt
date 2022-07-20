package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Either
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.Qualification

/**
 * Service to get qualifications.
 */
class GettingQualification {

    private val qualificationRepository = DomainModule.qualificationRepository()

    fun getAll(): Set<Qualification> {
        return qualificationRepository.getAll()
    }

    fun getById(qualificationId: QualificationId): Either<Error, Qualification> {
        return qualificationRepository.getById(qualificationId)
    }
}