package cloud.fabX.fabXaccess.user

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.user.rest.TokenResponse
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class UserAuthenticationIntegrationTest {

    @Test
    fun `given username password identity when http request then can authenticate via http basic auth`() =
        withTestApp {
            // given
            val userId = givenUser()
            val username = "username123"
            val password = "password123"
            givenUsernamePasswordIdentity(userId, username, password)

            // when
            val response = c().get("/api/v1/tool") {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        }

    @Test
    fun `given removed username password identity when http request then no longer can authenticate via http basic auth`() =
        withTestApp {
            // given
            val userId = givenUser()
            val username = "username123"
            val password = "password123"
            givenUsernamePasswordIdentity(userId, username, password)

            val removeResponse = c().delete("/api/v1/user/$userId/identity/username-password/$username") {
                adminAuth()
            }
            assertThat(removeResponse.status).isEqualTo(HttpStatusCode.NoContent)

            // when
            val response = c().get("/api/v1/tool") {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }

    @Test
    fun `given basic auth when getting jwt then returns jwt`() = withTestApp {
        // given
        val userId = givenUser()
        val username = "username123"
        val password = "password123"
        givenUsernamePasswordIdentity(userId, username, password)

        // when
        val response = c().get("/api/v1/login") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<TokenResponse>())
            .transform { it.token }
            .isNotEmpty()
    }

    @Test
    fun `given invalid basic auth when getting jwt then returns http unauthorized`() = withTestApp {
        // given
        val invalidUsername = "invalidUsername"
        val invalidPassword = "invalidPassword"

        // when
        val response = c().get("/api/v1/login") {
            basicAuth(invalidUsername, invalidPassword)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given jwt auth when getting jwt then returns http forbidden`() = withTestApp {
        // given
        val userId = givenUser()
        val username = "username123"
        val password = "password123"
        givenUsernamePasswordIdentity(userId, username, password)

        val loginResponse = c().get("/api/v1/login") {
            basicAuth(username, password)
        }
        assertThat(loginResponse.status).isEqualTo(HttpStatusCode.OK)
        val token = loginResponse.body<TokenResponse>().token

        // when
        val response = c().get("/api/v1/login") {
            bearerAuth(token)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given jwt auth when http request then can authenticate`() = withTestApp {
        // given
        val userId = givenUser()
        val username = "username123"
        val password = "password123"
        givenUsernamePasswordIdentity(userId, username, password)

        val loginResponse = c().get("/api/v1/login") {
            basicAuth(username, password)
        }
        assertThat(loginResponse.status).isEqualTo(HttpStatusCode.OK)
        val token = loginResponse.body<TokenResponse>().token

        // when
        val response = c().get("/api/v1/tool") {
            bearerAuth(token)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }
}