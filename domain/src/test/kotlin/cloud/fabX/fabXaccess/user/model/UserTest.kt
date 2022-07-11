package cloud.fabX.fabXaccess.user.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import kotlin.test.Test

internal class UserTest {

    private val userId = UserIdFixture.arbitraryId()

    @Test
    fun `given valid values when constructing user then user is constructed`() {
        // given

        // when
        val user = User(
            userId,
            "Nikola",
            "Tesla",
            "nick",
            "00491234567890",
            false,
            null
        )

        // then
        assertThat(user).isNotNull()
        assertThat(user.id).isEqualTo(userId)
    }

    @Test
    fun `when changing personal information then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitraryUser(userId)

        val expectedSourcingEvent = UserPersonalInformationChanged(
            aggregateRootId = userId,
            firstName = ChangeableValue.ChangeToValue("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("newWikiName"),
            phoneNumber = ChangeableValue.ChangeToValue(null)
        )

        // when
        val result = user.changePersonalInformation(
            firstName = ChangeableValue.ChangeToValue("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("newWikiName"),
            phoneNumber = ChangeableValue.ChangeToValue(null)
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `when changing lock state then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitraryUser(userId)

        val expectedSourcingEvent = UserLockStateChanged(
            aggregateRootId = userId,
            locked = ChangeableValue.ChangeToValue(true),
            notes = ChangeableValue.ChangeToValue(null)
        )

        // when
        val result = user.changeLockState(
            locked = ChangeableValue.ChangeToValue(true),
            notes = ChangeableValue.ChangeToValue(null)
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given valid user when stringifying then result is correct`() {
        // given
        val user = User(
            UserIdFixture.staticId(42),
            "Nikola",
            "Tesla",
            "nick",
            "00491234567890",
            false,
            null
        )

        // when
        val result = user.toString()

        // then
        assertThat(result).isEqualTo(
            "User(id=UserId(value=f4a3f34c-e12a-395c-9fd2-23e167422c32), " +
                    "firstName=Nikola, " +
                    "lastName=Tesla, " +
                    "wikiName=nick, " +
                    "phoneNumber=00491234567890, " +
                    "locked=false, " +
                    "notes=null)"
        )
    }
}