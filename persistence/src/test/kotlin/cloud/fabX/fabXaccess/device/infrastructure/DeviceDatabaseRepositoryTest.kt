package cloud.fabX.fabXaccess.device.infrastructure

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.infrastructure.withTestApp
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.model.DeviceCreated
import cloud.fabX.fabXaccess.device.model.DeviceDeleted
import cloud.fabX.fabXaccess.device.model.DeviceDetailsChanged
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.GettingDeviceByIdentity
import cloud.fabX.fabXaccess.device.model.GettingDevicesByAttachedTool
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
import cloud.fabX.fabXaccess.device.model.ToolAttached
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import isLeft
import isNone
import isRight
import isSome
import java.util.stream.Stream
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import org.kodein.di.DI
import org.kodein.di.bindInstance
import org.kodein.di.instance

internal class DeviceDatabaseRepositoryTest {
    companion object {
        private val deviceId = DeviceIdFixture.static(4242)
        private val deviceId2 = DeviceIdFixture.static(4343)

        private val toolId1 = ToolIdFixture.static(678)
        private val toolId2 = ToolIdFixture.static(679)

        private val actorId = UserIdFixture.static(1234)
        private val correlationId = CorrelationIdFixture.arbitrary()

        private val fixedInstant = Clock.System.now()
    }

    // TODO dynamically start postgres instance via Testcontainers
    private fun withConfiguredTestApp(block: (DI) -> Unit) = withTestApp({
        bindInstance(tag = "dburl") { "jdbc:postgresql://localhost/postgres" }
        bindInstance(tag = "dbdriver") { "org.postgresql.Driver" }
        bindInstance(tag = "dbuser") { "postgres" }
        bindInstance(tag = "dbpassword") { "postgrespassword" }
    }, block)

