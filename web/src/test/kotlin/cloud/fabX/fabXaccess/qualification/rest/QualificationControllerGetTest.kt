package cloud.fabX.fabXaccess.qualification.rest

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class QualificationControllerGetTest {
    private lateinit var gettingQualification: GettingQualification
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary()

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingQualification: GettingQualification,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingQualification = gettingQualification
        this.authenticationService = authenticationService

        runTest {
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))
        }
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingQualification }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `given no qualifications when get qualifications then returns empty set`() = withConfiguredTestApp {
        // given
        whenever(gettingQualification.getAll(eq(actingUser.asMember()), any()))
            .thenReturn(setOf())

        // when
        val response = c().get("/api/v1/qualification") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Set<Qualification>>())
            .isEqualTo(setOf())
    }

    @Test
    fun `when get qualifications then returns mapped qualifications`() = withConfiguredTestApp {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualification1 = QualificationFixture.arbitrary(
            qualificationId1,
            41,
            "qualification1",
            "description 1",
            "#111111",
            1
        )

        val qualificationId2 = QualificationIdFixture.arbitrary()
        val qualification2 = QualificationFixture.arbitrary(
            qualificationId2,
            42,
            "qualification2",
            "description 2",
            "#222222",
            2
        )

        whenever(gettingQualification.getAll(eq(actingUser.asMember()), any()))
            .thenReturn(setOf(qualification1, qualification2))

        val mappedQualification1 = Qualification(
            qualificationId1.serialize(),
            41,
            "qualification1",
            "description 1",
            "#111111",
            1
        )

        val mappedQualification2 = Qualification(
            qualificationId2.serialize(),
            42,
            "qualification2",
            "description 2",
            "#222222",
            2
        )

        // when
        val response = c().get("/api/v1/qualification") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Set<Qualification>>())
            .containsExactlyInAnyOrder(mappedQualification1, mappedQualification2)
    }

    @Test
    fun `given qualification exists when get qualification by id then returns mapped qualification`() =
        withConfiguredTestApp {
            // given
            val qualificationId = QualificationIdFixture.arbitrary()
            val qualification = QualificationFixture.arbitrary(
                qualificationId,
                41,
                "qualification1",
                "description 1",
                "#111111",
                1
            )

            val mappedQualification = Qualification(
                qualificationId.serialize(),
                41,
                "qualification1",
                "description 1",
                "#111111",
                1
            )

            whenever(gettingQualification.getById(eq(actingUser.asMember()), any(), eq(qualificationId)))
                .thenReturn(qualification.right())

            // when
            val response = c().get("/api/v1/qualification/${qualificationId.serialize()}") {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.body<Qualification>())
                .isEqualTo(mappedQualification)
        }

    @Test
    fun `given qualification not found when get qualification by id then returns mapped error`() =
        withConfiguredTestApp {
            // given
            val qualificationId = QualificationIdFixture.arbitrary()
            val error = Error.QualificationNotFound("msg", qualificationId)

            whenever(gettingQualification.getById(eq(actingUser.asMember()), any(), eq(qualificationId)))
                .thenReturn(error.left())

            // when
            val response = c().get("/api/v1/qualification/${qualificationId.serialize()}") {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
            assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
                .isError(
                    "QualificationNotFound",
                    "msg",
                    mapOf("qualificationId" to qualificationId.serialize())
                )
        }
}