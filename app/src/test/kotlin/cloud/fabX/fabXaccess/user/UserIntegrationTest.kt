package cloud.fabX.fabXaccess.user

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.isError
import cloud.fabX.fabXaccess.common.memberAuth
import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.qualification.givenQualification
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import cloud.fabX.fabXaccess.user.rest.IsAdminDetails
import cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity
import cloud.fabX.fabXaccess.user.rest.PinIdentityDetails
import cloud.fabX.fabXaccess.user.rest.QualificationAdditionDetails
import cloud.fabX.fabXaccess.user.rest.User
import cloud.fabX.fabXaccess.user.rest.UserCreationDetails
import cloud.fabX.fabXaccess.user.rest.UserDetails
import cloud.fabX.fabXaccess.user.rest.UserLockDetails
import cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentityAdditionDetails
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

// TODO remove InternalAPI annotation (no longer necessary?)
@InternalAPI
@ExperimentalSerializationApi
internal class UserIntegrationTest {

    @Test
    fun `given no authentication when get users then returns http unauthorized`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/user")

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given invalid authentication when get users then returns http unauthorized`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/user") {
            basicAuth("no.body", "nobodyssecret")
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given non-admin authentication when get users then returns http forbidden`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/user") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
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
        val response = c().get("/api/v1/user") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Set<User>>())
            .containsExactlyInAnyOrder(
                User(
                    "c63b3a7d-bd18-4272-b4ed-4bcf9683c602",
                    2,
                    "Member",
                    "",
                    "member",
                    false,
                    null,
                    setOf(UsernamePasswordIdentity("member")),
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
                    setOf(UsernamePasswordIdentity("admin")),
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
        val response = c().get("/api/v1/user/$userId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<User>())
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
                    setOf(),
                    null,
                    false
                )
            )
    }

    @Test
    fun `when get me then returns user`() = withTestApp {
        // given
        val userId = givenUser(
            "Alan",
            "Turing",
            "turing"
        )
        givenUsernamePasswordIdentity(
            userId,
            "alan.turing",
            "enigma"
        )

        // when
        val response = c().get("/api/v1/user/me") {
            basicAuth("alan.turing", "enigma")
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<User>())
            .isEqualTo(
                User(
                    userId,
                    2,
                    "Alan",
                    "Turing",
                    "turing",
                    false,
                    null,
                    setOf(UsernamePasswordIdentity("alan.turing")),
                    setOf(),
                    null,
                    false
                )
            )
    }

    @Test
    fun `given instructor authentication when getting user id by wiki name then returns id`() = withTestApp {
        // given
        val userId = givenUser(
            "Alan",
            "Turing",
            "turing"
        )

        val qualificationId = givenQualification()
        val instructorId = givenUser(
            "Some",
            "Instructor",
            "instructor"
        )
        givenUsernamePasswordIdentity(instructorId, "instructor", "instructorpassword")
        givenUserIsInstructorFor(instructorId, qualificationId)

        // when
        val response = c().get("/api/v1/user/id-by-wiki-name") {
            url {
                parameters.append("wikiName", "turing")
            }
            basicAuth("instructor", "instructorpassword")
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        println(response.bodyAsText())
        assertThat(response.body<String>()).isEqualTo(userId)
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
        val response = c().post("/api/v1/user") {
            memberAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
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
        val response = c().put("/api/v1/user/$userId") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)

        val responseGet = c().get("/api/v1/user/$userId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<User>())
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
        val response = c().put("/api/v1/user/${UserIdFixture.arbitrary().serialize()}") {
            memberAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
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
        val response = c().put("/api/v1/user/$userId/lock") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)

        val responseGet = c().get("/api/v1/user/$userId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<User>())
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
        val response = c().put("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/lock") {
            memberAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when deleting user then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        // when
        val response = c().delete("/api/v1/user/$userId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()

        val responseGet = c().get("/api/v1/user/$userId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(responseGet.body<Error>())
            .isError(
                "UserNotFound",
                "User with id UserId(value=$userId) not found.",
                mapOf("userId" to userId)
            )
    }

    @Test
    fun `given non-admin authentication when deleting user then returns http forbidden`() = withTestApp {
        // given

        // when
        val response = c().delete("/api/v1/user/${UserIdFixture.arbitrary().serialize()}") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `given non-admin when changing is admin then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val requestBody = IsAdminDetails(true)

        // when
        val response = c().put("/api/v1/user/$userId/is-admin") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)

        val responseGet = c().get("/api/v1/user/$userId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<User>())
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
        val response = c().put("/api/v1/user/$userId/is-admin") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)

        val responseGet = c().get("/api/v1/user/$userId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<User>())
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
        val response = c().put("/api/v1/user/$userId/is-admin") {
            memberAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `given soft-deleted user when getting soft-deleted users then returns user`() = withTestApp {
        // given
        val userId = givenUser()
        givenUserIsSoftDeleted(userId)

        // when
        val response = c().get("/api/v1/user/soft-deleted") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val softDeletedUsers = response.body<Set<User>>()
        assertThat(softDeletedUsers).hasSize(1)
        assertThat(softDeletedUsers.first())
            .all {
                transform { it.id }.isEqualTo(userId)
                transform { it.aggregateVersion }.isEqualTo(1)
            }
    }

    @Test
    fun `given non-admin authentication when getting soft-deleted users then returns http forbidden`() = withTestApp {
        // given
        // when
        val response = c().get("/api/v1/user/soft-deleted") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }


    @Test
    fun `when adding instructor qualification then returns http no content`() = withTestApp {
        // given
        val qualificationId = givenQualification()
        val userId = givenUser()

        val requestBody = QualificationAdditionDetails(qualificationId)

        // when
        val response = c().post("/api/v1/user/$userId/instructor-qualification") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)

        val responseGet = c().get("/api/v1/user/$userId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<User>())
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
            val response = c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/instructor-qualification") {
                memberAuth()
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when removing instructor qualification then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()
        val qualificationId = givenQualification()
        givenUserIsInstructorFor(userId, qualificationId)

        // when
        val response = c().delete("/api/v1/user/$userId/instructor-qualification/$qualificationId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)

        val responseGet = c().get("/api/v1/user/$userId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<User>())
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
            val response = c().delete(
                "/api/v1/user/${
                    UserIdFixture.arbitrary().serialize()
                }/instructor-qualification/${QualificationIdFixture.arbitrary().serialize()}"
            ) {
                memberAuth()
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
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
        val response = c().post("/api/v1/user/$userId/member-qualification") {
            basicAuth(instructorUsername, instructorPassword)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.bodyAsText()).isEmpty()
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)

        val responseGet = c().get("/api/v1/user/$userId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<User>())
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
            val response = c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/member-qualification") {
                adminAuth()
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
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
        val response = c().delete("/api/v1/user/$userId/member-qualification/$qualificationId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)

        val responseGet = c().get("/api/v1/user/$userId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<User>())
            .isEqualTo(
                User(
                    userId,
                    4,
                    "first",
                    "last",
                    "wiki",
                    false,
                    null,
                    setOf(),
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
            val response = c().delete(
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/member-qualification/$qualificationId"
            ) {
                basicAuth(instructorUsername, instructorPassword)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when adding username password identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()
        val requestBody = UsernamePasswordIdentityAdditionDetails(
            "some.one",
            "supersecret123"
        )

        // when
        val response = c().post("/api/v1/user/$userId/identity/username-password") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when adding username password identity then returns http forbidden`() =
        withTestApp {
            // given
            val requestBody = UsernamePasswordIdentityAdditionDetails(
                "some.one",
                "supersecret123"
            )

            // when
            val response =
                c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/username-password") {
                    memberAuth()
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when removing username password identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val username = "username123"
        givenUsernamePasswordIdentity(userId, username, "password123")

        // when
        val response = c().delete("/api/v1/user/$userId/identity/username-password/$username") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when removing username password identity then returns http forbidden`() =
        withTestApp {
            // given

            // when
            val response = c().delete(
                "/api/v1/user/${
                    UserIdFixture.arbitrary().serialize()
                }/identity/username-password/username123"
            ) {
                memberAuth()
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when adding card identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val requestBody = CardIdentity(cardId, cardSecret)

        // when
        val response = c().post("/api/v1/user/$userId/identity/card") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when adding card identity then returns http forbidden`() = withTestApp {
        // given
        val requestBody = CardIdentity(
            "11223344556677",
            "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        )

        // when
        val response = c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/card") {
            memberAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
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
        val response = c().delete("/api/v1/user/$userId/identity/card/$cardId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when removing card identity then returns http forbidden`() = withTestApp {
        // given

        // when
        val response = c().delete(
            "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/card/AA11BB22CC33DD"
        ) {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when adding phone number identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val phoneNr = "+49123456789"
        val requestBody = PhoneNrIdentity(phoneNr)

        // when
        val response = c().post("/api/v1/user/$userId/identity/phone") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when adding phone number identity then returns http forbidden`() =
        withTestApp {
            // given
            val requestBody = PhoneNrIdentity("+49123456789")

            // when
            val response = c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/phone") {
                memberAuth()
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when removing phone number identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val phoneNr = "+491123581321"
        givenPhoneNrIdentity(userId, phoneNr)

        // when
        val response = c().delete("/api/v1/user/$userId/identity/phone/$phoneNr") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when removing phone number identity then returns http forbidden`() =
        withTestApp {
            // given

            // when
            val response =
                c().delete("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/phone/+491123581321") {
                    memberAuth()
                }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        }

    @Test
    fun `when adding pin identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()

        val pin = "7658"
        val requestBody = PinIdentityDetails(pin)

        // when
        val response = c().post("/api/v1/user/$userId/identity/pin") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when adding pin identity then returns http forbidden`() = withTestApp {
        // given
        val requestBody = PinIdentityDetails("7658")

        // when
        val response = c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/pin") {
            memberAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when removing pin identity then returns http no content`() = withTestApp {
        // given
        val userId = givenUser()
        givenPinIdentity(userId, "4356")

        // when
        val response = c().delete("/api/v1/user/$userId/identity/pin") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `given non-admin authentication when removing pin identity then returns http forbidden`() = withTestApp {
        // given

        // when
        val response =
            c().delete("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/pin") {
                memberAuth()
            }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }
}