package cloud.fabX.fabXaccess.user.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationDeleted
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.MemberQualificationRemoved
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isNone
import isSome
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class RemovingMemberQualificationTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()
    private val qualificationId = QualificationIdFixture.arbitrary()

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null

    private var testee: RemovingMemberQualification? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureUserRepository(userRepository)

        testee = RemovingMemberQualification()
    }

    @Test
    fun `given user can be found and has qualification when removing member qualification then sourcing event is created and stored`() {
        // given
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            memberQualifications = setOf(qualificationId)
        )

        val expectedSourcingEvent = MemberQualificationRemoved(
            userId,
            2,
            adminActor.id,
            correlationId,
            qualificationId
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.removeMemberQualification(
            adminActor,
            correlationId,
            userId,
            qualificationId
        )

        // then
        assertThat(result).isNone()
        val inOrder = inOrder(userRepository!!)
        inOrder.verify(userRepository!!).getById(userId)
        inOrder.verify(userRepository!!).store(expectedSourcingEvent)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun `given triggered by domain event when removing member qualification then sourcing event is created and stored`() {
        // given
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            memberQualifications = setOf(qualificationId)
        )

        val actorId = UserIdFixture.arbitrary()

        val domainEvent = QualificationDeleted(
            actorId,
            Clock.System.now(),
            correlationId,
            qualificationId
        )

        val expectedSourcingEvent = MemberQualificationRemoved(
            userId,
            2,
            actorId,
            correlationId,
            qualificationId
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.removeMemberQualification(
            domainEvent,
            userId,
            qualificationId
        )

        // then
        assertThat(result).isNone()
        val inOrder = inOrder(userRepository!!)
        inOrder.verify(userRepository!!).getById(userId)
        inOrder.verify(userRepository!!).store(expectedSourcingEvent)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun `given user cannot be found when removing member qualification then returns error`() {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(userRepository!!.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee!!.removeMemberQualification(
            adminActor,
            correlationId,
            userId,
            qualificationId
        )

        // then
        assertThat(result).isSome().isEqualTo(error)
    }

    @Test
    fun `given user can be found but not has qualification when removing member qualification then returns error`() {
        // given
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            memberQualifications = setOf()
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee!!.removeMemberQualification(
            adminActor,
            correlationId,
            userId,
            qualificationId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(
                Error.MemberQualificationNotFound(
                    "Not able to find member qualification with id $qualificationId.",
                    qualificationId
                )
            )
    }

    @Test
    fun `given sourcing event cannot be stored when removing member qualification then returns error`() {
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            memberQualifications = setOf(qualificationId)
        )

        val expectedSourcingEvent = MemberQualificationRemoved(
            userId,
            2,
            adminActor.id,
            correlationId,
            qualificationId
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(error.some())

        val result = testee!!.removeMemberQualification(
            adminActor,
            correlationId,
            userId,
            qualificationId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}