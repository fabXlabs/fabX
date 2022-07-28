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
import cloud.fabX.fabXaccess.qualification.givenQualification
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import cloud.fabX.fabXaccess.user.rest.IsAdminDetails
import cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity
import cloud.fabX.fabXaccess.user.rest.QualificationAdditionDetails
import cloud.fabX.fabXaccess.user.rest.User
import cloud.fabX.fabXaccess.user.rest.UserCreationDetails
import cloud.fabX.fabXaccess.user.rest.UserDetails
import cloud.fabX.fabXaccess.user.rest.UserLockDetails
import cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentity
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
                    null,
                    setOf(),
                    null,
                    false
                ),
                User(
                    "337be01a-fee3-4938-8dc3-c801d37c0e95",
                    3,
                    "Admin",
                    "",
                    "admin",
                    false,
                    null,
                    setOf(),
                    null,
                    true
                ),
                User(
                    userId1,
                    1,
                    "Alan",
                    "Turing",
                    "turing",
                    false,
                    null,
                    setOf(),
                    null,
                    false
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
                    null,
                    setOf(),
                    null,
                    false
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
    fun `when changing user details then returns http no content`() = withTestApp {
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
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)

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
                    null,
                    setOf(),
                    null,
                    false
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
    fun `when changing user lock state then returns http no content`() = withTestApp {
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
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)

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
                    "some notes",
                    setOf(),
                    null,
                    false
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
    fun `when deleting user then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/user/$userId") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
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
                    "UserNotFound",
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

    @Test
    fun `given non-admin when changing is admin then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val requestBody = IsAdminDetails(true)

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/$userId/is-admin") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)

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
                    false,
                    null,
                    setOf(),
                    null,
                    true
                )
            )
    }

    @Test
    fun `given admin when changing is admin then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()
        givenUserIsAdmin(userId, true)

        val requestBody = IsAdminDetails(false)

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/$userId/is-admin") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)

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
                    3,
                    "first",
                    "last",
                    "wiki",
                    false,
                    null,
                    setOf(),
                    null,
                    false
                )
            )
    }

    @Test
    fun `given non-admin authentication when changing is admin then returns http forbidden`() = withTestApp {
        // given
        val userId = givenUser()
        val requestBody = IsAdminDetails(false)

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/$userId/is-admin") {
            addMemberAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when adding instructor qualification then returns http no content`() = withTestApp {
        // given
        val qualificationId = givenQualification()
        val userId = givenUser()

        val requestBody = QualificationAdditionDetails(qualificationId)

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user/$userId/instructor-qualification") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)

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
                    false,
                    null,
                    setOf(),
                    setOf(qualificationId),
                    false
                )
            )
    }

    @Test
    fun `given non-admin authentication when adding instructor qualification then returns http forbidden`() =
        withTestApp {
            // given
            val requestBody = QualificationAdditionDetails(QualificationIdFixture.arbitrary().serialize())

            // when
            val result = handleRequest(
                HttpMethod.Post,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/instructor-qualification"
            ) {
                addMemberAuth()
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(requestBody))
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when removing instructor qualification then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()
        val qualificationId = givenQualification()
        givenUserIsInstructorFor(userId, qualificationId)

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/user/$userId/instructor-qualification/$qualificationId"
        ) {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)

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
                    3,
                    "first",
                    "last",
                    "wiki",
                    false,
                    null,
                    setOf(),
                    null,
                    false
                )
            )
    }

    @Test
    fun `given non-admin authentication when removing instructor qualification then returns http forbidden`() =
        withTestApp {
            // given

            // when
            val result = handleRequest(
                HttpMethod.Delete,
                "/api/v1/user/${
                    UserIdFixture.arbitrary().serialize()
                }/instructor-qualification/${QualificationIdFixture.arbitrary().serialize()}"
            ) {
                addMemberAuth()
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when adding member qualification then returns http no content`() = withTestApp {
        // given
        val qualificationId = givenQualification()

        val instructorUserId = givenUser(wikiName = "instructor")
        val instructorUsername = "instructor123"
        val instructorPassword = "instructorpassword321"
        givenUsernamePasswordIdentity(instructorUserId, instructorUsername, instructorPassword)
        givenUserIsInstructorFor(instructorUserId, qualificationId)

        val userId = givenUser()

        val requestBody = QualificationAdditionDetails(qualificationId)

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user/$userId/member-qualification") {
            addBasicAuth(instructorUsername, instructorPassword)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.content).isNull()
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)

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
                    false,
                    null,
                    setOf(qualificationId),
                    null,
                    false
                )
            )
    }

    @Test
    fun `given non-instructor authentication when adding member qualification then returns http forbidden`() =
        withTestApp {
            // given
            val qualificationId = QualificationIdFixture.arbitrary().serialize()
            val requestBody = QualificationAdditionDetails(qualificationId)

            // when
            val result = handleRequest(
                HttpMethod.Post,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/member-qualification"
            ) {
                addAdminAuth()
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(requestBody))
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when removing member qualification then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()
        val qualificationId = givenQualification()
        val qualificationId2 = givenQualification()
        givenUserHasQualificationFor(userId, qualificationId)
        givenUserHasQualificationFor(userId, qualificationId2)

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/user/$userId/member-qualification/$qualificationId"
        ) {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)

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
                    4,
                    "first",
                    "last",
                    "wiki",
                    false,
                    null,
                    setOf(qualificationId2),
                    null,
                    false
                )
            )
    }

    @Test
    fun `given non-admin authentication when removing member qualification then returns http forbidden`() =
        withTestApp {
            // given
            val qualificationId = givenQualification()

            val instructorUserId = givenUser()
            val instructorUsername = "instructor123"
            val instructorPassword = "instructorPassword123"
            givenUsernamePasswordIdentity(instructorUserId, instructorUsername, instructorPassword)
            givenUserIsInstructorFor(instructorUserId, qualificationId)

            // when
            val result = handleRequest(
                HttpMethod.Delete,
                "/api/v1/user/${
                    UserIdFixture.arbitrary().serialize()
                }/member-qualification/$qualificationId"
            ) {
                addBasicAuth(instructorUsername, instructorPassword)
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when adding username password identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()
        val requestBody = UsernamePasswordIdentity(
            "some.one",
            "supersecret123"
        )

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user/$userId/identity/username-password") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when adding username password identity then returns http forbidden`() =
        withTestApp {
            // given
            val requestBody = UsernamePasswordIdentity(
                "some.one",
                "supersecret123"
            )

            // when
            val result = handleRequest(
                HttpMethod.Post,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/username-password"
            ) {
                addMemberAuth()
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(requestBody))
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when removing username password identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val username = "username123"
        givenUsernamePasswordIdentity(userId, username, "password123")

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/user/$userId/identity/username-password/$username"
        ) {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when removing username password identity then returns http forbidden`() =
        withTestApp {
            // given

            // when
            val result = handleRequest(
                HttpMethod.Delete,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/username-password/username123"
            ) {
                addMemberAuth()
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when adding card identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val requestBody = CardIdentity(cardId, cardSecret)

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user/$userId/identity/card") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when adding card identity then returns http forbidden`() = withTestApp {
        // given
        val requestBody = CardIdentity(
            "11223344556677",
            "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        )

        // when
        val result = handleRequest(
            HttpMethod.Post,
            "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/card"
        ) {
            addMemberAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when removing card identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val cardId = "11223344556677"
        givenCardIdentity(
            userId,
            cardId,
            "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        )

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/user/$userId/identity/card/$cardId"
        ) {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when removing card identity then returns http forbidden`() = withTestApp {
        // given

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/card/AA11BB22CC33DD"
        ) {
            addMemberAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when adding phone number identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val phoneNr = "+49123456789"
        val requestBody = PhoneNrIdentity(phoneNr)

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user/$userId/identity/phone") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when adding phone number identity then returns http forbidden`() = withTestApp {
        // given
        val requestBody = PhoneNrIdentity("+49123456789")

        // when
        val result = handleRequest(
            HttpMethod.Post,
            "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/phone"
        ) {
            addMemberAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when removing phone number identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val phoneNr = "+491123581321"
        givenPhoneNrIdentity(userId, phoneNr)

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/user/$userId/identity/phone/$phoneNr"
        ) {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when removing phone number identity then returns http forbidden`() =
        withTestApp {
            // given

            // when
            val result = handleRequest(
                HttpMethod.Delete,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/phone/+491123581321"
            ) {
                addMemberAuth()
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        }
}