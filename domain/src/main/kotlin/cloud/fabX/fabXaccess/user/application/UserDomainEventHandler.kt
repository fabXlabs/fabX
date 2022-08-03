package cloud.fabX.fabXaccess.user.application

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationDeleted
import cloud.fabX.fabXaccess.user.model.GettingUsersByInstructorQualification
import cloud.fabX.fabXaccess.user.model.GettingUsersByMemberQualification

class UserDomainEventHandler(
    loggerFactory: LoggerFactory,
    private val removingMemberQualification: RemovingMemberQualification,
    private val removingInstructorQualification: RemovingInstructorQualification,
    private val gettingUsersByMemberQualification: GettingUsersByMemberQualification,
    private val gettingUsersByInstructorQualification: GettingUsersByInstructorQualification
) : DomainEventHandler {

    private val log: Logger = loggerFactory.invoke(this::class.java)

    override suspend fun handle(domainEvent: DomainEvent) {
        log.debug("ignoring event $domainEvent")
    }

    override suspend fun handle(domainEvent: QualificationDeleted) {
        gettingUsersByMemberQualification.getByMemberQualification(domainEvent.qualificationId)
            .map {
                removingMemberQualification.removeMemberQualification(
                    domainEvent,
                    it.id,
                    domainEvent.qualificationId
                )
            }
            .mapNotNull { it.orNull() }
            .forEach { log.warn("Error while handling domain event ($domainEvent): $it") }

        gettingUsersByInstructorQualification.getByInstructorQualification(domainEvent.qualificationId)
            .map {
                removingInstructorQualification.removeInstructorQualification(
                    domainEvent,
                    it.id,
                    domainEvent.qualificationId
                )
            }
            .mapNotNull { it.orNull() }
            .forEach { log.warn("Error while handling domain event ($domainEvent): $it") }
    }
}