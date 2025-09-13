package cloud.fabX.fabXaccess.user.application

import arrow.core.right
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.CardCreatedAtDevice
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationDeleted
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.GettingUserById
import cloud.fabX.fabXaccess.user.model.GettingUsersByInstructorQualification
import cloud.fabX.fabXaccess.user.model.GettingUsersByMemberQualification
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.eq
import org.mockito.kotlin.same
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalTime::class)
@MockitoSettings
internal class UserDomainEventHandlerTest {

    private val actorId: ActorId = UserIdFixture.arbitrary()

    private val qualificationId = QualificationIdFixture.arbitrary()
    private val userId = UserIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var addingCardIdentity: AddingCardIdentity
    private lateinit var gettingUsersByMemberQualification: GettingUsersByMemberQualification
    private lateinit var gettingUsersByInstructorQualification: GettingUsersByInstructorQualification
    private lateinit var removingMemberQualification: RemovingMemberQualification
    private lateinit var removingInstructorQualification: RemovingInstructorQualification
    private lateinit var gettingUserById: GettingUserById

    private lateinit var testee: UserDomainEventHandler

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock addingCardIdentity: AddingCardIdentity,
        @Mock gettingUsersByMemberQualification: GettingUsersByMemberQualification,
        @Mock gettingUsersByInstructorQualification: GettingUsersByInstructorQualification,
        @Mock removingMemberQualification: RemovingMemberQualification,
        @Mock removingInstructorQualification: RemovingInstructorQualification,
        @Mock gettingUserById: GettingUserById
    ) {
        this.logger = logger
        this.addingCardIdentity = addingCardIdentity
        this.gettingUsersByMemberQualification = gettingUsersByMemberQualification
        this.gettingUsersByInstructorQualification = gettingUsersByInstructorQualification
        this.removingMemberQualification = removingMemberQualification
        this.removingInstructorQualification = removingInstructorQualification
        this.gettingUserById = gettingUserById

        testee = UserDomainEventHandler(
            { logger },
            addingCardIdentity,
            removingMemberQualification,
            removingInstructorQualification,
            gettingUsersByMemberQualification,
            gettingUsersByInstructorQualification,
            gettingUserById
        )
    }


    @Test
    fun `when handling QualificationDeleted then removes member and instructor qualifications`() = runTest {
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

        whenever(gettingUsersByMemberQualification.getByMemberQualification(qualificationId))
            .thenReturn(setOf(user1, user2))

        val userId3 = UserIdFixture.arbitrary()
        val user3 = UserFixture.arbitrary(userId3)

        whenever(gettingUsersByInstructorQualification.getByInstructorQualification(qualificationId))
            .thenReturn(setOf(user1, user3))

        whenever(removingMemberQualification.removeMemberQualification(domainEvent, userId1, qualificationId))
            .thenReturn(Unit.right())
        whenever(removingMemberQualification.removeMemberQualification(domainEvent, userId2, qualificationId))
            .thenReturn(Unit.right())
        whenever(removingInstructorQualification.removeInstructorQualification(domainEvent, userId1, qualificationId))
            .thenReturn(Unit.right())
        whenever(removingInstructorQualification.removeInstructorQualification(domainEvent, userId3, qualificationId))
            .thenReturn(Unit.right())

        // when
        testee.handle(domainEvent)

        // then
        verify(removingMemberQualification).removeMemberQualification(
            same(domainEvent),
            eq(userId1),
            eq(qualificationId)
        )
        verify(removingMemberQualification).removeMemberQualification(
            same(domainEvent),
            eq(userId2),
            eq(qualificationId)
        )

        verify(removingInstructorQualification).removeInstructorQualification(
            same(domainEvent),
            eq(userId1),
            eq(qualificationId)
        )
        verify(removingInstructorQualification).removeInstructorQualification(
            same(domainEvent),
            eq(userId3),
            eq(qualificationId)
        )
    }

    @Test
    fun `when handling CardCreatedAtDevice then adds card identity to user`() = runTest {
        // given
        val actor = UserFixture.arbitrary(userId = actorId as UserId, isAdmin = true)

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"

        val correlationId = CorrelationIdFixture.arbitrary()

        val domainEvent = CardCreatedAtDevice(
            actorId,
            Clock.System.now(),
            correlationId,
            userId,
            cardId,
            cardSecret
        )

        whenever(gettingUserById.getUserById(actorId))
            .thenReturn(actor.right())

        whenever(
            addingCardIdentity.addCardIdentity(
                actor.asAdmin().getOrNull()!!,
                correlationId,
                userId,
                cardId,
                cardSecret
            )
        )
            .thenReturn(Unit.right())

        // when
        testee.handle(domainEvent)

        // then
        // asserted by whenever statements
    }
}