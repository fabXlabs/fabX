package cloud.fabX.fabXaccess.device.application

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.CreateCardAtDevice
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.GettingUserById
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalTime::class)
@MockitoSettings
internal class AddingCardIdentityAtDeviceTest {

    private val adminActor = AdminFixture.arbitrary()
    private val deviceId = DeviceIdFixture.arbitrary()
    private val userId = UserIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var domainEventPublisher: DomainEventPublisher
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var gettingUserById: GettingUserById
    private lateinit var createCardAtDevice: CreateCardAtDevice

    private lateinit var testee: AddingCardIdentityAtDevice

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock domainEventPublisher: DomainEventPublisher,
        @Mock deviceRepository: DeviceRepository,
        @Mock gettingUserById: GettingUserById,
        @Mock createCardAtDevice: CreateCardAtDevice
    ) {
        this.logger = logger
        this.domainEventPublisher = domainEventPublisher
        this.deviceRepository = deviceRepository
        this.gettingUserById = gettingUserById
        this.createCardAtDevice = createCardAtDevice

        testee = AddingCardIdentityAtDevice(
            { logger },
            domainEventPublisher,
            deviceRepository,
            gettingUserById,
            createCardAtDevice,
            fixedClock
        )
    }

    @Test
    fun `when adding card identity at device then adds card identity`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)
        val user = UserFixture.arbitrary(userId, firstName = "Some", lastName = "One")

        val cardId = "AABBCC11223344"

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(gettingUserById.getUserById(userId))
            .thenReturn(user.right())

        whenever(createCardAtDevice.createCard(eq(deviceId), eq(correlationId), eq("Some One"), any()))
            .thenReturn(cardId.right())

        // when
        val result = testee.addCardIdentityAtDevice(
            adminActor,
            correlationId,
            deviceId,
            userId
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given unknown deviceId when adding card identity at device then returns error`() = runTest {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee.addCardIdentityAtDevice(
            adminActor,
            correlationId,
            deviceId,
            userId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given unknown userId when adding card identity at device then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(gettingUserById.getUserById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.addCardIdentityAtDevice(
            adminActor,
            correlationId,
            deviceId,
            userId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given disconnected device when adding card identity at device then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)
        val user = UserFixture.arbitrary(userId, firstName = "Some", lastName = "One")

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(gettingUserById.getUserById(userId))
            .thenReturn(user.right())

        whenever(createCardAtDevice.createCard(eq(deviceId), eq(correlationId), eq("Some One"), any()))
            .thenReturn(error.left())

        // when
        val result = testee.addCardIdentityAtDevice(
            adminActor,
            correlationId,
            deviceId,
            userId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}
