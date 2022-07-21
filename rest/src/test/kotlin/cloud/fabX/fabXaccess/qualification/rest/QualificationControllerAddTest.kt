package cloud.fabX.fabXaccess.qualification.rest

import arrow.core.None
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.RestModule
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@ExperimentalSerializationApi
@MockitoSettings
class QualificationControllerAddTest {
    private lateinit var addingQualification: AddingQualification

    @BeforeEach
    fun `configure RestModule`(
        @Mock gettingQualification: GettingQualification,
        @Mock addingQualification: AddingQualification
    ) {
        this.addingQualification = addingQualification

        RestModule.reset()
        RestModule.configureGettingQualification(gettingQualification)
        RestModule.configureAddingQualification(addingQualification)
    }

    @Test
    fun `when adding qualification then returns http ok`() = withTestApp {
        // given
        val name = "qualification1"
        val description = "some qualification"
        val colour = "#aabbcc"
        val orderNr = 42

        val requestBody = QualificationCreationDetails(name, description, colour, orderNr)

        whenever(
            addingQualification.addQualification(
                any(),
                any(),
                eq(name),
                eq(description),
                eq(colour),
                eq(orderNr)
            )
        ).thenReturn(None)

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/qualification") {
            setBody(Json.encodeToString(requestBody))
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }

        // then

        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content).isNull()
    }
}