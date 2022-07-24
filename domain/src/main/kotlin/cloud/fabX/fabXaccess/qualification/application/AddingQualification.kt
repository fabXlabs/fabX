package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.QualificationIdFactory
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to add new qualifications.
 */
class AddingQualification(
    loggerFactory: LoggerFactory,
    private val qualificationRepository: QualificationRepository,
    private val qualificationIdFactory: QualificationIdFactory
) {
    private val log = loggerFactory.invoke(this::class.java)

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
            qualificationIdFactory,
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