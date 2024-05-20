package cloud.fabX.fabXaccess.device.model

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.application.ThumbnailCreator
import cloud.fabX.fabXaccess.common.model.AggregateVersionDoesNotIncreaseOneByOne
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.IterableIsEmpty
import cloud.fabX.fabXaccess.common.model.ToolDeleted
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import isLeft
import isNone
import isRight
import isSome
import java.awt.Color
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class DeviceTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private val deviceId = DeviceIdFixture.arbitrary()
    private val aggregateVersion = 567L

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
            null,
            null,
            mapOf(),
            MacSecretIdentity("aabbccddeeff", "supersecret")
        )

        // then
        assertThat(device).isNotNull()
        assertThat(device.id).isEqualTo(deviceId)
        assertThat(device.aggregateVersion).isEqualTo(aggregateVersion)
    }

    @Test
    fun `when adding new device then returns expected sourcing event`() {
        // given
        val name = "name"
        val background = "https://example.com/bg.bmp"
        val backupBackendUrl = "https://backup.example.com"

        val expectedSourcingEvent = DeviceCreated(
            deviceId,
            adminActor.id,
            fixedInstant,
            correlationId,
            name,
            background,
            backupBackendUrl,
            "AABBCCDDEEFF",
            "4b99e67a0251e93ce84c919acf69cbad"
        )

        // when
        val result = Device.addNew(
            { deviceId },
            adminActor,
            fixedClock,
            correlationId,
            name,
            background,
            backupBackendUrl,
            "AABBCCDDEEFF",
            "4b99e67a0251e93ce84c919acf69cbad"
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
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
            fixedInstant,
            correlationId,
            name = ChangeableValue.ChangeToValueString("name"),
            background = ChangeableValue.ChangeToValueString("https://example.com/bg.bmp"),
            backupBackendUrl = ChangeableValue.ChangeToValueString("https://backup.example.com")
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
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
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
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            ChangeableValue.ChangeToValueString("name2"),
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )
        val event3 = DeviceDetailsChanged(
            deviceId,
            3,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.ChangeToValueString("backupUrl3")
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
                    null,
                    null,
                    mapOf(),
                    MacSecretIdentity("aabbccddeeff", "supersecret")
                )
            )
    }

    @Test
    fun `given multiple out-of-order sourcing events when constructing device then throws exception`() {
        // given
        val event1 = DeviceCreated(
            deviceId,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
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
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.ChangeToValueString("backupUrl3")
        )
        val event2 = DeviceDetailsChanged(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            ChangeableValue.ChangeToValueString("name2"),
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
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
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
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
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
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            "Woodworking",
            "https://example.com/image.bmp",
            "https://fabx-backup.example.com",
            "aabbccddeeff",
            "supersecret"
        )

        val event2 = DeviceDeleted(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary()
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
            deviceId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            name = ChangeableValue.ChangeToValueString("newName"),
            background = ChangeableValue.LeaveAsIs,
            backupBackendUrl = ChangeableValue.ChangeToValueString("https://new.example.com")
        )

        // when
        val result = device.changeDetails(
            adminActor,
            fixedClock,
            correlationId,
            name = ChangeableValue.ChangeToValueString("newName"),
            background = ChangeableValue.LeaveAsIs,
            backupBackendUrl = ChangeableValue.ChangeToValueString("https://new.example.com")
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `when changing thumbnail then created thumbnail is returned`() {
        // given
        val thumbnailData = ImmutableImage.create(600, 600)
            .fill(Color.GRAY)
            .bytes(JpegWriter.Default)

        val device = DeviceFixture.arbitrary(deviceId)

        // when
        val result = device.changeThumbnail(
            adminActor,
            correlationId,
            thumbnailData
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(thumbnailData)
    }

    @Test
    fun `when getting thumbnail then image from repository is returned`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        val thumbnailData = ByteArray(42) { it.toByte() }

        // when
        val result = device.getThumbnail(
            adminActor,
            correlationId,
            thumbnailData.right()
        )

        // then
        assertThat(result).isEqualTo(thumbnailData)
    }

    @Test
    fun `given no image from repository when getting thumbnail then default thumbnail is returned`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        // when
        val result = device.getThumbnail(
            adminActor,
            correlationId,
            Error.DeviceThumbnailNotFound("a message", deviceId).left()
        )

        // then
        assertThat(result)
            .isEqualTo(ThumbnailCreator.default)
    }

    @Test
    fun `when setting desired firmware version then expected sourcing event is returned`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = DesiredFirmwareVersionChanged(
            deviceId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            "42.0.0"
        )

        // when
        val result = device.changeDesiredFirmwareVersion(
            adminActor,
            fixedClock,
            correlationId,
            desiredFirmwareVersion = "42.0.0"
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `when setting actual firmware version then expected sourcing event is returned`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = ActualFirmwareVersionChanged(
            deviceId,
            aggregateVersion + 1,
            deviceId,
            fixedInstant,
            correlationId,
            "42.0.0"
        )

        // when
        val result = device.setActualFirmwareVersion(
            device.asActor(),
            fixedClock,
            correlationId,
            actualFirmwareVersion = "42.0.0"
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given other actor when setting actual firmware version then returns error`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = aggregateVersion)
        val otherDevice = DeviceFixture.arbitrary()

        // when
        val result = device.setActualFirmwareVersion(
            otherDevice.asActor(),
            fixedClock,
            correlationId,
            actualFirmwareVersion = "42.0.0"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.DeviceNotActor(
                    "Device not actor",
                    correlationId
                )
            )
    }

    @Test
    @Suppress("MoveLambdaOutsideParentheses")
    fun `when attaching tool then returns expected sourcing event`() = runTest {
        // given
        val pin = 3
        val toolId = ToolIdFixture.arbitrary()
        val tool = ToolFixture.arbitrary(toolId = toolId)

        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion = aggregateVersion,
            attachedTools = mapOf()
        )

        val expectedSourcingEvent = ToolAttached(
            deviceId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            pin = pin,
            toolId = toolId
        )

        // when
        val result = device.attachTool(
            adminActor,
            fixedClock,
            correlationId,
            pin,
            toolId,
            { tool.right() }
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    @Suppress("MoveLambdaOutsideParentheses")
    fun `given pin is already in use when attaching tool then returns error`() = runTest {
        // given
        val pin = 3
        val toolId = ToolIdFixture.arbitrary()
        val tool = ToolFixture.arbitrary(toolId = toolId)

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
            fixedClock,
            correlationId,
            pin,
            toolId,
            { tool.right() }
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.PinInUse(
                    "Tool (with id $toolId) already attached at pin $pin.",
                    pin,
                    correlationId
                )
            )
    }

    @Test
    @Suppress("MoveLambdaOutsideParentheses")
    fun `given tool does not exist when attaching tool then returns error`() = runTest {
        // given
        val pin = 3
        val invalidToolId = ToolIdFixture.arbitrary()

        val error = Error.ToolNotFound(
            "error message",
            invalidToolId
        )

        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion,
            attachedTools = mapOf()
        )

        // when
        val result = device.attachTool(
            adminActor,
            fixedClock,
            correlationId,
            pin,
            invalidToolId,
            { error.left() }
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.ReferencedToolNotFound(
                    "error message",
                    invalidToolId,
                    correlationId
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
            deviceId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            pin = pin
        )

        // when
        val result = device.detachTool(
            adminActor,
            fixedClock,
            correlationId,
            pin
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given triggered by domain event when detaching tool then returns sourcing event`() {
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

        val actorId = UserIdFixture.arbitrary()

        val domainEvent = ToolDeleted(
            actorId,
            Clock.System.now(),
            correlationId,
            toolId
        )

        val expectedSourcingEvent = ToolDetached(
            deviceId,
            aggregateVersion + 1,
            actorId,
            fixedInstant,
            correlationId,
            pin = pin
        )

        // when
        val result = device.detachTool(domainEvent, fixedClock, pin)

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
            fixedClock,
            correlationId,
            pin
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.PinNotInUse(
                    "No tool attached at pin $pin.",
                    pin,
                    correlationId
                )
            )

    }

    @Test
    fun `when deleting then expected sourcing event is returned`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = DeviceDeleted(
            deviceId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId
        )

        // when
        val result = device.delete(adminActor, fixedClock, correlationId)

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

    @ParameterizedTest
    @CsvSource(
        value = [
            "123, true",
            "0, false",
            "1, false",
            "124, false"
        ]
    )
    fun `when checking has attached tools then returns expected result`(
        toCheck: Int,
        expectedResult: Boolean
    ) {
        // given
        val toolId = ToolIdFixture.static(123)
        val toolIdToCheck = ToolIdFixture.static(toCheck)

        val device = DeviceFixture.arbitrary(
            attachedTools = mapOf(1 to toolId)
        )

        // when
        val result = device.hasAttachedTool(toolIdToCheck)

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
            "1.2.3",
            "4.5.6",
            mapOf(3 to ToolIdFixture.static(345)),
            MacSecretIdentity("aabbccddeeff", "supersecret")
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
                    "actualFirmwareVersion=1.2.3, " +
                    "desiredFirmwareVersion=4.5.6, " +
                    "attachedTools={3=ToolId(value=b6f53dde-e176-3672-8b65-819b4c168e6f)}, " +
                    "identity=MacSecretIdentity(mac=aabbccddeeff, secret=supersecret))"
        )
    }
}