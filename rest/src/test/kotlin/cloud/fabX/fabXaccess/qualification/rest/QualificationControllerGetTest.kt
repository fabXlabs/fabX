package cloud.fabX.fabXaccess.qualification.rest

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.RestModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.isJson
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@ExperimentalSerializationApi
@MockitoSettings
internal class QualificationControllerGetTest {
    private var gettingQualification: GettingQualification? = null

    @BeforeEach
    fun `configure RestModule`(
        @Mock gettingQualification: GettingQualification,
        @Mock addingQualification: AddingQualification
    ) {
        this.gettingQualification = gettingQualification

        RestModule.reset()
        RestModule.configureGettingQualification(gettingQualification)
        RestModule.configureAddingQualification(addingQualification)
    }

    @Test
    fun `given no qualifications when get qualifications then returns empty set`() = withTestApp {
        // given
        whenever(gettingQualification!!.getAll(any(), any()))
            .thenReturn(setOf())

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification")

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Qualification>>()
            .isEqualTo(setOf())
    }

    @Test
    fun `when get qualifications then returns mapped qualifications`() = withTestApp {
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

        whenever(gettingQualification!!.getAll(any(), any()))
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
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification")

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Qualification>>()
            .containsExactlyInAnyOrder(mappedQualification1, mappedQualification2)
    }

    @Test
    fun `given qualification exists when get qualification by id then returns mapped qualification`() = withTestApp {
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

        whenever(gettingQualification!!.getById(any(), any(), eq(qualificationId)))
            .thenReturn(qualification.right())

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification/${qualificationId.serialize()}")

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Qualification>()
            .isEqualTo(mappedQualification)
    }

    @Test
    fun `given qualification not found when get qualification by id then returns mapped error`() = withTestApp {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()
        val error = Error.QualificationNotFound("msg", qualificationId)

        whenever(gettingQualification!!.getById(any(), any(), eq(qualificationId)))
            .thenReturn(error.left())

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification/${qualificationId.serialize()}")

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NotFound)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<cloud.fabX.fabXaccess.common.rest.Error>()
            .isEqualTo(
                cloud.fabX.fabXaccess.common.rest.Error(
                    "msg",
                    mapOf("qualificationId" to qualificationId.serialize())
                )
            )
    }
}