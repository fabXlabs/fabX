package cloud.fabX.fabXaccess.user

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.addBasicAuth
import cloud.fabX.fabXaccess.common.addMemberAuth
import cloud.fabX.fabXaccess.common.isJson
import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.rest.User
import cloud.fabX.fabXaccess.user.rest.UserCreationDetails
import cloud.fabX.fabXaccess.user.rest.UserDetails
import cloud.fabX.fabXaccess.user.rest.UserLockDetails
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class UserIntegrationTest {

    @Test
    fun `given no authentication when get users then returns http unauthorized`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/user")

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given invalid authentication when get users then returns http unauthorized`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/user") {
            addBasicAuth("no.body", "nobodyssecret")
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given non-admin authentication when get users then returns http forbidden`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/user") {
            addMemberAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `given admin authentication when get users then returns users`() = withTestApp {
        // given
        val userId1 = givenUser(
            "Alan",
            "Turing",
            "turing"
        )

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/user") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<User>>()
            .containsExactlyInAnyOrder(
                User(
                    "c63b3a7d-bd18-4272-b4ed-4bcf9683c602",
                    2,
                    "Member",
                    "",
                    "member",
                    false,
                    null
                ),
                User(
                    "337be01a-fee3-4938-8dc3-c801d37c0e95",
                    3,
                    "Admin",
                    "",
                    "admin",
                    false,
                    null
                ),
                User(
                    userId1,
                    1,
                    "Alan",
                    "Turing",
                    "turing",
                    false,
                    null
                )
            )
    }

    @Test
    fun `given user when get user by id then returns user`() = withTestApp {
        // given
        val userId = givenUser(
            "Alan",
            "Turing",
            "turing"
        )

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/user/$userId") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<User>()
            .isEqualTo(
                User(
                    userId,
                    1,
                    "Alan",
                    "Turing",
                    "turing",
                    false,
                    null
                )
            )
    }

    @Test
    fun `given non-admin authentication when adding user then returns http forbidden`() = withTestApp {
        // given
        val requestBody = UserCreationDetails(
            "Alan",
            "Turing",
            "turing"
        )

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user") {
            addMemberAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when changing user details then returns http ok`() = withTestApp {
        // given
        val userId = givenUser(firstName = "first", lastName = "last")

        val requestBody = UserDetails(
            ChangeableValue("newFirstName"),
            ChangeableValue("newLastName"),
            null
        )

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/$userId") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)

        val resultGet = handleRequest(HttpMethod.Get, "/api/v1/user/$userId") {
            addAdminAuth()
        }
        assertThat(resultGet.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(resultGet.response.content)
            .isNotNull()
            .isJson<User>()
            .isEqualTo(
                User(
                    userId,
                    2,
                    "newFirstName",
                    "newLastName",
                    "wiki",
                    false,
                    null
                )
            )
    }

    @Test
    fun `given non-admin authentication when changing user details then returns http forbidden`() = withTestApp {
        // given
        val requestBody = UserDetails(
            null,
            null,
            null
        )

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/${UserIdFixture.arbitrary().serialize()}") {
            addMemberAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when changing user lock state then returns http ok`() = withTestApp {
        // given
        val userId = givenUser()

        val requestBody = UserLockDetails(
            ChangeableValue(true),
            ChangeableValue("some notes")
        )

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/$userId/lock") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)

        val resultGet = handleRequest(HttpMethod.Get, "/api/v1/user/$userId") {
            addAdminAuth()
        }
        assertThat(resultGet.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(resultGet.response.content)
            .isNotNull()
            .isJson<User>()
            .isEqualTo(
                User(
                    userId,
                    2,
                    "first",
                    "last",
                    "wiki",
                    true,
                    "some notes"
                )
            )
    }

    @Test
    fun `given non-admin authentication when changing user lock state then returns http forbidden`() = withTestApp {
        // given
        val requestBody = UserLockDetails(
            null,
            null
        )

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/lock") {
            addMemberAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when deleting user then returns http ok`() = withTestApp {
        // given
        val userId = givenUser()

        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/user/$userId") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content).isNull()

        val resultGet = handleRequest(HttpMethod.Get, "/api/v1/user/$userId") {
            addAdminAuth()
        }
        assertThat(resultGet.response.status()).isEqualTo(HttpStatusCode.NotFound)
        assertThat(resultGet.response.content)
            .isNotNull()
            .isJson<Error>()
            .isEqualTo(
                Error(
                    "User with id UserId(value=$userId) not found.",
                    mapOf(
                        "userId" to userId
                    )
                )
            )
    }

    @Test
    fun `given non-admin authentication when deleting user then returns http forbidden`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/user/${UserIdFixture.arbitrary().serialize()}") {
            addMemberAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }
}