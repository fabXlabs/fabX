package cloud.fabX.fabXaccess.tool.model

import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.SourcingEvent
import cloud.fabX.fabXaccess.qualification.model.QualificationId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

sealed class ToolSourcingEvent(
    override val aggregateRootId: ToolId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    override val timestamp: Instant = Clock.System.now()
) : SourcingEvent {

    abstract fun processBy(eventHandler: EventHandler, tool: Option<Tool>): Option<Tool>

    interface EventHandler {
        fun handle(event: ToolCreated, tool: Option<Tool>): Option<Tool>
        fun handle(event: ToolDetailsChanged, tool: Option<Tool>): Option<Tool>
        fun handle(event: ToolDeleted, tool: Option<Tool>): Option<Tool>
    }
}

data class ToolCreated(
    override val aggregateRootId: ToolId,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val name: String,
    val type: ToolType,
    val time: Int, // in ms
    val idleState: IdleState,
    val wikiLink: String,
    val requiredQualifications: Set<QualificationId>
) : ToolSourcingEvent(aggregateRootId, 1, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, tool: Option<Tool>): Option<Tool> =
        eventHandler.handle(this, tool)
}

data class ToolDetailsChanged(
    override val aggregateRootId: ToolId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val name: ChangeableValue<String>,
    val type: ChangeableValue<ToolType>,
    val time: ChangeableValue<Int>,
    val idleState: ChangeableValue<IdleState>,
    val enabled: ChangeableValue<Boolean>,
    val wikiLink: ChangeableValue<String>,
    val requiredQualifications: ChangeableValue<Set<QualificationId>>
) : ToolSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, tool: Option<Tool>): Option<Tool> =
        eventHandler.handle(this, tool)
}

data class ToolDeleted(
    override val aggregateRootId: ToolId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId
) : ToolSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, tool: Option<Tool>): Option<Tool> =
        eventHandler.handle(this, tool)
}