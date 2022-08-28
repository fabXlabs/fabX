package cloud.fabX.fabXaccess.user.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.Instructor
import cloud.fabX.fabXaccess.user.model.InstructorFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
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
internal class GettingUserIdByWikiNameTest {

    private val qualificationId = QualificationIdFixture.arbitrary()

    private val instructorActor: Instructor = InstructorFixture.arbitrary(qualifications = setOf(qualificationId))

    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()
    private val wikiName = "some.body"
    private val user = UserFixture.arbitrary(userId = userId)

    private lateinit var logger: Logger
    private lateinit var gettingUserByWikiName: GettingUserByWikiName

    private lateinit var testee: GettingUserIdByWikiName

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock gettingUserByWikiName: GettingUserByWikiName
    ) {
        this.logger = logger
        this.gettingUserByWikiName = gettingUserByWikiName

        testee = GettingUserIdByWikiName({ logger }, gettingUserByWikiName)
    }

    @Test
    fun `when getting user id by wiki name then returns user id`() = runTest {
        // given
        whenever(gettingUserByWikiName.getByWikiName(wikiName))
            .thenReturn(user.right())

        // when
        val result = testee.getUserIdByWikiName(instructorActor, correlationId, wikiName)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(userId)
    }

    @Test
    fun `given user does not exist when getting user id by wiki name then returns error`() = runTest {
        // given
        val expectedError = ErrorFixture.arbitrary()

        whenever(gettingUserByWikiName.getByWikiName(wikiName))
            .thenReturn(expectedError.left())

        // when
        val result = testee.getUserIdByWikiName(instructorActor, correlationId, wikiName)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }
}