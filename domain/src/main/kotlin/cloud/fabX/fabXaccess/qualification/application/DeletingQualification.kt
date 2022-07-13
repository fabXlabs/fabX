package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to handle deleting a qualification.
 */
class DeletingQualification {

    private val log = logger()
    private val qualificationRepository = DomainModule.qualificationRepository()

    fun deleteQualification(
        actor: Admin,
        qualificationId: QualificationId
    ): Option<Error> {
        log.debug("deleteQualification...")

        return qualificationRepository.getById(qualificationId)
            .map {
                it.delete(actor)
            }
            .flatMap {
                qualificationRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...deleteQualification done") }
            .tap { log.error("...deleteQualification error: $it") }
    }
}