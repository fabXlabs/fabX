package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

/**
 * Service to handle changing qualification properties.
 */
class ChangingQualification(
    loggerFactory: LoggerFactory,
    private val qualificationRepository: QualificationRepository,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun changeQualificationDetails(
        actor: Admin,
        correlationId: CorrelationId,
        qualificationId: QualificationId,
        name: ChangeableValue<String>,
        description: ChangeableValue<String>,
        colour: ChangeableValue<String>,
        orderNr: ChangeableValue<Int>
    ): Either<Error, Unit> =
        qualificationRepository.getAndStoreMap(
            qualificationId,
            actor,
            correlationId,
            log,
            "changeQualificationDetails"
        ) {
            it.changeDetails(actor, clock, correlationId, name, description, colour, orderNr)
        }
}