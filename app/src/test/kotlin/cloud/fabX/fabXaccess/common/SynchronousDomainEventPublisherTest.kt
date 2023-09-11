package cloud.fabX.fabXaccess.common

import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.QualificationDeleted
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.same
import org.mockito.kotlin.verify

@MockitoSettings
internal class SynchronousDomainEventPublisherTest {

    @Test
    fun `given handlers when publishing then all handlers are called`(
        @Mock handler1: DomainEventHandler,
        @Mock handler2: DomainEventHandler,
        @Mock handler3: DomainEventHandler
    ) = runTest {
        // given
        val testee = SynchronousDomainEventPublisher()
        testee.addHandler(handler1)
        testee.addHandler(handler2)
        testee.addHandler(handler3)

        val domainEvent = QualificationDeleted(
            UserIdFixture.arbitrary(),
            Clock.System.now(),
            CorrelationIdFixture.arbitrary(),
            QualificationIdFixture.arbitrary()
        )

        // when
        testee.publish(domainEvent)

        // then
        verify(handler1).handle(same(domainEvent))
        verify(handler2).handle(same(domainEvent))
        verify(handler3).handle(same(domainEvent))
    }

    @Test
    fun `given no handlers when removing handler then no exception is thrown`(
        @Mock handler1: DomainEventHandler
    ) {
        // given
        val testee = SynchronousDomainEventPublisher()

        // when & then
        assertDoesNotThrow { testee.removeHandler(handler1) }
    }
}