package cloud.fabX.fabXaccess.tool.model

import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.SourcingEvent
import cloud.fabX.fabXaccess.common.model.ToolId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class ToolSourcingEvent : SourcingEvent {
    abstract override val aggregateRootId: ToolId
    abstract override val aggregateVersion: Long
    abstract override val actorId: ActorId
    abstract override val correlationId: CorrelationId
    abstract override val timestamp: Instant

    abstract fun processBy(eventHandler: EventHandler, tool: Option<Tool>): Option<Tool>

    interface EventHandler {
        fun handle(event: ToolCreated, tool: Option<Tool>): Option<Tool>
        fun handle(event: ToolDetailsChanged, tool: Option<Tool>): Option<Tool>
        fun handle(event: ToolDeleted, tool: Option<Tool>): Option<Tool>
    }
}

@Serializable
data class ToolCreated(
    override val aggregateRootId: ToolId,
    override val actorId: ActorId,
    override val timestamp: Instant,
    override val correlationId: CorrelationId,
    val name: String,
    val toolType: ToolType,
    val requires2FA: Boolean,
    val time: Int, // in ms
    val idleState: IdleState,
    val wikiLink: String,
    val requiredQualifications: Set<QualificationId>
) : ToolSourcingEvent() {
    override val aggregateVersion: Long = 1

    override fun processBy(eventHandler: EventHandler, tool: Option<Tool>): Option<Tool> =
        eventHandler.handle(this, tool)
}

@Serializable
data class ToolDetailsChanged(
    override val aggregateRootId: ToolId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val timestamp: Instant,
    override val correlationId: CorrelationId,
    val name: ChangeableValue<String>,
    val toolType: ChangeableValue<ToolType>,
    val requires2FA: ChangeableValue<Boolean>,
    val time: ChangeableValue<Int>,
    val idleState: ChangeableValue<IdleState>,
    val enabled: ChangeableValue<Boolean>,
    val notes: ChangeableValue<String?>,
    val wikiLink: ChangeableValue<String>,
    val requiredQualifications: ChangeableValue<Set<QualificationId>>
) : ToolSourcingEvent() {

    override fun processBy(eventHandler: EventHandler, tool: Option<Tool>): Option<Tool> =
        eventHandler.handle(this, tool)
}

@Serializable
data class ToolDeleted(
    override val aggregateRootId: ToolId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val timestamp: Instant,
    override val correlationId: CorrelationId
) : ToolSourcingEvent() {

    override fun processBy(eventHandler: EventHandler, tool: Option<Tool>): Option<Tool> =
        eventHandler.handle(this, tool)
}