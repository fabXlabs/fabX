package cloud.fabX.fabXaccess.device.infrastructure

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceCreated
import cloud.fabX.fabXaccess.device.model.DeviceDetailsChanged
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import isLeft
import isNone
import isRight
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class DeviceDatabaseRepositoryTest {
    val deviceId = DeviceIdFixture.staticId(4242)
    val actorId = UserIdFixture.staticId(1234)

    @Test
    fun `given empty repository when getting device by id then returns device not found error`() {
        // given
        val repository = DeviceDatabaseRepository()

        // when
        val result = repository.getById(deviceId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.DeviceNotFound(
                    "Device with id DeviceId(value=a47a7eb7-4f7d-3d6d-8287-0d27bda3d39a) not found.",
                    deviceId
                )
            )
    }

    @Nested
    internal inner class GivenEventsForDeviceStoredInRepository {

        private var repository: DeviceRepository? = null

        @BeforeEach
        fun setup() {
            repository = DeviceDatabaseRepository()

            val event1 = DeviceCreated(
                deviceId,
                actorId,
                name = "device",
                background = "https://example.com/1.bmp",
                backupBackendUrl = "https://backup.example.com"
            )
            repository!!.store(event1)

            val event2 = DeviceDetailsChanged(
                deviceId,
                2,
                actorId,
                name = ChangeableValue.LeaveAsIs,
                background = ChangeableValue.ChangeToValue("https://example.com/2.bmp"),
                backupBackendUrl = ChangeableValue.LeaveAsIs
            )
            repository!!.store(event2)
        }

        @Test
        fun `when getting device by id then returns device from events`() {
            // given
            val repository = this.repository!!

            // when
            val result = repository.getById(deviceId)

            // then
            assertThat(result)
                .isRight()
                .all {
                    transform { it.id }.isEqualTo(deviceId)
                    transform { it.aggregateVersion }.isEqualTo(2)
                    transform { it.name }.isEqualTo("device")
                    transform { it.background }.isEqualTo("https://example.com/2.bmp")
                    transform { it.backupBackendUrl }.isEqualTo("https://backup.example.com")
                }
        }

        @Test
        fun `when storing then accepts aggregate version number increased by one`() {
            // given
            val repository = this.repository!!

            val event = DeviceDetailsChanged(
                deviceId,
                3,
                actorId,
                name = ChangeableValue.LeaveAsIs,
                background = ChangeableValue.LeaveAsIs,
                backupBackendUrl = ChangeableValue.LeaveAsIs
            )

            // when
            val result = repository.store(event)

            // then
            assertThat(result).isNone()

            assertThat(repository.getById(deviceId))
                .isRight()
                .transform { it.aggregateVersion }.isEqualTo(3)
        }

        @ParameterizedTest
        @ValueSource(longs = [-1, 0, 1, 2, 4, 1234])
        fun `when storing then not accepts version numbers other than increased by one`(version: Long) {
            // given
            val repository = this.repository!!

            val event = DeviceDetailsChanged(
                deviceId,
                version,
                actorId,
                name = ChangeableValue.LeaveAsIs,
                background = ChangeableValue.LeaveAsIs,
                backupBackendUrl = ChangeableValue.LeaveAsIs
            )

            // when
            val result = repository.store(event)

            // then
            assertThat(result)
                .isSome()
                .isEqualTo(
                    Error.VersionConflict(
                        "Previous version of device DeviceId(value=a47a7eb7-4f7d-3d6d-8287-0d27bda3d39a) is 2, " +
                                "desired new version is $version."
                    )
                )

            assertThat(repository.getById(deviceId))
                .isRight()
                .transform { it.aggregateVersion }.isEqualTo(2)
        }
    }
}