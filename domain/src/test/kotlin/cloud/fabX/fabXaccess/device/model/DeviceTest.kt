package cloud.fabX.fabXaccess.device.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.AggregateVersionDoesNotIncreaseOneByOne
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.IterableIsEmpty
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isNone
import isRight
import isSome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class DeviceTest {

    private val deviceId = DeviceIdFixture.arbitrary()
    private val aggregateVersion = 567L

    private val adminActor = AdminFixture.arbitrary()

    @Test
    fun `given valid values when constructing device then it is constructed`() {
        // given

        // when
        val device = Device(
            deviceId,
            aggregateVersion,
            "Woodworking",
            "https://example.com/image.bmp",
            "https://fabx-backup.example.com",
            MacSecretIdentity("aabbccddeeff", "supersecret"),
            mapOf()
        )

        // then
        assertThat(device).isNotNull()
        assertThat(device.id).isEqualTo(deviceId)
        assertThat(device.aggregateVersion).isEqualTo(aggregateVersion)
    }

    @Test
    fun `when adding new device then returns expected sourcing event`() {
        // given
        DomainModule.configureDeviceIdFactory { deviceId }

        val name = "name"
        val background = "https://example.com/bg.bmp"
        val backupBackendUrl = "https://backup.example.com"

        val expectedSourcingEvent = DeviceCreated(
            deviceId,
            adminActor.id,
            name,
            background,
            backupBackendUrl,
            "aabbccddeeff",
            "supersecret"
        )


        // when
        val result = Device.addNew(
            adminActor,
            name,
            background,
            backupBackendUrl,
            MacSecretIdentity("aabbccddeeff", "supersecret")
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given no sourcing events when constructing device from sourcing events then throws exception`() {
        // given

        // when
        val exception = assertThrows<IterableIsEmpty> {
            Device.fromSourcingEvents(listOf())
        }

        // then
        assertThat(exception.message)
            .isEqualTo("No sourcing events contained in iterable.")
    }

    @Test
    fun `given no DeviceCreated event when constructing device from sourcing events then throws exception`() {
        // given
        val event = DeviceDetailsChanged(
            deviceId,
            1,
            adminActor.id,
            name = ChangeableValue.ChangeToValue("name"),
            background = ChangeableValue.ChangeToValue("https://example.com/bg.bmp"),
            backupBackendUrl = ChangeableValue.ChangeToValue("https://backup.example.com")
        )

        // when
        val exception = assertThrows<Device.EventHistoryDoesNotStartWithDeviceCreated> {
            Device.fromSourcingEvents(listOf(event))
        }

        // then
        assertThat(exception.message)
            .isNotNull()
            .isEqualTo("Event history starts with $event, not a DeviceCreated event.")
    }

    @Test
    fun `given multiple in-order sourcing events when constructing device from sourcing events then applies all`() {
        // given
        val event1 = DeviceCreated(
            deviceId,
            adminActor.id,
            "name1",
            "background1",
            "backupUrl1",
            "aabbccddeeff",
            "supersecret"
        )
        val event2 = DeviceDetailsChanged(
            deviceId,
            2,
            adminActor.id,
            ChangeableValue.ChangeToValue("name2"),
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )
        val event3 = DeviceDetailsChanged(
            deviceId,
            3,
            adminActor.id,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.ChangeToValue("backupUrl3")
        )

        // when
        val result = Device.fromSourcingEvents(listOf(event1, event2, event3))

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(
                Device(
                    deviceId,
                    3,
                    "name2",
                    "background1",
                    "backupUrl3",
                    MacSecretIdentity("aabbccddeeff", "supersecret"),
                    mapOf()
                )
            )
    }

    @Test
    fun `given multiple out-of-order sourcing events when constructing device then throws exception`() {
        // given
        val event1 = DeviceCreated(
            deviceId,
            adminActor.id,
            "name1",
            "background1",
            "backupUrl1",
            "aabbccddeeff",
            "supersecret"
        )
        val event3 = DeviceDetailsChanged(
            deviceId,
            3,
            adminActor.id,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.ChangeToValue("backupUrl3")
        )
        val event2 = DeviceDetailsChanged(
            deviceId,
            2,
            adminActor.id,
            ChangeableValue.ChangeToValue("name2"),
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // when
        val exception = assertThrows<AggregateVersionDoesNotIncreaseOneByOne> {
            Device.fromSourcingEvents(listOf(event1, event3, event2))
        }

        // then
        assertThat(exception.message)
            .isNotNull()
            .isEqualTo("Aggregate version does not increase one by one for ${listOf(event1, event3, event2)}.")
    }

    @Test
    fun `given sourcing events including ToolAttached then applies it`() {
        // given
        val toolId1 = ToolIdFixture.arbitrary()
        val toolId3 = ToolIdFixture.arbitrary()

        val device = DeviceFixture.arbitrary(
            deviceId = deviceId,
            aggregateVersion = 1,
            attachedTools = mapOf(1 to toolId1)
        )

        val event = ToolAttached(
            deviceId,
            2,
            adminActor.id,
            3,
            toolId3
        )

        // when
        val result = device.apply(event)

        // then
        assertThat(result)
            .isSome()
            .transform { it.attachedTools }
            .isEqualTo(mapOf(1 to toolId1, 3 to toolId3))
    }

    @Test
    fun `given sourcing events including ToolDetached then applies it`() {
        // given
        val toolId1 = ToolIdFixture.arbitrary()
        val toolId2 = ToolIdFixture.arbitrary()

        val device = DeviceFixture.arbitrary(
            deviceId = deviceId,
            aggregateVersion = 1,
            attachedTools = mapOf(1 to toolId1, 2 to toolId2)
        )

        val event = ToolDetached(
            deviceId,
            2,
            adminActor.id,
            1
        )

        // when
        val result = device.apply(event)

        // then
        assertThat(result)
            .isSome()
            .transform { it.attachedTools }
            .isEqualTo(mapOf(2 to toolId2))
    }

    @Test
    fun `given sourcing events including DeviceDeleted event when constructing device then returns None`() {
        // given
        val event1 = DeviceCreated(
            deviceId,
            adminActor.id,
            "Woodworking",
            "https://example.com/image.bmp",
            "https://fabx-backup.example.com",
            "aabbccddeeff",
            "supersecret"
        )

        val event2 = DeviceDeleted(
            deviceId,
            2,
            adminActor.id
        )

        // when
        val result = Device.fromSourcingEvents(listOf(event1, event2))

        // then
        assertThat(result)
            .isNone()
    }

    @Test
    fun `when changing details then expected sourcing event is returned`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = DeviceDetailsChanged(
            aggregateRootId = deviceId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id,
            name = ChangeableValue.ChangeToValue("newName"),
            background = ChangeableValue.LeaveAsIs,
            backupBackendUrl = ChangeableValue.ChangeToValue("https://new.example.com")
        )

        // when
        val result = device.changeDetails(
            adminActor,
            name = ChangeableValue.ChangeToValue("newName"),
            background = ChangeableValue.LeaveAsIs,
            backupBackendUrl = ChangeableValue.ChangeToValue("https://new.example.com")
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `when attaching tool then returns expected sourcing event`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = aggregateVersion)

        val pin = 3
        val toolId = ToolIdFixture.arbitrary()

        val expectedSourcingEvent = ToolAttached(
            aggregateRootId = deviceId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id,
            pin = pin,
            toolId = toolId
        )

        // when
        val result = device.attachTool(
            adminActor,
            pin,
            toolId
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given pin is already in use when attaching tool then returns error`() {
        // given
        val pin = 3
        val toolId = ToolIdFixture.arbitrary()

        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion,
            attachedTools = mapOf(
                pin to toolId
            )
        )

        // when
        val result = device.attachTool(
            adminActor,
            pin,
            toolId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.PinInUse(
                    "Tool (with id $toolId) already attached at pin $pin.",
                    pin
                )
            )
    }

    @Test
    fun `when detaching tool then returns expected sourcing event`() {
        // given
        val pin = 3
        val toolId = ToolIdFixture.arbitrary()

        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion,
            attachedTools = mapOf(
                pin to toolId
            )
        )

        val expectedSourcingEvent = ToolDetached(
            aggregateRootId = deviceId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id,
            pin = pin
        )

        // when
        val result = device.detachTool(
            adminActor,
            pin
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given pin is not in use when detaching tool then returns error`() {
        // given
        val pin = 3

        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion,
            attachedTools = mapOf()
        )

        // when
        val result = device.detachTool(
            adminActor,
            pin
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.PinNotInUse(
                    "No tool attached at pin $pin.",
                    pin
                )
            )

    }

    @Test
    fun `when deleting then expected sourcing event is returned`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = DeviceDeleted(
            aggregateRootId = deviceId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id
        )

        // when
        val result = device.delete(adminActor)

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "aabbccddeeff, supersecret, true"
        ]
    )
    fun `given mac secret identity when checking hasIdentity then returns expected result`(
        mac: String,
        secret: String,
        expectedResult: Boolean
    ) {
        // given
        val device = DeviceFixture.withIdentity(
            MacSecretIdentity(
                "aabbccddeeff",
                "supersecret"
            )
        )

        // when
        val result = device.hasIdentity(MacSecretIdentity(mac, secret))

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `given valid device when stringifying then result is correct`() {
        // given
        val device = Device(
            DeviceIdFixture.static(9876),
            aggregateVersion,
            "Woodworking",
            "https://example.com/image.bmp",
            "https://fabx-backup.example.com",
            MacSecretIdentity("aabbccddeeff", "supersecret"),
            mapOf(3 to ToolIdFixture.static(345))
        )

        // when
        val result = device.toString()

        // then
        assertThat(result).isEqualTo(
            "Device(id=DeviceId(value=b51364fb-0487-3d2d-8da4-e7478a1c163d), " +
                    "aggregateVersion=567, " +
                    "name=Woodworking, " +
                    "background=https://example.com/image.bmp, " +
                    "backupBackendUrl=https://fabx-backup.example.com, " +
                    "identity=MacSecretIdentity(mac=aabbccddeeff, secret=supersecret), " +
                    "attachedTools={3=ToolId(value=b6f53dde-e176-3672-8b65-819b4c168e6f)})"
        )
    }
}