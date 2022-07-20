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
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.PhoneNrIdentityRemoved
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdentityFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isNone
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class RemovingPhoneNrIdentityTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null

    private var testee: RemovingPhoneNrIdentity? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository
        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureUserRepository(userRepository)

        testee = RemovingPhoneNrIdentity()
    }

    @Test
    fun `given user and identity can be found when removing identity then sourcing event is created and stored`() {
        // given
        val phoneNr = "+491234567890"

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            identities = setOf(
                UserIdentityFixture.phoneNr(phoneNr)
            )
        )

        val expectedSourcingEvent = PhoneNrIdentityRemoved(
            userId,
            2,
            adminActor.id,
            correlationId,
            phoneNr
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.removePhoneNrIdentity(
            adminActor,
            correlationId,
            userId,
            phoneNr
        )

        // then
        assertThat(result).isNone()

        val inOrder = inOrder(userRepository!!)
        inOrder.verify(userRepository!!).getById(userId)
        inOrder.verify(userRepository!!).store(expectedSourcingEvent)
    }

    @Test
    fun `given user cannot be found when removing identity then returns error`() {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository!!.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee!!.removePhoneNrIdentity(
            adminActor,
            correlationId,
            userId,
            "username"
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when adding identity then returns error`() {
        // given
        val phoneNr = "+491234567890"

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            identities = setOf(
                UserIdentityFixture.phoneNr(phoneNr)
            )
        )

        val expectedSourcingEvent = PhoneNrIdentityRemoved(
            userId,
            2,
            adminActor.id,
            correlationId,
            phoneNr
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee!!.removePhoneNrIdentity(
            adminActor,
            correlationId,
            userId,
            phoneNr
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when removing identity then error is returned`() {
        // given
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            identities = setOf()
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee!!.removePhoneNrIdentity(
            adminActor,
            correlationId,
            userId,
            "+49123"
        )

        // then
        assertThat(result)
            .isSome()

        val inOrder = inOrder(userRepository!!)
        inOrder.verify(userRepository!!).getById(userId)
    }
}