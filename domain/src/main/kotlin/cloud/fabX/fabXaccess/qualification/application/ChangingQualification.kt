package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to handle changing qualification properties.
 */
class ChangingQualification {

    private val log = logger()
    private val qualificationRepository = DomainModule.qualificationRepository()

    fun changeQualificationDetails(
        actor: Admin,
        qualificationId: QualificationId,
        name: ChangeableValue<String>,
        description: ChangeableValue<String>,
        colour: ChangeableValue<String>,
        orderNr: ChangeableValue<Int>
    ): Option<Error> {
        log.debug("changeQualificationDetails...")

        return qualificationRepository.getById(qualificationId)
            .map {
                it.changeDetails(actor, name, description, colour, orderNr)
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