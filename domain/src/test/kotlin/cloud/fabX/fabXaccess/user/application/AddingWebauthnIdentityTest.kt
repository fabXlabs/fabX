package cloud.fabX.fabXaccess.user.application

import FixedClock
import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdentityFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.WebauthnIdentityAdded
import com.webauthn4j.authenticator.Authenticator
import isNone
import isSome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class AddingWebauthnIdentityTest {

    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository
    private lateinit var webauthnService: WebauthnIdentityService

    private lateinit var testee: AddingWebauthnIdentity

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock webauthnService: WebauthnIdentityService
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.webauthnService = webauthnService

        testee = AddingWebauthnIdentity({ logger }, userRepository, webauthnService, fixedClock)
    }

    @Test
    fun `given user can be found when adding identity, then sourcing event is created and stored`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, identities = setOf())

        val challenge = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val attestationObject = byteArrayOf(1, 2, 3)
        val clientDataJson = byteArrayOf(4, 5)

        val authenticator = UserIdentityFixture.webauthn().authenticator

        val expectedSourcingEvent = WebauthnIdentityAdded(
            userId,
            2,
            userId,
            fixedInstant,
            correlationId,
            authenticator
        )

        whenever(webauthnService.getChallenge(userId))
            .thenReturn(challenge.right())

        whenever(
            webauthnService.parseAndValidateRegistration(
                correlationId,
                challenge,
                attestationObject,
                clientDataJson
            )
        ).thenReturn(authenticator.right())

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee.addWebauthnIdentity(
            user.asMember(),
            correlationId,
            userId,
            attestationObject,
            clientDataJson
        )

        // then
        assertThat(result).isNone()

        val inOrder = inOrder(webauthnService, userRepository)
        inOrder.verify(webauthnService).getChallenge(userId)
        inOrder.verify(webauthnService).parseAndValidateRegistration(
            correlationId,
            challenge,
            attestationObject,
            clientDataJson
        )
        inOrder.verify(userRepository).getById(userId)
        inOrder.verify(userRepository).store(expectedSourcingEvent)
    }

    @Test
    fun `given user cannot be found when adding identity then returns error`(
        @Mock authenticator: Authenticator
    ) = runTest {
        // given
        val error = Error.UserNotFound("message", userId)

        val challenge = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val attestationObject = byteArrayOf(1, 2, 3)
        val clientDataJson = byteArrayOf(4, 5)

        whenever(webauthnService.getChallenge(userId))
            .thenReturn(challenge.right())

        whenever(
            webauthnService.parseAndValidateRegistration(
                correlationId,
                challenge,
                attestationObject,
                clientDataJson
            )
        ).thenReturn(authenticator.right())

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.addWebauthnIdentity(
            UserFixture.arbitrary().asMember(),
            correlationId,
            userId,
            attestationObject,
            clientDataJson
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when adding identity then returns error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, identities = setOf())

        val challenge = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val attestationObject = byteArrayOf(1, 2, 3)
        val clientDataJson = byteArrayOf(4, 5)

        val authenticator = UserIdentityFixture.webauthn().authenticator

        val expectedSourcingEvent = WebauthnIdentityAdded(
            userId,
            2,
            userId,
            fixedInstant,
            correlationId,
            authenticator
        )

        val error = ErrorFixture.arbitrary()

        whenever(webauthnService.getChallenge(userId))
            .thenReturn(challenge.right())

        whenever(
            webauthnService.parseAndValidateRegistration(
                correlationId,
                challenge,
                attestationObject,
                clientDataJson
            )
        ).thenReturn(authenticator.right())

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee.addWebauthnIdentity(
            user.asMember(),
            correlationId,
            userId,
            attestationObject,
            clientDataJson
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when adding identity then returns domain error`(
        @Mock authenticator: Authenticator
    ) = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, identities = setOf())

        val challenge = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val attestationObject = byteArrayOf(1, 2, 3)
        val clientDataJson = byteArrayOf(4, 5)

        val expectedDomainError = Error.UserNotActor("User is not actor.", correlationId)

        whenever(webauthnService.getChallenge(userId))
            .thenReturn(challenge.right())

        whenever(
            webauthnService.parseAndValidateRegistration(
                correlationId,
                challenge,
                attestationObject,
                clientDataJson
            )
        ).thenReturn(authenticator.right())

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee.addWebauthnIdentity(
            UserFixture.arbitrary().asMember(),
            correlationId,
            userId,
            attestationObject,
            clientDataJson
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given error while parsing and validating registration when adding identity then returns error`(
        @Mock authenticator: Authenticator
    ) = runTest {
        // given
        val challenge = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val attestationObject = byteArrayOf(1, 2, 3)
        val clientDataJson = byteArrayOf(4, 5)

        val error = ErrorFixture.arbitrary()

        whenever(webauthnService.getChallenge(userId))
            .thenReturn(challenge.right())

        whenever(
            webauthnService.parseAndValidateRegistration(
                correlationId,
                challenge,
                attestationObject,
                clientDataJson
            )
        ).thenReturn(error.left())

        // when
        val result = testee.addWebauthnIdentity(
            UserFixture.arbitrary().asMember(),
            correlationId,
            userId,
            attestationObject,
            clientDataJson
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given error while getting challenge when adding identity then returns error`() = runTest {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(webauthnService.getChallenge(userId))
            .thenReturn(error.left())

        // when
        val result = testee.addWebauthnIdentity(
            UserFixture.arbitrary().asMember(),
            correlationId,
            userId,
            byteArrayOf(),
            byteArrayOf()
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}