    @Test
    fun `given empty repository when getting device by id then returns device not found error`() =
        withConfiguredTestApp { di ->
            // given
            val repository: DeviceDatabaseRepository by di.instance()

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

        private fun withSetupTestApp(block: (DI) -> Unit) = withConfiguredTestApp { di ->
            val repository: DeviceDatabaseRepository by di.instance()

            val event1 = DeviceCreated(
                deviceId,
                actorId,
                fixedInstant,
                correlationId,
                name = "device",
                background = "https://example.com/1.bmp",
                backupBackendUrl = "https://backup.example.com",
                mac = "aabbccddeeff",
                secret = "supersecret"
            )
            repository.store(event1)

            val event2 = DeviceDetailsChanged(
                deviceId,
                2,
                actorId,
                fixedInstant,
                correlationId,
                name = ChangeableValue.LeaveAsIs,
                background = ChangeableValue.ChangeToValueString("https://example.com/2.bmp"),
                backupBackendUrl = ChangeableValue.LeaveAsIs
            )
            repository.store(event2)

            block(di)
        }

        @Test
        fun `when getting device by id then returns device from events`() = withSetupTestApp { di ->
            // given
            val repository: DeviceDatabaseRepository by di.instance()

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
        fun `when storing then accepts aggregate version number increased by one`() = withSetupTestApp { di ->
            // given
            val repository: DeviceDatabaseRepository by di.instance()

            val event = DeviceDetailsChanged(
                deviceId,
                3,
                actorId,
                fixedInstant,
                correlationId,
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
        fun `when storing then not accepts version numbers other than increased by one`(version: Long) =
            withSetupTestApp { di ->
                // given
                val repository: DeviceDatabaseRepository by di.instance()

                val event = DeviceDetailsChanged(
                    deviceId,
                    version,
                    actorId,
                    fixedInstant,
                    correlationId,
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

        private fun withSetupTestApp(block: (DI) -> Unit) = withConfiguredTestApp { di ->
            val repository: DeviceDatabaseRepository by di.instance()

            val device1event1 = DeviceCreated(
                deviceId,
                actorId,
                fixedInstant,
                correlationId,
                name = "device1",
                background = "https://example.com/1.bmp",
                backupBackendUrl = "https://backup.example.com",
                mac = "aabbccddeeff",
                secret = "supersecret"
            )
            repository.store(device1event1)

            val device1event2 = DeviceDetailsChanged(
                deviceId,
                2,
                actorId,
                fixedInstant,
                correlationId,
                name = ChangeableValue.LeaveAsIs,
                background = ChangeableValue.ChangeToValueString("https://example.com/2.bmp"),
                backupBackendUrl = ChangeableValue.LeaveAsIs
            )
            repository.store(device1event2)

            val device3event1 = DeviceCreated(
                deviceId3,
                actorId,
                fixedInstant,
                correlationId,
                name = "device3",
                background = "https://background.com/device3.bmp",
                backupBackendUrl = "https://backup3.example.com",
                mac = "aabbccddee33",
                secret = "supersecret3"
            )
            repository.store(device3event1)

            val device2event1 = DeviceCreated(
                deviceId2,
                actorId,
                fixedInstant,
                correlationId,
                name = "device2",
                background = "https://background.com/device2.bmp",
                backupBackendUrl = "https://backup2.example.com",
                mac = "aabbccddee22",
                secret = "supersecret2"
            )
            repository.store(device2event1)

            val device1event3 = DeviceDetailsChanged(
                deviceId,
                3,
                actorId,
                fixedInstant,
                correlationId,
                name = ChangeableValue.LeaveAsIs,
                background = ChangeableValue.ChangeToValueString("https://example.com/3.bmp"),
                backupBackendUrl = ChangeableValue.LeaveAsIs
            )
            repository.store(device1event3)

            val device3event2 = DeviceDeleted(
                deviceId3,
                2,
                actorId,
                fixedInstant,
                correlationId,
            )
            repository.store(device3event2)

            val device2event2 = DeviceDetailsChanged(
                deviceId2,
                2,
                actorId,
                fixedInstant,
                correlationId,
                name = ChangeableValue.LeaveAsIs,
                background = ChangeableValue.LeaveAsIs,
                backupBackendUrl = ChangeableValue.ChangeToValueString("https://backup42.example.com"),
            )
            repository.store(device2event2)

            block(di)
        }

        @Test
        fun `when getting all devices then returns all devices from events`() = withSetupTestApp { di ->
            // given
            val repository: DeviceDatabaseRepository by di.instance()

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

        private fun withSetupTestApp(block: (DI) -> Unit) = withConfiguredTestApp { di ->
            val repository: DeviceDatabaseRepository by di.instance()

            val device1Created = DeviceCreated(
                deviceId,
                actorId,
                fixedInstant,
                correlationId,
                name = "device1",
                background = "https://example.com/1.bmp",
                backupBackendUrl = "https://backup.example.com",
                mac = "aabbccddeeff",
                secret = "supersecret"
            )
            repository.store(device1Created)

            val device2Created = DeviceCreated(
                deviceId2,
                actorId,
                fixedInstant,
                correlationId,
                name = "device2",
                background = "https://background.com/device2.bmp",
                backupBackendUrl = "https://backup2.example.com",
                mac = "aabbccddee22",
                secret = "supersecret2"
            )
            repository.store(device2Created)

            block(di)
        }

        @Test
        fun `when getting by known identity then returns device`() = withSetupTestApp { di ->
            // given
            val repository: GettingDeviceByIdentity by di.instance()

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
        fun `when getting by unknown identity then returns error`() = withSetupTestApp { di ->
            // given
            val repository: GettingDeviceByIdentity by di.instance()

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

    @Nested
    internal inner class GivenEventsForDevicesWithAttachedToolsStoredInRepository {

        private fun withSetupTestApp(block: (DI) -> Unit) = withConfiguredTestApp { di ->
            val repository: DeviceDatabaseRepository by di.instance()

            val device1Created = DeviceCreated(
                deviceId,
                actorId,
                fixedInstant,
                correlationId,
                name = "device1",
                background = "https://example.com/1.bmp",
                backupBackendUrl = "https://backup.example.com",
                mac = "aabbccddeeff",
                secret = "supersecret"
            )
            repository.store(device1Created)

            val device1Tool1Attached = ToolAttached(
                deviceId,
                2,
                actorId,
                fixedInstant,
                correlationId,
                1,
                toolId1
            )
            repository.store(device1Tool1Attached)

            val device1Tool2Attached = ToolAttached(
                deviceId,
                3,
                actorId,
                fixedInstant,
                correlationId,
                2,
                toolId2
            )
            repository.store(device1Tool2Attached)

            val device2Created = DeviceCreated(
                deviceId2,
                actorId,
                fixedInstant,
                correlationId,
                name = "device2",
                background = "https://background.com/device2.bmp",
                backupBackendUrl = "https://backup2.example.com",
                mac = "aabbccddee22",
                secret = "supersecret2"
            )
            repository.store(device2Created)

            val device2Tool2Attached = ToolAttached(
                deviceId2,
                2,
                actorId,
                fixedInstant,
                correlationId,
                3,
                toolId2
            )
            repository.store(device2Tool2Attached)

            block(di)
        }

        @ParameterizedTest
        @ArgumentsSource(ToolIdProvider::class)
        fun `when getting by tool then returns expected devices`(
            toolId: ToolId,
            expectedDeviceIds: Set<DeviceId>
        ) = withSetupTestApp { di ->
            // given
            val repository: GettingDevicesByAttachedTool by di.instance()

            // when
            val result = repository.getByAttachedTool(toolId)

            // then
            assertThat(result)
                .transform { s -> s.map { it.id }.toSet() }
                .isEqualTo(expectedDeviceIds)
        }
    }

    class ToolIdProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(toolId1, setOf(deviceId)),
                Arguments.of(toolId2, setOf(deviceId, deviceId2))
            )
        }
    }
}