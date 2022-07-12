package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Either
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

    // TODO change return type to Option<Error>
    fun changeQualificationDetails(
        actor: Admin,
        qualificationId: QualificationId,
        name: ChangeableValue<String>,
        description: ChangeableValue<String>,
        colour: ChangeableValue<String>,
        orderNr: ChangeableValue<Int>
    ): Either<Error, Unit> {
        log.debug("changeQualificationDetails...")

        return qualificationRepository.getById(qualificationId)
            .map {
                it.changeDetails(actor, name, description, colour, orderNr)
            }
            .flatMap {
                qualificationRepository.store(it)
                    .toEither {  }
                    .swap()
            }
            .tap { log.debug("...changeQualificationDetails done") }
    }

}