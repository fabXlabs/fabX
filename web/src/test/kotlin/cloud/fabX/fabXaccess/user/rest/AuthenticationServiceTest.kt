package cloud.fabX.fabXaccess.user.rest

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.GettingDeviceByIdentity
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
import cloud.fabX.fabXaccess.device.ws.DevicePrincipal
import cloud.fabX.fabXaccess.user.application.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.GettingUserById
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import io.ktor.server.auth.UserPasswordCredential
import isLeft
import isRight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class AuthenticationServiceTest {

    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var gettingUserByIdentity: GettingUserByIdentity
    private lateinit var gettingUserById: GettingUserById
    private lateinit var gettingDeviceByIdentity: GettingDeviceByIdentity

    private lateinit var testee: AuthenticationService

    @BeforeEach
    fun setUp(
        @Mock gettingUserByIdentity: GettingUserByIdentity,
        @Mock gettingUserById: GettingUserById,
        @Mock gettingDeviceByIdentity: GettingDeviceByIdentity
    ) {
        this.gettingUserByIdentity = gettingUserByIdentity
        this.gettingUserById = gettingUserById
        this.gettingDeviceByIdentity = gettingDeviceByIdentity

        testee = AuthenticationService(gettingUserByIdentity, gettingUserById, gettingDeviceByIdentity)
    }

    @Test
    fun `when basic then returns UserPrincipal`() = runTest {
        // given
        val username = "some.one"
        val password = "supersecret42"
        val hash = "mUwJqfJlqOnyvpUc2EcuaU7WXCy0MWQygM7LZAHcgV4="

        val usernamePasswordIdentity = UsernamePasswordIdentity(username, hash)

        val userId = UserIdFixture.arbitrary()
        val user = UserFixture.withIdentity(usernamePasswordIdentity, userId)

        whenever(gettingDeviceByIdentity.getByIdentity(eq(MacSecretIdentity(username, password))))
            .thenReturn(Error.DeviceNotFoundByIdentity("msg").left())

        whenever(
            gettingUserByIdentity.getUserByIdentity(
                eq(SystemActor),
                eq(usernamePasswordIdentity)
            )
        )
            .thenReturn(user.right())

        // when
        val result = testee.basic(UserPasswordCredential(username, password))

        // then
        assertThat(result)
            .isInstanceOf(UserPrincipal::class)
            .transform { it.asMember() }
            .transform { it.userId }
            .isEqualTo(userId)
    }

    @Test
    fun `given invalid username when basic then returns ErrorPrincipal`() = runTest {
        // given
        val invalidUsername = "---"
        val password = "bla"

        whenever(gettingDeviceByIdentity.getByIdentity(eq(MacSecretIdentity(invalidUsername, password))))
            .thenReturn(Error.DeviceNotFoundByIdentity("msg").left())

        // when
        val result = testee.basic(UserPasswordCredential(invalidUsername, password))

        // then
        assertThat(result)
            .isInstanceOf(ErrorPrincipal::class)
            .transform { it.error }
            .isInstanceOf(Error.UsernameInvalid::class)
    }

    @Test
    fun `given user not exists when basic then returns ErrorPrincipal`() = runTest {
        // given
        val username = "some.one"
        val password = "supersecret42"
        val hash = "mUwJqfJlqOnyvpUc2EcuaU7WXCy0MWQygM7LZAHcgV4="

        val usernamePasswordIdentity = UsernamePasswordIdentity(username, hash)

        val error = ErrorFixture.arbitrary()

        whenever(gettingDeviceByIdentity.getByIdentity(eq(MacSecretIdentity(username, password))))
            .thenReturn(Error.DeviceNotFoundByIdentity("msg").left())

        whenever(
            gettingUserByIdentity.getUserByIdentity(
                eq(SystemActor),
                eq(usernamePasswordIdentity)
            )
        )
            .thenReturn(error.left())

        // when
        val result = testee.basic(UserPasswordCredential(username, password))

        // then
        assertThat(result)
            .isInstanceOf(ErrorPrincipal::class)
            .transform { it.error }
            .isSameAs(error)
    }

    @Test
    fun `given device exists when basic then returns DevicePrincipal`() = runTest {
        // given
        val mac = "AABBCCDDEEFF"
        val secret = "abcdef0123456789abcdef0123456789"

        val deviceId = DeviceIdFixture.arbitrary()
        val device = DeviceFixture.arbitrary(deviceId)

        whenever(gettingDeviceByIdentity.getByIdentity(eq(MacSecretIdentity(mac, secret))))
            .thenReturn(device.right())

        whenever(
            gettingUserByIdentity.getUserByIdentity(
                eq(SystemActor),
                eq(UsernamePasswordIdentity(mac, hash(secret)))
            )
        ).thenReturn(Error.UserNotFoundByIdentity("msg").left())

        // when
        val result = testee.basic(UserPasswordCredential(mac, secret))

        // then
        assertThat(result)
            .isInstanceOf(DevicePrincipal::class)
            .transform { it.device }
            .isEqualTo(device)
    }

    @Test
    fun `when jwt then returns UserPrincipal`() = runTest {
        // given
        val userId = UserIdFixture.arbitrary()
        val user = UserFixture.arbitrary(userId)

        whenever(gettingUserById.getUserById(eq(userId)))
            .thenReturn(user.right())

        // when
        val result = testee.jwt(userId.serialize())

        // then
        assertThat(result)
            .isInstanceOf(UserPrincipal::class)
            .transform { it.asMember() }
            .transform { it.userId }
            .isEqualTo(userId)
    }

    @Test
    fun `given user not exists when jwt then returns ErrorPrincipal`() = runTest {
        // given
        val userId = UserIdFixture.arbitrary()

        val error = ErrorFixture.arbitrary()

        whenever(gettingUserById.getUserById(eq(userId)))
            .thenReturn(error.left())

        // when
        val result = testee.jwt(userId.serialize())

        // then
        assertThat(result)
            .isInstanceOf(ErrorPrincipal::class)
            .transform { it.error }
            .isSameAs(error)
    }

    @Test
    fun `given invalid user id when jwt then returns ErrorPrincipal`() = runTest {
        // given
        val invalidUserId = "123"

        // when
        val result = testee.jwt(invalidUserId)

        // then
        assertThat(result)
            .isInstanceOf(ErrorPrincipal::class)
            .transform { it.error }
            .isInstanceOf(Error.UserIdInvalid::class)
    }

    @Test
    fun `given card identity when augmenting on behalf of user then returns actor on behalf of user`() = runTest {
        // given
        val deviceActor = DeviceFixture.arbitrary().asActor()

        val user = UserFixture.arbitrary()

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val cardIdentity = CardIdentity(cardId, cardSecret)

        whenever(
            gettingUserByIdentity.getUserByIdentity(
                SystemActor, correlationId,
                cloud.fabX.fabXaccess.user.model.CardIdentity(cardId, cardSecret)
            )
        ).thenReturn(user.right())

        // when
        val result = testee.augmentDeviceActorOnBehalfOfUser(
            deviceActor,
            cardIdentity,
            null,
            correlationId
        )

        // then
        assertThat(result)
            .isRight()
            .transform { it.onBehalfOf }
            .isEqualTo(user.asMember())
    }

    @Test
    fun `given phone nr identity when augmenting on behalf of user then returns actor on behalf of user`() = runTest {
        // given
        val deviceActor = DeviceFixture.arbitrary().asActor()

        val user = UserFixture.arbitrary()

        val phoneNr = "+49123456789"
        val phoneNrIdentity = PhoneNrIdentity(phoneNr)

        whenever(
            gettingUserByIdentity.getUserByIdentity(
                SystemActor,
                correlationId,
                cloud.fabX.fabXaccess.user.model.PhoneNrIdentity(phoneNr)
            )
        ).thenReturn(user.right())

        // when
        val result = testee.augmentDeviceActorOnBehalfOfUser(deviceActor, null, phoneNrIdentity, correlationId)

        // then
        assertThat(result)
            .isRight()
            .transform { it.onBehalfOf }
            .isEqualTo(user.asMember())
    }

    @Test
    fun `given matching card and phone nr identity when augmenting on behalf of user then returns actor on behalf of user`() =
        runTest {
            // given
            val deviceActor = DeviceFixture.arbitrary().asActor()

            val user = UserFixture.arbitrary()

            val cardId = "11223344556677"
            val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
            val cardIdentity = CardIdentity(cardId, cardSecret)

            val phoneNr = "+49123456789"
            val phoneNrIdentity = PhoneNrIdentity(phoneNr)

            whenever(
                gettingUserByIdentity.getUserByIdentity(
                    SystemActor, correlationId,
                    cloud.fabX.fabXaccess.user.model.CardIdentity(cardId, cardSecret)
                )
            ).thenReturn(user.right())

            whenever(
                gettingUserByIdentity.getUserByIdentity(
                    SystemActor,
                    correlationId,
                    cloud.fabX.fabXaccess.user.model.PhoneNrIdentity(phoneNr)
                )
            ).thenReturn(user.right())

            // when
            val result =
                testee.augmentDeviceActorOnBehalfOfUser(deviceActor, cardIdentity, phoneNrIdentity, correlationId)

            // then
            assertThat(result)
                .isRight()
                .transform { it.onBehalfOf }
                .isEqualTo(user.asMember())
        }

    @Test
    fun `given non-matching card and phone nr identity when augmenting on behalf of user then returns error`() =
        runTest {
            // given
            val deviceActor = DeviceFixture.arbitrary().asActor()

            val user = UserFixture.arbitrary()
            val otherUser = UserFixture.arbitrary()
            assertThat(user.id).isNotEqualTo(otherUser.id)

            val cardId = "11223344556677"
            val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
            val cardIdentity = CardIdentity(cardId, cardSecret)

            val phoneNr = "+49123456789"
            val phoneNrIdentity = PhoneNrIdentity(phoneNr)

            whenever(
                gettingUserByIdentity.getUserByIdentity(
                    SystemActor, correlationId,
                    cloud.fabX.fabXaccess.user.model.CardIdentity(cardId, cardSecret)
                )
            ).thenReturn(user.right())

            whenever(
                gettingUserByIdentity.getUserByIdentity(
                    SystemActor,
                    correlationId,
                    cloud.fabX.fabXaccess.user.model.PhoneNrIdentity(phoneNr)
                )
            ).thenReturn(otherUser.right())

            // when
            val result =
                testee.augmentDeviceActorOnBehalfOfUser(deviceActor, cardIdentity, phoneNrIdentity, correlationId)

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(Error.NotAuthenticated("Required authentication not found."))
        }

    @Test
    fun `given no user identity when augmenting on behalf of user then returns actor not acting on behalf of user`() =
        runTest {
            // given
            val deviceActor = DeviceFixture.arbitrary().asActor()

            // when
            val result =
                testee.augmentDeviceActorOnBehalfOfUser(deviceActor, null, null, correlationId)

            // then
            assertThat(result)
                .isRight()
                .transform { it.onBehalfOf }
                .isNull()
        }

    @Test
    fun `given invalid card identity when augmenting on behalf of user then returns error`() = runTest {
        // given
        val deviceActor = DeviceFixture.arbitrary().asActor()

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val cardIdentity = CardIdentity(cardId, cardSecret)

        val error = ErrorFixture.arbitrary()

        whenever(
            gettingUserByIdentity.getUserByIdentity(
                SystemActor, correlationId,
                cloud.fabX.fabXaccess.user.model.CardIdentity(cardId, cardSecret)
            )
        ).thenReturn(error.left())

        // when
        val result = testee.augmentDeviceActorOnBehalfOfUser(
            deviceActor,
            cardIdentity,
            null,
            correlationId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given invalid phone nr identity when augmenting on behalf of user then returns error`() = runTest {
        // given
        val deviceActor = DeviceFixture.arbitrary().asActor()

        val phoneNr = "+49123456789"
        val phoneNrIdentity = PhoneNrIdentity(phoneNr)

        val error = ErrorFixture.arbitrary()

        whenever(
            gettingUserByIdentity.getUserByIdentity(
                SystemActor,
                correlationId,
                cloud.fabX.fabXaccess.user.model.PhoneNrIdentity(phoneNr)
            )
        ).thenReturn(error.left())

        // when
        val result = testee.augmentDeviceActorOnBehalfOfUser(deviceActor, null, phoneNrIdentity, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}