package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Option
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
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
        name: String,
        description: String,
        colour: String,
        orderNr: Int
    ): Option<Error> {
        log.debug("addQualification...")

        return qualificationRepository
            .store(
                Qualification.addNew(
                    actor,
                    name,
                    description,
                    colour,
                    orderNr
                )
            )
            .tapNone { log.debug("...addQualification done") }
            .tap { log.error("...addQualification error: $it") }
    }
}