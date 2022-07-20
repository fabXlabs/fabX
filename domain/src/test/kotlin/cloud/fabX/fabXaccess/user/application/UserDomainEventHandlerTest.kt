package cloud.fabX.fabXaccess.user.application

import arrow.core.None
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationDeleted
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.GettingUsersByInstructorQualification
import cloud.fabX.fabXaccess.user.model.GettingUsersByMemberQualification
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.eq
import org.mockito.kotlin.same
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@MockitoSettings
internal class UserDomainEventHandlerTest {

    private val actorId: ActorId = UserIdFixture.arbitrary()

    private val qualificationId = QualificationIdFixture.arbitrary()

    private var logger: Logger? = null
    private var gettingUsersByMemberQualification: GettingUsersByMemberQualification? = null
    private var gettingUsersByInstructorQualification: GettingUsersByInstructorQualification? = null
    private var removingMemberQualification: RemovingMemberQualification? = null
    private var removingInstructorQualification: RemovingInstructorQualification? = null

    private var testee: UserDomainEventHandler? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock gettingUsersByMemberQualification: GettingUsersByMemberQualification,
        @Mock gettingUsersByInstructorQualification: GettingUsersByInstructorQualification,
        @Mock removingMemberQualification: RemovingMemberQualification,
        @Mock removingInstructorQualification: RemovingInstructorQualification
    ) {
        this.logger = logger
        this.gettingUsersByMemberQualification = gettingUsersByMemberQualification
        this.gettingUsersByInstructorQualification = gettingUsersByInstructorQualification
        this.removingMemberQualification = removingMemberQualification
        this.removingInstructorQualification = removingInstructorQualification

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureGettingUsersByMemberQualification(gettingUsersByMemberQualification)
        DomainModule.configureGettingUsersByInstructorQualification(gettingUsersByInstructorQualification)

        testee = UserDomainEventHandler(removingMemberQualification, removingInstructorQualification)
    }


    @Test
    fun `when handling QualificationDeleted then removes member and instructor qualifications`() {
        // given
        val domainEvent = QualificationDeleted(
            actorId,
            Clock.System.now(),
            CorrelationIdFixture.arbitrary(),
            qualificationId
        )

        val userId1 = UserIdFixture.arbitrary()
        val user1 = UserFixture.arbitrary(userId1)

        val userId2 = UserIdFixture.arbitrary()
        val user2 = UserFixture.arbitrary(userId2)

        whenever(gettingUsersByMemberQualification!!.getByMemberQualification(qualificationId))
            .thenReturn(setOf(user1, user2))

        val userId3 = UserIdFixture.arbitrary()
        val user3 = UserFixture.arbitrary(userId3)

        whenever(gettingUsersByInstructorQualification!!.getByInstructorQualification(qualificationId))
            .thenReturn(setOf(user1, user3))

        whenever(removingMemberQualification!!.removeMemberQualification(domainEvent, userId1, qualificationId))
            .thenReturn(None)
        whenever(removingMemberQualification!!.removeMemberQualification(domainEvent, userId2, qualificationId))
            .thenReturn(None)
        whenever(removingInstructorQualification!!.removeInstructorQualification(domainEvent, userId1, qualificationId))
            .thenReturn(None)
        whenever(removingInstructorQualification!!.removeInstructorQualification(domainEvent, userId3, qualificationId))
            .thenReturn(None)

        // when
        testee!!.handle(domainEvent)

        // then
        verify(removingMemberQualification!!).removeMemberQualification(
            same(domainEvent),
            eq(userId1),
            eq(qualificationId)
        )
        verify(removingMemberQualification!!).removeMemberQualification(
            same(domainEvent),
            eq(userId2),
            eq(qualificationId)
        )

        verify(removingInstructorQualification!!).removeInstructorQualification(
            same(domainEvent),
            eq(userId1),
            eq(qualificationId)
        )
        verify(removingInstructorQualification!!).removeInstructorQualification(
            same(domainEvent),
            eq(userId3),
            eq(qualificationId)
        )
    }
}