package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.QualificationIdFactory
import cloud.fabX.fabXaccess.device.application.logError
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.Admin
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service to add new qualifications.
 */
@OptIn(ExperimentalTime::class)
class AddingQualification(
    loggerFactory: LoggerFactory,
    private val qualificationRepository: QualificationRepository,
    private val qualificationIdFactory: QualificationIdFactory,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun addQualification(
        actor: Admin,
        correlationId: CorrelationId,
        name: String,
        description: String,
        colour: String,
        orderNr: Int
    ): Either<Error, QualificationId> =
        log.logError(actor, correlationId, "addQualification") {
            val sourcingEvent = Qualification.addNew(
                qualificationIdFactory,
                actor,
                clock,
                correlationId,
                name,
                description,
                colour,
                orderNr
            )

            qualificationRepository
                .store(sourcingEvent)
                .map { sourcingEvent.aggregateRootId }
        }
}