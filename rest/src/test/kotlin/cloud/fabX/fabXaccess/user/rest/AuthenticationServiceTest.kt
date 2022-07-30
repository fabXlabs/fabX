package cloud.fabX.fabXaccess.user.rest

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isSameAs
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.user.application.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import io.ktor.auth.UserPasswordCredential
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

    private lateinit var gettingUserByIdentity: GettingUserByIdentity

    private lateinit var testee: AuthenticationService

    @BeforeEach
    fun setUp(
        @Mock gettingUserByIdentity: GettingUserByIdentity
    ) {
        this.gettingUserByIdentity = gettingUserByIdentity

        testee = AuthenticationService(gettingUserByIdentity)
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

        // when
        val result = testee.basic(UserPasswordCredential(invalidUsername, "bla"))

        // then
        assertThat(result)
            .isInstanceOf(ErrorPrincipal::class)
            .transform { it.error }
            .isInstanceOf(Error.UsernameInvalid::class)
    }

    @Test
    fun `given user not exists when basic then returns null`() = runTest {
        // given
        val username = "some.one"
        val password = "supersecret42"
        val hash = "mUwJqfJlqOnyvpUc2EcuaU7WXCy0MWQygM7LZAHcgV4="

        val usernamePasswordIdentity = UsernamePasswordIdentity(username, hash)

        val error = ErrorFixture.arbitrary()

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
}