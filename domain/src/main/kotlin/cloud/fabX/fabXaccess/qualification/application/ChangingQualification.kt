package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Option
import arrow.core.flatMap
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

    fun changeQualificationDetails(
        actor: Admin,
        correlationId: CorrelationId,
        qualificationId: QualificationId,
        name: ChangeableValue<String>,
        description: ChangeableValue<String>,
        colour: ChangeableValue<String>,
        orderNr: ChangeableValue<Int>
    ): Option<Error> {
        log.debug("changeQualificationDetails...")

        return qualificationRepository.getById(qualificationId)
            .map {
                it.changeDetails(actor, clock, correlationId, name, description, colour, orderNr)
            }
            .flatMap {
                qualificationRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...changeQualificationDetails done") }
            .tap { log.error("...changeQualificationDetails error: $it") }
    }
}