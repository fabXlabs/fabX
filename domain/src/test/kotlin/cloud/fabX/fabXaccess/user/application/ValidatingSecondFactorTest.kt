package cloud.fabX.fabXaccess.user.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.user.model.Member
import cloud.fabX.fabXaccess.user.model.PinIdentity
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isLeft
import isRight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class ValidatingSecondFactorTest {
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository

    private lateinit var testee: ValidatingSecondFactor

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
    ) {
        this.logger = logger
        this.userRepository = userRepository

        testee = ValidatingSecondFactor({ logger }, userRepository)
    }

    @Test
    fun `given valid second factor when validating second factor then returns unit`() = runTest {
        // given
        val userId = UserIdFixture.arbitrary()
        val pinIdentity = PinIdentity("1234")
        val user = UserFixture.arbitrary(userId, identities = setOf(pinIdentity))

        val deviceId = DeviceIdFixture.arbitrary()
        val member = Member(userId, "some member", setOf())
        val deviceActorOnBehalfOfMember = DeviceActor(deviceId, "aabbcc112233", onBehalfOf = member)

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee.validateSecondFactor(
            deviceActorOnBehalfOfMember,
            correlationId,
            pinIdentity
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given invalid second factor when validating second factor then returns unit`() = runTest {
        // given
        val userId = UserIdFixture.arbitrary()
        val user = UserFixture.arbitrary(userId, identities = setOf())

        val deviceId = DeviceIdFixture.arbitrary()
        val member = Member(userId, "some member", setOf())
        val deviceActorOnBehalfOfMember = DeviceActor(deviceId, "aabbcc112233", onBehalfOf = member)

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee.validateSecondFactor(
            deviceActorOnBehalfOfMember,
            correlationId,
            PinIdentity("0000")
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.InvalidSecondFactor("Invalid second factor provided.", correlationId))
    }

    @Test
    fun `given user cannot be found when validating second factor then returns error`() = runTest {
        // given
        val userId = UserIdFixture.arbitrary()
        val deviceId = DeviceIdFixture.arbitrary()
        val member = Member(userId, "some member", setOf())
        val deviceActorOnBehalfOfMember = DeviceActor(deviceId, "aabbcc112233", onBehalfOf = member)

        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.validateSecondFactor(
            deviceActorOnBehalfOfMember,
            correlationId,
            PinIdentity("0000")
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}