package cloud.fabX.fabXaccess.device.infrastructure

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceCreated
import cloud.fabX.fabXaccess.device.model.DeviceDeleted
import cloud.fabX.fabXaccess.device.model.DeviceDetailsChanged
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.GettingDeviceByIdentity
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
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
    private val deviceId = DeviceIdFixture.static(4242)
    private val actorId = UserIdFixture.static(1234)

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
                backupBackendUrl = "https://backup.example.com",
                mac = "aabbccddeeff",
                secret = "supersecret"
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

    @Nested
    internal inner class GivenEventsForDevicesStoredInRepository {

        private val deviceId2 = DeviceIdFixture.static(4343)
        private val deviceId3 = DeviceIdFixture.static(4444)

        private var repository: DeviceRepository? = null

        @BeforeEach
        fun setup() {
            repository = DeviceDatabaseRepository()

            val device1event1 = DeviceCreated(
                deviceId,
                actorId,
                name = "device1",
                background = "https://example.com/1.bmp",
                backupBackendUrl = "https://backup.example.com",
                mac = "aabbccddeeff",
                secret = "supersecret"
            )
            repository!!.store(device1event1)

            val device1event2 = DeviceDetailsChanged(
                deviceId,
                2,
                actorId,
                name = ChangeableValue.LeaveAsIs,
                background = ChangeableValue.ChangeToValue("https://example.com/2.bmp"),
                backupBackendUrl = ChangeableValue.LeaveAsIs
            )
            repository!!.store(device1event2)

            val device3event1 = DeviceCreated(
                deviceId3,
                actorId,
                name = "device3",
                background = "https://background.com/device3.bmp",
                backupBackendUrl = "https://backup3.example.com",
                mac = "aabbccddee33",
                secret = "supersecret3"
            )
            repository!!.store(device3event1)

            val device2event1 = DeviceCreated(
                deviceId2,
                actorId,
                name = "device2",
                background = "https://background.com/device2.bmp",
                backupBackendUrl = "https://backup2.example.com",
                mac = "aabbccddee22",
                secret = "supersecret2"
            )
            repository!!.store(device2event1)

            val device1event3 = DeviceDetailsChanged(
                deviceId,
                3,
                actorId,
                name = ChangeableValue.LeaveAsIs,
                background = ChangeableValue.ChangeToValue("https://example.com/3.bmp"),
                backupBackendUrl = ChangeableValue.LeaveAsIs
            )
            repository!!.store(device1event3)

            val device3event2 = DeviceDeleted(
                deviceId3,
                2,
                actorId
            )
            repository!!.store(device3event2)

            val device2event2 = DeviceDetailsChanged(
                deviceId2,
                2,
                actorId,
                name = ChangeableValue.LeaveAsIs,
                background = ChangeableValue.LeaveAsIs,
                backupBackendUrl = ChangeableValue.ChangeToValue("https://backup42.example.com"),
            )
            repository!!.store(device2event2)
        }

        @Test
        fun `when getting all devices then returns all devices from events`() {
            // given
            val repository = this.repository!!

            // when
            val result = repository.getAll()

            // then
            assertThat(result).containsExactlyInAnyOrder(
                DeviceFixture.arbitrary(
                    deviceId,
                    3,
                    "device1",
                    "https://example.com/3.bmp",
                    "https://backup.example.com",
                    "aabbccddeeff",
                    "supersecret"
                ),
                DeviceFixture.arbitrary(
                    deviceId2,
                    2,
                    "device2",
                    "https://background.com/device2.bmp",
                    "https://backup42.example.com",
                    "aabbccddee22",
                    "supersecret2"
                )
            )
        }
    }

    @Nested
    internal inner class GivenDevicesWithIdentitiesStoredInRepository {

        private val deviceId2 = DeviceIdFixture.static(4242)

        private var repository: DeviceRepository? = null

        @BeforeEach
        fun setup() {
            repository = DeviceDatabaseRepository()

            val device1Created = DeviceCreated(
                deviceId,
                actorId,
                name = "device1",
                background = "https://example.com/1.bmp",
                backupBackendUrl = "https://backup.example.com",
                mac = "aabbccddeeff",
                secret = "supersecret"
            )
            repository!!.store(device1Created)

            val device2Created = DeviceCreated(
                deviceId2,
                actorId,
                name = "device2",
                background = "https://background.com/device2.bmp",
                backupBackendUrl = "https://backup2.example.com",
                mac = "aabbccddee22",
                secret = "supersecret2"
            )
            repository!!.store(device2Created)
        }

        @Test
        fun `when getting by known identity then returns device`() {
            // given
            val repository = this.repository!! as GettingDeviceByIdentity

            // when
            val result = repository.getByIdentity(
                MacSecretIdentity("aabbccddeeff", "supersecret")
            )

            // then
            assertThat(result)
                .isRight()
                .transform { it.id }.isEqualTo(deviceId)
        }

        @Test
        fun `when getting by unknown identity then returns error`() {
            // given
            val repository = this.repository!! as GettingDeviceByIdentity

            // when
            val result = repository.getByIdentity(
                MacSecretIdentity("000000000000", "supersecret")
            )

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(
                    Error.DeviceNotFoundByIdentity("Not able to find device for given identity.")
                )
        }
    }
}