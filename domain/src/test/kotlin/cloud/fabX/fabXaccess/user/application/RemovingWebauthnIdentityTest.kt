package cloud.fabX.fabXaccess.user.application

import FixedClock
import arrow.core.None
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdentityFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.WebauthnIdentityRemoved
import isNone
import isSome
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class RemovingWebauthnIdentityTest {
    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository

    private lateinit var testee: RemovingWebauthnIdentity

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository

        testee = RemovingWebauthnIdentity({ logger }, userRepository, fixedClock)
    }

    @Test
    fun `given user and identity can be found when removing identity then sourcing event is created and stored`() =
        runTest {
            // given
            val credentialId = byteArrayOf(1, 2, 3, 4)

            val user = UserFixture.arbitrary(
                userId,
                aggregateVersion = 1,
                identities = setOf(
                    UserIdentityFixture.webauthn(credentialId)
                )
            )

            val expectedSourcingEvent = WebauthnIdentityRemoved(
                userId,
                2,
                adminActor.id,
                fixedInstant,
                correlationId,
                credentialId
            )

            whenever(userRepository.getById(userId))
                .thenReturn(user.right())

            whenever(userRepository.store(expectedSourcingEvent))
                .thenReturn(None)

            // when
            val result = testee.removeWebauthnIdentity(
                adminActor,
                correlationId,
                userId,
                credentialId
            )

            // then
            assertThat(result).isNone()

            val inOrder = inOrder(userRepository)
            inOrder.verify(userRepository).getById(userId)
            inOrder.verify(userRepository).store(expectedSourcingEvent)
        }

    @Test
    fun `given user cannot be found when removing identity then returns error`() = runTest {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.removeWebauthnIdentity(
            adminActor,
            correlationId,
            userId,
            byteArrayOf()
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when removing identity then returns error`() = runTest {
        // given
        val credentialId = byteArrayOf(1, 2, 3, 4)
        val otherCredentialId = byteArrayOf(5, 6, 7, 8)

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            identities = setOf(
                UserIdentityFixture.webauthn(credentialId)
            )
        )

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee.removeWebauthnIdentity(
            adminActor,
            correlationId,
            userId,
            otherCredentialId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with credentialId \"05060708\".",
                    mapOf("credentialId" to "05060708"),
                    correlationId
                )
            )
    }
}