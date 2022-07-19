package cloud.fabX.fabXaccess.user.application

import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.QualificationDeleted

// TODO use DI library instead of creating service multiple times?
class UserDomainEventHandler(
    private val removingMemberQualification: RemovingMemberQualification = RemovingMemberQualification(),
    private val removingInstructorQualification: RemovingInstructorQualification = RemovingInstructorQualification()
) : DomainEventHandler {

    private val log = logger()
    private val gettingUsersByMemberQualification = DomainModule.gettingUsersByMemberQualification()
    private val gettingUsersByInstructorQualification = DomainModule.gettingUsersByInstructorQualification()

    override fun handle(domainEvent: DomainEvent) {
        log.debug("ignoring event $domainEvent")
    }

    override fun handle(domainEvent: QualificationDeleted) {
        gettingUsersByMemberQualification.getByMemberQualification(domainEvent.qualificationId)
            .map {
                removingMemberQualification.removeMemberQualification(
                    domainEvent,
                    it.id,
                    domainEvent.qualificationId
                )
            }

        gettingUsersByInstructorQualification.getByInstructorQualification(domainEvent.qualificationId)
            .map {
                removingInstructorQualification.removeInstructorQualification(
                    domainEvent,
                    it.id,
                    domainEvent.qualificationId
                )
            }
    }
}