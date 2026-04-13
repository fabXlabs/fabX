package cloud.fabX.fabXaccess.device.infrastructure

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.infrastructure.withTestApp
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DevicePinStatus
import cloud.fabX.fabXaccess.device.model.DevicePinStatusRepository
import isLeft
import isRight
import org.junit.jupiter.api.Test
import org.kodein.di.instance

internal class DevicePinStatusInMemoryRepositoryTest {
    @Test
    fun `given empty repository when getting device pin status then returns empty`() = withTestApp { di ->
        // given
        val repository: DevicePinStatusRepository by di.instance()

        // when
        val result = repository.getAll()

        // then
        assertThat(result)
            .isEmpty()
    }

    @Test
    fun `given empty repository when getting device pin status by id then returns error`() = withTestApp { di ->
        // given
        val repository: DevicePinStatusRepository by di.instance()
        val id = DeviceIdFixture.arbitrary()

        // when
        val result = repository.getById(id)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.DevicePinStatusUnknown(
                    "Pin status of Device with id $id not known.",
                    id
                )
            )
    }

    @Test
    fun `given status stored in repository when getting device pin status then returns all status`() = withTestApp { di ->
        // given
        val repository: DevicePinStatusRepository by di.instance()

        val status1 = DevicePinStatus(
            DeviceIdFixture.arbitrary(),
            mapOf(1 to true, 2 to false)
        )

        val status2 = DevicePinStatus(
            DeviceIdFixture.arbitrary(),
            mapOf(1 to false, 2 to true)
        )

        repository.store(status1)
        repository.store(status2)

        // when
        val result = repository.getAll()

        // then
        assertThat(result)
            .containsExactlyInAnyOrder(
                status1,
                status2
            )
    }


    @Test
    fun `given status stored in repository when getting device pin status by id then returns all status`() = withTestApp { di ->
        // given
        val repository: DevicePinStatusRepository by di.instance()
        val deviceId = DeviceIdFixture.arbitrary()

        val status = DevicePinStatus(
            deviceId,
            mapOf(1 to true, 2 to false)
        )

        repository.store(status)

        // when
        val result = repository.getById(deviceId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(status)
    }

    @Test
    fun `given updated status when getting device pin status then returns updated status`() = withTestApp { di ->
        // given
        val repository: DevicePinStatusRepository by di.instance()

        val deviceId = DeviceIdFixture.arbitrary()

        val status1 = DevicePinStatus(
            deviceId,
            mapOf(1 to true, 2 to false)
        )

        val status2 = DevicePinStatus(
            deviceId,
            mapOf(1 to false, 2 to true)
        )

        repository.store(status1)
        repository.store(status2)

        // when
        val result = repository.getAll()

        // then
        assertThat(result)
            .containsExactlyInAnyOrder(
                status2
            )
    }

    @Test
    fun `given updated status when getting device pin status by id then returns updated status`() = withTestApp { di ->
        // given
        val repository: DevicePinStatusRepository by di.instance()

        val deviceId = DeviceIdFixture.arbitrary()

        val status1 = DevicePinStatus(
            deviceId,
            mapOf(1 to true, 2 to false)
        )

        val status2 = DevicePinStatus(
            deviceId,
            mapOf(1 to false, 2 to true)
        )

        repository.store(status1)
        repository.store(status2)

        // when
        val result = repository.getById(deviceId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(status2)
    }

    @Test
    fun `given no stored status for device when getting device pin status then returns error`() = withTestApp { di ->
        // given
        val repository: DevicePinStatusRepository by di.instance()
        val deviceId = DeviceIdFixture.arbitrary()

        // when
        val result = repository.getById(deviceId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.DevicePinStatusUnknown(
                    "Pin status of Device with id $deviceId not known.",
                    deviceId
                )
            )
    }
}
