package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Either
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to add new qualifications.
 */
class AddingQualification {

    private val log = logger()
    private val qualificationRepository = DomainModule.qualificationRepository()

    fun addQualification(
        actor: Admin,
        correlationId: CorrelationId,
        name: String,
        description: String,
        colour: String,
        orderNr: Int
    ): Either<Error, QualificationId> {
        log.debug("addQualification...")

        val sourcingEvent = Qualification.addNew(
            actor,
            correlationId,
            name,
            description,
            colour,
            orderNr
        )

        return qualificationRepository
            .store(sourcingEvent)
            .toEither { }
            .swap()
            .map { sourcingEvent.aggregateRootId }
            .tap { log.debug("...addQualification done") }
            .tapLeft { log.error("...addQualification error: $it") }
    }
}